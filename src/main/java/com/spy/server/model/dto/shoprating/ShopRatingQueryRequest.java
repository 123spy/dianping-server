package com.spy.server.model.dto.shoprating;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.spy.server.common.PageRequest;
import lombok.Data;

import java.util.Date;

/**
 * 店铺评分表
 * @TableName shop_rating
 */
@Data
public class ShopRatingQueryRequest extends PageRequest {
    /**
     * 主键 id
     */
    private Long id;

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