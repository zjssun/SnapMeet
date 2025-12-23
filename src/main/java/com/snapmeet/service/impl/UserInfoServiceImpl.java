package com.snapmeet.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.snapmeet.config.AppConfig;
import com.snapmeet.constants.Constants;
import com.snapmeet.entity.dto.TokenUserInfoDto;
import com.snapmeet.entity.po.UserInfo;
import com.snapmeet.entity.vo.UserInfoVO;
import com.snapmeet.enums.UserStatusEnum;
import com.snapmeet.exception.BusinessException;
import com.snapmeet.mapper.UserInfoMapper;
import com.snapmeet.redis.RedisComponent;
import com.snapmeet.service.IUserInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.snapmeet.utils.StringTools;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

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

    @Resource
    private AppConfig appConfig;
    @Resource
    private RedisComponent redisComponent;

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
        userInfo.setSex(2);
        userInfo.setUserId(userId);
        userInfo.setEmail(email);
        userInfo.setNickName(nickName);
        userInfo.setPassword(StringTools.encodeByMD5(password));
        userInfo.setCreateTime(curDate);
        userInfo.setLastOffTime(curDate.toInstant(ZoneOffset.of("+8")).toEpochMilli());
        userInfo.setStatus(UserStatusEnum.ENABLE.getStatus());
        userInfo.setMeeting(StringTools.getMeetingNoOrMeetingId());
        this.save(userInfo);
    }

    @Override
    public UserInfoVO login(String email, String password) {
        // 检查邮箱是否已存在
        UserInfo existUser = this.getOne(new LambdaQueryWrapper<UserInfo>().eq(UserInfo::getEmail, email));
        if(existUser == null || !existUser.getPassword().equals(password)){
            throw new BusinessException("邮箱或密码不正确！");
        }
        // 检查账号状态
        if(UserStatusEnum.DISABLE.getStatus().equals(existUser.getStatus())){
            throw new BusinessException("账号已被禁用！");
        }
        // 检查账号是否已登录
        if(existUser.getLastLoginTime() != null && existUser.getLastOffTime() <= existUser.getLastLoginTime()){
            throw new BusinessException("此账号已在别处登录！");
        }
        // TokenUserInfoDto是保存到Redis的
        TokenUserInfoDto tokenUserInfoDto = StringTools.copy(existUser,TokenUserInfoDto.class);
        String token = StringTools.encodeByMD5(tokenUserInfoDto.getUserId()+StringTools.getRandomString(Constants.LENGTH_20));
        tokenUserInfoDto.setToken(token);
        tokenUserInfoDto.setMyMeetingNo(existUser.getMeeting());
        tokenUserInfoDto.setAdmin(appConfig.getAdminEmails().contains(email));
        redisComponent.saveTokenUserInfoDto(tokenUserInfoDto);

        // 返回给客户端的VO
        UserInfoVO userInfoVO = StringTools.copy(existUser,UserInfoVO.class);
        userInfoVO.setToken(token);
        userInfoVO.setAdmin(tokenUserInfoDto.getAdmin());
        return userInfoVO;
    }
}
