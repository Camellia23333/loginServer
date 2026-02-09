package com.example.logintest.entity;

import java.io.Serializable;

/**
 * 菜单权限表实体
 * 对应数据库表：sys_menu
 */
public class SysMenu implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String menuName; // 菜单名称
    private String perms;    // 权限标识 (如: product:add)
    private String path;     // 路由地址 (前端跳转用)
    private Integer type;    // 类型 (0目录 1菜单 2按钮)

    public SysMenu() {}

    // Getter & Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getMenuName() { return menuName; }
    public void setMenuName(String menuName) { this.menuName = menuName; }
    public String getPerms() { return perms; }
    public void setPerms(String perms) { this.perms = perms; }
    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }
    public Integer getType() { return type; }
    public void setType(Integer type) { this.type = type; }
}
