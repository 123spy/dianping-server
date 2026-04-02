package com.spy.server.mq.consumer;

import cn.hutool.core.lang.UUID;
import com.rabbitmq.client.Channel;
import com.spy.server.common.ErrorCode;
import com.spy.server.exception.BusinessException;
import com.spy.server.model.domain.Coupon;
import com.spy.server.model.domain.CouponOrder;
import com.spy.server.mq.config.CouponOrderMqConfig;
import com.spy.server.mq.model.CouponOrderEvent;
import com.spy.server.mq.producer.CouponOrderEventProducer;
import com.spy.server.mq.service.MqConsumeRecordService;
import com.spy.server.service.CouponOrderService;
import com.spy.server.service.CouponService;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class CouponOrderDLXConsumer {

    // event是处理的业务数据，message是rabbitmq附加的消息数据
    @RabbitListener(queues = CouponOrderMqConfig.COUPON_ORDER_DLX_QUEUE)
    public void handle(CouponOrderEvent event, Message message, Channel channel) throws Exception {
        Long couponId = event.getCouponId();
        Long userId = event.getUserId();
        LocalDateTime eventTime = event.getEventTime();
        int retryCount = event.getRetryCount();
        log.error("用户抢购，订单创建失败。 couponId={}, userId={}, eventTime={}, retryCount={}", couponId, userId, eventTime, retryCount);
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }
}
