package com.example.logintest.interceptor;

import com.example.logintest.dao.UserSessionMapper;
import com.example.logintest.entity.Result;
import com.example.logintest.entity.UserSession;
import com.example.logintest.service.UserService;
import com.example.logintest.utils.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserSessionMapper userSessionMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 定义不需要验证的路径
        String requestURI = request.getRequestURI();
        if ("/api/login".equals(requestURI) || "/api/test".equals(requestURI)) {
            return true; // 不需要验证
        }

        // 获取Token
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            sendErrorResponse(response, Result.error("缺少认证Token"), 401);
            return false;
        }

        token = token.substring(7);

        try {
            // 从Token中提取用户ID
            Long userId = jwtUtil.getUserIdFromToken(token);

            // 验证Token是否有效且为当前登录的唯一token
            if (userService.validateToken(userId, token)) {
                // 刷新Token过期时间
                userService.refreshToken(userId, token);
                return true;
            } else {
                // 检查是哪种情况导致验证失败
                UserSession session = userSessionMapper.findByToken(token);
                if (session != null) {
                    if (session.getStatus() == 0) {
                        // Token存在但已被其他设备登录替换
                        sendErrorResponse(response, Result.error("已在其他设备登录"), 409);
                    } else if (session.getExpiresAt().before(new java.util.Date())) {
                        // Token已过期
                        sendErrorResponse(response, Result.error("Token已过期"), 401);
                    } else {
                        // 其他验证失败情况
                        sendErrorResponse(response, Result.error("Token无效"), 401);
                    }
                } else {
                    // Token不存在
                    sendErrorResponse(response, Result.error("Token不存在"), 401);
                }
                return false;
            }
        } catch (Exception e) {
            sendErrorResponse(response, Result.error("Token解析失败: " + e.getMessage()), 401);
            return false;
        }
    }

    private void sendErrorResponse(HttpServletResponse response, Result<?> result, int statusCode) throws Exception {
        response.setStatus(statusCode);
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        try {
            out.write(new ObjectMapper().writeValueAsString(result));
            out.flush();
        } finally {
            out.close();
        }
    }
}
