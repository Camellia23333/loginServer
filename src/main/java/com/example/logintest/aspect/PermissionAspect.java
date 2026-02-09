package com.example.logintest.aspect;

import com.example.logintest.annotation.PreAuthorize;
import com.example.logintest.entity.Result;
import com.example.logintest.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Set;

@Aspect
@Component
public class PermissionAspect {

    @Autowired
    private UserService userService;

    /**
     * 环绕通知：拦截带有 @PreAuthorize 注解的方法
     */
    @Around("@annotation(preAuthorize)")
    public Object checkPermission(ProceedingJoinPoint point, PreAuthorize preAuthorize) throws Throwable {
        //获取当前请求
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();

        //获取当前登录用户ID ，从 AuthInterceptor 放进去的 request 属性中取
        Long userId = (Long) request.getAttribute("currentUser");

        // 如果拦截器漏了，或者没有 userId，说明代码有 bug 或者未登录
        if (userId == null) {
            return Result.error("权限验证失败：未获取到用户信息");
        }

        //获取方法上要求的权限字符
        String requiredPerm = preAuthorize.value();

        System.out.println(">>> AOP权限校验: 用户ID=" + userId + ", 需要权限=" + requiredPerm);

        //查询用户拥有的所有权限
        //优化点：这里每次都查库，高并发下可以用 Redis 缓存权限列表
        Set<String> myPerms = userService.getUserPermissions(userId);

        //核心判断：我有这个权限吗？
        // 如果是超级管理员 (admin)，通常直接放行，这里我们先严格判断
        if (myPerms.contains("*:*:*") || myPerms.contains(requiredPerm)) {
            // 权限通过，放行，执行原来的方法
            return point.proceed();
        }

        //权限不足，直接拦截，不执行原方法
        System.out.println("<<< 权限不足！用户拥有的权限: " + myPerms);
        // 这里可以直接返回 Result，或者抛出异常由全局异常处理器处理
        // 为了简单直观，我们这里直接返回错误 Result (前提是 Controller 返回值也是 Result)
        return Result.error("无权访问：缺少 " + requiredPerm + " 权限");
    }
}
