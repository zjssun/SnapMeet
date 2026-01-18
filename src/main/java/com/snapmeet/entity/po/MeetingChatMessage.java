package com.snapmeet.entity.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 
 * </p>
 *
 * @author sam
 * @since 2026-01-16
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("meeting_chat_message")
public class MeetingChatMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "message_id", type = IdType.NONE)
    private Long messageId;

    @TableField("meeting_id")
    private String meetingId;

    @TableField("message_type")
    private Integer messageType;

    @TableField("message_content")
    private String messageContent;

    @TableField("send_user_id")
    private String sendUserId;

    @TableField("send_user_nick_name")
    private String sendUserNickName;

    @TableField("send_time")
    private Long sendTime;

    @TableField("receive_type")
    private Integer receiveType;

    @TableField("receive_user_id")
    private String receiveUserId;

    @TableField("file_size")
    private Long fileSize;

    @TableField("file_name")
    private String fileName;

    @TableField("file_type")
    private Integer fileType;

    @TableField("file_suffx")
    private String fileSuffx;

    @TableField("status")
    private Integer status;


}
