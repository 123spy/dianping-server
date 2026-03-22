package com.spy.server.model.vo;

import lombok.Data;

import java.util.Date;

/**
 * 店铺分类表
 *
 * @TableName category
 */
@Data
public class CategoryVO {
    /**
     * 主键 id
     */
    private Long id;

    /**
     * 分类名称
     */
    private String name;

    /**
     * 排序值，越小越靠前
     */
    private Integer sort;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}