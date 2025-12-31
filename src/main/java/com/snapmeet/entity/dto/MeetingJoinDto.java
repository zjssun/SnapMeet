package com.snapmeet.entity.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class MeetingJoinDto {
    private MeetingMemberDTO newMember;
    private List<MeetingMemberDTO> meetingMemberList;
}
