package com.spy.server.mq.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.spy.server.mq.model.MqConsumeRecord;

/**
* @author OUC
* @description 针对表【mq_consume_record】的数据库操作Service
* @createDate 2026-04-01 21:47:15
*/
public interface MqConsumeRecordService extends IService<MqConsumeRecord> {
    /**
     * 尝试插入消费记录
     * true: 第一次消费
     * false: 重复消息
     */
    boolean tryInsert(String msgId, String bizType);

    /**
     * 标记消费成功
     */
    void markSuccess(String msgId, String bizType);

    /**
     * 标记消费失败
     */
    void markFail(String msgId, String bizType);
}
