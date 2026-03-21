package com.spy.server.model.dto.comment;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.spy.server.common.PageRequest;
import lombok.Data;

import java.util.Date;

/**
 * 评论表
 * @TableName comment
 */
@Data
public class CommentQueryRequest extends PageRequest {
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
     * 评论内容
     */
    private String content;

    /**
     * 评分，1-5
     */
    private Integer score;

    /**
     * 点赞数
     */
    private Integer likeCount;

    /**
     * 评论状态：0-正常 1-隐藏/删除 2-待审核
     */
    private Integer status;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}