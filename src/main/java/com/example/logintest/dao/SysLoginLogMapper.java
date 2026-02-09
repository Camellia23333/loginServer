package com.example.logintest.dao;

import com.example.logintest.entity.SysLoginLog;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface SysLoginLogMapper {

    @Insert("INSERT INTO sys_login_log(user_id, ip_addr, login_time) " +
            "VALUES(#{userId}, #{ipAddr}, #{loginTime})")
    int insert(SysLoginLog log);

    // 统计接口预留：查询最近7天的每日登录人数 ，会计看板用
    // 这里的SQL使用了 DATE_FORMAT 按天分组
    @Select("SELECT DATE_FORMAT(login_time, '%Y-%m-%d') as date, COUNT(*) as count " +
            "FROM sys_login_log " +
            "GROUP BY date " +
            "ORDER BY date DESC LIMIT 7")
    List<Map<String, Object>> statDailyLoginCount();
}
