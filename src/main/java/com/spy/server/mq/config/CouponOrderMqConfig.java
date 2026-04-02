package com.spy.server.mq.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// 处理抢购成功后的一些后操作
@EnableRabbit
@Configuration
public class CouponOrderMqConfig {

    // 消息队列
    public static final String COUPON_ORDER_EXCHANGE = "coupon.order.exchange";
    public static final String COUPON_ORDER_QUEUE = "coupon.order.queue";
    public static final String COUPON_ORDER_ROUTING_KEY = "coupon.order.changed";


    // 延迟队列
    public static final String COUPON_ORDER_RETRY_EXCHANGE = "coupon.order.retry.exchange";
    public static final String COUPON_ORDER_RETRY_QUEUE = "coupon.order.retry.queue";
    public static final String COUPON_ORDER_RETRY_ROUTING_KEY = "coupon.order.retry.changed";


    // 死信队列
    public static final String COUPON_ORDER_DLX_EXCHANGE = "coupon.order.dlx.exchange";
    public static final String COUPON_ORDER_DLX_QUEUE = "coupon.order.dlx.queue";
    public static final String COUPON_ORDER_DLX_ROUTING_KEY = "coupon.order.dlx.changed";

    // 消息队列
    @Bean
    public DirectExchange couponOrderExchange() {
        // 第一个true，是否持久化
        // 第二个false，是否自动删除。如果为false即便目前没有队列绑定也会存在着。
        return new DirectExchange(COUPON_ORDER_EXCHANGE, true, false);
    }

    @Bean
    public Queue couponOrderQueue() {
        // 绑定死信队列
        return QueueBuilder
                .durable(COUPON_ORDER_QUEUE)
                .deadLetterExchange(COUPON_ORDER_DLX_EXCHANGE)
                .deadLetterRoutingKey(COUPON_ORDER_DLX_ROUTING_KEY)
                .build();
    }

    @Bean
    public Binding couponOrderBinding() {
        return BindingBuilder.bind(couponOrderQueue()).to(couponOrderExchange()).with(COUPON_ORDER_ROUTING_KEY);
    }

    // 延迟队列
    @Bean
    public DirectExchange couponOrderRetryExchange() {
        // 第一个true，是否持久化
        // 第二个false，是否自动删除。如果为false即便目前没有队列绑定也会存在着。
        return new DirectExchange(COUPON_ORDER_RETRY_EXCHANGE, true, false);
    }

    @Bean
    public Queue couponOrderRetryQueue() {
        // 加TTl

        return QueueBuilder
                .durable(COUPON_ORDER_RETRY_QUEUE)
                .ttl(5000)
                .deadLetterExchange(COUPON_ORDER_EXCHANGE)
                .deadLetterRoutingKey(COUPON_ORDER_ROUTING_KEY)
                .build();
    }

    @Bean
    public Binding couponOrderRetryBinding() {
        return BindingBuilder.bind(couponOrderRetryQueue()).to(couponOrderRetryExchange()).with(COUPON_ORDER_RETRY_ROUTING_KEY);
    }

    // 死信队列
    @Bean
    public DirectExchange couponOrderDlxExchange() {
        // 第一个true，是否持久化
        // 第二个false，是否自动删除。如果为false即便目前没有队列绑定也会存在着。
        return new DirectExchange(COUPON_ORDER_DLX_EXCHANGE, true, false);
    }

    @Bean
    public Queue couponOrderDlqQueue() {
        return QueueBuilder.durable(COUPON_ORDER_DLX_QUEUE).build();
    }

    @Bean
    public Binding couponOrderDlqBinding() {
        return BindingBuilder.bind(couponOrderDlqQueue()).to(couponOrderDlxExchange()).with(COUPON_ORDER_DLX_ROUTING_KEY);
    }
}
