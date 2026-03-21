package com.spy.server.model.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 用户拥有的优惠券表
 * @TableName user_coupon
 */
@TableName(value ="user_coupon")
@Data
public class UserCoupon {
    /**
     * 主键 id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户 id
     */
    private Long userId;

    /**
     * 优惠券 id
     */
    private Long couponId;

    /**
     * 关联订单 id
     */
    private Long orderId;

    /**
     * 券码/核销码
     */
    private String code;

    /**
     * 用户券状态：0-未使用 1-已使用 2-已过期 3-已退款
     */
    private Integer status;

    /**
     * 获得时间
     */
    private Date obtainTime;

    /**
     * 使用时间
     */
    private Date useTime;

    /**
     * 过期时间
     */
    private Date expireTime;

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