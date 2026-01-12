package com.snapmeet.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.snapmeet.entity.dto.TokenUserInfoDto;
import com.snapmeet.entity.po.MeetingInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.snapmeet.enums.MeetingMemberStatusEnum;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

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

    void qucikMeeting(MeetingInfo meetingInfo, String nickName);

    void joinMeeting(String meetingId,String UserId,String nickName,Integer sex,Boolean videoOpen);

    String preJoinMeeting(@NotNull String meetingNo, TokenUserInfoDto tokenUserInfoDto, String password);

    void exitMeetingRoom(TokenUserInfoDto tokenUserInfoDto, MeetingMemberStatusEnum statusEnum);

    void forceExitMeeting(TokenUserInfoDto tokenUserInfoDto,String userId,MeetingMemberStatusEnum statusEnum);

    MeetingInfo getMeetingInfoListByMeetingId(String currentMeetingId);

    void finishMeeting(String currentMeetingId, String userId);

    void reserveJoinMeeting(@NotEmpty String meetingId, TokenUserInfoDto tokenUserInfoDto, String joinPassword);

    void inviteMember(TokenUserInfoDto tokenUserInfoDto, @NotEmpty String selectContactIds);

    void acceptInvite(TokenUserInfoDto tokenUserInfoDto,String meetingId);
}
