package com.example.logintest.controller;

import com.example.logintest.entity.LoginRequest;
import com.example.logintest.entity.Result;
import com.example.logintest.entity.User;
import com.example.logintest.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 登录控制器
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class LoginController {

    @Autowired
    private UserService userService;

    /**
     * 登录接口
     * POST /api/login
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody LoginRequest request) {
        System.out.println("========== 收到登录请求 ==========");
        System.out.println("手机号: " + request.getPhone());

        String phone = request.getPhone();
        String password = request.getPassword();

        // 参数校验
        if (phone == null || phone.trim().isEmpty()) {
            return Result.error("手机号不能为空");
        }
        if (password == null || password.trim().isEmpty()) {
            return Result.error("密码不能为空");
        }

        // 登录验证
        User user = userService.login(phone, password);

        if (user != null) {
            // 登录成功
            Map<String, Object> data = new HashMap<>();

            // 生成Token
            String token = UUID.randomUUID().toString().replace("-", "");
            data.put("token", token);

            // 用户信息
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("userId", user.getId());
            userInfo.put("username", user.getUsername() != null ? user.getUsername() : "用户");
            userInfo.put("phone", user.getPhone());
            data.put("userInfo", userInfo);

            System.out.println("登录成功！返回Token: " + token);

            return Result.success("登录成功", data);
        } else {
            return Result.error("手机号或密码错误");
        }
    }

    /**
     * 测试接口
     * GET /api/test
     */
    @GetMapping("/test")
    public Result<String> test() {
        return Result.success("服务正常运行!", "Hello World");
    }
}
