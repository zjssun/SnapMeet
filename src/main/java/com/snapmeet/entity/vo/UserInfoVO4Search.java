package com.snapmeet.entity.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class UserInfoVO4Search {
    private String userId;
    private String nickName;
    private Integer status;
}
