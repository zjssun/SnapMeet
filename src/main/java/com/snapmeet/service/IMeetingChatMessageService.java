package com.snapmeet.service;

import com.snapmeet.entity.po.MeetingChatMessage;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author sam
 * @since 2026-01-16
 */
public interface IMeetingChatMessageService extends IService<MeetingChatMessage> {

    void saveChatMessage(MeetingChatMessage chatMessage);
}
