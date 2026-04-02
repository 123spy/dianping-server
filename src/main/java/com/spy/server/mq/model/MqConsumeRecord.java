package com.spy.server.mq.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 
 * @TableName mq_consume_record
 */
@TableName(value ="mq_consume_record")
@Data
public class MqConsumeRecord{
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 
     */
    private String msgId;

    /**
     * 
     */
    private String bizType;

    /**
     * 0：处理中
     * 1：成功
     * 2：失败
     */
    private Integer status;

    /**
     * 
     */
    private Date createTime;

    /**
     * 
     */
    private Date updateTime;
}