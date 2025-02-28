package com.gxz.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gxz.dto.Result;
import com.gxz.dto.UserDTO;
import com.gxz.entity.User;
import com.gxz.mapper.UserMapper;
import com.gxz.service.IUserService;
import com.gxz.utils.RegexUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public Result sendCode(String phone, HttpSession session) {
        // 校验手机号
        if (RegexUtils.isPhoneInvalid(phone)) {
            return Result.fail("手机号格式错误！");
        }
        // 生成验证码
        String code = RandomUtil.randomNumbers(6);
        // 保存验证码到 session
//        session.setAttribute("code",code);
        redisTemplate.opsForValue().set("login:phone:"+phone, code, 2, TimeUnit.MINUTES);//设置两分钟有效期的验证码
        // 5.发送验证码
        log.debug(String.format("发送短信验证码成功，验证码：%s", code));
        // 返回ok
        return Result.ok();
    }

    @Override
    public Result login(String phone,String code,HttpSession session) {
        //从redis中校验验证码
       if(code!=null && !code.equals(redisTemplate.opsForValue().get("login:phone:" + phone))){
           return Result.fail("验证码错误");
       }
        User user = userMapper.selectByPhone(phone);
        if(user == null){
            //新建用户
            user = new User();
            user.setNickName("user_"+ RandomUtil.randomString(6));
            user.setPhone(phone);
            user.setCreateTime(LocalDateTime.now());
            userMapper.insert(user);
        }
//        session.setAttribute("user", BeanUtil.copyProperties(user, UserDTO.class));
        String token = UUID.randomUUID().toString(true);
        UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);//user转userDto，去掉敏感信息
        //userDto转map，便于redis存储(因此将userDto中的字段都转为string)
        Map<String, Object> userMap = BeanUtil.beanToMap(userDTO,new HashMap<>(),
                CopyOptions.create().setIgnoreNullValue(true).setFieldValueEditor((key,value) -> value.toString()));
        //注意StringRedisTemplate要求所有的key和vaule都是String，它对key和value都进行string序列化
        redisTemplate.opsForHash().putAll("login:token:"+token,userMap);
        redisTemplate.expire("login:token:"+token, 30, TimeUnit.MINUTES); //设置token有效期30min
        System.out.println("userDTO = " + userDTO);
        return Result.ok(token);
    }
}
