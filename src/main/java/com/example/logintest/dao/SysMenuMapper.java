package com.example.logintest.dao;

import com.example.logintest.entity.SysMenu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Set;

@Mapper
public interface SysMenuMapper {

    /**
     * 核心查询：根据用户ID查询其拥有的所有权限标识 (perms)
     * 逻辑：用户 -> 用户角色 -> 角色菜单 -> 菜单 -> 筛选出 perms 字段
     * 用 distinct 去重，防止多个角色拥有同一个权限导致重复
     */
    @Select("SELECT DISTINCT m.perms " +
            "FROM sys_menu m " +
            "LEFT JOIN sys_role_menu rm ON m.id = rm.menu_id " +
            "LEFT JOIN sys_user_role ur ON rm.role_id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND m.perms IS NOT NULL AND m.perms != ''")
    Set<String> findPermsByUserId(@Param("userId") Long userId);

    /**
     * 根据用户ID查询其可以看到的菜单列表 (用于前端生成左侧菜单栏)
     * 类型为 0(目录) 和 1(菜单) 的才查出来，按钮(2)不需要显示在左侧
     */
    @Select("SELECT DISTINCT m.* " +
            "FROM sys_menu m " +
            "LEFT JOIN sys_role_menu rm ON m.id = rm.menu_id " +
            "LEFT JOIN sys_user_role ur ON rm.role_id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND m.type IN (0, 1) " +
            "ORDER BY m.id") // 简单按ID排序，实际项目中可能有 order_num 字段
    List<SysMenu> findMenusByUserId(@Param("userId") Long userId);
}
