package com.gxz.mapper;

import com.gxz.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import javax.servlet.http.HttpSession;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
public interface UserMapper extends BaseMapper<User> {

    User selectByPhone(String phone);
}
