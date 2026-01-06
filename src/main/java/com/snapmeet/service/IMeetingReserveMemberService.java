package com.snapmeet.service;

import com.snapmeet.entity.po.MeetingReserve;
import com.snapmeet.entity.po.MeetingReserveMember;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author sam
 * @since 2026-01-06
 */
public interface IMeetingReserveMemberService extends IService<MeetingReserveMember> {
    void deleteMeetingReserve(String meetingId,String userId);
}
