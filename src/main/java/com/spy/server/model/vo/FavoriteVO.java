package com.spy.server.model.vo;

import lombok.Data;

import java.util.Date;

/**
 * 收藏表
 * @TableName favorite
 */
@Data
public class FavoriteVO {
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
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}