package com.snapmeet.aspect;

import com.snapmeet.annotation.GlobalInterceptor;
import com.snapmeet.entity.dto.TokenUserInfoDto;
import com.snapmeet.enums.ResponseCodeEnum;
import com.snapmeet.exception.BusinessException;
import com.snapmeet.redis.RedisComponent;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;

@Component
@Aspect
@Slf4j
public class GlobalOperationAspect {
    @Resource
    RedisComponent redisComponent;

    @Before("@annotation(com.snapmeet.annotation.GlobalInterceptor)")
    public void interceporDo(JoinPoint point){
        try {
            Method method = ((MethodSignature)point.getSignature()).getMethod();
            GlobalInterceptor interceptor = method.getAnnotation(GlobalInterceptor.class);
            if(interceptor==null){
                return;
            }
            if(interceptor.checkLogin() || interceptor.checkAdmin()){
                checkLogin(interceptor.checkAdmin());
            }
        }catch (BusinessException e){
            log.error("全局拦截器异常",e);
            throw e;
        }catch (Exception e){
            log.error("全局拦截器异常",e);
            throw new BusinessException(ResponseCodeEnum.CODE_500);
        }
    }

    private void checkLogin(Boolean checkAdmin){
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String token = request.getHeader("token");
        TokenUserInfoDto tokenUserInfoDto = redisComponent.getTokenUserInfoDto(token);
        if(tokenUserInfoDto==null){
            throw new BusinessException(ResponseCodeEnum.CODE_901);
        }
        if(checkAdmin && !tokenUserInfoDto.getAdmin()){
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
    }
}
