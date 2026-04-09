package com.spy.server.mq.config;

import com.rabbitmq.client.ConfirmCallback;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class RabbitTemplateConfig {

    @Lazy
    private final RabbitTemplate rabbitTemplate;

    @PostConstruct
    public void init() {
        rabbitTemplate.setConfirmCallback(this::confirmCallback);
        rabbitTemplate.setReturnsCallback(returned -> {
            log.error("消息路由失败：交换机={}，路由键={}，失败原因={}，消息体={}", returned.getExchange(), returned.getRoutingKey(), returned.getReplyText(), new String(returned.getMessage().getBody()));
        });
    }

    private void confirmCallback(CorrelationData correlationData, boolean ack, String cause) {
        if(ack) {
            log.info("消息到达交换机成功：关联ID={}", correlationData == null ? "null" : correlationData.getId());
        } else {
            log.error("消息到达交换机失败：关联ID={}，失败原因={}", correlationData == null ? null : correlationData.getId(), cause);
        }
    }
}
