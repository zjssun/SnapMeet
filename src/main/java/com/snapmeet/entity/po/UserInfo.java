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
 * @since 2025-12-07
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("user_info")
public class UserInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    @TableId(value = "user_id", type = IdType.NONE)
    private String userId;

    /**
     * 邮箱
     */
    @TableField("email")
    private String email;

    /**
     * 昵称
     */
    @TableField("nick_name")
    private String nickName;

    /**
     * 0:女 1:男 2：保密
     */
    @TableField("sex")
    private Integer sex;

    /**
     * 密码
     */
    @TableField("password")
    private String password;

    /**
     * 状态
     */
    @TableField("status")
    private Integer status;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 最后登录时间
     */
    @TableField("last_login_time")
    private Long lastLoginTime;

    /**
     * 最后离开时间
     */
    @TableField("last_off_time")
    private Long lastOffTime;

    /**
     * 个人会议号
     */
    @TableField("meeting")
    private String meeting;


}
