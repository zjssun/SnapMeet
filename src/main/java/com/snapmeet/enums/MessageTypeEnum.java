package com.snapmeet.enums;

import lombok.Getter;

public enum MessageTypeEnum {
    INIT(0,"连接ws获取信息"),
    ADD_MEETING_ROOM(1,"加入房间"),
    PEER(2,"发送peer"),
    EXIT_MEETING_ROOM(3,"退出房间"),
    FINISH_MEETING(4,"结束会议"),
    CHAT_TEXT_MESSAGE(5,"文本消息"),
    CHAT_MEDIA_MESSAGE(6,"媒体消息"),
    CHAT_MEDIA_MESSAGE_UPDATE(7,"媒体消息更新"),
    USER_CONTACT_APPLY(8,"好友申请消息"),
    INVITE_MEMBER_MEETING(9,"邀请入会"),
    USER_CONTACT_DEAL_WITH(12,"处理好友申请");
    @Getter
    private Integer type;
    @Getter
    private String desc;

    MessageTypeEnum(Integer type,String desc){
        this.type = type;
        this.desc = desc;
    }

    public static MessageTypeEnum getByType(Integer type){
        for (MessageTypeEnum item : MessageTypeEnum.values()){
            if(item.getType().equals(type)){
                return  item;
            }
        }
        return null;
    }
}
