package com.example.logintest.manager;

import com.example.logintest.dao.UserSessionMapper;
import com.example.logintest.entity.UserSession;
import com.example.logintest.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class DatabaseSessionManager {

    @Autowired
    private UserSessionMapper userSessionMapper;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 用户登录 - 处理唯一登录
     */
    public String handleUserLogin(Long userId, String phone, String deviceInfo) {
        // 移除用户之前的活动会话
        userSessionMapper.removeByUserId(userId);

        // 生成新token
        String newToken = jwtUtil.generateToken(userId, phone);

        // 创建新的会话记录
        Date expiresAt = new Date(System.currentTimeMillis() + jwtUtil.getExpireTime());
        UserSession session = new UserSession(userId, newToken, expiresAt);
        session.setDeviceInfo(deviceInfo);

        userSessionMapper.insert(session);

        return newToken;
    }

    /**
     * 验证Token是否有效且为当前登录的唯一token
     */
    public boolean isValidAndCurrentToken(Long userId, String token) {
        // 验证JWT token本身是否有效
        if (!jwtUtil.validateToken(token) || jwtUtil.isTokenExpired(token)) {
            return false;
        }

        // 验证数据库中的token是否与传入token一致
        UserSession currentSession = userSessionMapper.findByToken(token);
        if (currentSession == null) {
            return false; // Token不存在于数据库中
        }

        // 检查用户ID是否匹配
        if (!currentSession.getUserId().equals(userId)) {
            return false;
        }

        // 检查会话状态
        if (currentSession.getStatus() != 1) {
            return false; // 会话已被标记为失效
        }

        // 检查是否过期（双重保险）
        if (currentSession.getExpiresAt().before(new java.util.Date())) {
            return false;
        }

        return true;
    }

    /**
     * 用户登出
     */
    public void logout(Long userId, String token) {
        userSessionMapper.removeByToken(token);
    }

    /**
     * 刷新Token过期时间
     */
    public void refreshToken(Long userId, String token) {
        // 更新最后活跃时间
        userSessionMapper.updateLastActiveTime(token);
    }
}
