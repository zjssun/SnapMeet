package com.snapmeet.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.snapmeet.annotation.GlobalInterceptor;
import com.snapmeet.entity.dto.TokenUserInfoDto;
import com.snapmeet.entity.po.UserContact;
import com.snapmeet.entity.po.UserContactApply;
import com.snapmeet.entity.vo.ResponseVO;
import com.snapmeet.entity.vo.UserInfoVO4Search;
import com.snapmeet.enums.UserContactStatusEnum;
import com.snapmeet.service.impl.UserContactApplyServiceImpl;
import com.snapmeet.service.impl.UserContactServiceImpl;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/meeting")
@Validated
public class UserContactController extends ABaseController{

    @Resource
    private UserContactServiceImpl userContactService;

    @Resource
    private UserContactApplyServiceImpl userContactApplyService;

    @RequestMapping("/loadContactApplyDealWhitCount")
    @GlobalInterceptor
    public ResponseVO loadContactApplyDealWhitCount(){

        return getSuccessResponseVO(null);
    }

    @RequestMapping("/searchContact")
    @GlobalInterceptor
    public ResponseVO searchContact(@NotEmpty String userId){
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        UserInfoVO4Search userInfoVO4Search = userContactService.searchContact(tokenUserInfoDto.getUserId(),userId);
        return getSuccessResponseVO(userInfoVO4Search);
    }

    @RequestMapping("/contactApply")
    @GlobalInterceptor
    public ResponseVO contactApply(@NotEmpty String receiverUserId){
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        UserContactApply userContactApply = new UserContactApply();
        userContactApply.setApplyUserId(tokenUserInfoDto.getUserId());
        userContactApply.setReceiveUserId(receiverUserId);
        Integer status = userContactApplyService.saveUserContactApply(userContactApply);
        return getSuccessResponseVO(status);
    }

    @RequestMapping("/dealWithApply")
    @GlobalInterceptor
    public ResponseVO dealWithApply(@NotEmpty String applyUserId, @NotNull Integer status){
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        userContactApplyService.dealWithApply(applyUserId,tokenUserInfoDto.getUserId(),tokenUserInfoDto.getNickName(),status);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/loadContactUser")
    @GlobalInterceptor
    public ResponseVO loadContactUser(){
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        List<UserContact> userContactList = userContactService.list(new LambdaQueryWrapper<UserContact>()
                .eq(UserContact::getUserId,tokenUserInfoDto.getUserId())
                .eq(UserContact::getStatus, UserContactStatusEnum.FRIEND.getStatus()));
        return getSuccessResponseVO(userContactList);
    }

    @RequestMapping("/loadContactApply")
    @GlobalInterceptor
    public ResponseVO loadContactApply(){
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        List<UserContactApply> ApplyList = userContactApplyService.list(new LambdaQueryWrapper<UserContactApply>()
                .eq(UserContactApply::getReceiveUserId,tokenUserInfoDto.getUserId()));
        return getSuccessResponseVO(ApplyList);
    }
}
