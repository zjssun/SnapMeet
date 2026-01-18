package com.snapmeet.enums;

import lombok.Getter;

public enum ReceiveTypeEnum {
    ALL(0,"全员"),
    USER(1,"个人");

    @Getter
    private Integer type;
    @Getter
    private String msg;

    ReceiveTypeEnum(Integer type, String msg) {
        this.type = type;
        this.msg = msg;
    }

    public static ReceiveTypeEnum getByStatus(Integer status) {
        for (ReceiveTypeEnum item : ReceiveTypeEnum.values()) {
            if (item.getType().equals(status)) {
                return item;
            }
        }
        return null;
    }
}
