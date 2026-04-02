package com.spy.server.mq.consumer;

import cn.hutool.core.lang.UUID;
import com.rabbitmq.client.Channel;
import com.spy.server.common.ErrorCode;
import com.spy.server.exception.BusinessException;
import com.spy.server.model.domain.Coupon;
import com.spy.server.model.domain.CouponOrder;
import com.spy.server.model.domain.Shop;
import com.spy.server.mq.model.CouponOrderEvent;
import com.spy.server.mq.model.ShopRatingChangedEvent;
import com.spy.server.mq.producer.CouponOrderEventProducer;
import com.spy.server.mq.service.MqConsumeRecordService;
import com.spy.server.service.CouponOrderService;
import com.spy.server.service.CouponService;
import com.spy.server.service.ShopService;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class CouponOrderConsumer {

    @Resource
    private CouponOrderService couponOrderService;

    @Resource
    private CouponService couponService;

    @Resource
    private MqConsumeRecordService consumeRecordService;

    @Resource
    private CouponOrderEventProducer couponOrderEventProducer;

    // event是处理的业务数据，message是rabbitmq附加的消息数据
    @RabbitListener(queues = "coupon.order.queue")
    public void handle(CouponOrderEvent event, Message message, Channel channel) throws Exception {

        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        String msgId = message.getMessageProperties().getMessageId();
        String bizType = "COUPON_ORDER";
        log.info("订单创建消息到达消费者, deliveryTag={}, msgId={}", deliveryTag, msgId);
        Long couponId = event.getCouponId();
        if (couponId == null || couponId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数异常");
        }
        int retryCount = event.getRetryCount();
        if (retryCount > 3) {
            // 丢给延迟队列
            channel.basicNack(deliveryTag, false, false);
            return;
        }
        // 反之，还不需要，就正常进行就可以
        synchronized (couponId.toString().intern()) {

            try {
                // 1. 判空保护
                if (msgId == null || msgId.isBlank()) {
                    log.error("messageId为空，拒绝消费");
                    channel.basicNack(deliveryTag, false, true);
                    return;
                }

                boolean firstConsume = consumeRecordService.tryInsert(msgId, "COUPON_ORDER");
                if (!firstConsume) {
                    log.warn("重复消息，直接ack，megId={}", msgId);
                    // 说明这条消息已经处理过
                    channel.basicAck(deliveryTag, false);
                    return;
                }
                Long userId = event.getUserId();
                LocalDateTime eventTime = event.getEventTime();

                // 先处理mysql中的stock数据
                Coupon coupon = couponService.getById(couponId);
                Coupon updateCoupon = new Coupon();
                BeanUtils.copyProperties(coupon, updateCoupon);
                updateCoupon.setStock(coupon.getStock() - 1);
                boolean updateRes = couponService.updateById(updateCoupon);
                if (!updateRes) {
                    throw new BusinessException(ErrorCode.PARAMS_ERROR, "库存更新失败");
                }

                // 将数据写入mysql中
                CouponOrder couponOrder = new CouponOrder();
                couponOrder.setOrderNo(UUID.randomUUID().toString());

                couponOrder.setUserId(userId);
                couponOrder.setShopId(coupon.getShopId());
                couponOrder.setCouponId(couponId);

                couponOrder.setTotalAmount(coupon.getDiscountPrice());
                couponOrder.setPayType(null);
                couponOrder.setPayAmount(BigDecimal.ZERO);
                couponOrder.setStatus(0);
                couponOrder.setPayTime(null);
                couponOrder.setCancelTime(null);
                couponOrder.setFinishTime(null);

                boolean result = couponOrderService.save(couponOrder);
                if (!result) {
                    throw new BusinessException(ErrorCode.OPERATION_ERROR, "保存失败");
                }

                consumeRecordService.markSuccess(msgId, bizType);
                // 第二个false代表的是，是否批量确认在这个消息之前的东西。
                channel.basicAck(deliveryTag, false);
                log.info("订单创建消息成功, couponId={}, userId={}", couponId, userId);
            } catch (Exception e) {
                log.error("消费失败, msgId={}", msgId, e);

                // 标记失败
                if (msgId != null && !msgId.isBlank()) {
                    consumeRecordService.markFail(msgId, bizType);
                }

                // 构造一条 retryCount + 1 的新消息
                // 发到 retry exchange
                CouponOrderEvent newEvent = new CouponOrderEvent();
                BeanUtils.copyProperties(event, newEvent);
                newEvent.setRetryCount(retryCount + 1);
                couponOrderEventProducer.sendRetryQueue(newEvent);

                // 这里先按你的现有思路走死信/不重回队列
                // 第一个false代表的是，是否批量否认
                // 第二个false代表的是，是否重新放回队列
                channel.basicAck(deliveryTag, false);
            }
        }
    }
}
