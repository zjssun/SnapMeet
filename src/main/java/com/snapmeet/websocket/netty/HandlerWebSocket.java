package com.snapmeet.websocket.netty;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.snapmeet.constants.Constants;
import com.snapmeet.entity.dto.MessageSendDto;
import com.snapmeet.entity.dto.PeerConnectionDataDto;
import com.snapmeet.entity.dto.PeerMessageDto;
import com.snapmeet.entity.dto.TokenUserInfoDto;
import com.snapmeet.entity.po.UserInfo;
import com.snapmeet.enums.MessageSend2TypeEnum;
import com.snapmeet.enums.MessageTypeEnum;
import com.snapmeet.mapper.UserInfoMapper;
import com.snapmeet.redis.RedisComponent;
import com.snapmeet.websocket.message.MessageHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@ChannelHandler.Sharable
@Slf4j
public class HandlerWebSocket extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    private final RedisComponent redisComponent;
    private final MessageHandler messageHandler;
    private UserInfoMapper userInfoMapper;

    public HandlerWebSocket(RedisComponent redisComponent, MessageHandler messageHandler) {
        this.redisComponent = redisComponent;
        this.messageHandler = messageHandler;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("有新的连接加入");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("有连接断开");
        // TODO 处理连接断开的逻辑
        Attribute<String> attribute = ctx.channel().attr(AttributeKey.valueOf(ctx.channel().id().toString()));
        String userId = attribute.get();
        UserInfo userInfo = new UserInfo();
        userInfo.setLastOffTime(System.currentTimeMillis());
        userInfoMapper.update(userInfo, new LambdaQueryWrapper<UserInfo>().eq(UserInfo::getUserId, userId));
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, TextWebSocketFrame textWebSocketFrame) throws Exception {
        String text = textWebSocketFrame.text();
        if(Constants.PING.equals(text)){

        }
        log.error("收到消息：{}",text);
        PeerConnectionDataDto peerConnectionDataDto = JSON.parseObject(text,PeerConnectionDataDto.class);
        TokenUserInfoDto tokenUserInfoDto = redisComponent.getTokenUserInfoDto(peerConnectionDataDto.getToken());
        if(tokenUserInfoDto==null){
            return;
        }
        MessageSendDto messageSendDto = new MessageSendDto();
        messageSendDto.setMessageType(MessageTypeEnum.PEER.getType());

        PeerMessageDto peerMessageDto = new PeerMessageDto();
        peerMessageDto.setSignalData(peerConnectionDataDto.getSignalData());
        peerMessageDto.setSignalType(peerConnectionDataDto.getSignalType());

        messageSendDto.setMessageContent(peerMessageDto);
        messageSendDto.setMeetingId(tokenUserInfoDto.getCurrentMeetingId());
        messageSendDto.setSendUserId(tokenUserInfoDto.getUserId());
        messageSendDto.setReceiveUserId(peerConnectionDataDto.getReceiveUserId());
        messageSendDto.setMessageSend2Type(MessageSend2TypeEnum.USER.getType());

        messageHandler.sendMessage(messageSendDto);
    }
}
