package com.spy.server.model.dto.couponorder;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 优惠券订单表
 * @TableName coupon_order
 */
@Data
public class CouponOrderPayRequest {
    /**
     * 主键 id
     */
    private Long id;

    /**
     * 支付方式：wechat,alipay
     */
    private String type;
}