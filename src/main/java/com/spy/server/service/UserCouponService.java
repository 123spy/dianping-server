package com.spy.server.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.spy.server.model.domain.UserCoupon;
import com.spy.server.model.dto.usercoupon.UserCouponAddRequest;
import com.spy.server.model.dto.usercoupon.UserCouponQueryRequest;
import com.spy.server.model.dto.usercoupon.UserCouponUpdateRequest;
import com.spy.server.model.vo.UserCouponVO;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
* @author OUC
* @description 针对表【user_coupon(用户拥有的优惠券表)】的数据库操作Service
 * @createDate 2026-03-22 13:49:50
*/
public interface UserCouponService extends IService<UserCoupon> {

    UserCouponVO getUserCouponVO(UserCoupon userCoupon, HttpServletRequest request);

    List<UserCouponVO> getUserCouponVO(List<UserCoupon> records, HttpServletRequest request);

    Long addUserCoupon(UserCouponAddRequest userCouponAddRequest);

    Boolean updateUserCoupon(UserCouponUpdateRequest userCouponUpdateRequest);

    Boolean adminDeleteUserCoupon(Long id);

    Wrapper<UserCoupon> getQueryWrapper(UserCouponQueryRequest userCouponQueryRequest);

    Page<UserCouponVO> listUserCouponVOByPage(UserCouponQueryRequest userCouponQueryRequest, HttpServletRequest request);
}
