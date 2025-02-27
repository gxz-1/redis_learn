package com.gxz.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gxz.dto.Result;
import com.gxz.dto.UserDTO;
import com.gxz.entity.User;
import com.gxz.mapper.UserMapper;
import com.gxz.service.IUserService;
import com.gxz.utils.RegexUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.Date;

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

    @Override
    public Result sendCode(String phone, HttpSession session) {
        // 校验手机号
        if (RegexUtils.isPhoneInvalid(phone)) {
            return Result.fail("手机号格式错误！");
        }
        // 生成验证码
        String code = RandomUtil.randomNumbers(6);
        // 保存验证码到 session
        session.setAttribute("code",code);
        // 5.发送验证码
        log.debug(String.format("发送短信验证码成功，验证码：%s", code));
        // 返回ok
        return Result.ok();
    }

    @Override
    public Result login(String phone,HttpSession session) {
        User user = userMapper.selectByPhone(phone);
        if(user == null){
            //新建用户
            user = new User();
            user.setNickName("user_"+ RandomUtil.randomString(6));
            user.setPhone(phone);
            user.setCreateTime(LocalDateTime.now());
            userMapper.insert(user);
        }
        session.setAttribute("user", BeanUtil.copyProperties(user, UserDTO.class));
        System.out.println("user = " + user);
        return Result.ok();
    }
}
