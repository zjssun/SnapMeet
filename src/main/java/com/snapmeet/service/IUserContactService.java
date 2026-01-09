package com.snapmeet.service;

import com.snapmeet.entity.po.UserContact;
import com.baomidou.mybatisplus.extension.service.IService;
import com.snapmeet.entity.vo.UserInfoVO4Search;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author sam
 * @since 2026-01-07
 */
public interface IUserContactService extends IService<UserContact> {
    UserInfoVO4Search searchContact(String myUserId,String userId);
}
