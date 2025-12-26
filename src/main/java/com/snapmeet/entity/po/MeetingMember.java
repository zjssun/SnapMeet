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
@TableName("meeting_member")
public class MeetingMember implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "meeting_id", type = IdType.NONE)
    private String meetingId;

    @TableField("user_id")
    private String userId;

    @TableField("nick_name")
    private String nickName;

    @TableField("last_join_time")
    private LocalDateTime lastJoinTime;

    @TableField("status")
    private Integer status;

    @TableField("member_type")
    private Integer memberType;

    @TableField("meeting_status")
    private Integer meetingStatus;


}
