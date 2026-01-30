package com.example.logintest.dao;

import com.example.logintest.entity.Orders;
import org.apache.ibatis.annotations.*;

@Mapper
public interface OrdersMapper {

    /**
     * 创建订单
     */
    @Insert("INSERT INTO orders(order_no, user_id, product_id, count, amount, status, create_time, expire_time) " +
            "VALUES(#{orderNo}, #{userId}, #{productId}, #{count}, #{amount}, #{status}, #{createTime}, #{expireTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Orders order);

    /**
     * 根据订单号查询
     */
    @Select("SELECT * FROM orders WHERE order_no = #{orderNo}")
    Orders findByOrderNo(String orderNo);

    /**
     * 更新订单状态 ，支付成功后调用
     */
    @Update("UPDATE orders SET status = #{status}, pay_time = #{payTime} WHERE order_no = #{orderNo}")
    int updateStatus(@Param("orderNo") String orderNo,
                     @Param("status") Integer status,
                     @Param("payTime") java.util.Date payTime);

}
