package com.example.snapmeet.controller;


import com.example.snapmeet.entity.vo.ResponseVO;
import com.example.snapmeet.mapper.UserInfoMapper;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author sam
 * @since 2025-12-08
 */
@RestController
@RequestMapping("/account")
@Validated
public class AccountController extends ABaseController{
    @Resource
    private UserInfoMapper userInfoMapper;

    @RequestMapping("/checkCode")
    public ResponseVO checkCode(){

        return getSuccessResponseVO(null);
    }
}
