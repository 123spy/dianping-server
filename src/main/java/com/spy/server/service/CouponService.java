package com.spy.server.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.spy.server.model.domain.Coupon;
import com.baomidou.mybatisplus.extension.service.IService;
import com.spy.server.model.dto.coupon.CouponAddRequest;
import com.spy.server.model.dto.coupon.CouponQueryRequest;
import com.spy.server.model.dto.coupon.CouponUpdateRequest;
import com.spy.server.model.vo.CouponVO;

import java.util.List;

/**
* @author OUC
* @description 针对表【coupon(优惠券表（券模板）)】的数据库操作Service
 * @createDate 2026-03-22 13:49:36
*/
public interface CouponService extends IService<Coupon> {

    Long addCoupon(CouponAddRequest couponAddRequest);

    Boolean updateCoupon(CouponUpdateRequest couponUpdateRequest);

    CouponVO getCouponVO(Coupon coupon);

    Wrapper<Coupon> getQueryWrapper(CouponQueryRequest couponQueryRequest);

    Page<CouponVO> listCouponVOByPage(CouponQueryRequest couponQueryRequest);

    List<CouponVO> getCouponVO(List<Coupon> records);
}
