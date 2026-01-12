package com.snapmeet.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.snapmeet.annotation.GlobalInterceptor;
import com.snapmeet.entity.dto.MeetingMemberDTO;
import com.snapmeet.entity.dto.MessageSendDto;
import com.snapmeet.entity.dto.TokenUserInfoDto;
import com.snapmeet.entity.po.MeetingInfo;
import com.snapmeet.entity.po.MeetingMember;
import com.snapmeet.entity.vo.ResponseVO;
import com.snapmeet.enums.*;
import com.snapmeet.exception.BusinessException;
import com.snapmeet.redis.RedisComponent;
import com.snapmeet.service.impl.MeetingInfoServiceImpl;
import com.snapmeet.service.impl.MeetingMemberServiceImpl;
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
import java.util.Optional;

@RestController
@RequestMapping("/meeting")
@Validated
public class MeetingInfoController extends ABaseController{
    @Resource
    MeetingInfoServiceImpl meetingInfoServiceImpl;

    @Resource
    MeetingInfoServiceImpl meetingInfoService;

    @Resource
    MeetingMemberServiceImpl meetingMemberService;

    @Resource
    private MessageHandler messageHandler;
    @Autowired
    private RedisComponent redisComponent;

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

    //退出会议
    @RequestMapping("/exitMeeting")
    @GlobalInterceptor
    public ResponseVO exitMeeting(){
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        meetingInfoService.exitMeetingRoom(tokenUserInfoDto, MeetingMemberStatusEnum.EXIT_MEETING);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("KickOutMeeting")
    @GlobalInterceptor
    public ResponseVO kickOutMeeting(@NotEmpty String userId){
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        meetingInfoService.forceExitMeeting(tokenUserInfoDto,userId,MeetingMemberStatusEnum.KICK_OUT);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("blackMeeting")
    @GlobalInterceptor
    public ResponseVO blackMeeting(@NotEmpty String userId){
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        meetingInfoService.forceExitMeeting(tokenUserInfoDto,userId,MeetingMemberStatusEnum.KICK_OUT);
        return getSuccessResponseVO(null);
    }

    //获取当前正在进行的会议
    @RequestMapping("/getCurrentMeeting")
    @GlobalInterceptor
    public ResponseVO getCurrentMeeting(){
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        if(StringTools.isEmpty(tokenUserInfoDto.getCurrentMeetingId())){
            return getSuccessResponseVO(null);
        }
        MeetingInfo meetingInfo = this.meetingInfoService.getMeetingInfoListByMeetingId(tokenUserInfoDto.getCurrentMeetingId());
        if(MeetingStatusEnum.FINISHED.getStatus().equals(meetingInfo.getStatus())){
            return  getSuccessResponseVO(null);
        }
        return   getSuccessResponseVO(meetingInfo);
    }

    //结束会议
    @RequestMapping("/finishMeeting")
    @GlobalInterceptor
    public ResponseVO fishMeeting(){
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        meetingInfoService.finishMeeting(tokenUserInfoDto.getCurrentMeetingId(),tokenUserInfoDto.getUserId());
        return getSuccessResponseVO(null);
    }

    // 把会议标记为结束
    @RequestMapping("/delMeetingRecord")
    @GlobalInterceptor
    public ResponseVO delMeetingRecord(@NotEmpty String meetingId){
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        MeetingMember meetingMember = new MeetingMember();
        meetingMember.setStatus(MeetingMemberStatusEnum.DEL_MEETING.getStatus());
        meetingMemberService.updateByMeetingIdAndUserId(meetingMember,meetingId,tokenUserInfoDto.getUserId());
        return getSuccessResponseVO(null);
    }

    //获取会议人数
    @RequestMapping("/loadMeetingMembers")
    @GlobalInterceptor
    public ResponseVO loadMeetingMembers(@NotEmpty String meetingId){
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        List<MeetingMember> meetingMemberList = meetingMemberService.list(new LambdaQueryWrapper<MeetingMember>().eq(MeetingMember::getMeetingId,meetingId));
        Optional<MeetingMember> first = meetingMemberList.stream().filter(item->item.getUserId().equals(tokenUserInfoDto.getUserId())).findFirst();
        if(!first.isPresent()){
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        return getSuccessResponseVO(meetingMemberList);
    }

    // 加入预约会议
    @RequestMapping("/reserveJoinMeeting")
    @GlobalInterceptor
    public ResponseVO reserveJoinMeeting(@NotEmpty String meetingId,@NotEmpty String nickName,String joinPassword){
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        tokenUserInfoDto.setCurrentNickName(nickName);
        meetingInfoService.reserveJoinMeeting(meetingId,tokenUserInfoDto,joinPassword);
        return  getSuccessResponseVO(null);
    }

    //邀请加入会议
    @RequestMapping("/inviteMember")
    @GlobalInterceptor
    public ResponseVO inviteMember(@NotEmpty String selectContactIds){
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        meetingInfoService.inviteMember(tokenUserInfoDto,selectContactIds);
        return   getSuccessResponseVO(null);
    }

    //接受加入会议
    @RequestMapping("/acceptInvite")
    @GlobalInterceptor
    public ResponseVO acceptInvite(@NotEmpty String meetingId){
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        meetingInfoService.acceptInvite(tokenUserInfoDto,meetingId);
        return   getSuccessResponseVO(null);
    }
}
