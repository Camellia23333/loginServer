package com.example.logintest.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

@Configuration
public class RedisListenerConfig {
/**
 * 创建并配置Redis消息监听器容器
 *
 * @param connectionFactory Redis连接工厂，用于建立与Redis服务器的连接
 * @return 配置好的RedisMessageListenerContainer实例，用于管理Redis消息监听器
 */
    @Bean
    public RedisMessageListenerContainer container(RedisConnectionFactory connectionFactory) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        return container;
    }
}
