package com.spy.server.model.vo;

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
public class ShopRatingVO {
    /**
     * 主键 id
     */
    private Long id;

    /**
     * 用户 id
     */
    private Long userId;

    private UserVO userVO;

    /**
     * 店铺 id
     */
    private Long shopId;

    private ShopVO shopVO;

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
}