package com.snapmeet.enums;

import lombok.Getter;

public enum MeetingReserveStatusEnum {
    FINISHED(0,"未开始"),
    NO_START(1,"已结束");

    @Getter
    private Integer status;
    private String desc;

    MeetingReserveStatusEnum(Integer status,String desc){
        this.status = status;
        this.desc = desc;
    }

    public static MeetingReserveStatusEnum getByStatus(Integer status) {
        for (MeetingReserveStatusEnum item : MeetingReserveStatusEnum.values()) {
            if (item.getStatus().equals(status)) {
                return item;
            }
        }
        return null;
    }
}
