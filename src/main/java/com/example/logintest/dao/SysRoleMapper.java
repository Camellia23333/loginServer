package com.example.logintest.dao;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Set;

@Mapper
public interface SysRoleMapper {

    // 根据用户ID查角色Key ，用于鉴权，比如判断是不是 admin
    @Select("SELECT r.role_key " +
            "FROM sys_role r " +
            "LEFT JOIN sys_user_role ur ON r.id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND r.status = 1")
    Set<String> findRoleKeysByUserId(@Param("userId") Long userId);

    // 给用户分配角色 ，向中间表插入数据
    @Insert("INSERT INTO sys_user_role(user_id, role_id) VALUES(#{userId}, #{roleId})")
    int insertUserRole(@Param("userId") Long userId, @Param("roleId") Long roleId);
}
