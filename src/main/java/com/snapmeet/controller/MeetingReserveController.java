package com.snapmeet.controller;

import com.snapmeet.entity.dto.TokenUserInfoDto;
import com.snapmeet.entity.po.MeetingReserve;
import com.snapmeet.entity.vo.ResponseVO;
import com.snapmeet.enums.MeetingReserveStatusEnum;
import com.snapmeet.service.impl.MeetingReserveMemberServiceImpl;
import com.snapmeet.service.impl.MeetingReserveServiceImpl;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotEmpty;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/meetingReserve")
@Validated
@Slf4j
public class MeetingReserveController extends ABaseController{
    @Resource
    MeetingReserveServiceImpl meetingReserveService;

    @Resource
    MeetingReserveMemberServiceImpl meetingReserveMemberService;

    @RequestMapping("/loadMeetingReserve")
    public ResponseVO loadMeetingReserve(){
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        List<MeetingReserve> list = meetingReserveService.getReserveInfo(tokenUserInfoDto.getUserId(), MeetingReserveStatusEnum.NO_START.getStatus());
        return getSuccessResponseVO(list);
    }

    @RequestMapping("/CreateMeetingReserve")
    public ResponseVO CreateMeetingReserve(MeetingReserve meetingReserve){
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        meetingReserve.setCreateUserId(tokenUserInfoDto.getUserId());
        meetingReserveService.createMeetingReserve(meetingReserve);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/delMeetingReserve")
    public ResponseVO delMeetingReserve(@NotEmpty String meetingId){
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        meetingReserveMemberService.deleteMeetingReserve(meetingId,tokenUserInfoDto.getUserId());
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/loadTodayMeeting")
    public ResponseVO loadTodayMeeting(){
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        List<MeetingReserve> list = meetingReserveService.getTodayMeeting(tokenUserInfoDto.getUserId(),MeetingReserveStatusEnum.NO_START.getStatus());
        return getSuccessResponseVO(list);
    }
}
