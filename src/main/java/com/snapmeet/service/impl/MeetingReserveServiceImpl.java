package com.snapmeet.service.impl;

import com.github.yulichang.toolkit.JoinWrappers;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import com.snapmeet.entity.po.MeetingInfo;
import com.snapmeet.entity.po.MeetingReserve;
import com.snapmeet.entity.po.MeetingReserveMember;
import com.snapmeet.entity.po.UserInfo;
import com.snapmeet.enums.MeetingReserveStatusEnum;
import com.snapmeet.mapper.MeetingReserveMapper;
import com.snapmeet.mapper.MeetingReserveMemberMapper;
import com.snapmeet.service.IMeetingReserveService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.snapmeet.utils.StringTools;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author sam
 * @since 2026-01-06
 */
@Service
public class MeetingReserveServiceImpl extends ServiceImpl<MeetingReserveMapper, MeetingReserve> implements IMeetingReserveService {

    @Resource
    MeetingReserveMapper meetingReserveMapper;

    @Resource
    MeetingReserveMemberMapper meetingReserveMemberMapper;

    @Override
    public List<MeetingReserve> getReserveInfo(String userId, Integer status) {
        MPJLambdaWrapper<MeetingReserve> wrapper = JoinWrappers.lambda(MeetingReserve.class)
                .selectAll(MeetingInfo.class)
                .select(UserInfo::getNickName)
                .leftJoin(UserInfo.class, UserInfo::getUserId, MeetingReserve::getCreateUserId)
                .inSql(MeetingReserve::getMeetingId, "SELECT meeting_id FROM meeting_reserve_member WHERE invite_user_id = '" + userId + "'");
        return meetingReserveMapper.selectJoinList(MeetingReserve.class,wrapper);
    }

    @Override
    public void createMeetingReserve(MeetingReserve meetingReserve) {
        meetingReserve.setMeetingId(StringTools.getMeetingNoOrMeetingId());
        meetingReserve.setCreateTime(LocalDateTime.now());
        meetingReserve.setStatus(MeetingReserveStatusEnum.NO_START.getStatus());
        this.save(meetingReserve);

        List<MeetingReserveMember> reserveMemberList = new ArrayList<>();
        if(!StringTools.isEmpty(meetingReserve.getInviteUserIds())) {
            String[] inviteUserIdArray = meetingReserve.getInviteUserIds().split(",");
            for(String userId:inviteUserIdArray){
                MeetingReserveMember member = new MeetingReserveMember();
                member.setMeetingId(meetingReserve.getMeetingId());
                member.setInviteUserId(userId);
                reserveMemberList.add(member);
            }
        }
        MeetingReserveMember member = new MeetingReserveMember();
        member.setMeetingId(meetingReserve.getMeetingId());
        member.setInviteUserId(meetingReserve.getCreateUserId());
        reserveMemberList.add(member);
        meetingReserveMemberMapper.insertOrUpdate(reserveMemberList);
     }

    @Override
    public List<MeetingReserve> getTodayMeeting(String userId, Integer status) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().plusDays(1).atStartOfDay();
        MPJLambdaWrapper<MeetingReserve> wrapper = new MPJLambdaWrapper<MeetingReserve>()
                .selectAll(MeetingReserve.class)
                .distinct()
                .leftJoin(MeetingReserveMember.class, MeetingReserveMember::getMeetingId, MeetingReserve::getMeetingId)
                .eq(MeetingReserveMember::getInviteUserId, userId)
                .eq(MeetingReserve::getStatus, status)
                .ge(MeetingReserve::getCreateTime, startOfDay)
                .lt(MeetingReserve::getCreateTime, endOfDay);
        List<MeetingReserve> list = this.list(wrapper);
        return  list;
    }

    @Override
    public MeetingReserve getMeetingReserve(String meetingId) {
        return this.getOne(new MPJLambdaWrapper<MeetingReserve>().eq(MeetingReserve::getMeetingId, meetingId));
    }
}
