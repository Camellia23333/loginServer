package com.example.logintest.interceptor;

import com.example.logintest.entity.Result;
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
            sendErrorResponse(response, Result.error("缺少认证Token"));
            return false;
        }

        token = token.substring(7); // 移除"Bearer "前缀

        try {
            // 从Token中提取用户ID
            Long userId = jwtUtil.getUserIdFromToken(token);

            // 验证Token是否有效且为当前登录的唯一token
            if (userService.validateToken(userId, token)) {
                // 刷新Token过期时间
                userService.refreshToken(userId, token);
                return true;
            } else {
                sendErrorResponse(response, Result.error("Token无效或已被其他设备替换"));
                return false;
            }
        } catch (Exception e) {
            sendErrorResponse(response, Result.error("Token解析失败: " + e.getMessage()));
            return false;
        }
    }

    private void sendErrorResponse(HttpServletResponse response, Result<?> result) throws Exception {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        try {
            out.write(new ObjectMapper().writeValueAsString(result));
            out.flush();
        } finally {
            out.close(); // 确保资源释放
        }
    }
}
