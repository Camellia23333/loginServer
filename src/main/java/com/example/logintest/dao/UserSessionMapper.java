package com.example.logintest.dao;

import com.example.logintest.entity.UserSession;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface UserSessionMapper {

    /**
     * 保存用户会话
     */
    @Insert("INSERT INTO user_sessions(user_id, token, device_info, expires_at, login_time) VALUES(#{userId}, #{token}, #{deviceInfo}, #{expiresAt}, NOW())")
    int insert(UserSession session);

    /**
     * 根据用户ID查找当前活动会话
     */
    @Select("SELECT * FROM user_sessions WHERE user_id = #{userId} AND status = 1 ORDER BY login_time DESC LIMIT 1")
    UserSession findActiveByUserId(@Param("userId") Long userId);

    /**
     * 根据Token查找会话
     */
    @Select("SELECT * FROM user_sessions WHERE token = #{token} AND status = 1 AND expires_at > NOW()")
    UserSession findByToken(@Param("token") String token);

    /**
     * 更新最后活跃时间
     */
    @Update("UPDATE user_sessions SET last_active_time = NOW() WHERE token = #{token}")
    int updateLastActiveTime(@Param("token") String token);

    /**
     * 根据用户ID移除所有活动会话
     */
    @Update("UPDATE user_sessions SET status = 0 WHERE user_id = #{userId} AND status = 1")
    int removeByUserId(@Param("userId") Long userId);

    /**
     * 根据Token移除会话
     */
    @Update("UPDATE user_sessions SET status = 0 WHERE token = #{token}")
    int removeByToken(@Param("token") String token);

    /**
     * 清理过期会话
     */
    @Delete("DELETE FROM user_sessions WHERE expires_at <= NOW()")
    int cleanupExpiredSessions();
}
