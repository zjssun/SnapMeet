package com.snapmeet.enums;

import lombok.Getter;

public enum MemberTypeEnum {
    NORMAL(0,"普通成员"),
    COMPERE(1,"主持人");

    @Getter
    private Integer type;
    @Getter
    private String desc;

    MemberTypeEnum(Integer type,String desc){
        this.type = type;
        this.desc = desc;
    }

    public static MemberTypeEnum getByType(Integer type){
        for(MemberTypeEnum item : MemberTypeEnum.values()){
            if(item.getType().equals(type)){
                return item;
            }
        }
        return null;
    }
}
