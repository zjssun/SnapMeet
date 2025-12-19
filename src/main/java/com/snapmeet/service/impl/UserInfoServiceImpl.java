package com.snapmeet.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.snapmeet.entity.po.UserInfo;
import com.snapmeet.exception.BusinessException;
import com.snapmeet.mapper.UserInfoMapper;
import com.snapmeet.service.IUserInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author sam
 * @since 2025-12-07
 */
@Service
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements IUserInfoService {

    @Override
    public void register(String email, String nickName, String password) {

        // 检查邮箱是否已存在
        UserInfo existUser = this.getOne(new LambdaQueryWrapper<UserInfo>()
                .eq(UserInfo::getEmail, email));
        if (existUser != null) {
            throw new BusinessException("该邮箱已被注册");
        }

        //准备数据实体
        UserInfo userInfo = new UserInfo();


    }
}
