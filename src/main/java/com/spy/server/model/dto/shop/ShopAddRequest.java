package com.spy.server.model.dto.shop;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 店铺表
 * @TableName shop
 */
@Data
public class ShopAddRequest {

    /**
     * 店长/店铺管理员 id，对应 user.id
     */
    private Long managerId;

    /**
     * 店铺名称
     */
    private String name;

    /**
     * 店铺介绍
     */
    private String description;

    /**
     * 店铺标签，逗号分隔，如 火锅,夜宵,热门
     */
    private List<String> tags;

    /**
     * 店铺分类 id
     */
    private Long categoryId;

    /**
     * 经度
     */
    private BigDecimal longitude;

    /**
     * 纬度
     */
    private BigDecimal latitude;

    /**
     * 店铺地址
     */
    private String address;

    /**
     * 店铺所在城市
     */
    private String city;

    /**
     * 营业状态：0-歇业 1-营业
     */
    private Integer businessStatus;

    /**
     * 审核状态：0-审核中 1-审核通过 2-审核失败
     */
    private Integer auditStatus;

    /**
     * 店铺平均分，范围建议 0.00-5.00
     */
    private BigDecimal avgScore;

    /**
     * 评论数
     */
    private Integer commentCount;

    /**
     * 收藏数
     */
    private Integer favoriteCount;

    /**
     * 浏览量
     */
    private Integer viewCount;
}