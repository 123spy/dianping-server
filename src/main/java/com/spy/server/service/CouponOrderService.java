package com.spy.server.service;

import com.spy.server.model.domain.CouponOrder;
import com.baomidou.mybatisplus.extension.service.IService;
import com.spy.server.model.dto.couponorder.CouponOrderCancelRequest;
import com.spy.server.model.dto.couponorder.CouponOrderPayRequest;
import com.spy.server.model.dto.couponorder.CouponOrderSubmitRequest;
import jakarta.servlet.http.HttpServletRequest;

/**
* @author OUC
* @description 针对表【coupon_order(优惠券订单表)】的数据库操作Service
 * @createDate 2026-03-22 13:49:38
*/
public interface CouponOrderService extends IService<CouponOrder> {

    Long submitCouponOrder(CouponOrderSubmitRequest couponOrderSubmitRequest, HttpServletRequest request);

    Boolean payCouponOrder(CouponOrderPayRequest couponOrderPayRequest, HttpServletRequest request);

    Boolean cancelCouponOrder(CouponOrderCancelRequest couponOrderCancelRequest, HttpServletRequest request);
}
