package com.example.logintest.controller;

import com.example.logintest.annotation.RateLimit;
import com.example.logintest.entity.Orders;
import com.example.logintest.entity.Result;
import com.example.logintest.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

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

        return orderService.createOrder(userId, productId, count);
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
        if (orderNo == null || orderNo.isEmpty()) {
            return Result.error("订单号不能为空");
        }
        return orderService.cancelOrder(orderNo);
    }
}
