package com.spy.server.service.impl;

import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.spy.server.common.ErrorCode;
import com.spy.server.exception.BusinessException;
import com.spy.server.model.domain.Coupon;
import com.spy.server.model.domain.CouponOrder;
import com.spy.server.mapper.CouponOrderMapper;
import com.spy.server.model.domain.User;
import com.spy.server.model.domain.UserCoupon;
import com.spy.server.model.dto.couponorder.CouponOrderCancelRequest;
import com.spy.server.model.dto.couponorder.CouponOrderPayRequest;
import com.spy.server.model.dto.couponorder.CouponOrderSubmitRequest;
import com.spy.server.service.CouponOrderService;
import com.spy.server.service.CouponService;
import com.spy.server.service.UserCouponService;
import com.spy.server.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;

/**
* @author OUC
* @description 针对表【coupon_order(优惠券订单表)】的数据库操作Service实现
 * @createDate 2026-03-22 13:49:38
*/
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long submitCouponOrder(CouponOrderSubmitRequest couponOrderSubmitRequest, HttpServletRequest request) {
        // 多线程锁, 锁住这个优惠券，防止卖超
        // 事务，因为涉及到两个表的更新，因此需要加载事务
        synchronized (couponOrderSubmitRequest.getCouponId().toString().intern()) {
            // 校验
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

            // 这里需要先扣除一个库存
            coupon.setStock(coupon.getStock() - 1);
            couponService.updateById(coupon);

            // 创建一个订单
            CouponOrder couponOrder = new CouponOrder();
            // UUID随机生成一个
            couponOrder.setOrderNo(UUID.randomUUID().toString());

            User loginUser = userService.getLoginUser(request);
            couponOrder.setUserId(loginUser.getId());

            couponOrder.setShopId(shopId);
            couponOrder.setCouponId(couponId);
            // 总的金额应该是，打折后的支付价格
            couponOrder.setTotalAmount(coupon.getDiscountPrice());
            // 还未支付，支付方式设置为null
            couponOrder.setPayType(null);
            // 还未支付，已经支付的金额设置为0
            couponOrder.setPayAmount(new BigDecimal(0));
            // 订单状态：0-待支付 1-已支付 2-已取消 3-已完成 4-已退款
            couponOrder.setStatus(0);
            // 未支付，就设置为null
            couponOrder.setPayTime(null);
            // 未取消，设置为null
            couponOrder.setCancelTime(null);
            // 未完成，设置为null
            couponOrder.setFinishTime(null);

            boolean result = this.save(couponOrder);
            if (!result) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "保存失败");
            }

            // 减少一个优惠券的库存
            return couponOrder.getId();
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean payCouponOrder(CouponOrderPayRequest couponOrderPayRequest, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);

        // 校验
        Long id = couponOrderPayRequest.getId();
        CouponOrder order = this.getById(id);
        if (order == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "订单不存在");
        }

        Long userId = order.getUserId();
        if (ObjectUtil.notEqual(userId, loginUser.getId())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "非本人订单，不得支付");
        }

        // 看看是否已经支付
        BigDecimal payAmount = order.getPayAmount();
        Integer status = order.getStatus();
        // 订单状态：0-待支付 1-已支付 2-已取消 3-已完成 4-已退款
        if (status == 0) {
            // 走支付路径
        } else if (status == 1) {
            // 已经支付了
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "订单已经支付，请勿重复支付");
        } else if (status == 2) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "订单已经取消，无法支付");
        } else if (status == 3) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "订单已经完成，请勿重复支付");
        } else if (status == 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "订单已经退款，无法支付");
        } else {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "状态参数异常");
        }

        Date payTime = order.getPayTime();
        Date cancelTime = order.getCancelTime();
        Date finishTime = order.getFinishTime();

        String type = couponOrderPayRequest.getType();

        if (StringUtils.isBlank(type)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "支付类型为空");
        }
        if ((!type.equals("wechat")) && !type.equals("alipay")) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "支付类型非法");
        }

        BigDecimal totalAmount = order.getTotalAmount();
        Boolean result = false;
        if (totalAmount.intValue() == 0) {
            // 如果需要支付的金额为0,那就更新他的状态
            CouponOrder couponOrder = new CouponOrder();
            couponOrder.setId(order.getId());
            couponOrder.setOrderNo(order.getOrderNo());
            couponOrder.setUserId(order.getUserId());
            couponOrder.setShopId(order.getShopId());
            couponOrder.setCouponId(order.getCouponId());
            couponOrder.setPayType(type);
            couponOrder.setTotalAmount(order.getTotalAmount());
            couponOrder.setPayAmount(new BigDecimal(0));
            couponOrder.setStatus(1);
            couponOrder.setPayTime(new Date());
            couponOrder.setCancelTime(null);
            couponOrder.setFinishTime(null);
            result = this.updateById(couponOrder);

            if (!result) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "订单支付失败");
            }

            // 要将这个优惠券加入到用户的列表中
            UserCoupon userCoupon = new UserCoupon();

            userCoupon.setUserId(loginUser.getId());
            userCoupon.setCouponId(couponOrder.getCouponId());
            userCoupon.setOrderId(couponOrder.getId());
            // 生成随机的6位code串，只有数字和大写字母
            userCoupon.setCode(RandomStringUtils.random(6, true, true));
            // '用户券状态：0-未使用 1-已使用 2-已过期 3-已退款'
            userCoupon.setStatus(0);
            userCoupon.setObtainTime(new Date());
            userCoupon.setUseTime(null);


            // 这里设置的是10天，10天后会过期
            long time = 10L * 24 * 60 * 60 * 1000;
            Date tenDaysLater = new Date(new Date().getTime() + time);
            userCoupon.setExpireTime(tenDaysLater);
            boolean res = userCouponService.save(userCoupon);
            if (!res) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "系统错误");
            }
        } else if (totalAmount.intValue() > 0) {
            // 如果需要支付的金额大于0
            try {
                log.info("{}订单使用{}支付中....", order.getOrderNo(), type);
                Thread.sleep(3000L);
                log.info("{}订单使用{}支付完成....", order.getOrderNo(), type);

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

                // 要将这个优惠券加入到用户的列表中
                UserCoupon userCoupon = new UserCoupon();

                userCoupon.setUserId(loginUser.getId());
                userCoupon.setCouponId(couponOrder.getCouponId());
                userCoupon.setOrderId(couponOrder.getId());
                // 生成随机的6位code串，只有数字和大写字母
                userCoupon.setCode(RandomStringUtils.random(6, true, true));
                // '用户券状态：0-未使用 1-已使用 2-已过期 3-已退款'
                userCoupon.setStatus(0);
                userCoupon.setObtainTime(new Date());
                userCoupon.setUseTime(null);


                // 这里设置的是10天，10天后会过期
                long time = 10L * 24 * 60 * 60 * 1000;
                Date tenDaysLater = new Date(new Date().getTime() + time);
                userCoupon.setExpireTime(tenDaysLater);
                boolean res = userCouponService.save(userCoupon);
                if (!res) {
                    throw new BusinessException(ErrorCode.OPERATION_ERROR, "系统错误");
                }

            } catch (InterruptedException e) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "订单付款失败");
            }
        } else {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "支付金额异常");
        }

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean cancelCouponOrder(CouponOrderCancelRequest couponOrderCancelRequest, HttpServletRequest request) {
        // 校验，这个订单存在不存在
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
        if (couponOrderCancelRequest.getUserCouponId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数非法");
        }
        UserCoupon userCoupon = userCouponService.getById(couponOrderCancelRequest.getUserCouponId());
        if (userCoupon == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "优惠券不存在");
        }

        // 然后看看这个订单是个什么状态
        Integer status = order.getStatus();
        // 订单状态：0-待支付 1-已支付 2-已取消 3-已完成 4-已退款
        if (status == 0) {
            // 待支付状态，需要修改当前的这个order状态
            CouponOrder newCouponOrder = new CouponOrder();
            BeanUtils.copyProperties(order, newCouponOrder);
            newCouponOrder.setStatus(2);
            newCouponOrder.setCancelTime(new Date());
            boolean res = this.updateById(newCouponOrder);
            if (!res) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "取消订单失败");
            }
        } else if (status == 1) {
            // jiangcouponorder数据更新
            CouponOrder newCouponOrder = new CouponOrder();
            BeanUtils.copyProperties(order, newCouponOrder);
            newCouponOrder.setStatus(4);
            newCouponOrder.setCancelTime(new Date());

            boolean res = this.updateById(newCouponOrder);
            if (!res) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "取消订单失败");
            }
            // 已经支付了的，需要进行退款
            try {
                log.info("{}订单使用{}退款中....", order.getOrderNo(), order.getPayType());
                Thread.sleep(3000L);
                log.info("{}订单使用{}退款完成....", order.getOrderNo(), order.getPayType());
            } catch (InterruptedException e) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "退款失败");
            }
            // 将usercoupon数据也更新

            // 用户券状态：0-未使用 1-已使用 2-已过期 3-已退款
            userCoupon.setStatus(3);
            boolean result = userCouponService.updateById(userCoupon);
            if(!result){
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
}




