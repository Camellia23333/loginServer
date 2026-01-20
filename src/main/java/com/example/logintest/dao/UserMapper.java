package com.example.logintest.dao;

import com.example.logintest.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 用户数据访问层
 */
@Mapper
public interface UserMapper {

    /**
     * 根据手机号和密码查询用户
     */
    @Select("SELECT id, phone, password, username, create_time, update_time " +
            "FROM user WHERE phone = #{phone} AND password = #{password}")
    User findByPhoneAndPassword(@Param("phone") String phone,
                                @Param("password") String password);
}