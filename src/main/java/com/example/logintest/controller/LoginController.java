package com.example.logintest.controller;

import com.example.logintest.annotation.RateLimit;
import com.example.logintest.entity.LoginRequest;
import com.example.logintest.entity.Result;
import com.example.logintest.entity.User;
import com.example.logintest.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 登录控制器
 */
@RestController
@RequestMapping("/api")
public class LoginController {

    @Autowired
    private UserService userService;

    // 注入 Redis 工具类,Spring Boot 自动配置好的
    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 登录接口
     * POST /api/login
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        System.out.println("========== 收到登录请求 ==========");
        System.out.println("手机号: " + request.getPhone());

        String phone = request.getPhone();
        String password = request.getPassword();
        String deviceInfo = getDeviceInfo(httpRequest); // 获取设备信息
        String ipAddr = getClientIpAddress(httpRequest);// 获取客户端真实IP

        // 参数校验
        if (phone == null || phone.trim().isEmpty()) {
            return Result.error("手机号不能为空");
        }
        if (password == null || password.trim().isEmpty()) {
            return Result.error("密码不能为空");
        }

        // 登录验证，新增传入 IP 用于记录日志
        User user = userService.login(phone, password, ipAddr);

        if (user != null) {
            // 登录成功，生成唯一Token
            String token = userService.generateUniqueToken(user, deviceInfo);

            // 准备返回数据，开始组装返回给前端
            Map<String, Object> data = new HashMap<>();
            data.put("token", token);

            // 用户信息
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("userId", user.getId());
            userInfo.put("username", user.getUsername() != null ? user.getUsername() : "用户");
            userInfo.put("phone", user.getPhone());
            data.put("userInfo", userInfo);

            // 新增核心：获取权限信息
            // 既然登录了，就顺便告诉前端这个人的身份，省得前端再发请求问
            // 在超大型系统中，通常会把这个拆分为独立的 /api/user/info 接口，方便页面刷新时单独获取

            // 获取角色集合 (Set去重) -> ["admin"]
            data.put("roles", userService.getUserRoleKeys(Long.valueOf(user.getId())));//修改将user.getId()的返回值从Integer转换为Long类型

            // 获取按钮权限集合 -> ["product:add", "user:delete"]
            data.put("permissions", userService.getUserPermissions(Long.valueOf(user.getId())));

            // 获取菜单列表 ，用于渲染左侧边栏
            data.put("menus", userService.getUserMenuList(Long.valueOf(user.getId())));


            System.out.println("登录成功！返回Token: " + token);

            return Result.success("登录成功", data);
        } else {
            return Result.error("手机号或密码错误");
        }
    }


    /**
     * 登出接口
     * POST /api/logout
     */
    @PostMapping("/logout")
    public Result<String> logout(@RequestHeader("Authorization") String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return Result.error("缺少认证Token");
        }

        String token = authorization.substring(7);
        try {
            Long userId = userService.getUserIdFromToken(token);
            userService.logout(userId, token);
            return Result.success("登出成功");
        } catch (Exception e) {
            return Result.error("登出失败");
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

    /**
     * 获取设备信息
     */
    private String getDeviceInfo(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        return userAgent != null ? userAgent.substring(0, Math.min(userAgent.length(), 200)) : "Unknown";
    }

    /**
     * 获取客户端真实IP地址
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        // 如果有多个IP地址，取第一个（处理代理链的情况）
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        return ip;
    }

    /**
     * 发送短信验证码接口
     * POST /api/send-code
     * 锁1 (AOP): IP 限流
     * 锁2 (业务代码): 手机号限流
     */
    //锁1限制：同一个 IP，60秒内只能请求 5 次 ，防止一直换号刷钱
    @RateLimit(time = 60, count = 5)
    @PostMapping("/send-code")
    public Result<String> sendCode(@RequestBody Map<String, String> params) {
        String phone = params.get("phone");

        //新增两个参数
        String captchaCode = params.get("captchaCode"); // 用户输入的验证码 (比如 A8B2)
        String captchaUuid = params.get("captchaUuid"); // 图片的身份证 ID

        if (phone == null || phone.isEmpty()) return Result.error("手机号不能为空");
        if (captchaCode == null || captchaCode.isEmpty()) return Result.error("请输入图形验证码");
        if (captchaUuid == null || captchaUuid.isEmpty()) return Result.error("验证码已失效，请刷新");

        // =================图形验证码校验 =================
        String redisKey = "captcha:verify:" + captchaUuid;
        String realCode = redisTemplate.opsForValue().get(redisKey);

        if (realCode == null) {
            return Result.error("验证码已过期，请点击图片刷新");
        }
        // 忽略大小写比对 (用户输入 a8b2 也能过)
        if (!realCode.equalsIgnoreCase(captchaCode)) {
            return Result.error("图形验证码错误");
        }

        // 校验通过后，立刻删除该验证码 (防止同一个验证码被复用刷接口)
        redisTemplate.delete(redisKey);

        // ================= 短信频率限制 =================
        //Redis 原子性检查：60秒内不允许重复发送
        //锁2限制: 手机号限流，同一个手机号，60秒内只能发 1 条，防止骚扰用户
        String limitKey = "sms:limit:" + phone;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(limitKey))) {
            return Result.error("发送太频繁，请稍后再试");
        }

        //生成 4 位随机验证码
        String code = String.valueOf((int)((Math.random() * 9 + 1) * 1000));

        // 存入 Redis，设置 5 分钟过期
        // key: "sms:code:13800138000" -> value: "8899"
        String codeKey = "sms:code:" + phone;
        redisTemplate.opsForValue().set(codeKey, code, 5, TimeUnit.MINUTES);

        //防刷限制，设置手机号限流的 Key，有效期 60 秒
        redisTemplate.opsForValue().set(limitKey, "1", 60, TimeUnit.SECONDS);

        //模拟发送短信,在控制台打印,
        System.out.println("========================================");
        System.out.println("[模拟短信] 发送给 " + phone + " 的验证码是: " + code);
        System.out.println("========================================");

        return Result.success("验证码发送成功");
    }

    @PostMapping("/register")
    public Result<String> register(@RequestBody Map<String, String> params) {
        //注册逻辑
        String phone = params.get("phone");
        String password = params.get("password");
        String code = params.get("code");
        return userService.register(phone, password, code);
    }

}
