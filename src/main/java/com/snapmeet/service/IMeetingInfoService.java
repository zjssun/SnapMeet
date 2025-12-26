package com.snapmeet.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.snapmeet.entity.po.MeetingInfo;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author sam
 * @since 2025-12-25
 */
public interface IMeetingInfoService extends IService<MeetingInfo> {

    Page<MeetingInfo> getMeetingInfoList(String userId, Integer pageNo);
}
