package com.spy.server.model.dto.category;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 店铺分类表
 *
 * @TableName category
 */
@Data
public class CategoryAddRequest {

    /**
     * 分类名称
     */
    private String name;

    /**
     * 排序值，越小越靠前
     */
    private Integer sort;
}