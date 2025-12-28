package com.snapmeet.controller;

import com.snapmeet.annotation.GlobalInterceptor;
import com.snapmeet.entity.vo.ResponseVO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/loadMeeting")
@Validated
public class AdminController extends ABaseController{
    @RequestMapping("loadUser")
    @GlobalInterceptor(checkAdmin = true)
    public ResponseVO loadUser(Integer pageNo){
        return getSuccessResponseVO(null);
    }
}
