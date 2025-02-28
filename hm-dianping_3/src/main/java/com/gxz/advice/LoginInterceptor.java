package com.gxz.advice;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import com.gxz.dto.UserDTO;
import com.gxz.entity.User;
import com.gxz.utils.UserHolder;
import net.sf.jsqlparser.util.validation.metadata.NamedObject;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static net.sf.jsqlparser.util.validation.metadata.NamedObject.user;

public class LoginInterceptor implements HandlerInterceptor {

    //拦截器类是new出来的，因此无法通过@Autowired注入redisTemplate
    private StringRedisTemplate redisTemplate;

    public LoginInterceptor(StringRedisTemplate redisTemplate){
        this.redisTemplate=redisTemplate;
    }

    //前置拦截器
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //获取session中的用户
//        HttpSession session = request.getSession();
//        Object user = session.getAttribute("user");
        //获取request中的token，根据token在redis获取user
        String token = request.getHeader("authorization");
        //判断用户是否存在
        if(StrUtil.isBlank(token)){
            response.setStatus(401);
            return false;
        }
        Map<Object, Object> userMap = redisTemplate.opsForHash().entries("login:token:" + token);
        if(userMap.isEmpty()){
            response.setStatus(401);
            return false;
        }
        UserDTO userDTO = BeanUtil.fillBeanWithMap(userMap, new UserDTO(), false);//Map转userDto
        //存在，保存用户信息到Threadlocal(线程私有的存储区域)
        UserHolder.saveUser(userDTO);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserHolder.removeUser();
    }
}
