package com.snapmeet.service;

import com.snapmeet.entity.po.UserInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.snapmeet.entity.vo.UserInfoVO;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author sam
 * @since 2025-12-07
 */
public interface IUserInfoService extends IService<UserInfo>{
    void register(String email,String nickName,String password);

    UserInfoVO login(@NotEmpty @Email String email, @NotEmpty @Size(max=20) String password);
}
