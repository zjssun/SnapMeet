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
 * @since 2026-01-07
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("user_contact_apply")
public class UserContactApply implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "apply_id", type = IdType.NONE)
    private Integer applyId;

    @TableField("apply_user_id")
    private String applyUserId;

    @TableField("receive_user_id")
    private String receiveUserId;

    @TableField("last_apply_time")
    private LocalDateTime lastApplyTime;

    @TableField("status")
    private Integer status;


}
