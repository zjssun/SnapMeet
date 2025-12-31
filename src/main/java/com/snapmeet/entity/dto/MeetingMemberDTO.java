package com.snapmeet.entity.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class MeetingMemberDTO {
    private String userId;
    private String nickName;
    private Integer sex;
    private Boolean videoOpen;
    private Integer memberType;
    private LocalDateTime joinTime;
    private Integer status;
}
