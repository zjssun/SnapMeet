package com.snapmeet.entity.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class UserInfoVO implements Serializable {
    private String userId;
    private String nickName;
    private Integer sex;
    private String meeting;
    private String token;
    private Boolean admin;
}
