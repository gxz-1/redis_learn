package com.gxz.advice;

import com.gxz.dto.UserDTO;
import com.gxz.entity.User;
import com.gxz.utils.UserHolder;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class LoginInterceptor implements HandlerInterceptor {
    //前置拦截器
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //获取session中的用户
        HttpSession session = request.getSession();
        Object user = session.getAttribute("user");
        //判断用户是否存在
        if(user == null){
            response.setStatus(401);
            return false;
        }
        //存在，保存用户信息到Threadlocal(线程私有的存储区域)
        UserHolder.saveUser((UserDTO) user);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserHolder.removeUser();
    }
}
