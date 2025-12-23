package com.snapmeet.enums;

import lombok.Getter;

public enum MessageSend2TypeEnum {
    USER(0,"个人"),
    GROUP(1,"群");

    @Getter
    private Integer type;
    @Getter
    private String desc;

    MessageSend2TypeEnum(Integer type,String desc){
        this.type = type;
        this.desc = desc;
    }
}
