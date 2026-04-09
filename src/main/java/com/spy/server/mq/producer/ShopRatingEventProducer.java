package com.spy.server.mq.producer;

import com.spy.server.mq.config.ShopRatingMqConfig;
import com.spy.server.mq.model.ShopRatingChangedEvent;
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
public class ShopRatingEventProducer {

    private final RabbitTemplate rabbitTemplate;

    public void sendShopRatingChangedEvent(ShopRatingChangedEvent event) {
        CorrelationData correlationData = new CorrelationData(UUID.randomUUID().toString());

        rabbitTemplate.convertAndSend(
                ShopRatingMqConfig.SHOP_RATING_EXCHANGE,
                ShopRatingMqConfig.SHOP_RATING_ROUTING_KEY,
                event,
                message -> {
                    message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                    message.getMessageProperties().setMessageId(correlationData.getId());
                    return message;
                    },
                correlationData
        );

        log.info("发送评分变更消息成功：店铺ID={}，评分ID={}，操作类型={}", event.getShopId(), event.getRatingId(), event.getAction());
    }
}
