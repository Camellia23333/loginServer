package com.example.logintest.service;

import com.example.logintest.dao.UserMapper;
import com.example.logintest.entity.Result;
import com.example.logintest.entity.User;
import com.example.logintest.manager.DatabaseSessionManager;
import com.example.logintest.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * 用户服务层
 */
@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private DatabaseSessionManager sessionManager; //替换原来的UserSessionManager

    @Autowired
    private JwtUtil jwtUtil;

    // 注入 Redis
    @Autowired
    private StringRedisTemplate redisTemplate;

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
    public String generateUniqueToken(User user, String deviceInfo) {
        return sessionManager.handleUserLogin((long) user.getId(), user.getPhone(), deviceInfo);
    }

    /**
     * 验证Token是否有效
     */
    public boolean validateToken(Long userId, String token) {
        return sessionManager.isValidAndCurrentToken(userId, token);
    }

    /**
     * 刷新Token
     */
    public void refreshToken(Long userId, String token) {
        sessionManager.refreshToken(userId, token);
    }

    /**
     * 用户登出
     */
    public void logout(Long userId, String token) {
        sessionManager.logout(userId, token);
    }

    /**
     * 从Token中获取用户ID（代理JwtUtil的方法）
     */
    public Long getUserIdFromToken(String token) {
        return jwtUtil.getUserIdFromToken(token);
    }

    /**
     * 用户注册（带验证码校验）
     */
    public Result<String> register(String phone, String password, String code) {
        //校验验证码
        String codeKey = "sms:code:" + phone;
        String cacheCode = redisTemplate.opsForValue().get(codeKey);

        if (cacheCode == null) {
            return Result.error("验证码已过期或未获取");
        }
        if (!cacheCode.equals(code)) {
            return Result.error("验证码错误");
        }

        //校验手机号是否已存在，使用countByPhone
        int count = userMapper.countByPhone(phone);
        if (count > 0) {
            // 查到了，说明有人用过这个手机号
            return Result.error("该手机号已注册");
        }

        //构造新用户并保存
        User newUser = new User();
        newUser.setPhone(phone);
        newUser.setPassword(password); //后续要进行加密，这里用明文
        newUser.setUsername("用户" + phone.substring(phone.length() - 4));
        newUser.setCreateTime(new java.util.Date());

        //调用 Mapper 去执行 SQL,Insert 语句
        int rows = userMapper.insert(newUser);

        if (rows <= 0) {
            return Result.error("注册失败，请稍后重试");
        }
        // 模拟保存成功
        System.out.println("用户注册成功: " + phone);

        //注册成功后，删除 Redis 里的验证码，防止被重复使用
        redisTemplate.delete(codeKey);

        return Result.success("注册成功");
    }
}
