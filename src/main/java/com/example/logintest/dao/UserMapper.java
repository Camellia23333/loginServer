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

    // 新增：登录成功：更新用户的 Token 和 更新时间
    @Update("UPDATE user SET token = #{token}, update_time = CURRENT_TIMESTAMP WHERE id = #{userId}")
    int updateUserToken(@Param("userId") Long userId, @Param("token") String token);

    // 新增：通过Token查询用户（用于校验）
    @Select("SELECT id, phone, password, username, token, create_time, update_time " +
            "FROM user WHERE token = #{token} AND token IS NOT NULL")
    User findByToken(@Param("token") String token);

    //根据 ID 查询用户当前的 Token (用于校验)*
    @Select("SELECT token FROM user WHERE id = #{userId}")
    String findTokenByUserId(@Param("userId") Long userId);

    // 【可选】如果你需要根据ID查整个用户
    @Select("SELECT * FROM user WHERE id = #{userId}")
    User findById(@Param("userId") Long userId);

    //新增：注册，插入新用户信息
    @Insert("INSERT INTO user(phone, password, username, create_time, update_time) " +
            "VALUES(#{phone}, #{password}, #{username}, #{createTime}, NOW())")
    int insert(User user);

    //新增：统计手机号存在的数量@return 0表示不存在，大于0表示已存在
    @Select("SELECT count(*) FROM user WHERE phone = #{phone}")
    int countByPhone(@Param("phone") String phone);
}
