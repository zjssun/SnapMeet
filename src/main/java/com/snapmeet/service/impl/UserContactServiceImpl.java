package com.snapmeet.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.snapmeet.entity.po.UserContact;
import com.snapmeet.entity.po.UserContactApply;
import com.snapmeet.entity.po.UserInfo;
import com.snapmeet.entity.vo.UserInfoVO4Search;
import com.snapmeet.enums.ResponseCodeEnum;
import com.snapmeet.enums.UserContactApplyStatusEnum;
import com.snapmeet.enums.UserContactStatusEnum;
import com.snapmeet.exception.BusinessException;
import com.snapmeet.mapper.UserContactMapper;
import com.snapmeet.service.IUserContactService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author sam
 * @since 2026-01-07
 */
@Service
public class UserContactServiceImpl extends ServiceImpl<UserContactMapper, UserContact> implements IUserContactService {

    @Resource
    private UserInfoServiceImpl userInfoService;

    @Resource
    private UserContactApplyServiceImpl userContactApplyService;

    @Override
    public UserInfoVO4Search searchContact(String myUserId, String userId) {

        UserInfo userInfo = userInfoService.getOne(new LambdaQueryWrapper<UserInfo>().eq(UserInfo::getUserId,userId));
        if(userInfo==null){
            return null;
        }
        UserInfoVO4Search result = new UserInfoVO4Search();
        result.setUserId(userInfo.getUserId());
        result.setNickName(userInfo.getNickName());
        if(myUserId.equals(userId)){
            result.setStatus(-UserContactApplyStatusEnum.PASS.getStatus());
        }
        UserContactApply contactApply = userContactApplyService.getOne(new LambdaQueryWrapper<UserContactApply>()
                .eq(UserContactApply::getApplyUserId,myUserId).eq(UserContactApply::getReceiveUserId,userId));

        UserContact userContact = this.getOne(new LambdaQueryWrapper<UserContact>().eq(UserContact::getUserId,userId)
                .eq(UserContact::getContactId,myUserId));
        if(contactApply!=null&&UserContactApplyStatusEnum.BLACKLIST.getStatus().equals(contactApply.getStatus()) ||
        userContact != null && UserContactApplyStatusEnum.BLACKLIST.getStatus().equals(userContact.getStatus())){
            result.setStatus(UserContactApplyStatusEnum.BLACKLIST.getStatus());
            return result;
        }
        if(contactApply!=null&&UserContactApplyStatusEnum.INIT.getStatus().equals(contactApply.getStatus())){
            result.setStatus(UserContactApplyStatusEnum.INIT.getStatus());
            return result;
        }
        UserContact myUserContact = this.getOne(new LambdaQueryWrapper<UserContact>().eq(UserContact::getUserId,myUserId).eq(UserContact::getContactId,userId));
        if(userContact!=null && UserContactStatusEnum.FRIEND.getStatus().equals(userContact.getStatus())&&
        myUserContact!=null&& UserContactStatusEnum.FRIEND.getStatus().equals(myUserContact.getStatus())){
            result.setStatus(UserContactStatusEnum.FRIEND.getStatus());
            return  result;
        }

        return result;
    }

    @Override
    public void delContact(String userId,String contactId, Integer status) {
        if(!ArrayUtils.contains(new Integer[]{UserContactStatusEnum.DEL.getStatus(),UserContactStatusEnum.BLACKLIST.getStatus()},status)){
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        UserContact userContact = new UserContact();
        userContact.setLastUpdateTime(LocalDateTime.now());
        userContact.setStatus(status);
        this.update(userContact,new LambdaQueryWrapper<UserContact>().eq(UserContact::getUserId,userId).eq(UserContact::getContactId,contactId));
    }


}
