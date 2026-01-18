package com.snapmeet.enums;

import lombok.Getter;

public enum MessageStatusEnum {
    SENDING(0,"正在发送"),
    SENDED(1,"已发送");

    @Getter
    private Integer status;
    @Getter
    private String desc;

    MessageStatusEnum(Integer status, String desc) {
        this.status = status;
        this.desc = desc;
    }

    public static MessageStatusEnum getByStatus(Integer status) {
        for (MessageStatusEnum item : MessageStatusEnum.values()) {
            if (item.getStatus().equals(status)) {
                return item;
            }
        }
        return null;
    }
}
