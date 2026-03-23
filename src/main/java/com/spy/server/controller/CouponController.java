package com.spy.server.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.spy.server.annotation.AuthCheck;
import com.spy.server.common.BaseResponse;
import com.spy.server.common.DeleteRequest;
import com.spy.server.common.ErrorCode;
import com.spy.server.constant.UserConstant;
import com.spy.server.exception.BusinessException;
import com.spy.server.model.domain.Coupon;
import com.spy.server.model.dto.coupon.*;
import com.spy.server.model.vo.CouponVO;
import com.spy.server.service.CouponService;
import com.spy.server.utils.ResultUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/coupon")
@Slf4j
public class CouponController {

    @Resource
    private CouponService couponService;

    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addCoupon(@RequestBody CouponAddRequest couponAddRequest) {
        // 校验
        if (couponAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long id = couponService.addCoupon(couponAddRequest);
        return ResultUtil.success(id);
    }

    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteCoupon(@RequestBody DeleteRequest deleteRequest) {
        // 1. 校验
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 2. 删除
        boolean result = couponService.removeById(deleteRequest.getId());
        if (!result) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return ResultUtil.success(true);
    }

    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateCoupon(@RequestBody CouponUpdateRequest couponUpdateRequest, HttpServletRequest request) {
        if (couponUpdateRequest == null || couponUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Coupon oldCoupon = couponService.getById(couponUpdateRequest.getId());
        if (oldCoupon == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Boolean result = couponService.updateCoupon(couponUpdateRequest);

        return ResultUtil.success(result);
    }

    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Coupon> getCouponById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Coupon coupon = couponService.getById(id);
        if (coupon == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtil.success(coupon);
    }

    @GetMapping("/get/vo")
    public BaseResponse<CouponVO> getCouponVOById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Coupon coupon = couponService.getById(id);
        if (coupon == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtil.success(couponService.getCouponVO(coupon));
    }

    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Coupon>> listCouponByPage(@RequestBody CouponQueryRequest couponQueryRequest, HttpServletRequest request) {
        if (couponQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        int current = couponQueryRequest.getCurrent();
        int pageSize = couponQueryRequest.getPageSize();
        Page<Coupon> couponPage = couponService.page(new Page<>(current, pageSize), couponService.getQueryWrapper(couponQueryRequest));
        return ResultUtil.success(couponPage);
    }

    @PostMapping("/list/page/vo")
    public BaseResponse<Page<CouponVO>> listCouponVOByPage(@RequestBody CouponQueryRequest couponQueryRequest, HttpServletRequest request) {
        if (couponQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Page<CouponVO> couponVOPage = couponService.listCouponVOByPage(couponQueryRequest);
        return ResultUtil.success(couponVOPage);
    }
}
