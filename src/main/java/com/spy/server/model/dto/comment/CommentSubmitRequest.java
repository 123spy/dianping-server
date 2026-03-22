package com.spy.server.model.dto.comment;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 评论表
 *
 * @TableName comment
 */
@Data
public class CommentSubmitRequest {
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
}