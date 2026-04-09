package com.spy.server.model.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 店铺媒体表
 */
@TableName(value = "shop_media")
@Data
public class ShopMedia {

    /**
     * 主键 id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 店铺 id
     */
    private Long shopId;

    /**
     * 媒体类型：1-图片 2-视频
     */
    private Integer type;

    /**
     * 资源地址
     */
    private String url;

    /**
     * 排序号
     */
    private Integer sortNo;

    /**
     * 是否封面：0-否 1-是
     */
    private Integer isCover;

    /**
     * 原始文件名
     */
    private String fileName;

    /**
     * 文件类型
     */
    private String contentType;

    /**
     * 文件大小（字节）
     */
    private Long fileSize;

    /**
     * 上传人 id
     */
    private Long createUserId;

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
