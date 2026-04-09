package com.spy.server.model.vo;

import lombok.Data;

import java.util.Date;

@Data
public class ShopMediaVO {

    /**
     * 主键 id
     */
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
     * 是否封面
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
     * 文件大小
     */
    private Long fileSize;

    /**
     * 创建时间
     */
    private Date createTime;
}
