package com.snapmeet.websocket.netty;

import com.snapmeet.entity.dto.TokenUserInfoDto;
import com.snapmeet.redis.RedisComponent;
import com.snapmeet.utils.StringTools;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ChannelHandler.Sharable
@Slf4j
public class HandlerTokenValidation extends SimpleChannelInboundHandler<FullHttpRequest> {

    @Resource
    private RedisComponent redisComponent;

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, FullHttpRequest fullHttpRequest) throws Exception {
        String uri = fullHttpRequest.uri();
        QueryStringDecoder queryStringDecoder = new QueryStringDecoder(uri);
        List<String> tokens = queryStringDecoder.parameters().get("token");
        if(tokens==null){
            senErrorResponse(channelHandlerContext);
            return;
        }
        String token = tokens.get(0);
        TokenUserInfoDto tokenUserInfoDto = checkToken(token);
        if(tokenUserInfoDto==null){
            log.info("校验token失败{}",token);
            senErrorResponse(channelHandlerContext);
        }
        channelHandlerContext.fireChannelRead(fullHttpRequest.retain());
        //TODO 连接成功后初始化工作

    }

    private TokenUserInfoDto checkToken(String token){
        if(StringTools.isEmpty(token)){
            return null;
        }
        return redisComponent.getTokenUserInfoDto(token);
    }

    private void senErrorResponse(ChannelHandlerContext ctx){
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,HttpResponseStatus.FORBIDDEN, Unpooled.copiedBuffer("token无效", CharsetUtil.UTF_8));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE,"text/plain ;charset=UTF-8");
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH,response.content().readableBytes());
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }
}
