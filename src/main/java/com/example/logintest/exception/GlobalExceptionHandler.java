package com.example.logintest.exception;

import com.example.logintest.entity.Result;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 捕获我们自定义的限流异常
    @ExceptionHandler(RateLimitException.class)
    public Result<String> handleRateLimitException(RateLimitException e) {
        // 返回 429 状态码
        return Result.error(429, e.getMessage());
    }

    // 也可以顺便捕获其他异常
    @ExceptionHandler(Exception.class)
    public Result<String> handleException(Exception e) {
        e.printStackTrace(); // 打印堆栈以便调试
        return Result.error(500, "服务器繁忙，请稍后重试");
    }
}