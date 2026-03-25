package com.spy.server.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.spy.server.model.domain.CouponOrder;
import com.spy.server.model.dto.couponorder.CouponOrderAddRequest;
import com.spy.server.model.dto.couponorder.CouponOrderCancelRequest;
import com.spy.server.model.dto.couponorder.CouponOrderPayRequest;
import com.spy.server.model.dto.couponorder.CouponOrderQueryRequest;
import com.spy.server.model.dto.couponorder.CouponOrderSubmitRequest;
import com.spy.server.model.dto.couponorder.CouponOrderUpdateRequest;
import com.spy.server.model.vo.CouponOrderVO;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
* @author OUC
* @description 针对表【coupon_order(优惠券订单表)】的数据库操作Service
 * @createDate 2026-03-22 13:49:38
*/
public interface CouponOrderService extends IService<CouponOrder> {

    CouponOrderVO getCouponOrderVO(CouponOrder couponOrder, HttpServletRequest request);

    List<CouponOrderVO> getCouponOrderVO(List<CouponOrder> records, HttpServletRequest request);

    Long addCouponOrder(CouponOrderAddRequest couponOrderAddRequest);

    Boolean updateCouponOrder(CouponOrderUpdateRequest couponOrderUpdateRequest);

    Boolean adminDeleteCouponOrder(Long id);

    Wrapper<CouponOrder> getQueryWrapper(CouponOrderQueryRequest couponOrderQueryRequest);

    Page<CouponOrderVO> listCouponOrderVOByPage(CouponOrderQueryRequest couponOrderQueryRequest, HttpServletRequest request);

    Long submitCouponOrder(CouponOrderSubmitRequest couponOrderSubmitRequest, HttpServletRequest request);

    Boolean payCouponOrder(CouponOrderPayRequest couponOrderPayRequest, HttpServletRequest request);

    Boolean cancelCouponOrder(CouponOrderCancelRequest couponOrderCancelRequest, HttpServletRequest request);
}
