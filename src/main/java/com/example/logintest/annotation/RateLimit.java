package com.example.logintest.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {

    /**
     * 限流键值（支持 SpEL 表达式）
     * 例如：#params['phone'] 表示根据入参 params 中的 phone 字段限流
     * 如果为空，默认根据 "IP地址 + 方法名" 限流
     */
    String key() default "";

    /**
     * 限流时间 (秒)
     * 默认 60 秒
     */
    int time() default 60;

    /**
     * 限流次数
     * 默认 60 秒内 5 次
     */
    int count() default 5;
}