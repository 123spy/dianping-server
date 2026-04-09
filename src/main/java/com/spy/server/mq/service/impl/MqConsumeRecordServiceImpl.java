package com.spy.server.mq.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.spy.server.common.ErrorCode;
import com.spy.server.exception.BusinessException;
import com.spy.server.mapper.MqConsumeRecordMapper;
import com.spy.server.mq.model.MqConsumeRecord;
import com.spy.server.mq.service.MqConsumeRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
public class MqConsumeRecordServiceImpl
        extends ServiceImpl<MqConsumeRecordMapper, MqConsumeRecord>
        implements MqConsumeRecordService {

    @Override
    public boolean tryInsert(String msgId, String bizType) {
        synchronized ((msgId + bizType).intern()) {
            try {
                QueryWrapper<MqConsumeRecord> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("bizType", bizType);
                queryWrapper.eq("msgId", msgId);
                MqConsumeRecord data = this.getOne(queryWrapper);
                if (data == null) {
                    MqConsumeRecord rec = new MqConsumeRecord();
                    rec.setMsgId(msgId);
                    rec.setBizType(bizType);
                    rec.setStatus(0);
                    this.save(rec);
                    return true;
                }

                Integer status = data.getStatus();
                if (status == 0) {
                    return false;
                } else if (status == 1) {
                    return false;
                } else if (status == 2) {
                    data.setStatus(0);
                    boolean updateRes = this.updateById(data);
                    if (!updateRes) {
                        throw new BusinessException(ErrorCode.OPERATION_ERROR, "操作异常");
                    }
                    return true;
                }
                return true;
            } catch (DuplicateKeyException e) {
                log.warn("检测到重复消息：消息ID={}，业务类型={}", msgId, bizType);
                return false;
            }
        }
    }

    @Override
    public void markSuccess(String msgId, String bizType) {
        LambdaUpdateWrapper<MqConsumeRecord> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(MqConsumeRecord::getMsgId, msgId)
                .eq(MqConsumeRecord::getBizType, bizType)
                .set(MqConsumeRecord::getStatus, 1)
                .set(MqConsumeRecord::getUpdateTime, LocalDateTime.now());
        update(wrapper);
    }

    @Override
    public void markFail(String msgId, String bizType) {
        LambdaUpdateWrapper<MqConsumeRecord> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(MqConsumeRecord::getMsgId, msgId)
                .eq(MqConsumeRecord::getBizType, bizType)
                .set(MqConsumeRecord::getStatus, 2)
                .set(MqConsumeRecord::getUpdateTime, LocalDateTime.now());
        update(wrapper);
    }
}
