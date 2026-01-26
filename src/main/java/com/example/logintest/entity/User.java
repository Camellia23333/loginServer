package com.example.logintest.entity;

import java.util.Date;

/**
 * 用户实体类
 */
public class User {

    private Integer id;
    private String phone;
    private String password;
    private String token; // 新增token字段
    private String username;
    private Date createTime;
    private Date updateTime;

    // 无参构造
    public User() {
    }

    // 有参构造（补充token）
    public User(String phone, String password, String username) {
        this.phone = phone;
        this.password = password;
        this.username = username;
        //this.token = token;
    }

    // Getter 和 Setter
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    // Getter 和 Setter（新增token的get/set）
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", phone='" + phone + '\'' +
                ", username='" + username + '\'' +
                '}';
    }
}