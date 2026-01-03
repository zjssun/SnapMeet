package com.snapmeet.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.snapmeet.entity.dto.MeetingJoinDto;
import com.snapmeet.entity.dto.MeetingMemberDTO;
import com.snapmeet.entity.dto.MessageSendDto;
import com.snapmeet.entity.dto.TokenUserInfoDto;
import com.snapmeet.entity.po.MeetingInfo;
import com.snapmeet.entity.po.MeetingMember;
import com.snapmeet.enums.*;
import com.snapmeet.exception.BusinessException;
import com.snapmeet.mapper.MeetingInfoMapper;
import com.snapmeet.mapper.MeetingMemberMapper;
import com.snapmeet.redis.RedisComponent;
import com.snapmeet.service.IMeetingInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.snapmeet.utils.StringTools;
import com.snapmeet.websocket.ChannelContextUtils;
import com.snapmeet.websocket.message.MessageHandler;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author sam
 * @since 2025-12-25
 */
@Service
public class MeetingInfoServiceImpl extends ServiceImpl<MeetingInfoMapper, MeetingInfo> implements IMeetingInfoService {

    @Resource
    MeetingInfoMapper meetingInfoMapper;

    @Resource
    private ChannelContextUtils channelContextUtils;

    @Resource
    private MeetingMemberServiceImpl meetingMemberService;

    @Autowired
    private RedisComponent redisComponent;

    @Resource
    private MessageHandler messageHandler;

    @Override
    public Page<MeetingInfo> getMeetingInfoList(String userId, Integer pageNo) {
        Page<MeetingInfo> page = new Page<>(pageNo, 15);

        QueryWrapper<MeetingInfo> wrapper = new QueryWrapper<>();
        wrapper.select(
                "meeting_id", "meeting_no", "meeting_name", "create_time", "create_user_id", "join_type","join_password","start_time","end_time","status",
                "(SELECT count(1) FROM meeting_member mm WHERE mm.meeting_id = meeting_info.meeting_id) AS memberCount"
        );
        wrapper.inSql("meeting_id",
                "SELECT meeting_id FROM meeting_member WHERE user_id = '" + userId + "' AND status = 1");

        wrapper.orderByDesc("create_time");
        meetingInfoMapper.selectPage(page, wrapper);
        return page;
    }

    @Override
    public void qucikMeeting(MeetingInfo meetingInfo, String nickName) {
        LocalDateTime curDate = LocalDateTime.now();
        meetingInfo.setCreateTime(curDate);
        meetingInfo.setMeetingId(StringTools.getMeetingNoOrMeetingId());
        meetingInfo.setStartTime(curDate);
        meetingInfo.setStatus(MeetingStatusEnum.RUNING.getStatus());
        this.save(meetingInfo);
    }

    private void addMeetingMember(String meetingId,String userId,String nickName,Integer memberType){
        MeetingMember meetingMember = new MeetingMember();
        meetingMember.setMeetingId(meetingId);
        meetingMember.setUserId(userId);
        meetingMember.setNickName(nickName);
        LocalDateTime localDateTime = LocalDateTime.now();
        meetingMember.setLastJoinTime(localDateTime);
        meetingMember.setStatus(MeetingMemberStatusEnum.NORMAL.getStatus());
        meetingMember.setMemberType(memberType);
        meetingMember.setMeetingStatus(MeetingStatusEnum.RUNING.getStatus());
        meetingMemberService.insertOrUpdate(meetingMember);
    }

    private void add2Meeting(String meetingId,String userId,String nickName,Integer sex,Integer memberType,Boolean videoOpen){
        MeetingMemberDTO meetingMemberDTO = new MeetingMemberDTO();
        LocalDateTime localDateTime = LocalDateTime.now();
        meetingMemberDTO.setUserId(userId)
                .setNickName(nickName)
                .setJoinTime(localDateTime)
                .setSex(sex)
                .setMemberType(memberType)
                .setVideoOpen(videoOpen)
                .setStatus(MeetingMemberStatusEnum.NORMAL.getStatus());
        redisComponent.add2Meeting(meetingId,meetingMemberDTO);
    }

    private void checkMeetingJoin(String meetingId,String userId){
        MeetingMemberDTO meetingMemberDTO = redisComponent.getMeetingMember(meetingId,userId);
        if(meetingMemberDTO!=null&&MeetingMemberStatusEnum.BLACKLIST.getStatus().equals(meetingMemberDTO.getStatus())){
            throw new BusinessException("你已经被拉黑无法加入会议");
        }
    }

    @Override
    public void joinMeeting(String meetingId, String userId, String nickName, Integer sex, Boolean videoOpen) {
        if(StringTools.isEmpty(meetingId)){
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        MeetingInfo meetingInfo = this.getOne(new LambdaQueryWrapper<MeetingInfo>().eq(MeetingInfo::getMeetingId, meetingId));
        if(meetingInfo == null || MeetingStatusEnum.FINISHED.getStatus().equals(meetingInfo.getStatus())){
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }

        //校验用户
        checkMeetingJoin(meetingId,userId);
        //加入成员
        MemberTypeEnum memberTypeEnum = meetingInfo.getCreateUserId().equals(userId) ? MemberTypeEnum.COMPERE : MemberTypeEnum.NORMAL;
        addMeetingMember(meetingId,userId,nickName,memberTypeEnum.getType());
        //加入会议
        add2Meeting(meetingId,userId,nickName,sex,memberTypeEnum.getType(),videoOpen);
        //加入ws 房间
        channelContextUtils.addMeetingRoom(meetingId,userId);
        //发送ws消息
        MeetingJoinDto meetingJoinDto = new MeetingJoinDto();
        meetingJoinDto.setMeetingMemberList(redisComponent.getMeetingMemberList(meetingId));
        meetingJoinDto.setNewMember(redisComponent.getMeetingMember(meetingId,userId));

        MessageSendDto messageSendDto = new MessageSendDto();
        messageSendDto.setMessageType(MessageTypeEnum.ADD_MEETING_ROOM.getType());
        messageSendDto.setMeetingId(meetingId);
        messageSendDto.setMessageSend2Type(MessageSend2TypeEnum.GROUP.getType());
        messageSendDto.setMessageContent(meetingJoinDto);
        messageHandler.sendMessage(messageSendDto);
    }

    @Override
    public String preJoinMeeting(String meetingNo, TokenUserInfoDto tokenUserInfoDto, String password) {
        String userId = tokenUserInfoDto.getUserId();
        LambdaQueryWrapper<MeetingInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MeetingInfo::getMeetingNo, meetingNo)
                .eq(MeetingInfo::getStatus, MeetingStatusEnum.RUNING.getStatus())
                .orderByDesc(MeetingInfo::getCreateTime);
        List<MeetingInfo> meetingInfoList = this.list(wrapper);
        if(meetingInfoList.isEmpty()){
            throw new BusinessException("会议不存在");
        }
        MeetingInfo meetingInfo = meetingInfoList.get(0);
        if(!MeetingStatusEnum.RUNING.getStatus().equals(meetingInfo.getStatus())){
            throw  new BusinessException("会议结束");
        }
        if(!StringTools.isEmpty(tokenUserInfoDto.getCurrentMeetingId())&&!meetingInfo.getMeetingId().equals(tokenUserInfoDto.getCurrentMeetingId())){
            throw  new BusinessException("你有未结束的会议");
        }
        checkMeetingJoin(meetingInfo.getMeetingId(),userId);
        if(MeetingJoinTypeEnum.PASSWORD.getType().equals(meetingInfo.getJoinType()) && !meetingInfo.getJoinPassword().equals(password)){
            throw new BusinessException("入会密码不正确");
        }

        tokenUserInfoDto.setCurrentMeetingId(meetingInfo.getMeetingId());
        redisComponent.saveTokenUserInfoDto(tokenUserInfoDto);
        return meetingInfo.getMeetingId();
    }
}
