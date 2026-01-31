package com.example.logintest.controller;

import com.example.logintest.annotation.RateLimit;
import com.example.logintest.entity.Orders;
import com.example.logintest.entity.Result;
import com.example.logintest.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private StringRedisTemplate redisTemplate; //Redis 移动到 Controller

    /**
     * 提交订单接口
     * 加上限流注解，防止脚本疯狂刷单
     */
    @RateLimit(count = 2, time = 5) // 5秒内只能点2次
    @PostMapping("/create")
    public Result<Orders> createOrder(@RequestBody Map<String, Object> params) {
        //获取参数
        //为了测试方便先传参
        Long userId = Long.valueOf(params.get("userId").toString());
        Long productId = Long.valueOf(params.get("productId").toString());
        Integer count = Integer.valueOf(params.get("count").toString());

        //先执行数据库事务
        Result<Orders> dbResult = orderService.createOrder(userId, productId, count);

        //判断数据库是否成功
        if (dbResult.getCode() == 200 && dbResult.getData() != null) {
            Orders order = dbResult.getData();

            //只有 DB 成功了，才去埋 Redis 的雷
            // 这样就算 Redis 挂了，最多也就是不自动取消，不会导致数据不一致（而且 try-catch redis 可以保证不影响主流程）
            try {
                redisTemplate.opsForValue().set(
                        "order:expire:" + order.getOrderNo(),
                        "",
                        30, TimeUnit.SECONDS // 测试用30秒，实际15分钟
                );
            } catch (Exception e) {

                System.err.println("Redis埋点失败，订单可能无法自动取消: " + e.getMessage());
            }
        }

        return dbResult;

        //return orderService.createOrder(userId, productId, count);
    }

    /**
     * 模拟支付接口
     */
    @PostMapping("/pay")
    public Result<String> payOrder(@RequestBody Map<String, String> params) {
        String orderNo = params.get("orderNo");
        if (orderNo == null || orderNo.isEmpty()) {
            return Result.error("订单号不能为空");
        }
        return orderService.payOrder(orderNo);
    }
    /**
     * 取消订单接口
     */
    @PostMapping("/cancel")
    public Result<String> cancelOrder(@RequestBody Map<String, String> params) {
        String orderNo = params.get("orderNo");
        //先执行数据库回滚
        Result<String> dbResult = orderService.cancelOrder(orderNo);

        //DB 成功了，顺手清理 Redis
        if (dbResult.getCode() == 200) {
            redisTemplate.delete("order:expire:" + orderNo);
        }

        return dbResult;

/*        String orderNo = params.get("orderNo");
        if (orderNo == null || orderNo.isEmpty()) {
            return Result.error("订单号不能为空");
        }
        return orderService.cancelOrder(orderNo);*/
    }
}
