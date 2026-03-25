package com.spy.server.service.impl;

import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.spy.server.common.ErrorCode;
import com.spy.server.constant.CommonConstant;
import com.spy.server.exception.BusinessException;
import com.spy.server.mapper.CouponOrderMapper;
import com.spy.server.model.domain.Coupon;
import com.spy.server.model.domain.CouponOrder;
import com.spy.server.model.domain.Shop;
import com.spy.server.model.domain.User;
import com.spy.server.model.domain.UserCoupon;
import com.spy.server.model.dto.couponorder.CouponOrderAddRequest;
import com.spy.server.model.dto.couponorder.CouponOrderCancelRequest;
import com.spy.server.model.dto.couponorder.CouponOrderPayRequest;
import com.spy.server.model.dto.couponorder.CouponOrderQueryRequest;
import com.spy.server.model.dto.couponorder.CouponOrderSubmitRequest;
import com.spy.server.model.dto.couponorder.CouponOrderUpdateRequest;
import com.spy.server.model.vo.CouponOrderVO;
import com.spy.server.model.vo.CouponVO;
import com.spy.server.model.vo.ShopVO;
import com.spy.server.model.vo.UserVO;
import com.spy.server.service.CouponOrderService;
import com.spy.server.service.CouponService;
import com.spy.server.service.ShopService;
import com.spy.server.service.UserCouponService;
import com.spy.server.service.UserService;
import com.spy.server.utils.SqlUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CouponOrderServiceImpl extends ServiceImpl<CouponOrderMapper, CouponOrder>
        implements CouponOrderService {

    @Resource
    private UserService userService;

    @Resource
    private CouponService couponService;

    @Resource
    private UserCouponService userCouponService;

    @Lazy
    @Resource
    private ShopService shopService;

    @Override
    public CouponOrderVO getCouponOrderVO(CouponOrder couponOrder, HttpServletRequest request) {
        if (couponOrder == null) {
            return null;
        }

        CouponOrderVO couponOrderVO = new CouponOrderVO();
        BeanUtils.copyProperties(couponOrder, couponOrderVO);

        User user = userService.getById(couponOrder.getUserId());
        if (user != null) {
            couponOrderVO.setUserVO(userService.getUserVO(user));
        }

        Shop shop = shopService.getById(couponOrder.getShopId());
        if (shop != null) {
            couponOrderVO.setShopVO(shopService.getShopVO(shop, request));
        }

        Coupon coupon = couponService.getById(couponOrder.getCouponId());
        if (coupon != null) {
            couponOrderVO.setCouponVO(couponService.getCouponVO(coupon));
        }

        QueryWrapper<UserCoupon> userCouponQueryWrapper = new QueryWrapper<>();
        userCouponQueryWrapper.eq("orderId", couponOrder.getId()).eq("isDelete", 0).last("limit 1");
        UserCoupon userCoupon = userCouponService.getOne(userCouponQueryWrapper);
        if (userCoupon != null) {
            couponOrderVO.setUserCouponId(userCoupon.getId());
        }

        return couponOrderVO;
    }

    @Override
    public List<CouponOrderVO> getCouponOrderVO(List<CouponOrder> records, HttpServletRequest request) {
        if (CollectionUtils.isEmpty(records)) {
            return new ArrayList<>();
        }

        Set<Long> userIdSet = records.stream()
                .map(CouponOrder::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Set<Long> shopIdSet = records.stream()
                .map(CouponOrder::getShopId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Set<Long> couponIdSet = records.stream()
                .map(CouponOrder::getCouponId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Set<Long> orderIdSet = records.stream()
                .map(CouponOrder::getId)
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

        Map<Long, ShopVO> shopVOMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(shopIdSet)) {
            shopVOMap = shopService.listByIds(shopIdSet).stream().collect(Collectors.toMap(
                    Shop::getId,
                    shop -> shopService.getShopVO(shop, request),
                    (a, b) -> a
            ));
        }

        Map<Long, CouponVO> couponVOMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(couponIdSet)) {
            couponVOMap = couponService.listByIds(couponIdSet).stream().collect(Collectors.toMap(
                    Coupon::getId,
                    coupon -> couponService.getCouponVO(coupon),
                    (a, b) -> a
            ));
        }

        Map<Long, Long> userCouponIdMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(orderIdSet)) {
            QueryWrapper<UserCoupon> queryWrapper = new QueryWrapper<>();
            queryWrapper.in("orderId", orderIdSet).eq("isDelete", 0);
            List<UserCoupon> userCouponList = userCouponService.list(queryWrapper);
            userCouponIdMap = userCouponList.stream().collect(Collectors.toMap(
                    UserCoupon::getOrderId,
                    UserCoupon::getId,
                    (a, b) -> a
            ));
        }

        Map<Long, UserVO> finalUserVOMap = userVOMap;
        Map<Long, ShopVO> finalShopVOMap = shopVOMap;
        Map<Long, CouponVO> finalCouponVOMap = couponVOMap;
        Map<Long, Long> finalUserCouponIdMap = userCouponIdMap;

        return records.stream().map(couponOrder -> {
            CouponOrderVO couponOrderVO = new CouponOrderVO();
            BeanUtils.copyProperties(couponOrder, couponOrderVO);
            couponOrderVO.setUserVO(finalUserVOMap.get(couponOrder.getUserId()));
            couponOrderVO.setShopVO(finalShopVOMap.get(couponOrder.getShopId()));
            couponOrderVO.setCouponVO(finalCouponVOMap.get(couponOrder.getCouponId()));
            couponOrderVO.setUserCouponId(finalUserCouponIdMap.get(couponOrder.getId()));
            return couponOrderVO;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addCouponOrder(CouponOrderAddRequest couponOrderAddRequest) {
        if (couponOrderAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        CouponOrder couponOrder = buildAndValidateCouponOrder(couponOrderAddRequest, null);
        boolean saved = this.save(couponOrder);
        if (!saved) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "新增失败");
        }
        return couponOrder.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateCouponOrder(CouponOrderUpdateRequest couponOrderUpdateRequest) {
        if (couponOrderUpdateRequest == null || couponOrderUpdateRequest.getId() == null || couponOrderUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        CouponOrder oldCouponOrder = this.getById(couponOrderUpdateRequest.getId());
        if (oldCouponOrder == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "订单不存在");
        }

        CouponOrderAddRequest payload = new CouponOrderAddRequest();
        BeanUtils.copyProperties(couponOrderUpdateRequest, payload);
        CouponOrder couponOrder = buildAndValidateCouponOrder(payload, oldCouponOrder);
        boolean updated = this.updateById(couponOrder);
        if (!updated) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "更新失败");
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean adminDeleteCouponOrder(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        CouponOrder couponOrder = this.getById(id);
        if (couponOrder == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "订单不存在");
        }
        boolean removed = this.removeById(id);
        if (!removed) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "删除失败");
        }
        return true;
    }

    @Override
    public Wrapper<CouponOrder> getQueryWrapper(CouponOrderQueryRequest couponOrderQueryRequest) {
        if (couponOrderQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        QueryWrapper<CouponOrder> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(couponOrderQueryRequest.getId() != null, "id", couponOrderQueryRequest.getId());
        queryWrapper.eq(couponOrderQueryRequest.getUserId() != null, "userId", couponOrderQueryRequest.getUserId());
        queryWrapper.eq(couponOrderQueryRequest.getShopId() != null, "shopId", couponOrderQueryRequest.getShopId());
        queryWrapper.eq(couponOrderQueryRequest.getCouponId() != null, "couponId", couponOrderQueryRequest.getCouponId());
        queryWrapper.like(StringUtils.isNotBlank(couponOrderQueryRequest.getOrderNo()), "orderNo", couponOrderQueryRequest.getOrderNo());
        queryWrapper.eq(couponOrderQueryRequest.getStatus() != null, "status", couponOrderQueryRequest.getStatus());
        queryWrapper.like(StringUtils.isNotBlank(couponOrderQueryRequest.getPayType()), "payType", couponOrderQueryRequest.getPayType());
        queryWrapper.ge(couponOrderQueryRequest.getPayTime() != null, "payTime", couponOrderQueryRequest.getPayTime());
        queryWrapper.ge(couponOrderQueryRequest.getCancelTime() != null, "cancelTime", couponOrderQueryRequest.getCancelTime());
        queryWrapper.ge(couponOrderQueryRequest.getFinishTime() != null, "finishTime", couponOrderQueryRequest.getFinishTime());
        queryWrapper.ge(couponOrderQueryRequest.getCreateTime() != null, "createTime", couponOrderQueryRequest.getCreateTime());
        queryWrapper.ge(couponOrderQueryRequest.getUpdateTime() != null, "updateTime", couponOrderQueryRequest.getUpdateTime());

        if (StringUtils.isNotBlank(couponOrderQueryRequest.getSearchText())) {
            queryWrapper.and(wrapper -> wrapper.like("orderNo", couponOrderQueryRequest.getSearchText()));
        }

        queryWrapper.eq("isDelete", 0);
        boolean asc = CommonConstant.SORT_ORDER_ASC.equals(couponOrderQueryRequest.getSortOrder());
        queryWrapper.orderBy(SqlUtil.validSortField(couponOrderQueryRequest.getSortField()), asc, couponOrderQueryRequest.getSortField());
        return queryWrapper;
    }

    @Override
    public Page<CouponOrderVO> listCouponOrderVOByPage(CouponOrderQueryRequest couponOrderQueryRequest, HttpServletRequest request) {
        int current = couponOrderQueryRequest.getCurrent();
        int pageSize = couponOrderQueryRequest.getPageSize();
        Page<CouponOrder> couponOrderPage = this.page(new Page<>(current, pageSize), this.getQueryWrapper(couponOrderQueryRequest));
        Page<CouponOrderVO> couponOrderVOPage = new Page<>(current, pageSize, couponOrderPage.getTotal());
        couponOrderVOPage.setRecords(this.getCouponOrderVO(couponOrderPage.getRecords(), request));
        return couponOrderVOPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long submitCouponOrder(CouponOrderSubmitRequest couponOrderSubmitRequest, HttpServletRequest request) {
        synchronized (couponOrderSubmitRequest.getCouponId().toString().intern()) {
            Long shopId = couponOrderSubmitRequest.getShopId();
            Long couponId = couponOrderSubmitRequest.getCouponId();
            Coupon coupon = couponService.getById(couponId);
            if (coupon == null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "优惠券不存在");
            }
            Integer stock = coupon.getStock();
            if (stock <= 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "已经卖完");
            }

            coupon.setStock(coupon.getStock() - 1);
            couponService.updateById(coupon);

            CouponOrder couponOrder = new CouponOrder();
            couponOrder.setOrderNo(UUID.randomUUID().toString());

            User loginUser = userService.getLoginUser(request);
            couponOrder.setUserId(loginUser.getId());
            couponOrder.setShopId(shopId);
            couponOrder.setCouponId(couponId);
            couponOrder.setTotalAmount(coupon.getDiscountPrice());
            couponOrder.setPayType(null);
            couponOrder.setPayAmount(BigDecimal.ZERO);
            couponOrder.setStatus(0);
            couponOrder.setPayTime(null);
            couponOrder.setCancelTime(null);
            couponOrder.setFinishTime(null);

            boolean result = this.save(couponOrder);
            if (!result) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "保存失败");
            }
            return couponOrder.getId();
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean payCouponOrder(CouponOrderPayRequest couponOrderPayRequest, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        Long id = couponOrderPayRequest.getId();
        CouponOrder order = this.getById(id);
        if (order == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "订单不存在");
        }

        if (ObjectUtil.notEqual(order.getUserId(), loginUser.getId())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "非本人订单，不得支付");
        }

        Integer status = order.getStatus();
        if (status == 1) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "订单已经支付，请勿重复支付");
        } else if (status == 2) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "订单已经取消，无法支付");
        } else if (status == 3) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "订单已经完成，请勿重复支付");
        } else if (status == 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "订单已经退款，无法支付");
        } else if (status != 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "状态参数异常");
        }

        String type = couponOrderPayRequest.getType();
        if (StringUtils.isBlank(type)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "支付类型为空");
        }
        if (!"wechat".equals(type) && !"alipay".equals(type)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "支付类型非法");
        }

        BigDecimal totalAmount = order.getTotalAmount();
        boolean result;
        if (totalAmount.intValue() == 0) {
            CouponOrder couponOrder = new CouponOrder();
            couponOrder.setId(order.getId());
            couponOrder.setOrderNo(order.getOrderNo());
            couponOrder.setUserId(order.getUserId());
            couponOrder.setShopId(order.getShopId());
            couponOrder.setCouponId(order.getCouponId());
            couponOrder.setPayType(type);
            couponOrder.setTotalAmount(order.getTotalAmount());
            couponOrder.setPayAmount(BigDecimal.ZERO);
            couponOrder.setStatus(1);
            couponOrder.setPayTime(new Date());
            couponOrder.setCancelTime(null);
            couponOrder.setFinishTime(null);
            result = this.updateById(couponOrder);
            if (!result) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "订单支付失败");
            }
            createUserCoupon(loginUser.getId(), couponOrder.getCouponId(), couponOrder.getId());
        } else if (totalAmount.intValue() > 0) {
            try {
                log.info("{}订单使用{}支付中...", order.getOrderNo(), type);
                Thread.sleep(3000L);
                log.info("{}订单使用{}支付完成....", order.getOrderNo(), type);
            } catch (InterruptedException e) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "订单付款失败");
            }

            CouponOrder couponOrder = new CouponOrder();
            couponOrder.setId(order.getId());
            couponOrder.setOrderNo(order.getOrderNo());
            couponOrder.setUserId(order.getUserId());
            couponOrder.setShopId(order.getShopId());
            couponOrder.setCouponId(order.getCouponId());
            couponOrder.setTotalAmount(order.getTotalAmount());
            couponOrder.setPayType(type);
            couponOrder.setPayAmount(order.getTotalAmount());
            couponOrder.setStatus(1);
            couponOrder.setPayTime(new Date());
            couponOrder.setCancelTime(null);
            couponOrder.setFinishTime(null);
            result = this.updateById(couponOrder);
            if (!result) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "订单支付失败");
            }
            createUserCoupon(loginUser.getId(), couponOrder.getCouponId(), couponOrder.getId());
        } else {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "支付金额异常");
        }

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean cancelCouponOrder(CouponOrderCancelRequest couponOrderCancelRequest, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        Long id = couponOrderCancelRequest.getId();
        if (id == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数错误");
        }
        CouponOrder order = this.getById(id);
        if (order == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "订单不存在");
        }
        if (!order.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "无权限取消订单");
        }
        if (order.getStatus() == 1 && couponOrderCancelRequest.getUserCouponId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "????");
        }
        UserCoupon userCoupon = couponOrderCancelRequest.getUserCouponId() == null
                ? null
                : userCouponService.getById(couponOrderCancelRequest.getUserCouponId());
        if (order.getStatus() == 1 && userCoupon == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "??????");
        }

        Integer status = order.getStatus();
        if (status == 0) {
            CouponOrder newCouponOrder = new CouponOrder();
            BeanUtils.copyProperties(order, newCouponOrder);
            newCouponOrder.setStatus(2);
            newCouponOrder.setCancelTime(new Date());
            boolean res = this.updateById(newCouponOrder);
            if (!res) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "取消订单失败");
            }
        } else if (status == 1) {
            CouponOrder newCouponOrder = new CouponOrder();
            BeanUtils.copyProperties(order, newCouponOrder);
            newCouponOrder.setStatus(4);
            newCouponOrder.setCancelTime(new Date());
            boolean res = this.updateById(newCouponOrder);
            if (!res) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "取消订单失败");
            }
            try {
                log.info("{}订单使用{}退款中....", order.getOrderNo(), order.getPayType());
                Thread.sleep(3000L);
                log.info("{}订单使用{}退款完成...", order.getOrderNo(), order.getPayType());
            } catch (InterruptedException e) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "退款失败");
            }
            userCoupon.setStatus(3);
            boolean result = userCouponService.updateById(userCoupon);
            if (!result) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "更新失败");
            }
        } else if (status == 2) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "订单已经取消，请勿重复取消");
        } else if (status == 3) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "订单已经完成，无法进行取消");
        } else if (status == 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "订单已经退款，无法进行取消");
        } else {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "参数异常");
        }

        return true;
    }

    private CouponOrder buildAndValidateCouponOrder(CouponOrderAddRequest couponOrderAddRequest, CouponOrder oldCouponOrder) {
        Long userId = couponOrderAddRequest.getUserId();
        Long shopId = couponOrderAddRequest.getShopId();
        Long couponId = couponOrderAddRequest.getCouponId();

        if (userId == null || userId <= 0 || shopId == null || shopId <= 0 || couponId == null || couponId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数错误");
        }
        if (userService.getById(userId) == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在");
        }
        if (shopService.getById(shopId) == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "店铺不存在");
        }
        if (couponService.getById(couponId) == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "优惠券不存在");
        }

        String orderNo = couponOrderAddRequest.getOrderNo();
        if (StringUtils.isBlank(orderNo)) {
            orderNo = UUID.randomUUID().toString();
            couponOrderAddRequest.setOrderNo(orderNo);
        }
        QueryWrapper<CouponOrder> existsWrapper = new QueryWrapper<>();
        existsWrapper.eq("orderNo", orderNo).eq("isDelete", 0);
        if (oldCouponOrder != null) {
            existsWrapper.ne("id", oldCouponOrder.getId());
        }
        CouponOrder duplicate = this.getOne(existsWrapper);
        if (duplicate != null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "订单号已存在");
        }

        Integer status = couponOrderAddRequest.getStatus();
        if (status != null && (status < 0 || status > 4)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "状态非法");
        }

        CouponOrder couponOrder = new CouponOrder();
        BeanUtils.copyProperties(couponOrderAddRequest, couponOrder);
        if (oldCouponOrder != null) {
            couponOrder.setId(oldCouponOrder.getId());
        }
        return couponOrder;
    }

    private void createUserCoupon(Long userId, Long couponId, Long orderId) {
        QueryWrapper<UserCoupon> existsWrapper = new QueryWrapper<>();
        existsWrapper.eq("orderId", orderId).eq("isDelete", 0).last("limit 1");
        if (userCouponService.getOne(existsWrapper) != null) {
            return;
        }

        UserCoupon userCoupon = new UserCoupon();
        userCoupon.setUserId(userId);
        userCoupon.setCouponId(couponId);
        userCoupon.setOrderId(orderId);
        userCoupon.setCode(RandomStringUtils.random(6, true, true));
        userCoupon.setStatus(0);
        userCoupon.setObtainTime(new Date());
        userCoupon.setUseTime(null);
        long time = 10L * 24 * 60 * 60 * 1000;
        userCoupon.setExpireTime(new Date(new Date().getTime() + time));
        boolean res = userCouponService.save(userCoupon);
        if (!res) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "系统错误");
        }
    }
}
