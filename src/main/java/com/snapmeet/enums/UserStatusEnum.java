package com.snapmeet.enums;

import lombok.Getter;

public enum UserStatusEnum {
    DISABLE(0,"禁用"),
    ENABLE(1,"启用");

    @Getter
    private Integer status;
    @Getter
    private String desc;

    UserStatusEnum(Integer status, String desc){
        this.status = status;
        this.desc = desc;
    }

    public static UserStatusEnum getByStatus(Integer status){
        for(UserStatusEnum item:UserStatusEnum.values()){
            if(item.getStatus().equals(status)){
                return item;
            }

        }
        return  null;
    }
}
