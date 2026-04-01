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

    @RabbitListener(queues = "coupon.order.queue")
    public void handle(CouponOrderEvent event, Message message, Channel channel) throws Exception {

        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        log.info("订单创建消息到达消费者, deliveryTag={}", deliveryTag);
        try {
            Long couponId = event.getCouponId();
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

            CouponOrder order = couponOrderService.getById(couponId);
            couponOrder.setUserId(userId);
            couponOrder.setShopId(order.getShopId());
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

            channel.basicAck(deliveryTag, false);
            log.info("订单创建消息成功, couponId={}, userId={}", couponId, userId);
        } catch (Exception e) {
            log.error("订单创建消息失败", e);
            channel.basicNack(deliveryTag, false, false);
        }
    }
}
