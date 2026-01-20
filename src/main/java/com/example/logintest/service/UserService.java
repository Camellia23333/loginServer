package com.example.logintest.service;

import com.example.logintest.dao.UserMapper;
import com.example.logintest.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 用户服务层
 */
@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    /**
     * 用户登录验证
     */
    public User login(String phone, String password) {
        if (phone == null || phone.trim().isEmpty()) {
            System.out.println("登录失败：手机号为空");
            return null;
        }
        if (password == null || password.trim().isEmpty()) {
            System.out.println("登录失败：密码为空");
            return null;
        }

        System.out.println("尝试登录 - 手机号: " + phone);

        User user = userMapper.findByPhoneAndPassword(phone.trim(), password.trim());

        if (user != null) {
            System.out.println("登录成功 - 用户: " + user.getUsername());
        } else {
            System.out.println("登录失败 - 手机号或密码错误");
        }

        return user;
    }
}