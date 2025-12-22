package com.snapmeet.websocket.netty;

import com.snapmeet.config.AppConfig;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class NettyWebSocketStarter implements Runnable{
    // boos线程，用于处理连接
    private EventLoopGroup boosGroup = new NioEventLoopGroup();

    // work线程,用于处理消息。
    private EventLoopGroup workerGroup = new NioEventLoopGroup();

    @Resource
    private HandlerTokenValidation handlerTokenValidation;

    @Resource
    private HandlerWebSocket handlerWebSocket;

    @Resource
    private AppConfig appConfig;

    @Override
    public void run() {
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(boosGroup,workerGroup);
            serverBootstrap.channel(NioServerSocketChannel.class).handler(new LoggingHandler(LogLevel.DEBUG)).
                    childHandler(new ChannelInitializer<Channel>() {
                        @Override
                        protected void initChannel(Channel channel) throws Exception {
                            ChannelPipeline pipeline = channel.pipeline();
                            //对http协议的支持，使用http的编码器和解码器
                            pipeline.addLast(new HttpServerCodec());
                            //http消息聚合器，将http消息聚合成完整FullHttpRequest
                            pipeline.addLast(new HttpObjectAggregator(64*1024));
                            //
                            pipeline.addLast(new IdleStateHandler(6,0,0));
                            //心跳处理
                            pipeline.addLast(new HandlerHeartBeat());
                            //token校验 拦截 channelRead事件
                            pipeline.addLast(handlerTokenValidation);
                            /*
                                websocket协议处理器
                            * String websocketPath, 路径
                            * String subprotocols,  指定支持的子协议
                            * boolean allowExtensions, 是否允许websocket扩展
                            * int maxFrameSize, 设置最大帧数 6553
                            * boolean allowMaskMismatch, 是否允许掩码不匹配
                            * boolean checkStartsWith, 是否严格检查路径开头
                            * long handshakeTimeoutMillis 握手超时
                            * */
                            pipeline.addLast(new WebSocketServerProtocolHandler("/ws",null,true,6553,true,true,10000L));

                            pipeline.addLast(handlerWebSocket);
                        }
                    });
                    Channel channel = serverBootstrap.bind(appConfig.getWsProt()).sync().channel();
                    log.info("Netty服务启动成功，端口:{}",appConfig.getWsProt());
                    channel.closeFuture().sync();
        }catch (Exception e){
            log.error("netty启动失败：",e);
        }finally {
            boosGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
    @PreDestroy
    public void close(){
        boosGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }
}
