package com.snapmeet.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.snapmeet.annotation.GlobalInterceptor;
import com.snapmeet.entity.dto.TokenUserInfoDto;
import com.snapmeet.entity.po.MeetingInfo;
import com.snapmeet.entity.vo.ResponseVO;
import com.snapmeet.exception.BusinessException;
import com.snapmeet.service.impl.MeetingInfoServiceImpl;
import com.snapmeet.utils.StringTools;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/meeting")
@Validated
public class MeetingInfoController extends ABaseController{
    @Resource
    MeetingInfoServiceImpl meetingInfoServiceImpl;

    @Resource
    MeetingInfoServiceImpl meetingInfoService;

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
}
