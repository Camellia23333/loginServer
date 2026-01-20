package com.example.logintest;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.logintest.dao")
public class LoginTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(LoginTestApplication.class, args);
        System.out.println("=========================================");
        System.out.println("=====  登录服务启动成功! 端口: 8080  =====");
        System.out.println("=========================================");
    }
}
