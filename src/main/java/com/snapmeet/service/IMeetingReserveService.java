package com.snapmeet.service;

import com.snapmeet.entity.po.MeetingReserve;
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
public interface IMeetingReserveService extends IService<MeetingReserve> {
    List<MeetingReserve> getReserveInfo(String userId, Integer status);

    void createMeetingReserve(MeetingReserve meetingReserve);

    List<MeetingReserve> getTodayMeeting(String userId, Integer status);

    MeetingReserve getMeetingReserve(String meetingId);
}
