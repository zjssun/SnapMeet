package com.snapmeet.service.impl;

import com.snapmeet.entity.po.UserInfo;
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

}
