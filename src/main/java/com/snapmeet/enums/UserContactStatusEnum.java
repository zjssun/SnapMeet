package com.snapmeet.enums;

import lombok.Getter;

public enum UserContactStatusEnum {
    FRIEND(1,"好友"),
    DEL(2,"删好友"),
    BLACKLIST(3,"已拉黑");

    @Getter
    private Integer status;
    @Getter
    private String desc;

    UserContactStatusEnum(Integer status, String desc) {
        this.status = status;
        this.desc = desc;
    }

    public static UserContactStatusEnum getByStatus(Integer status) {
        for (UserContactStatusEnum item : UserContactStatusEnum.values()) {
            if (item.getStatus().equals(status)) {
                return item;
            }
        }
        return null;
    }
}
