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
        String requestURI = request.getRequestURI();
        System.out.println("========== 拦截器处理请求: " + requestURI + " ==========");

        // 定义不需要验证的路径,加上新的白名单路径
        if ("/api/login".equals(requestURI) ||
                "/api/test".equals(requestURI) ||
                "/api/send-code".equals(requestURI) ||
                "/api/register".equals(requestURI)) {
            System.out.println("白名单路径,放行");
            return true;
        }

        // 获取Token
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            System.out.println("缺少认证Token");
            sendErrorResponse(response, Result.error("缺少认证Token"), 401);
            return false;
        }

        token = token.substring(7);
        System.out.println("Token前20字符: " + token.substring(0, Math.min(20, token.length())) + "...");

        try {
            // 从Token中提取用户ID
            Long userId = jwtUtil.getUserIdFromToken(token);
            System.out.println("提取到用户ID: " + userId);

            // 验证Token是否有效且为当前登录的唯一token
            boolean isValid = userService.validateToken(userId, token);
            System.out.println("Token验证结果: " + isValid);

            if (isValid) {
                // 刷新Token过期时间
                userService.refreshToken(userId, token);
                System.out.println("✓ Token验证通过,请求放行");
                return true;
            } else {
                // 检查是哪种情况导致验证失败
                UserSession session = userSessionMapper.findByToken(token);

                if (session == null) {
                    System.out.println("   Token在数据库中不存在");
                    sendErrorResponse(response, Result.error("Token不存在"), 401);
                } else if (session.getStatus() == 0) {
                    System.out.println("   Token已被其他设备登录替换");
                    System.out.println("   会话状态: " + session.getStatus());
                    System.out.println("   登录时间: " + session.getLoginTime());
                    sendErrorResponse(response, Result.error("已在其他设备登录"), 409);
                } else if (session.getExpiresAt().before(new java.util.Date())) {
                    System.out.println("   Token已过期");
                    System.out.println("   过期时间: " + session.getExpiresAt());
                    System.out.println("   当前时间: " + new java.util.Date());
                    sendErrorResponse(response, Result.error("Token已过期"), 401);
                } else {
                    System.out.println("   其他验证失败情况");
                    System.out.println("   会话状态: " + session.getStatus());
                    System.out.println("   用户ID匹配: " + session.getUserId().equals(userId));
                    sendErrorResponse(response, Result.error("Token无效"), 401);
                }
                return false;
            }
        } catch (Exception e) {
            System.err.println("Token解析失败: " + e.getMessage());
            e.printStackTrace();
            sendErrorResponse(response, Result.error("Token解析失败: " + e.getMessage()), 401);
            return false;
        }
    }

    private void sendErrorResponse(HttpServletResponse response, Result<?> result, int statusCode) throws Exception {
        response.setStatus(statusCode);
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        try {
            String json = new ObjectMapper().writeValueAsString(result);
            System.out.println("返回错误响应: " + json);
            out.write(json);
            out.flush();
        } finally {
            out.close();
        }
    }
}