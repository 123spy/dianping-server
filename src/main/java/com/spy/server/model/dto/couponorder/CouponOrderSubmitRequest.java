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
public class CouponOrderSubmitRequest {
    /**
     * 店铺 id
     */
    private Long shopId;

    /**
     * 优惠券 id
     */
    private Long couponId;
}