package com.spy.server.model.dto.shop;

import com.spy.server.common.PageRequest;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 店铺表
 * @TableName shop
 */
@Data
public class ShopNearQueryRequest {
    /**
     * 经度
     */
    private BigDecimal longitude;

    /**
     * 纬度
     */
    private BigDecimal latitude;

    /**
     * 种类
     */
    private Long categoryId;

    /**
     * 距离
     */
    private Long distance;
}