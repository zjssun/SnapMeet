package com.snapmeet.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.snapmeet.entity.dto.TokenUserInfoDto;
import com.snapmeet.entity.po.MeetingInfo;
import com.snapmeet.entity.vo.ResponseVO;
import com.snapmeet.service.impl.MeetingInfoServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/meeting")
@Validated
public class MeetingInfoController extends ABaseController{
    @Resource
    MeetingInfoServiceImpl meetingInfoServiceImpl;

    @RequestMapping("/getCurrenMeeting")
    public ResponseVO getCurrenMeeting(){

        return getSuccessResponseVO(null);
    }

    @RequestMapping("/loadMeeting")
    public ResponseVO loadMeeting(Integer pageNo){
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        Page<MeetingInfo> page = meetingInfoServiceImpl.getMeetingInfoList(tokenUserInfoDto.getUserId(),pageNo);
        return getSuccessResponseVO(page);
    }
}
