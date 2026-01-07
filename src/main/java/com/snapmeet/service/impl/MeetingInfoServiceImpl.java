package com.snapmeet.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.snapmeet.entity.dto.*;
import com.snapmeet.entity.po.MeetingInfo;
import com.snapmeet.entity.po.MeetingMember;
import com.snapmeet.entity.po.MeetingReserve;
import com.snapmeet.entity.po.MeetingReserveMember;
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
import org.apache.commons.lang3.ArrayUtils;
import org.apache.ibatis.reflection.ArrayUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

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

    @Resource
    private MeetingReserveServiceImpl meetingReserveService;

    @Resource
    private MeetingReserveMemberServiceImpl meetingReserveMemberService;

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

    @Override
    public void exitMeetingRoom(TokenUserInfoDto tokenUserInfoDto, MeetingMemberStatusEnum statusEnum) {
        String meetingId = tokenUserInfoDto.getCurrentMeetingId();
        if(StringTools.isEmpty(meetingId)){
            return;
        }
        String userId = tokenUserInfoDto.getUserId();
        Boolean exit = redisComponent.exitMeeting(meetingId,userId,statusEnum);
        if(!exit){
            // Redis里没成功退出（可能本来就不在），但仍需清理用户的本地状态
            tokenUserInfoDto.setCurrentMeetingId(null);
            redisComponent.saveTokenUserInfoDto(tokenUserInfoDto);
            return;
        }
        //清空当前正在进行的会议
        tokenUserInfoDto.setCurrentMeetingId(null);
        redisComponent.saveTokenUserInfoDto(tokenUserInfoDto);

        MessageSendDto messageSendDto = new MessageSendDto();
        messageSendDto.setMessageType(MessageTypeEnum.EXIT_MEETING_ROOM.getType());
        // 获取最新名单
        List<MeetingMemberDTO> meetingMemberDTOList = redisComponent.getMeetingMemberList(meetingId);
        // 封装业务包
        MeetingExitDto meetingExitDto = new MeetingExitDto();
        meetingExitDto.setExitUserId(userId)
                .setMeetingMemberDTOList(meetingMemberDTOList)
                .setExitStatus(statusEnum.getStatus());
        messageSendDto.setMessageContent(JSON.toJSON(meetingExitDto));
        messageSendDto.setMeetingId(meetingId);
        messageSendDto.setMessageSend2Type(MessageSend2TypeEnum.GROUP.getType());
        // 发送
        messageHandler.sendMessage(messageSendDto);

        List<MeetingMemberDTO> onLineMember = meetingMemberDTOList.stream().filter(item-> MeetingMemberStatusEnum.NORMAL.getStatus().equals(item.getStatus())).collect(Collectors.toList());
        if(onLineMember.isEmpty()){
            //TODO 结束会议
            MeetingReserve meetingReserve = meetingReserveService.getMeetingReserve(meetingId);
            if(onLineMember.isEmpty()){
                finishMeeting(meetingId,tokenUserInfoDto.getUserId());
                return;
            }
            if(System.currentTimeMillis() > meetingReserve.getStartTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()){
                finishMeeting(meetingId,null);
                return;
            }

        }
        if(ArrayUtils.contains(new Integer[]{MeetingMemberStatusEnum.KICK_OUT.getStatus(),MeetingMemberStatusEnum.BLACKLIST.getStatus()},statusEnum.getStatus())){
            MeetingMember meetingMember = new MeetingMember();
            meetingMember.setStatus(statusEnum.getStatus());
            meetingMemberService.updateByMeetingIdAndUserId(meetingMember,meetingId,userId);
        }
    }

    @Override
    public void forceExitMeeting(TokenUserInfoDto tokenUserInfoDto,String userId, MeetingMemberStatusEnum statusEnum) {

        MeetingInfo meetingInfo = this.getOne(new LambdaQueryWrapper<MeetingInfo>().eq(MeetingInfo::getMeetingId, tokenUserInfoDto.getCurrentMeetingId()));
        if(!meetingInfo.getCreateUserId().equals(tokenUserInfoDto.getUserId())){
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        TokenUserInfoDto userInfoDto = this.redisComponent.getTokenUserInfoDtoByUserId(userId);
        exitMeetingRoom(userInfoDto,statusEnum);
    }

    @Override
    public MeetingInfo getMeetingInfoListByMeetingId(String currentMeetingId) {
        return this.getOne(new LambdaQueryWrapper<MeetingInfo>().eq(MeetingInfo::getMeetingId, currentMeetingId));
    }

    @Override
    public void finishMeeting(String currentMeetingId, String userId) {
        MeetingInfo meetingInfo = this.getOne(new LambdaQueryWrapper<MeetingInfo>().eq(MeetingInfo::getMeetingId, currentMeetingId));
        if(userId!=null&&!meetingInfo.getCreateUserId().equals(userId)){
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        MeetingInfo updateInfo = new MeetingInfo();
        updateInfo.setStatus(MeetingStatusEnum.FINISHED.getStatus());
        updateInfo.setEndTime(LocalDateTime.now());
        this.update(updateInfo,new LambdaQueryWrapper<MeetingInfo>().eq(MeetingInfo::getMeetingId, currentMeetingId));

        MessageSendDto messageSendDto = new MessageSendDto<>();
        messageSendDto.setMessageSend2Type(MessageSend2TypeEnum.GROUP.getType());
        messageSendDto.setMessageType(MessageTypeEnum.FINISH_MEETING.getType());
        messageHandler.sendMessage(messageSendDto);

        MeetingMember meetingMember = new MeetingMember();
        meetingMember.setMeetingStatus(MeetingStatusEnum.FINISHED.getStatus());
        meetingMemberService.updateByMeeingId(meetingMember,currentMeetingId);

        //TODO 更新预约会议状态
        MeetingReserve updateMeetingReserve = new MeetingReserve();
        updateMeetingReserve.setStatus(MeetingStatusEnum.FINISHED.getStatus());
        meetingReserveService.update(updateMeetingReserve,new LambdaQueryWrapper<MeetingReserve>().eq(MeetingReserve::getMeetingId, currentMeetingId));

        List<MeetingMemberDTO> meetingMemberDTOList = redisComponent.getMeetingMemberList(currentMeetingId);
        for (MeetingMemberDTO meetingMemberDTO:meetingMemberDTOList){
            TokenUserInfoDto userInfoDto = this.redisComponent.getTokenUserInfoDtoByUserId(meetingMemberDTO.getUserId());
            userInfoDto.setCurrentMeetingId(null);
            redisComponent.saveTokenUserInfoDto(userInfoDto);
        }
    }

    @Override
    public void reserveJoinMeeting(String meetingId, TokenUserInfoDto tokenUserInfoDto, String joinPassword) {
        String userId = tokenUserInfoDto.getUserId();
        if(!StringTools.isEmpty(tokenUserInfoDto.getCurrentMeetingId()) && !meetingId.equals(tokenUserInfoDto.getCurrentMeetingId())){
            throw new BusinessException("你有未结束的会议");
        }
        checkMeetingJoin(meetingId,userId);
        MeetingReserve meetingReserve = meetingReserveService.getMeetingReserve(meetingId);
        if(meetingReserve==null){
            throw  new BusinessException(ResponseCodeEnum.CODE_600);
        }
        MeetingReserveMember member = meetingReserveMemberService.selectByMeetingIdAndUserId(meetingId,userId);
        if (member == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        if(MeetingJoinTypeEnum.PASSWORD.getType().equals(meetingReserve.getJoinType())&&!meetingReserve.getJoinPassword().equals(joinPassword)){
            throw  new BusinessException("入会密码不正确");
        }
        MeetingInfo meetingInfo = this.getOne(new LambdaQueryWrapper<MeetingInfo>().eq(MeetingInfo::getMeetingId, meetingId));
        if(meetingInfo==null){
            meetingInfo = new MeetingInfo();
            meetingInfo.setMeetingName(meetingReserve.getMeetingName());
            meetingInfo.setMeetingNo(StringTools.getMeetingNoOrMeetingId());
            meetingInfo.setJoinType(meetingReserve.getJoinType());
            meetingInfo.setJoinPassword(meetingReserve.getJoinPassword());
            meetingInfo.setCreateTime(LocalDateTime.now());
            meetingInfo.setMeetingId(meetingId);
            meetingInfo.setStartTime(LocalDateTime.now());
            meetingInfo.setStatus(MeetingStatusEnum.RUNING.getStatus());
            meetingInfo.setCreateUserId(meetingReserve.getCreateUserId());
            this.save(meetingInfo);
        }
        tokenUserInfoDto.setCurrentMeetingId(meetingId);
        redisComponent.saveTokenUserInfoDto(tokenUserInfoDto);
    }
}
