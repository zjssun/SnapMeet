package com.snapmeet.entity.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class MeetingExitDto {
    public String exitUserId;
    private List<MeetingMemberDTO> meetingMemberDTOList;
    private Integer exitStatus;
}
