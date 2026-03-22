package com.spy.server.model.dto.favorite;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 收藏表
 *
 * @TableName favorite
 */
@Data
public class FavoriteSubmitRequest {
    /**
     * 店铺 id
     */
    private Long shopId;
}