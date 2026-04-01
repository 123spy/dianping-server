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
                    message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                    message.getMessageProperties().setMessageId(correlationData.getId());
                    return message;
                    },
                correlationData
        );

        log.info("发送抢购完成信息成功, couponId={}, userId={}", event.getCouponId(), event.getUserId());
    }
}
