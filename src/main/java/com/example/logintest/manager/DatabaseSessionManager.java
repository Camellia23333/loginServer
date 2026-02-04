package com.example.logintest.manager;

import com.example.logintest.dao.UserMapper;
import com.example.logintest.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;


@Component
public class DatabaseSessionManager {


    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserMapper userMapper;

    /**
     * 用户登录 - 处理唯一登录(使用事务保证原子性)
     * 直接更新 User 表的 token 字段，实现互踢
     */
    @Transactional(rollbackFor = Exception.class)
    public String handleUserLogin(Long userId, String phone, String deviceInfo) {
        System.out.println("========== 处理用户登录 ==========");
        System.out.println("用户ID: " + userId);
        System.out.println("设备信息: " + deviceInfo);

        //生成新 Token
        String newToken = jwtUtil.generateToken(userId, phone);

        // 将新 Token 更新到 User 表
        // 只要更新成功，数据库里旧的 Token 就被覆盖了，之前的设备再次请求时会发现 Token 不一致
        userMapper.updateUserToken(userId, newToken);

        System.out.println("新Token已写入User表，旧Token已失效");
        return newToken;

    }

    /**
     * 验证Token是否有效且为当前登录的唯一token
     */
    public boolean isValidAndCurrentToken(Long userId, String token) {
        System.out.println("--- 验证Token ---");
        System.out.println("用户ID: " + userId);
        System.out.println("Token: " + token.substring(0, 20) + "...");

        //验证JWT token本身是否有效
        if (!jwtUtil.validateToken(token)) {
            System.out.println("  JWT验证失败");
            return false;
        }

        if (jwtUtil.isTokenExpired(token)) {
            System.out.println("  JWT已过期");
            return false;
        }

        //验证数据库中的token是否与传入token一致
        String dbToken = userMapper.findTokenByUserId(userId);
        if (dbToken == null) {
            System.out.println("  Token在数据库中不存在");
            return false;
        }

        if (!dbToken.equals(token)) {
            System.out.println("Token不匹配！当前用户可能已在其他地方登录。");
            System.out.println("传入Token: " + token.substring(0, 10) + "...");
            System.out.println("DB中Token: " + dbToken.substring(0, 10) + "...");
            return false;
        }

        System.out.println("Token验证通过");
        return true;
    }

    /**
     * 用户登出
     */
    @Transactional(rollbackFor = Exception.class)
    public void logout(Long userId, String token) {
        System.out.println("========== 用户登出 ==========");
        System.out.println("用户ID: " + userId);

        // 登出时清空user表的token
        userMapper.updateUserToken(userId, null);

        System.out.println("========== 登出完成 ==========");
    }

    /**
     * 刷新Token，更新最后活跃时间
     */
    public void refreshToken(Long userId, String token) {
        // 更新最后活跃时间
        userMapper.updateUserToken(userId, token);
    }
}
