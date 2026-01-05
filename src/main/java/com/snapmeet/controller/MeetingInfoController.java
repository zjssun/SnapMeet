package com.snapmeet.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.snapmeet.annotation.GlobalInterceptor;
import com.snapmeet.entity.dto.MeetingMemberDTO;
import com.snapmeet.entity.dto.MessageSendDto;
import com.snapmeet.entity.dto.TokenUserInfoDto;
import com.snapmeet.entity.po.MeetingInfo;
import com.snapmeet.entity.vo.ResponseVO;
import com.snapmeet.enums.MeetingMemberStatusEnum;
import com.snapmeet.enums.MessageSend2TypeEnum;
import com.snapmeet.enums.MessageTypeEnum;
import com.snapmeet.exception.BusinessException;
import com.snapmeet.redis.RedisComponent;
import com.snapmeet.service.impl.MeetingInfoServiceImpl;
import com.snapmeet.utils.StringTools;
import com.snapmeet.websocket.message.MessageHandler;
import com.snapmeet.websocket.message.MessageHandler4RabbitMQ;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/meeting")
@Validated
public class MeetingInfoController extends ABaseController{
    @Resource
    MeetingInfoServiceImpl meetingInfoServiceImpl;

    @Resource
    MeetingInfoServiceImpl meetingInfoService;

    @Resource
    private MessageHandler messageHandler;
    @Autowired
    private RedisComponent redisComponent;

    @RequestMapping("/getCurrenMeeting")
    @GlobalInterceptor
    public ResponseVO getCurrenMeeting(){

        return getSuccessResponseVO(null);
    }

    @RequestMapping("/loadMeeting")
    @GlobalInterceptor
    public ResponseVO loadMeeting(Integer pageNo){
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        Page<MeetingInfo> page = meetingInfoServiceImpl.getMeetingInfoList(tokenUserInfoDto.getUserId(),pageNo);
        return getSuccessResponseVO(page);
    }

    @RequestMapping("/quickMeeting")
    @GlobalInterceptor
    public ResponseVO quickMeeting(@NotNull Integer meetingNoType,
                                   @NotEmpty @Size(max = 100) String meetingName,
                                   @NotNull Integer joinType, @Max(5) String joinPassword){
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        if(tokenUserInfoDto.getCurrentMeetingId() != null){
            throw new BusinessException("你有未结束的会议,无法创建新的会议");
        }
        MeetingInfo meetingInfo = new  MeetingInfo();
        meetingInfo.setMeetingName(meetingName);
        meetingInfo.setMeetingNo(meetingNoType==0?tokenUserInfoDto.getMyMeetingNo(): StringTools.getMeetingNoOrMeetingId());
        meetingInfo.setJoinType(joinType);
        meetingInfo.setJoinPassword(joinPassword);
        meetingInfo.setCreateUserId(tokenUserInfoDto.getUserId());
        meetingInfoService.qucikMeeting(meetingInfo,tokenUserInfoDto.getNickName());

        tokenUserInfoDto.setCurrentMeetingId(meetingInfo.getMeetingId());
        tokenUserInfoDto.setCurrentNickName(tokenUserInfoDto.getNickName());
        resetTokenUserInfo(tokenUserInfoDto);
        return getSuccessResponseVO(meetingInfo.getMeetingId());
    }

    @RequestMapping("/joinMeeting")
    @GlobalInterceptor
    public ResponseVO joinMeeting(@NotNull Boolean videOpen){
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        meetingInfoService.joinMeeting(tokenUserInfoDto.getCurrentMeetingId(),tokenUserInfoDto.getUserId(),tokenUserInfoDto.getNickName(),tokenUserInfoDto.getSex(),videOpen);
        return  getSuccessResponseVO(null);
    }

    @RequestMapping("/preJoinMeeting")
    @GlobalInterceptor
    public ResponseVO preJoinMeeting(@NotNull String meetingNo,@NotEmpty String nickName,String password){
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        meetingNo = meetingNo.replace(" ","");
        tokenUserInfoDto.setCurrentNickName(nickName);
        String meetingId = meetingInfoService.preJoinMeeting(meetingNo,tokenUserInfoDto,password);
        return getSuccessResponseVO(meetingId);
    }

    @RequestMapping("/existMeeting")
    @GlobalInterceptor
    public ResponseVO exitMeeting(){
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        meetingInfoService.exitMeetingRoom(tokenUserInfoDto, MeetingMemberStatusEnum.EXIT_MEETING);
        return getSuccessResponseVO(null);
    }
}
