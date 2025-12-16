package com.snapmeet.controller;


import com.snapmeet.entity.vo.CheckCodeVO;
import com.snapmeet.entity.vo.ResponseVO;
import com.snapmeet.mapper.UserInfoMapper;
import com.wf.captcha.ArithmeticCaptcha;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;

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
        ArithmeticCaptcha captcha = new ArithmeticCaptcha(100,42);
        String code = captcha.text();
        String checkCodeKey = "";
        // base64图片
        String checkCodeBase64 = captcha.toBase64();

        CheckCodeVO checkCodeVO = new CheckCodeVO();
        checkCodeVO.setCheckCode(checkCodeBase64).setCheckCodeKey(checkCodeKey);

        return getSuccessResponseVO(checkCodeVO);
    }
}
