package com.example.logintest.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

public class Orders implements Serializable {

    private static final long serialVersionUID = 1L;

    //状态常量定义
    //状态：0-待支付
    public static final int STATUS_UNPAID = 0;
    //状态：1-已支付
    public static final int STATUS_PAID = 1;
    //状态：2-已取消 (超时或手动)
    public static final int STATUS_CANCELLED = 2;

    private Long id;
    private String orderNo;
    private Long userId;
    private Long productId;
    private Integer count;
    private BigDecimal amount;
    private Integer status;
    private Date createTime;
    private Date expireTime;
    private Date payTime;

    public Orders() {
    }

    public Orders(Long id, String orderNo, Long userId, Long productId, Integer count, BigDecimal amount, Integer status, Date createTime, Date expireTime, Date payTime) {
        this.id = id;
        this.orderNo = orderNo;
        this.userId = userId;
        this.productId = productId;
        this.count = count;
        this.amount = amount;
        this.status = status;
        this.createTime = createTime;
        this.expireTime = expireTime;
        this.payTime = payTime;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(Date expireTime) {
        this.expireTime = expireTime;
    }

    public Date getPayTime() {
        return payTime;
    }

    public void setPayTime(Date payTime) {
        this.payTime = payTime;
    }

    @Override
    public String toString() {
        return "Orders{" +
                "id=" + id +
                ", orderNo='" + orderNo + '\'' +
                ", userId=" + userId +
                ", productId=" + productId +
                ", count=" + count +
                ", amount=" + amount +
                ", status=" + status +
                ", createTime=" + createTime +
                ", expireTime=" + expireTime +
                ", payTime=" + payTime +
                '}';
    }
}
