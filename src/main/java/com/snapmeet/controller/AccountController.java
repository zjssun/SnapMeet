package com.snapmeet.controller;


import com.snapmeet.entity.vo.CheckCodeVO;
import com.snapmeet.entity.vo.ResponseVO;
import com.snapmeet.entity.vo.UserInfoVO;
import com.snapmeet.exception.BusinessException;
import com.snapmeet.redis.RedisComponent;
import com.snapmeet.service.impl.UserInfoServiceImpl;
import com.wf.captcha.ArithmeticCaptcha;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
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
    UserInfoServiceImpl userInfoService;

    @Resource
    private RedisComponent redisComponent;

    @RequestMapping("/checkCode")
    public ResponseVO checkCode(){
        ArithmeticCaptcha captcha = new ArithmeticCaptcha(100,42);
        String code = captcha.text();
        // 存到redis
        String checkCodeKey = redisComponent.saveCheckCode(code);
        // 验证码base64图片
        String checkCodeBase64 = captcha.toBase64();
        // 返回redis的key和验证码图片
        CheckCodeVO checkCodeVO = new CheckCodeVO();
        checkCodeVO.setCheckCode(checkCodeBase64).setCheckCodeKey(checkCodeKey);

        return getSuccessResponseVO(checkCodeVO);
    }

    @RequestMapping("/register")
    public ResponseVO register(@NotEmpty String checkCodeKey,
                               @NotEmpty @Email String email,
                               @NotEmpty @Size(max=20)String password,
                               @NotEmpty @Size(max=20) String nickName,
                               @NotEmpty String checkCode){
        try {
            if(!checkCode.equalsIgnoreCase(redisComponent.getCheckCode(checkCodeKey))){
                throw new BusinessException("图片验证码不正确");
            }

            this.userInfoService.register(email,password,nickName);
            return getSuccessResponseVO(null);
        }finally {
            redisComponent.cleanCheckCode(checkCodeKey);
        }
    }

    @RequestMapping("/login")
    public ResponseVO login(@NotEmpty String checkCodeKey,
                            @NotEmpty @Email String email,
                            @NotEmpty @Size(max=20)String password,
                            @NotEmpty String checkCode){
        try {
            if(!checkCode.equalsIgnoreCase(redisComponent.getCheckCode(checkCodeKey))){
                throw new BusinessException("图片验证码不正确");
            }
            UserInfoVO userInfoVO = this.userInfoService.login(email,password);
            return getSuccessResponseVO(userInfoVO);
        }finally {
            redisComponent.cleanCheckCode(checkCodeKey);
        }
    }
}
