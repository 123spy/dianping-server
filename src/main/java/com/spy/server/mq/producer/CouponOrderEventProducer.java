package com.spy.server.mq.producer;

import com.spy.server.mq.config.CouponOrderMqConfig;
import com.spy.server.mq.model.CouponOrderEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class CouponOrderEventProducer {

    private final RabbitTemplate rabbitTemplate;

    public void sendCouponOrderSuccessEvent(CouponOrderEvent event) {
        CorrelationData correlationData = new CorrelationData(UUID.randomUUID().toString());

        rabbitTemplate.convertAndSend(
                CouponOrderMqConfig.COUPON_ORDER_EXCHANGE,
                CouponOrderMqConfig.COUPON_ORDER_ROUTING_KEY,
                event,
                message -> {
                    // 设置消息持久化
                    message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                    // 设置消息的id
                    message.getMessageProperties().setMessageId(correlationData.getId());
                    return message;
                    },
                correlationData
        );

        log.info("发送抢购完成消息成功：优惠券ID={}，用户ID={}", event.getCouponId(), event.getUserId());
    }

    public void sendRetryQueue(CouponOrderEvent newEvent) {
        CorrelationData correlationData = new CorrelationData(UUID.randomUUID().toString());

        rabbitTemplate.convertAndSend(
                CouponOrderMqConfig.COUPON_ORDER_RETRY_EXCHANGE,
                CouponOrderMqConfig.COUPON_ORDER_RETRY_ROUTING_KEY,
                newEvent,
                message -> {
                    // 设置消息持久化
                    message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                    // 设置消息的id
                    message.getMessageProperties().setMessageId(correlationData.getId());
                    return message;
                },
                correlationData
        );

        log.info("发送重试消息成功：优惠券ID={}，用户ID={}", newEvent.getCouponId(), newEvent.getUserId());
    }
}
