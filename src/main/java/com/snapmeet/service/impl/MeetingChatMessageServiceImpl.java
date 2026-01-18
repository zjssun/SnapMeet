package com.snapmeet.service.impl;

import com.snapmeet.entity.dto.MessageSendDto;
import com.snapmeet.entity.po.MeetingChatMessage;
import com.snapmeet.enums.*;
import com.snapmeet.exception.BusinessException;
import com.snapmeet.mapper.MeetingChatMessageMapper;
import com.snapmeet.service.IMeetingChatMessageService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.snapmeet.utils.SnowFlakeUtils;
import com.snapmeet.utils.StringTools;
import com.snapmeet.utils.TableSplitUtils;
import com.snapmeet.websocket.message.MessageHandler;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author sam
 * @since 2026-01-16
 */
@Service
public class MeetingChatMessageServiceImpl extends ServiceImpl<MeetingChatMessageMapper, MeetingChatMessage> implements IMeetingChatMessageService {

    @Resource
    private MessageHandler messageHandler;

    @Override
    public void saveChatMessage(MeetingChatMessage chatMessage) {
        if(!ArrayUtils.contains(new Integer[]{MessageTypeEnum.CHAT_TEXT_MESSAGE.getType(),MessageTypeEnum.CHAT_MEDIA_MESSAGE.getType()},chatMessage.getMessageType())){
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        ReceiveTypeEnum receiveTypeEnum = ReceiveTypeEnum.getByStatus(chatMessage.getReceiveType());
        if(null == receiveTypeEnum){
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        if(receiveTypeEnum==ReceiveTypeEnum.USER&& StringTools.isEmpty(chatMessage.getReceiveUserId())){
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        MessageTypeEnum messageTypeEnum = MessageTypeEnum.getByType(chatMessage.getMessageType());
        if(messageTypeEnum==MessageTypeEnum.CHAT_TEXT_MESSAGE){
            if(StringTools.isEmpty(chatMessage.getMessageContent())){
                throw new BusinessException(ResponseCodeEnum.CODE_600);
            }
            chatMessage.setStatus(MessageStatusEnum.SENDED.getStatus());
        }else if(messageTypeEnum==MessageTypeEnum.CHAT_MEDIA_MESSAGE){
            if(StringTools.isEmpty(chatMessage.getFileName()) || chatMessage.getFileSize()==null||chatMessage.getFileType()==null){
                throw  new BusinessException(ResponseCodeEnum.CODE_600);
            }
            chatMessage.setFileSuffx(StringTools.getFileSuffix(chatMessage.getFileName()));
            chatMessage.setStatus(MessageStatusEnum.SENDING.getStatus());
        }

        chatMessage.setSendTime(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        chatMessage.setMessageId(SnowFlakeUtils.nextId());
        String tableName = TableSplitUtils.getMeetingChatMessageTable(chatMessage.getMeetingId());
        //TODO sql语句
        MessageSendDto sendDto = StringTools.copyProperties(chatMessage,MessageSendDto.class);
        if(ReceiveTypeEnum.USER == receiveTypeEnum){
            sendDto.setMessageSend2Type(MessageSend2TypeEnum.USER.getType());
            messageHandler.sendMessage(sendDto);
            sendDto.setReceiveUserId(chatMessage.getSendUserId());
            messageHandler.sendMessage(sendDto);
        }else {
            sendDto.setMessageSend2Type(MessageSend2TypeEnum.GROUP.getType());
            messageHandler.sendMessage(sendDto);
        }
    }
}
