package com.example.logintest.dao;

import com.example.logintest.entity.User;
import org.apache.ibatis.annotations.*;

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

    // 新增：更新用户Token
    @Update("UPDATE user SET token = #{token}, update_time = CURRENT_TIMESTAMP WHERE id = #{userId}")
    int updateUserToken(@Param("userId") Long userId, @Param("token") String token);

    // 新增：通过Token查询用户（用于校验）
    @Select("SELECT id, phone, password, username, token, create_time, update_time " +
            "FROM user WHERE token = #{token} AND token IS NOT NULL")
    User findByToken(@Param("token") String token);

    //新增：注册，插入新用户信息
    @Insert("INSERT INTO user(phone, password, username, create_time, update_time) " +
            "VALUES(#{phone}, #{password}, #{username}, #{createTime}, NOW())")
    int insert(User user);

    //新增：统计手机号存在的数量@return 0表示不存在，大于0表示已存在
    @Select("SELECT count(*) FROM user WHERE phone = #{phone}")
    int countByPhone(@Param("phone") String phone);
}