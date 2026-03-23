package com.spy.server.model.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

/**
 * 优惠券订单表
 * @TableName coupon_order
 */
@TableName(value ="coupon_order")
@Data
public class CouponOrder {
    /**
     * 主键 id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 用户 id
     */
    private Long userId;

    /**
     * 店铺 id
     */
    private Long shopId;

    /**
     * 优惠券 id
     */
    private Long couponId;

    /**
     * 订单总金额
     */
    private BigDecimal totalAmount;

    /**
     * 实付金额
     */
    private BigDecimal payAmount;

    /**
     * 订单状态：0-待支付 1-已支付 2-已取消 3-已完成 4-已退款
     */
    private Integer status;

    /**
     * 支付时间
     */
    private Date payTime;

    /**
     * 支付方式
     */
    private String payType;

    /**
     * 取消时间
     */
    private Date cancelTime;

    /**
     * 完成时间
     */
    private Date finishTime;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 逻辑删除：0-未删 1-已删
     */
    private Integer isDelete;
}