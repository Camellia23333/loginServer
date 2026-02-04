package com.example.logintest.controller;

import com.example.logintest.dao.UserMapper;
import com.example.logintest.entity.Result;
import com.example.logintest.service.UserService;
import com.example.logintest.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserMapper userMapper;

    /**
     * 主动校验Token有效性
     * 注意：此接口仅用于前端主动校验，不应触发互踢逻辑
     */
    @GetMapping("/validate-token")
    public Result<String> validateToken(@RequestHeader("Authorization") String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return Result.error(401, "缺少认证Token");
        }

        String token = authorization.substring(7);

        try {
            //校验JWT有效性,纯算法校验，不查库,如果 JWT 本身过期了，直接返回 401，不需要查数据库了
            if (!jwtUtil.validateToken(token) || jwtUtil.isTokenExpired(token)) {
                return Result.error(401, "Token已过期");
            }

            //从JWT中提取用户ID
            Long userId = jwtUtil.getUserIdFromToken(token);

            //查 user 表,查询数据库中该用户当前的 Token
            String dbToken = userMapper.findTokenByUserId(userId);

            //各种状态判断
            if (dbToken == null) {
                // Token在数据库中不存在，可能已被删除
                return Result.error(401, "Token无效");
            }

            if (!dbToken.equals(token)) {

                return Result.error(409, "已在其他设备登录");
            }

            // 4. 刷新最后活跃时间
            userService.refreshToken(userId, token);

            return Result.success("Token有效");

        } catch (Exception e) {
            System.err.println("Token校验异常: " + e.getMessage());
            return Result.error(401, "Token解析失败");
        }
    }
}
