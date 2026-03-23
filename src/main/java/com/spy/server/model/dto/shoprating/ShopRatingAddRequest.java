package com.spy.server.model.dto.shoprating;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 店铺评分表
 * @TableName shop_rating
 */
@Data
public class ShopRatingAddRequest {

    /**
     * 用户 id
     */
    private Long userId;

    /**
     * 店铺 id
     */
    private Long shopId;

    /**
     * 评分，1-5
     */
    private Integer score;
}