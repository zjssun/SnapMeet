package com.snapmeet.entity.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import java.time.LocalDateTime;
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
 * @since 2025-12-25
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("meeting_info")
public class MeetingInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "meeting_id", type = IdType.NONE)
    private String meetingId;

    @TableField("meeting_no")
    private String meetingNo;

    @TableField("meeting_name")
    private String meetingName;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("create_user_id")
    private String createUserId;

    @TableField("join_type")
    private Integer joinType;

    @TableField("join_password")
    private String joinPassword;

    @TableField("start_time")
    private LocalDateTime startTime;

    @TableField("end_time")
    private LocalDateTime endTime;

    @TableField("status")
    private Integer status;

    @TableField(exist = false)
    private Integer memberCount;
}
