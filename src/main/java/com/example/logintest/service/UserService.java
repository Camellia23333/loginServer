package com.example.logintest.service;

import com.example.logintest.dao.UserMapper;
import com.example.logintest.entity.User;
import com.example.logintest.manager.UserSessionManager;
import com.example.logintest.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 用户服务层
 */
@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserSessionManager userSessionManager;

    @Autowired
    private JwtUtil jwtUtil;

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

    /**
     * 生成唯一登录Token
     */
    public String generateUniqueToken(User user, String oldToken) {
        return userSessionManager.handleUserLogin((long) user.getId(), user.getPhone(), oldToken);
    }

    /**
     * 验证Token是否有效
     */
    public boolean validateToken(Long userId, String token) {
        return userSessionManager.isValidAndCurrentToken(userId, token);
    }

    /**
     * 刷新Token
     */
    public void refreshToken(Long userId, String token) {
        userSessionManager.refreshToken(userId, token);
    }

    /**
     * 用户登出
     */
    public void logout(Long userId, String token) {
        userSessionManager.logout(userId, token);
    }

    /**
     * 从Token中获取用户ID（代理JwtUtil的方法）
     */
    public Long getUserIdFromToken(String token) {
        return jwtUtil.getUserIdFromToken(token);
    }
}
