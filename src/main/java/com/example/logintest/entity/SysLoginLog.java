package com.example.logintest.entity;

import java.io.Serializable;
import java.util.Date;

/**
 * 登录日志实体
 * 对应数据库表：sys_login_log
 */
public class SysLoginLog implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long userId;    // 谁登录的
    private String ipAddr;  // 登录IP
    private Date loginTime; // 登录时间

    public SysLoginLog() {}

    // 构造函数方便直接new
    public SysLoginLog(Long userId, String ipAddr, Date loginTime) {
        this.userId = userId;
        this.ipAddr = ipAddr;
        this.loginTime = loginTime;
    }

    // Getter & Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getIpAddr() { return ipAddr; }
    public void setIpAddr(String ipAddr) { this.ipAddr = ipAddr; }
    public Date getLoginTime() { return loginTime; }
    public void setLoginTime(Date loginTime) { this.loginTime = loginTime; }
}
