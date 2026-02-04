package com.example.logintest.dao;

import com.example.logintest.entity.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
@Mapper
public interface ProductMapper {

    /**
     * 根据ID查询商品信息
     */
    @Select("SELECT * FROM product WHERE id = #{id}")
    Product findById(Long id);

    /**
     * 新增:查询所有商品列表
     */
    // 在真实企业开发中，这里通常会配合分页插件 (PageHelper)，暂时我们先查全部
    @Select("SELECT * FROM product ORDER BY create_time DESC")
    List<Product> findAll();

    /**
     *扣减可下单库存
     * 只有当 available_stock >= count 时才会更新成功
     * @return 影响行数 (1表示成功，0表示库存不足)
     */
    @Update("UPDATE product SET available_stock = available_stock - #{count} " +
            "WHERE id = #{id} AND available_stock >= #{count}")
    int reduceStock(@Param("id") Long id, @Param("count") Integer count);

    /**
     *恢复库存 ,当订单取消时调用
     */
    @Update("UPDATE product SET available_stock = available_stock + #{count} WHERE id = #{id}")
    int recoverStock(@Param("id") Long id, @Param("count") Integer count);

    /**
     *支付成功后，扣减实际库存
     */
    @Update("UPDATE product SET total_stock = total_stock - #{count} WHERE id = #{id}")
    int reduceTotalStock(@Param("id") Long id, @Param("count") Integer count);
}
