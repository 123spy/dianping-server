package com.spy.server.model.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 优惠券表（券模板）
 * @TableName coupon
 */
@Data
public class CouponVO {
    /**
     * 主键 id
     */
    private Long id;

    /**
     * 店铺 id
     */
    private Long shopId;

    /**
     * 优惠券标题
     */
    private String title;

    /**
     * 优惠券描述
     */
    private String description;

    /**
     * 优惠券类型：0-普通券 1-团购券 2-秒杀券
     */
    private Integer type;

    /**
     * 原价
     */
    private BigDecimal price;

    /**
     * 优惠价/折后价
     */
    private BigDecimal discountPrice;

    /**
     * 库存
     */
    private Integer stock;

    /**
     * 可领取开始时间
     */
    private Date availableStartTime;

    /**
     * 可领取结束时间
     */
    private Date availableEndTime;

    /**
     * 可使用开始时间
     */
    private Date useStartTime;

    /**
     * 可使用结束时间
     */
    private Date useEndTime;

    /**
     * 状态：0-下架 1-上架 2-审核中
     */
    private Integer status;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}