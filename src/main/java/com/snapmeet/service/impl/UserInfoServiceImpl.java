package com.snapmeet.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.snapmeet.constants.Constants;
import com.snapmeet.entity.po.UserInfo;
import com.snapmeet.enums.UserStatusEnum;
import com.snapmeet.exception.BusinessException;
import com.snapmeet.mapper.UserInfoMapper;
import com.snapmeet.service.IUserInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.snapmeet.utils.StringTools;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;

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

        LocalDateTime curDate = LocalDateTime.now();
        String userId = StringTools.getRandomNumber(Constants.LENGTH_12);
        //准备数据实体
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(userId);
        userInfo.setEmail(email);
        userInfo.setNickName(nickName);
        userInfo.setPassword(StringTools.encodeByMD5(password));
        userInfo.setCreateTime(curDate);
        userInfo.setLastOffTime(curDate.toInstant(ZoneOffset.of("+8")).toEpochMilli());
        userInfo.setStatus(UserStatusEnum.ENABLE.getStatus());
        this.save(userInfo);
    }
}
