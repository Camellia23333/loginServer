package com.example.logintest.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import com.example.logintest.dao.OrdersMapper;
import com.example.logintest.dao.ProductMapper;
import com.example.logintest.entity.Orders;
import com.example.logintest.entity.Product;
import com.example.logintest.entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Service
public class OrderService {

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private OrdersMapper ordersMapper;

    //修改，将Redis移除，
/*    @Autowired
    private StringRedisTemplate redisTemplate; */

    /**
     * 创建订单,抢购核心逻辑
     * 加上 @Transactional：保证 扣库存 和 下订单 要么同时成功，要么同时失败
     */
    @Transactional(rollbackFor = Exception.class)
    public Result<Orders> createOrder(Long userId, Long productId, Integer count) {
        //查商品，看看这东西还存不存在
        Product product = productMapper.findById(productId);
        if (product == null) {
            return Result.error("商品不存在");
        }

        //算金额，单价 * 数量
        BigDecimal amount = product.getPrice().multiply(new BigDecimal(count));

        //先扣库存
        //利用数据库行锁防止超卖
        int rows = productMapper.reduceStock(productId, count);

        if (rows <= 0) {
            //影响行数为0，说明库存不足 ，没抢到
            //直接返回错误，不需要抛异常回滚，因为数据库压根没动
            return Result.error("手慢了，库存不足！");
        }

        //创建订单
        //到这里，说明库存已经扣除成功了，必须把订单建出来
        try {
            Orders order = new Orders();
            //生成唯一订单号 ，使用 Hutool 的雪花算法生成全局唯一的ID字符串
            String orderNo = IdUtil.getSnowflakeNextIdStr();
            order.setOrderNo(orderNo);

            order.setUserId(userId);
            order.setProductId(productId);
            order.setCount(count);
            order.setAmount(amount);
            order.setStatus(0); // 0-待支付

            Date now = new Date();
            order.setCreateTime(now);
            //设置过期时间，比如15分钟后过期
            order.setExpireTime(DateUtil.offset(now, cn.hutool.core.date.DateField.MINUTE, 15));

            ordersMapper.insert(order);

            //redisTemplate.opsForValue().set("order:expire:" + orderNo, "", 60, TimeUnit.SECONDS);
            //只返回数据，不操作Redis，Redis 由 Controller 负责
            return Result.success("抢购成功，请尽快支付", order);

        } catch (Exception e) {

            throw new RuntimeException("创建订单失败: " + e.getMessage());
        }
    }

    /**
     * 自动关闭超时订单
     * 先检查订单是不是还没付钱，再关单，最后补库存
     */
    @Transactional(rollbackFor = Exception.class)
    public void closeOrder(String orderNo) {
        //查询订单
        Orders order = ordersMapper.findByOrderNo(orderNo);
        if (order == null) {
            return; // 订单不存在，不管
        }

        //只有 待支付(0) 的才需要处理
        if (order.getStatus() != Orders.STATUS_UNPAID) {
            System.out.println("订单 " + orderNo + " 状态为 " + order.getStatus() + "，无需处理");
            return;
        }

        //修改订单状态为 已取消(2)
        //注意：这里 updateStatus 需要把时间传 null 或者当前时间，视业务而定，这里复用之前的方法
        int rows = ordersMapper.updateStatus(orderNo, Orders.STATUS_CANCELLED, null);

        if (rows > 0) {
            //恢复库存
            productMapper.recoverStock(order.getProductId(), order.getCount());
            System.out.println("订单超时，已自动回滚库存！订单号：" + orderNo);
        }
    }

    /**
     * 模拟支付成功
     * 加上 @Transactional，因为涉及两张表的更新
     */
    @Transactional(rollbackFor = Exception.class)
    public Result<String> payOrder(String orderNo) {
        //查询订单
        Orders order = ordersMapper.findByOrderNo(orderNo);
        if (order == null) {
            return Result.error("订单不存在");
        }

        //幂等性/状态检查
        //如果订单不是 "0-待支付" 状态，说明已经付过了，或者已经超时取消了
        if (order.getStatus() != Orders.STATUS_UNPAID) {
            return Result.error("订单状态异常，无法支付(已支付或已关闭)");
        }

        //执行支付，修改状态为 1-已支付，写入支付时间
        int rows = ordersMapper.updateStatus(orderNo, Orders.STATUS_PAID, new Date());

        if (rows > 0) {
            //扣减实际库存库存
            productMapper.reduceTotalStock(order.getProductId(), order.getCount());
            return Result.success("支付成功！");
        } else {
            return Result.error("支付失败，请稍后重试");
        }
    }

    /**
     * 手动取消订单，用户点击取消
     */
    @Transactional(rollbackFor = Exception.class)
    public Result<String> cancelOrder(String orderNo) {
        //查询订单
        Orders order = ordersMapper.findByOrderNo(orderNo);
        if (order == null) {
            return Result.error("订单不存在");
        }

        //状态检查，只有“待支付”的才能取消
        if (order.getStatus() != Orders.STATUS_UNPAID) {
            return Result.error("订单状态异常，无法取消");
        }

        //修改状态为 2-已取消
        int rows = ordersMapper.updateStatus(orderNo, Orders.STATUS_CANCELLED, new Date());

        if (rows > 0) {
            //立即恢复库存
            productMapper.recoverStock(order.getProductId(), order.getCount());

            //顺便把 Redis 里的过期 key 删了
            //redisTemplate.delete("order:expire:" + orderNo);

            return Result.success("订单已取消");
        }
        return Result.error("取消失败，请重试");
    }
}
