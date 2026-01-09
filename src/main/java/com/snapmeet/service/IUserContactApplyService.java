package com.snapmeet.service;

import com.snapmeet.entity.po.UserContactApply;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author sam
 * @since 2026-01-07
 */
public interface IUserContactApplyService extends IService<UserContactApply> {

    Integer saveUserContactApply(UserContactApply userContactApply);

    void dealWithApply(String applyUserId,String userId,String nickName,Integer status);
}
