package com.example.logintest.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 权限校验注解
 * 用法: @PreAuthorize("product:add")
 */
@Target(ElementType.METHOD) // 只能用在方法上
@Retention(RetentionPolicy.RUNTIME) // 运行时有效
public @interface PreAuthorize {
    /**
     * 需要的权限标识，如 "user:list"
     */
    String value();
}
