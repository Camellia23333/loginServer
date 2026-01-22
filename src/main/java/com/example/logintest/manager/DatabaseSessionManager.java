package com.example.logintest.manager;

import com.example.logintest.dao.UserSessionMapper;
import com.example.logintest.dao.UserMapper;
import com.example.logintest.entity.UserSession;
import com.example.logintest.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Component
public class DatabaseSessionManager {

    @Autowired
    private UserSessionMapper userSessionMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserMapper userMapper;

    /**
     * 用户登录 - 处理唯一登录(使用事务保证原子性)
     */
    @Transactional(rollbackFor = Exception.class)
    public String handleUserLogin(Long userId, String phone, String deviceInfo) {
        System.out.println("========== 处理用户登录 ==========");
        System.out.println("用户ID: " + userId);
        System.out.println("设备信息: " + deviceInfo);

        // 1. 移除用户之前的活动会话
        int removedCount = userSessionMapper.removeByUserId(userId);
        System.out.println("移除旧会话数量: " + removedCount);

        // 2. 生成新token
        String newToken = jwtUtil.generateToken(userId, phone);
        System.out.println("生成新Token: " + newToken.substring(0, 20) + "...");

        // 3. 创建新的会话记录
        Date expiresAt = new Date(System.currentTimeMillis() + jwtUtil.getExpireTime());
        UserSession session = new UserSession(userId, newToken, expiresAt);
        session.setDeviceInfo(deviceInfo);

        int insertResult = userSessionMapper.insert(session);
        System.out.println("插入会话记录结果: " + insertResult);

        // 4. 同步更新user表的token字段
        int updateResult = userMapper.updateUserToken(userId, newToken);
        System.out.println("更新user表token结果: " + updateResult);

        System.out.println("========== 登录处理完成 ==========");
        return newToken;
    }

    /**
     * 验证Token是否有效且为当前登录的唯一token
     */
    public boolean isValidAndCurrentToken(Long userId, String token) {
        System.out.println("--- 验证Token ---");
        System.out.println("用户ID: " + userId);
        System.out.println("Token: " + token.substring(0, 20) + "...");

        // 1. 验证JWT token本身是否有效
        if (!jwtUtil.validateToken(token)) {
            System.out.println("  JWT验证失败");
            return false;
        }

        if (jwtUtil.isTokenExpired(token)) {
            System.out.println("  JWT已过期");
            return false;
        }

        // 2. 验证数据库中的token是否与传入token一致
        UserSession currentSession = userSessionMapper.findByToken(token);
        if (currentSession == null) {
            System.out.println("  Token在数据库中不存在");
            return false;
        }

        // 3. 检查用户ID是否匹配
        if (!currentSession.getUserId().equals(userId)) {
            System.out.println("   用户ID不匹配");
            System.out.println("   Session中的用户ID: " + currentSession.getUserId());
            System.out.println("   传入的用户ID: " + userId);
            return false;
        }

        // 4. 检查会话状态
        if (currentSession.getStatus() != 1) {
            System.out.println("会话状态无效: " + currentSession.getStatus());
            return false;
        }

        // 5. 检查是否过期(双重保险)
        if (currentSession.getExpiresAt().before(new java.util.Date())) {
            System.out.println("   会话已过期");
            System.out.println("   过期时间: " + currentSession.getExpiresAt());
            System.out.println("   当前时间: " + new java.util.Date());
            return false;
        }

        System.out.println("✓ Token验证通过");
        return true;
    }

    /**
     * 用户登出
     */
    @Transactional(rollbackFor = Exception.class)
    public void logout(Long userId, String token) {
        System.out.println("========== 用户登出 ==========");
        System.out.println("用户ID: " + userId);

        userSessionMapper.removeByToken(token);
        // 登出时清空user表的token
        userMapper.updateUserToken(userId, null);

        System.out.println("========== 登出完成 ==========");
    }

    /**
     * 刷新Token过期时间
     */
    public void refreshToken(Long userId, String token) {
        // 更新最后活跃时间(不打印日志,避免日志过多)
        userSessionMapper.updateLastActiveTime(token);
    }
}