package com.example.logintest.aspect;

import com.example.logintest.annotation.RateLimit;
import com.example.logintest.exception.RateLimitException;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

@Aspect
@Component
public class RateLimitAspect {

    @Autowired
    private StringRedisTemplate redisTemplate;

    // Redis Lua 脚本：保证原子性,判断+自增+过期时间
    private static final String LUA_SCRIPT =
            "local key = KEYS[1]\n" +
                    "local count = tonumber(ARGV[1])\n" +
                    "local time = tonumber(ARGV[2])\n" +
                    "local current = redis.call('get', key)\n" +
                    "if current and tonumber(current) > count then\n" +
                    "    return tonumber(current)\n" +
                    "end\n" +
                    "current = redis.call('incr', key)\n" +
                    "if tonumber(current) == 1 then\n" +
                    "    redis.call('expire', key, time)\n" +
                    "end\n" +
                    "return tonumber(current)";

    @Before("@annotation(rateLimit)")
    public void doBefore(JoinPoint point, RateLimit rateLimit) {
        //获取 Key
        String key = getCombineKey(rateLimit, point);

        //执行 Lua 脚本
        List<String> keys = Collections.singletonList(key);
        //参数：限流次数，限流时间
        //避免类型转换错误，需要用String.valueOf() 包裹一下，将ateLimit.count()和rateLimit.time()的int类型强制转换为String类型。
        Long count = redisTemplate.execute(new DefaultRedisScript<>(LUA_SCRIPT, Long.class),
                keys, String.valueOf(rateLimit.count()), String.valueOf(rateLimit.time()));

        //判断结果
        if (count != null && count > rateLimit.count()) {
            throw new RateLimitException("访问过于频繁，请稍后再试");
        }
    }

    /**
     * 生成限流 Key
     * 格式：rate_limit:IP:方法名 (默认)
     * 或   rate_limit:自定义Key:方法名
     */
    private String getCombineKey(RateLimit rateLimit, JoinPoint point) {
        StringBuilder sb = new StringBuilder("rate_limit:");

        // 如果用户定义了 key (SpEL)，解析它
        if (rateLimit.key() != null && !rateLimit.key().isEmpty()) {
            MethodSignature signature = (MethodSignature) point.getSignature();
            Method method = signature.getMethod();
            ExpressionParser parser = new SpelExpressionParser();
            EvaluationContext context = new StandardEvaluationContext();

            // 将方法参数放入上下文
            Object[] args = point.getArgs();
            String[] paramNames = signature.getParameterNames();
            for (int i = 0; i < paramNames.length; i++) {
                context.setVariable(paramNames[i], args[i]);
            }

            // 解析 SpEL
            Expression expression = parser.parseExpression(rateLimit.key());
            String value = expression.getValue(context, String.class);
            sb.append(value);
        } else {
            // 默认：IP 地址
            sb.append(getIpAddress());
        }

        sb.append(":").append(point.getSignature().getName());
        return sb.toString();
    }

    // 简单的获取 IP 工具方法
    private String getIpAddress() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) return "unknown";
        HttpServletRequest request = attributes.getRequest();
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}