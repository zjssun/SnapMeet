package com.snapmeet.entity.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class MeetingInviteDto {
    private String meetingName;
    private String inviteUserName;
    private String meetingId;
}
