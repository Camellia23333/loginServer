package com.example.logintest.interceptor;

import com.example.logintest.dao.UserMapper;
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

    // 注入 UserMapper 用于查询最新的 Token 进行比对
    @Autowired
    private UserMapper userMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestURI = request.getRequestURI();
        System.out.println("========== 拦截器处理请求: " + requestURI + " ==========");

        // 定义不需要验证的路径,加上新的白名单路径
        if ("/api/login".equals(requestURI) ||
                "/api/test".equals(requestURI) ||
                "/api/send-code".equals(requestURI) ||
                "/api/register".equals(requestURI)
        ) {
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
                //新增，将 userId 存入 request 域，后续的 Controller 或 AOP 切面可以直接取用，不用再解析 Token,把 userId 存进去，这样后面的 AOP 切面才能知道是谁！
                request.setAttribute("currentUser", userId);
                return true;
            } else {
                //验证失败，进行详细的错误原因区分
                if (jwtUtil.isTokenExpired(token)) {
                    sendErrorResponse(response, Result.error("Token已过期，请重新登录"), 401);
                    return false;
                }
                // 查库比对
                String dbToken = userMapper.findTokenByUserId(userId);

                if (dbToken == null) {
                    // 数据库里没Token，说明被强制登出了
                    sendErrorResponse(response, Result.error("Token失效，请重新登录"), 401);
                } else if (!dbToken.equals(token)) {
                    // 数据库Token变了，说明被踢了
                    sendErrorResponse(response, Result.error("账号已在其他设备登录"), 409);
                } else {
                    // 其他未知情况
                    sendErrorResponse(response, Result.error("认证失败"), 401);
                }
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendErrorResponse(response, Result.error("Token解析失败"), 401);
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
