package com.spy.server.service.impl;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.spy.server.model.domain.UserCoupon;
import com.spy.server.mapper.UserCouponMapper;
import com.spy.server.service.UserCouponService;
import org.springframework.stereotype.Service;

/**
* @author OUC
* @description 针对表【user_coupon(用户拥有的优惠券表)】的数据库操作Service实现
 * @createDate 2026-03-22 13:49:50
*/
@Service
public class UserCouponServiceImpl extends ServiceImpl<UserCouponMapper, UserCoupon>
        implements UserCouponService {

}




