package com.spy.server.mq.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// 处理店铺打分后的消息队列处理
@EnableRabbit
@Configuration
public class ShopRatingMqConfig {

    public static final String SHOP_RATING_EXCHANGE = "shop.rating.exchange";
    public static final String SHOP_RATING_QUEUE = "shop.rating.queue";
    public static final String SHOP_RATING_ROUTING_KEY = "shop.rating.changed";

    @Bean
    public DirectExchange shopRatingExchange() {
        return new DirectExchange(SHOP_RATING_EXCHANGE, true, false);
    }

    @Bean
    public Queue shopRatingQueue() {
        return QueueBuilder.durable(SHOP_RATING_QUEUE).build();
    }

    @Bean
    public Binding shopRatingBinding() {
        return BindingBuilder.bind(shopRatingQueue()).to(shopRatingExchange()).with(SHOP_RATING_ROUTING_KEY);
    }

}
