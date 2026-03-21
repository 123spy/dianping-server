package com.spy.server.model.dto.favorite;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.spy.server.common.PageRequest;
import lombok.Data;

import java.util.Date;

/**
 * 收藏表
 * @TableName favorite
 */
@Data
public class FavoriteQueryRequest extends PageRequest {
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
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}