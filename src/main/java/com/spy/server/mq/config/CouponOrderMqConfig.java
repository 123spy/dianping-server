package com.spy.server.mq.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// 处理抢购成功后的一些后操作
@EnableRabbit
@Configuration
public class CouponOrderMqConfig {

    public static final String COUPON_ORDER_EXCHANGE = "coupon.order.exchange";
    public static final String COUPON_ORDER_QUEUE = "coupon.order.queue";
    public static final String COUPON_ORDER_ROUTING_KEY = "coupon.order.changed";

    @Bean
    public DirectExchange couponOrderExchange() {
        return new DirectExchange(COUPON_ORDER_EXCHANGE, true, false);
    }

    @Bean
    public Queue couponOrderQueue() {
        return QueueBuilder.durable(COUPON_ORDER_QUEUE).build();
    }

    @Bean
    public Binding couponOrderBinding() {
        return BindingBuilder.bind(couponOrderQueue()).to(couponOrderExchange()).with(COUPON_ORDER_ROUTING_KEY);
    }

}
