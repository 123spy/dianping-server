package com.spy.server.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.spy.server.common.BaseResponse;
import com.spy.server.common.ErrorCode;
import com.spy.server.exception.BusinessException;
import com.spy.server.model.domain.CouponOrder;
import com.spy.server.model.domain.User;
import com.spy.server.model.dto.couponorder.CouponOrderCancelRequest;
import com.spy.server.model.dto.couponorder.CouponOrderPayRequest;
import com.spy.server.model.dto.couponorder.CouponOrderSubmitRequest;
import com.spy.server.service.CouponOrderService;
import com.spy.server.service.UserService;
import com.spy.server.utils.ResultUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/couponOrder")
@Slf4j
public class CouponOrderController {

    @Resource
    private CouponOrderService couponOrderService;

    @Resource
    private UserService userService;

    // 这里是用户提交订单的地方，也就是说，目前只是创建一个订单，但是还没有支付任何金钱
    @PostMapping("/submit")
    public BaseResponse<Long> submitCouponOrder(@RequestBody CouponOrderSubmitRequest couponOrderSubmitRequest, HttpServletRequest request) {
        // 校验
        if (couponOrderSubmitRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        if(couponOrderSubmitRequest.getCouponId() == null || couponOrderSubmitRequest.getCouponId() < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "优惠券参数非法");
        }
        Long id = couponOrderService.submitCouponOrder(couponOrderSubmitRequest, request);
        return ResultUtil.success(id);
    }

    // 这里是用户支付的地方
    @PostMapping("/pay")
    public BaseResponse<Boolean> payCouponOrder(@RequestBody CouponOrderPayRequest couponOrderPayRequest, HttpServletRequest request) {
        // 校验
        if (couponOrderPayRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        if(couponOrderPayRequest.getId() == null || couponOrderPayRequest.getId() < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "优惠券参数非法");
        }
        Boolean result = couponOrderService.payCouponOrder(couponOrderPayRequest, request);
        return ResultUtil.success(result);
    }

    // 这里是用户取消的地方
    @PostMapping("/cancel")
    public BaseResponse<Boolean> cancelCouponOrder(@RequestBody CouponOrderCancelRequest couponOrderCancelRequest, HttpServletRequest request) {
        // 校验
        if (couponOrderCancelRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        if(couponOrderCancelRequest.getId() == null || couponOrderCancelRequest.getId() < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "优惠券参数非法");
        }
        if(couponOrderCancelRequest.getUserCouponId() == null || couponOrderCancelRequest.getUserCouponId() < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "优惠券参数非法");
        }
        Boolean result = couponOrderService.cancelCouponOrder(couponOrderCancelRequest, request);
        return ResultUtil.success(result);
    }
}
