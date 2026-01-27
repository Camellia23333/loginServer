package com.example.logintest.controller;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import com.example.logintest.annotation.RateLimit;
import com.example.logintest.entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api")
public class CaptchaController {

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 获取图形验证码
     */
/*    //新增限流，每分钟最多获取 40 次验证码
    @RateLimit(time = 60, count = 40)*/
    @GetMapping("/captcha")
    public Result<Map<String, String>> getCaptcha() {
        //生成验证码图片,宽150, 高60, 4个字符, 干扰线10条
        LineCaptcha lineCaptcha = CaptchaUtil.createLineCaptcha(150, 60, 4, 10);

        //获取验证码里的文字,用于存 Redis
        String code = lineCaptcha.getCode();

        //生成唯一标识 UUID
        String uuid = UUID.randomUUID().toString();

        //存入 Redis (Key: "captcha:verify:UUID", Value: code, 2分钟过期)
        String redisKey = "captcha:verify:" + uuid;
        redisTemplate.opsForValue().set(redisKey, code, 2, TimeUnit.MINUTES);

        //准备返回给前端的数据
        Map<String, String> map = new HashMap<>();
        map.put("uuid", uuid);
        // Hutool 可以直接生成带前缀的 Base64 字符串 (data:image/png;base64,...)
        map.put("image", lineCaptcha.getImageBase64Data());

        System.out.println("生成验证码: " + code + " | UUID: " + uuid);

        return Result.success("获取成功", map);
    }
}