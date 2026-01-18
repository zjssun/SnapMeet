package com.snapmeet.service.impl;

import com.snapmeet.config.AppConfig;
import com.snapmeet.constants.Constants;
import com.snapmeet.entity.dto.MessageSendDto;
import com.snapmeet.entity.po.MeetingChatMessage;
import com.snapmeet.enums.*;
import com.snapmeet.exception.BusinessException;
import com.snapmeet.mapper.MeetingChatMessageMapper;
import com.snapmeet.service.IMeetingChatMessageService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.snapmeet.utils.FFmpegutils;
import com.snapmeet.utils.SnowFlakeUtils;
import com.snapmeet.utils.StringTools;
import com.snapmeet.utils.TableSplitUtils;
import com.snapmeet.websocket.message.MessageHandler;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
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
    @Autowired
    private AppConfig appConfig;
    @Resource
    private FFmpegutils fmpegutils;

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

    @Override
    public void uploadFile(MultipartFile file,String meetingId, Long messageId, Long sendTime) throws IOException {
        LocalDate today = LocalDate.now();
        String time = today.toString();
        String folder = appConfig.getProjectFolder() + Constants.FILE_FOLDER_FILE + time;
        File folderFile = new File(folder);
        if(folderFile.exists()){
            folderFile.mkdirs();
        }
        String filePath = folder+"/"+messageId;
        String fileName = file.getOriginalFilename();
        String fileSuffix = StringTools.getFileSuffix(fileName);
        FileTypeEnum fileTypeEnum = FileTypeEnum.getBySuffix(fileSuffix);
        if(FileTypeEnum.IMAGE==fileTypeEnum){
            File tempFile = new File(appConfig.getProjectFolder() + Constants.FILE_FOLDER_TEMP+StringTools.getRandomString(30));
            file.transferTo(tempFile);
            filePath = filePath + Constants.IMAGE_SUFFIX;
            filePath = fmpegutils.transferImageType(tempFile,filePath);
            fmpegutils.createImageThumbnail(filePath);
        }else if(fileTypeEnum==FileTypeEnum.VIDEO){
            File tempFile = new File(appConfig.getProjectFolder() + Constants.FILE_FOLDER_TEMP+StringTools.getRandomString(30));
            file.transferTo(tempFile);
            filePath = filePath + Constants.VIDEO_SUFFIX;
            fmpegutils.transferVideoType(tempFile,filePath,fileSuffix);
            fmpegutils.createImageThumbnail(filePath);
        }else {
            filePath = filePath + fileSuffix;
            file.transferTo(new File(filePath));
        }
        String tableName = TableSplitUtils.getMeetingChatMessageTable(meetingId);
        MeetingChatMessage chatMessage = new MeetingChatMessage();
        chatMessage.setStatus(MessageStatusEnum.SENDED.getStatus());
        //TODO 更新数据

        MessageSendDto sendDto = new MessageSendDto<>();
        sendDto.setMeetingId(meetingId);
        sendDto.setMessageType(MessageTypeEnum.CHAT_MEDIA_MESSAGE_UPDATE.getType());
        sendDto.setStatus(MessageStatusEnum.SENDED.getStatus());
        sendDto.setMessageId(messageId);
        sendDto.setMessageSend2Type(MessageSend2TypeEnum.GROUP.getType());
        messageHandler.sendMessage(sendDto);
    }
}
