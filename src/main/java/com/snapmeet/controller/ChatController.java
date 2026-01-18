package com.snapmeet.controller;

import com.snapmeet.annotation.GlobalInterceptor;
import com.snapmeet.constants.Constants;
import com.snapmeet.entity.dto.TokenUserInfoDto;
import com.snapmeet.entity.po.MeetingChatMessage;
import com.snapmeet.entity.vo.ResponseVO;
import com.snapmeet.enums.ReceiveTypeEnum;
import com.snapmeet.service.impl.MeetingChatMessageServiceImpl;
import com.snapmeet.utils.TableSplitUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping
@Validated
@Slf4j
public class ChatController extends ABaseController{

    @Resource
    private MeetingChatMessageServiceImpl meetingChatMessageService;

    @RequestMapping("/loadMessage")
    @GlobalInterceptor
    public ResponseVO loadMessage(Long maxMessageId,Integer pageNo){
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        String meetingId = tokenUserInfoDto.getCurrentMeetingId();
        String tableName = TableSplitUtils.getMeetingChatMessageTable(meetingId);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/sendMessage")
    @GlobalInterceptor
    public ResponseVO sendMessage(String message,Integer messageType, String receiveUserId,String fileName,Long fileSize,Integer fileType){
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        MeetingChatMessage chatMessage = new MeetingChatMessage();
        chatMessage.setMessageType(messageType);
        chatMessage.setMessageContent(message);
        chatMessage.setFileName(fileName);
        chatMessage.setFileSize(fileSize);
        chatMessage.setFileType(fileType);
        chatMessage.setSendUserId(tokenUserInfoDto.getUserId());
        chatMessage.setSendUserNickName(tokenUserInfoDto.getNickName());
        chatMessage.setMeetingId(tokenUserInfoDto.getCurrentMeetingId());
        if(Constants.ZERO_STR.equals(receiveUserId)){
            chatMessage.setReceiveType(ReceiveTypeEnum.ALL.getType());
        }else {
            chatMessage.setReceiveType(ReceiveTypeEnum.USER.getType());
        }
        chatMessage.setReceiveUserId(receiveUserId);
        meetingChatMessageService.saveChatMessage(chatMessage);
        return getSuccessResponseVO(null);
    }
    @RequestMapping("/uploadFile")
    @GlobalInterceptor
    public ResponseVO uploadFile(MultipartFile file,Long messageId,Long sendTime) throws IOException {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        meetingChatMessageService.uploadFile(file,tokenUserInfoDto.getCurrentMeetingId(),messageId,sendTime);
        return  getSuccessResponseVO(null);
    }
}
