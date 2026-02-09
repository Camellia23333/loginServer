package com.example.logintest.entity;

import java.io.Serializable;

/**
 * 角色表实体
 * 对应数据库表：sys_role
 */
public class SysRole implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String roleName; // 角色名称 (如: 管理员)
    private String roleKey;  // 角色权限字符串 (如: admin)
    private Integer status;  // 角色状态 (1正常 0停用)

    // 无参构造
    public SysRole() {}

    // Getter & Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }
    public String getRoleKey() { return roleKey; }
    public void setRoleKey(String roleKey) { this.roleKey = roleKey; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
}
