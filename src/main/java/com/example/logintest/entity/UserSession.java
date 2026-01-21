package com.example.logintest.entity;

import java.util.Date;

public class UserSession {
    private Long id;
    private Long userId;
    private String token;
    private String deviceInfo;
    private Date loginTime;
    private Date lastActiveTime;
    private Date expiresAt;
    private Integer status; // 1-活跃, 0-已退出

    // 构造函数
    public UserSession() {}

    public UserSession(Long userId, String token, Date expiresAt) {
        this.userId = userId;
        this.token = token;
        this.expiresAt = expiresAt;
        this.status = 1; // 默认活跃
    }

    // Getter和Setter方法
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    
    public String getDeviceInfo() { return deviceInfo; }
    public void setDeviceInfo(String deviceInfo) { this.deviceInfo = deviceInfo; }
    
    public Date getLoginTime() { return loginTime; }
    public void setLoginTime(Date loginTime) { this.loginTime = loginTime; }
    
    public Date getLastActiveTime() { return lastActiveTime; }
    public void setLastActiveTime(Date lastActiveTime) { this.lastActiveTime = lastActiveTime; }
    
    public Date getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Date expiresAt) { this.expiresAt = expiresAt; }
    
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
}
