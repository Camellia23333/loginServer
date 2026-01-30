package com.example.logintest.listener;

import com.example.logintest.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

/**
 * 监听 Redis Key 过期事件
 */
@Component
public class RedisKeyExpirationListener extends KeyExpirationEventMessageListener {

    @Autowired
    private OrderService orderService;

    public RedisKeyExpirationListener(RedisMessageListenerContainer listenerContainer) {
        super(listenerContainer);
    }

    /**
     * 当有 Key 过期时，会回调这个方法
     * @param message 过期的 Key 的信息
     * @param pattern 监听模式
     */
    @Override
    public void onMessage(Message message, byte[] pattern) {
        //获取失效的 key
        String expiredKey = message.toString();

        //判断是不是我们的订单过期 key ，防止误判其他 key
        if (expiredKey != null && expiredKey.startsWith("order:expire:")) {
            // 提取订单号
            String orderNo = expiredKey.replace("order:expire:", "");

            System.out.println("收到 Redis 过期通知，准备关闭订单: " + orderNo);

            // 调用业务逻辑关闭订单
            orderService.closeOrder(orderNo);
        }
    }
}
