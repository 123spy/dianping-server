package com.spy.server.mq.consumer;

import com.rabbitmq.client.Channel;
import com.spy.server.model.domain.Shop;
import com.spy.server.mq.model.ShopRatingChangedEvent;
import com.spy.server.service.ShopService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class ShopRatingChangedConsumer {

    private final StringRedisTemplate stringRedisTemplate;

    private final ShopService shopService;

    @RabbitListener(queues = "shop.rating.queue")
    public void handle(ShopRatingChangedEvent event, Message message, Channel channel) throws Exception {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();

        try {
            Long shopId = event.getShopId();

            String shopDetailKey = "dianping:shop:get:vo:" + shopId;
            stringRedisTemplate.delete(shopDetailKey);

            Shop shop = shopService.getById(shopId);
            if(shop != null && shop.getAvgScore() != null) {
                stringRedisTemplate.opsForZSet().add("ranking:shop:rating", shopId.toString(), shop.getAvgScore().doubleValue());
            }

            channel.basicAck(deliveryTag, false);
            log.info("消费评分变更消息成功, shopId={}", shopId);
        } catch (Exception e) {
            log.error("消费评分变更消息失败", e);
            channel.basicNack(deliveryTag, false, false);
        }
    }
}
