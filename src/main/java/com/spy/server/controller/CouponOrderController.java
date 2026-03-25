package com.spy.server.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.spy.server.annotation.AuthCheck;
import com.spy.server.common.BaseResponse;
import com.spy.server.common.DeleteRequest;
import com.spy.server.common.ErrorCode;
import com.spy.server.constant.UserConstant;
import com.spy.server.exception.BusinessException;
import com.spy.server.model.domain.CouponOrder;
import com.spy.server.model.domain.User;
import com.spy.server.model.dto.couponorder.CouponOrderAddRequest;
import com.spy.server.model.dto.couponorder.CouponOrderCancelRequest;
import com.spy.server.model.dto.couponorder.CouponOrderPayRequest;
import com.spy.server.model.dto.couponorder.CouponOrderQueryRequest;
import com.spy.server.model.dto.couponorder.CouponOrderSubmitRequest;
import com.spy.server.model.dto.couponorder.CouponOrderUpdateRequest;
import com.spy.server.model.vo.CouponOrderVO;
import com.spy.server.service.CouponOrderService;
import com.spy.server.service.UserService;
import com.spy.server.utils.ResultUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/couponOrder")
@Slf4j
public class CouponOrderController {

    @Resource
    private CouponOrderService couponOrderService;

    @Resource
    private UserService userService;

    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addCouponOrder(@RequestBody CouponOrderAddRequest couponOrderAddRequest) {
        if (couponOrderAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long id = couponOrderService.addCouponOrder(couponOrderAddRequest);
        return ResultUtil.success(id);
    }

    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteCouponOrder(@RequestBody DeleteRequest deleteRequest) {
        if (deleteRequest == null || deleteRequest.getId() == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Boolean result = couponOrderService.adminDeleteCouponOrder(deleteRequest.getId());
        return ResultUtil.success(result);
    }

    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateCouponOrder(@RequestBody CouponOrderUpdateRequest couponOrderUpdateRequest) {
        if (couponOrderUpdateRequest == null || couponOrderUpdateRequest.getId() == null || couponOrderUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        CouponOrder oldCouponOrder = couponOrderService.getById(couponOrderUpdateRequest.getId());
        if (oldCouponOrder == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        Boolean result = couponOrderService.updateCouponOrder(couponOrderUpdateRequest);
        return ResultUtil.success(result);
    }

    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<CouponOrder> getCouponOrderById(long id) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        CouponOrder couponOrder = couponOrderService.getById(id);
        if (couponOrder == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtil.success(couponOrder);
    }

    @GetMapping("/get/vo")
    public BaseResponse<CouponOrderVO> getCouponOrderVOById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        CouponOrder couponOrder = couponOrderService.getById(id);
        if (couponOrder == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        if (!isAdmin(loginUser) && !loginUser.getId().equals(couponOrder.getUserId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        return ResultUtil.success(couponOrderService.getCouponOrderVO(couponOrder, request));
    }

    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<CouponOrder>> listCouponOrderByPage(@RequestBody CouponOrderQueryRequest couponOrderQueryRequest) {
        if (couponOrderQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        int current = couponOrderQueryRequest.getCurrent();
        int pageSize = couponOrderQueryRequest.getPageSize();
        Page<CouponOrder> couponOrderPage = couponOrderService.page(
                new Page<>(current, pageSize),
                couponOrderService.getQueryWrapper(couponOrderQueryRequest)
        );
        return ResultUtil.success(couponOrderPage);
    }

    @PostMapping("/list/page/vo")
    public BaseResponse<Page<CouponOrderVO>> listCouponOrderVOByPage(@RequestBody CouponOrderQueryRequest couponOrderQueryRequest,
                                                                     HttpServletRequest request) {
        if (couponOrderQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        if (!isAdmin(loginUser)) {
            couponOrderQueryRequest.setUserId(loginUser.getId());
        }
        Page<CouponOrderVO> couponOrderVOPage = couponOrderService.listCouponOrderVOByPage(couponOrderQueryRequest, request);
        return ResultUtil.success(couponOrderVOPage);
    }

    @PostMapping("/submit")
    public BaseResponse<Long> submitCouponOrder(@RequestBody CouponOrderSubmitRequest couponOrderSubmitRequest,
                                                HttpServletRequest request) {
        if (couponOrderSubmitRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        userService.getLoginUser(request);
        if (couponOrderSubmitRequest.getCouponId() == null || couponOrderSubmitRequest.getCouponId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "优惠券参数非法");
        }
        Long id = couponOrderService.submitCouponOrder(couponOrderSubmitRequest, request);
        return ResultUtil.success(id);
    }

    @PostMapping("/pay")
    public BaseResponse<Boolean> payCouponOrder(@RequestBody CouponOrderPayRequest couponOrderPayRequest,
                                                HttpServletRequest request) {
        if (couponOrderPayRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        userService.getLoginUser(request);
        if (couponOrderPayRequest.getId() == null || couponOrderPayRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "订单参数非法");
        }
        Boolean result = couponOrderService.payCouponOrder(couponOrderPayRequest, request);
        return ResultUtil.success(result);
    }

    @PostMapping("/cancel")
    public BaseResponse<Boolean> cancelCouponOrder(@RequestBody CouponOrderCancelRequest couponOrderCancelRequest,
                                                   HttpServletRequest request) {
        if (couponOrderCancelRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        userService.getLoginUser(request);
        if (couponOrderCancelRequest.getId() == null || couponOrderCancelRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "订单参数非法");
        }
        Boolean result = couponOrderService.cancelCouponOrder(couponOrderCancelRequest, request);
        return ResultUtil.success(result);
    }

    private boolean isAdmin(User loginUser) {
        return loginUser != null && UserConstant.ADMIN_ROLE.equals(loginUser.getUserRole());
    }
}
