package com.snapmeet.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import com.snapmeet.entity.po.MeetingReserve;
import com.snapmeet.entity.po.MeetingReserveMember;
import com.snapmeet.mapper.MeetingReserveMemberMapper;
import com.snapmeet.service.IMeetingReserveMemberService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
public class MeetingReserveMemberServiceImpl extends ServiceImpl<MeetingReserveMemberMapper, MeetingReserveMember> implements IMeetingReserveMemberService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMeetingReserve(String meetingId, String userId) {
        MPJLambdaWrapper<MeetingReserveMember> wrapper = new MPJLambdaWrapper<MeetingReserveMember>()
                .selectAll(MeetingReserveMember.class)
                // SQL: LEFT JOIN meeting_reserve t1 ON t1.id = t.meeting_id
                .leftJoin(MeetingReserve.class, MeetingReserve::getMeetingId, MeetingReserveMember::getMeetingId)
                .eq(MeetingReserveMember::getMeetingId, meetingId)
                .eq(MeetingReserve::getCreateUserId, userId);
        long count = this.count(wrapper);
        if(count>0){
            MeetingReserveMember meetingReserveMember = new MeetingReserveMember();
            meetingReserveMember.setMeetingId(meetingId);
            this.remove(new MPJLambdaWrapper<MeetingReserveMember>().eq(MeetingReserveMember::getMeetingId, meetingId));
        }
    }
}
