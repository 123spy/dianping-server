package com.spy.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.spy.server.common.ErrorCode;
import com.spy.server.constant.CommonConstant;
import com.spy.server.exception.BusinessException;
import com.spy.server.mapper.UserCouponMapper;
import com.spy.server.model.domain.Coupon;
import com.spy.server.model.domain.CouponOrder;
import com.spy.server.model.domain.Shop;
import com.spy.server.model.domain.User;
import com.spy.server.model.domain.UserCoupon;
import com.spy.server.model.dto.usercoupon.UserCouponAddRequest;
import com.spy.server.model.dto.usercoupon.UserCouponQueryRequest;
import com.spy.server.model.dto.usercoupon.UserCouponUpdateRequest;
import com.spy.server.model.vo.CouponVO;
import com.spy.server.model.vo.ShopVO;
import com.spy.server.model.vo.UserCouponVO;
import com.spy.server.model.vo.UserVO;
import com.spy.server.service.CouponOrderService;
import com.spy.server.service.CouponService;
import com.spy.server.service.ShopService;
import com.spy.server.service.UserCouponService;
import com.spy.server.service.UserService;
import com.spy.server.utils.SqlUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserCouponServiceImpl extends ServiceImpl<UserCouponMapper, UserCoupon>
        implements UserCouponService {

    @Resource
    private UserService userService;

    @Resource
    private CouponService couponService;

    @Lazy
    @Resource
    private ShopService shopService;

    @Lazy
    @Resource
    private CouponOrderService couponOrderService;

    @Override
    public UserCouponVO getUserCouponVO(UserCoupon userCoupon, HttpServletRequest request) {
        if (userCoupon == null) {
            return null;
        }

        UserCouponVO userCouponVO = new UserCouponVO();
        BeanUtils.copyProperties(userCoupon, userCouponVO);

        User user = userService.getById(userCoupon.getUserId());
        if (user != null) {
            userCouponVO.setUserVO(userService.getUserVO(user));
        }

        Coupon coupon = couponService.getById(userCoupon.getCouponId());
        if (coupon != null) {
            CouponVO couponVO = couponService.getCouponVO(coupon);
            userCouponVO.setCouponVO(couponVO);

            Shop shop = shopService.getById(coupon.getShopId());
            if (shop != null) {
              userCouponVO.setShopVO(shopService.getShopVO(shop, request));
            }
        }

        CouponOrder couponOrder = couponOrderService.getById(userCoupon.getOrderId());
        if (couponOrder != null) {
            userCouponVO.setOrderNo(couponOrder.getOrderNo());
        }

        return userCouponVO;
    }

    @Override
    public List<UserCouponVO> getUserCouponVO(List<UserCoupon> records, HttpServletRequest request) {
        if (CollectionUtils.isEmpty(records)) {
            return new ArrayList<>();
        }

        Set<Long> userIdSet = records.stream()
                .map(UserCoupon::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Set<Long> couponIdSet = records.stream()
                .map(UserCoupon::getCouponId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Set<Long> orderIdSet = records.stream()
                .map(UserCoupon::getOrderId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, UserVO> userVOMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(userIdSet)) {
            userVOMap = userService.listByIds(userIdSet).stream().collect(Collectors.toMap(
                    User::getId,
                    userService::getUserVO,
                    (a, b) -> a
            ));
        }

        Map<Long, Coupon> couponMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(couponIdSet)) {
            couponMap = couponService.listByIds(couponIdSet).stream().collect(Collectors.toMap(
                    Coupon::getId,
                    coupon -> coupon,
                    (a, b) -> a
            ));
        }

        Set<Long> shopIdSet = couponMap.values().stream()
                .map(Coupon::getShopId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, ShopVO> shopVOMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(shopIdSet)) {
            shopVOMap = shopService.listByIds(shopIdSet).stream().collect(Collectors.toMap(
                    Shop::getId,
                    shop -> shopService.getShopVO(shop, request),
                    (a, b) -> a
            ));
        }

        Map<Long, CouponVO> couponVOMap = new HashMap<>();
        for (Coupon coupon : couponMap.values()) {
            couponVOMap.put(coupon.getId(), couponService.getCouponVO(coupon));
        }

        Map<Long, CouponOrder> orderMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(orderIdSet)) {
            orderMap = couponOrderService.listByIds(orderIdSet).stream().collect(Collectors.toMap(
                    CouponOrder::getId,
                    order -> order,
                    (a, b) -> a
            ));
        }

        Map<Long, UserVO> finalUserVOMap = userVOMap;
        Map<Long, CouponVO> finalCouponVOMap = couponVOMap;
        Map<Long, ShopVO> finalShopVOMap = shopVOMap;
        Map<Long, CouponOrder> finalOrderMap = orderMap;
        Map<Long, Coupon> finalCouponMap = couponMap;

        return records.stream().map(userCoupon -> {
            UserCouponVO userCouponVO = new UserCouponVO();
            BeanUtils.copyProperties(userCoupon, userCouponVO);
            userCouponVO.setUserVO(finalUserVOMap.get(userCoupon.getUserId()));
            userCouponVO.setCouponVO(finalCouponVOMap.get(userCoupon.getCouponId()));

            Coupon coupon = finalCouponMap.get(userCoupon.getCouponId());
            if (coupon != null) {
                userCouponVO.setShopVO(finalShopVOMap.get(coupon.getShopId()));
            }

            CouponOrder couponOrder = finalOrderMap.get(userCoupon.getOrderId());
            if (couponOrder != null) {
                userCouponVO.setOrderNo(couponOrder.getOrderNo());
            }
            return userCouponVO;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addUserCoupon(UserCouponAddRequest userCouponAddRequest) {
        if (userCouponAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        UserCoupon userCoupon = buildAndValidateUserCoupon(userCouponAddRequest, null);
        boolean saved = this.save(userCoupon);
        if (!saved) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "新增失败");
        }
        return userCoupon.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateUserCoupon(UserCouponUpdateRequest userCouponUpdateRequest) {
        if (userCouponUpdateRequest == null || userCouponUpdateRequest.getId() == null || userCouponUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        UserCoupon oldUserCoupon = this.getById(userCouponUpdateRequest.getId());
        if (oldUserCoupon == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户优惠券不存在");
        }

        UserCouponAddRequest payload = new UserCouponAddRequest();
        BeanUtils.copyProperties(userCouponUpdateRequest, payload);
        UserCoupon userCoupon = buildAndValidateUserCoupon(payload, oldUserCoupon);
        boolean updated = this.updateById(userCoupon);
        if (!updated) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "更新失败");
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean adminDeleteUserCoupon(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        UserCoupon userCoupon = this.getById(id);
        if (userCoupon == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户优惠券不存在");
        }
        boolean removed = this.removeById(id);
        if (!removed) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "删除失败");
        }
        return true;
    }

    @Override
    public Wrapper<UserCoupon> getQueryWrapper(UserCouponQueryRequest userCouponQueryRequest) {
        if (userCouponQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        QueryWrapper<UserCoupon> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(userCouponQueryRequest.getId() != null, "id", userCouponQueryRequest.getId());
        queryWrapper.eq(userCouponQueryRequest.getUserId() != null, "userId", userCouponQueryRequest.getUserId());
        queryWrapper.eq(userCouponQueryRequest.getCouponId() != null, "couponId", userCouponQueryRequest.getCouponId());
        queryWrapper.eq(userCouponQueryRequest.getOrderId() != null, "orderId", userCouponQueryRequest.getOrderId());
        queryWrapper.like(StringUtils.isNotBlank(userCouponQueryRequest.getCode()), "code", userCouponQueryRequest.getCode());
        queryWrapper.eq(userCouponQueryRequest.getStatus() != null, "status", userCouponQueryRequest.getStatus());
        queryWrapper.ge(userCouponQueryRequest.getObtainTime() != null, "obtainTime", userCouponQueryRequest.getObtainTime());
        queryWrapper.ge(userCouponQueryRequest.getUseTime() != null, "useTime", userCouponQueryRequest.getUseTime());
        queryWrapper.ge(userCouponQueryRequest.getExpireTime() != null, "expireTime", userCouponQueryRequest.getExpireTime());
        queryWrapper.ge(userCouponQueryRequest.getCreateTime() != null, "createTime", userCouponQueryRequest.getCreateTime());
        queryWrapper.ge(userCouponQueryRequest.getUpdateTime() != null, "updateTime", userCouponQueryRequest.getUpdateTime());

        if (StringUtils.isNotBlank(userCouponQueryRequest.getSearchText())) {
            queryWrapper.and(wrapper -> wrapper.like("code", userCouponQueryRequest.getSearchText()));
        }

        queryWrapper.eq("isDelete", 0);
        boolean asc = CommonConstant.SORT_ORDER_ASC.equals(userCouponQueryRequest.getSortOrder());
        queryWrapper.orderBy(SqlUtil.validSortField(userCouponQueryRequest.getSortField()), asc, userCouponQueryRequest.getSortField());
        return queryWrapper;
    }

    @Override
    public Page<UserCouponVO> listUserCouponVOByPage(UserCouponQueryRequest userCouponQueryRequest, HttpServletRequest request) {
        int current = userCouponQueryRequest.getCurrent();
        int pageSize = userCouponQueryRequest.getPageSize();

        Page<UserCoupon> userCouponPage = this.page(new Page<>(current, pageSize), this.getQueryWrapper(userCouponQueryRequest));
        Page<UserCouponVO> userCouponVOPage = new Page<>(current, pageSize, userCouponPage.getTotal());
        userCouponVOPage.setRecords(this.getUserCouponVO(userCouponPage.getRecords(), request));
        return userCouponVOPage;
    }

    private UserCoupon buildAndValidateUserCoupon(UserCouponAddRequest userCouponAddRequest, UserCoupon oldUserCoupon) {
        Long userId = userCouponAddRequest.getUserId();
        Long couponId = userCouponAddRequest.getCouponId();
        Long orderId = userCouponAddRequest.getOrderId();

        if (userId == null || userId <= 0 || couponId == null || couponId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数错误");
        }
        if (userService.getById(userId) == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在");
        }
        Coupon coupon = couponService.getById(couponId);
        if (coupon == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "优惠券不存在");
        }
        if (orderId != null && orderId > 0 && couponOrderService.getById(orderId) == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "订单不存在");
        }

        Integer status = userCouponAddRequest.getStatus();
        if (status != null && (status < 0 || status > 3)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "状态非法");
        }

        String code = userCouponAddRequest.getCode();
        if (StringUtils.isBlank(code)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "券码不能为空");
        }

        QueryWrapper<UserCoupon> existsWrapper = new QueryWrapper<>();
        existsWrapper.eq("code", code).eq("isDelete", 0);
        if (oldUserCoupon != null) {
            existsWrapper.ne("id", oldUserCoupon.getId());
        }
        UserCoupon duplicate = this.getOne(existsWrapper);
        if (duplicate != null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "券码已存在");
        }

        UserCoupon userCoupon = new UserCoupon();
        BeanUtils.copyProperties(userCouponAddRequest, userCoupon);
        if (oldUserCoupon != null) {
            userCoupon.setId(oldUserCoupon.getId());
        }
        return userCoupon;
    }
}
