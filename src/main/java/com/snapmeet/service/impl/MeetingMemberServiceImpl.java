package com.snapmeet.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.snapmeet.entity.po.MeetingMember;
import com.snapmeet.mapper.MeetingMemberMapper;
import com.snapmeet.service.IMeetingMemberService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author sam
 * @since 2025-12-25
 */
@Service
public class MeetingMemberServiceImpl extends ServiceImpl<MeetingMemberMapper, MeetingMember> implements IMeetingMemberService {

    @Override
    public void insertOrUpdate(MeetingMember meetingMember) {
        this.saveOrUpdate(meetingMember);
    }

    @Override
    public void updateByMeetingIdAndUserId(MeetingMember meetingMember, String meetingId, String userId) {
        LambdaQueryWrapper<MeetingMember> wrapper = new  LambdaQueryWrapper<>();
        wrapper.eq(MeetingMember::getMeetingId, meetingId).eq(MeetingMember::getUserId, userId);
        this.update(meetingMember,  wrapper);
    }

    @Override
    public void updateByMeeingId(MeetingMember meetingMember, String meetingId) {
        this.update(meetingMember,new LambdaQueryWrapper<MeetingMember>().eq(MeetingMember::getMeetingId,meetingId));
    }
}
