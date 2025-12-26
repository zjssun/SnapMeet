package com.snapmeet.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.snapmeet.entity.po.MeetingInfo;
import com.snapmeet.entity.po.MeetingMember;
import com.snapmeet.mapper.MeetingInfoMapper;
import com.snapmeet.mapper.MeetingMemberMapper;
import com.snapmeet.service.IMeetingInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

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
}
