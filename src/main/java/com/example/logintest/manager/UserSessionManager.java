package com.example.logintest.manager;

import com.example.logintest.utils.JwtUtil;
import com.example.logintest.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserSessionManager {

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private JwtUtil jwtUtil;

    // Redis键的前缀
    private static final String LOGIN_TOKEN_PREFIX = "login_token:";
    private static final String USER_TOKEN_PREFIX = "user_token:";

    /**
     * 用户登录 - 处理唯一登录
     */
    public String handleUserLogin(Long userId, String phone, String oldToken) {
        // 构建Redis键
        String userTokenKey = LOGIN_TOKEN_PREFIX + userId;
        
        // 如果用户之前有登录，移除旧的token信息
        String previousToken = (String) redisUtil.get(userTokenKey);
        if (previousToken != null && !previousToken.equals(oldToken)) {
            // 移除旧token的用户信息
            redisUtil.delete(USER_TOKEN_PREFIX + previousToken);
        }

        // 生成新token
        String newToken = jwtUtil.generateToken(userId, phone);

        // 保存用户ID对应的新token
        redisUtil.set(userTokenKey, newToken, jwtUtil.getExpireTime());

        // 保存token对应的用户信息
        redisUtil.set(USER_TOKEN_PREFIX + newToken, 
                     String.valueOf(userId), 
                     jwtUtil.getExpireTime());

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

        // 验证Redis中的token是否与传入token一致
        String currentToken = (String) redisUtil.get(LOGIN_TOKEN_PREFIX + userId);
        return currentToken != null && currentToken.equals(token);
    }

    /**
     * 用户登出
     */
    public void logout(Long userId, String token) {
        String userTokenKey = LOGIN_TOKEN_PREFIX + userId;
        String tokenUserKey = USER_TOKEN_PREFIX + token;

        // 删除用户token关联
        redisUtil.delete(userTokenKey);
        // 删除token用户关联
        redisUtil.delete(tokenUserKey);
    }

    /**
     * 刷新Token过期时间
     */
    public void refreshToken(Long userId, String token) {
        String userTokenKey = LOGIN_TOKEN_PREFIX + userId;
        String tokenUserKey = USER_TOKEN_PREFIX + token;

        // 刷新两个键的过期时间
        if (redisUtil.hasKey(userTokenKey)) {
            redisUtil.set(userTokenKey, token, jwtUtil.getExpireTime());
        }
        if (redisUtil.hasKey(tokenUserKey)) {
            redisUtil.set(tokenUserKey, String.valueOf(userId), jwtUtil.getExpireTime());
        }
    }
}
