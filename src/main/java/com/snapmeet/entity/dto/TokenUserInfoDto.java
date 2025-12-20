package com.snapmeet.entity.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class TokenUserInfoDto implements Serializable {
    private String token;
    private String userId;
    private String nickName;
    private Integer sex;
    private String currentMeetingId;
    private Boolean currentNickName;
    private String myMeetingNo;
    private Boolean admin;
}
