package com.gxz.advice;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.TimeUnit;

public class RefreshTokenInterceptor implements HandlerInterceptor {
    //拦截器类是new出来的，因此无法通过@Autowired注入redisTemplate
    private StringRedisTemplate redisTemplate;

    public RefreshTokenInterceptor(StringRedisTemplate redisTemplate){
        this.redisTemplate=redisTemplate;
    }
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //在拦截器1中已经做了token校验，在这个拦截器中只做有效期的刷新
        String token = request.getHeader("authorization");
        //刷新token有效期
        redisTemplate.expire("login:token:"+token, 30, TimeUnit.MINUTES);
        return true;
    }
}
