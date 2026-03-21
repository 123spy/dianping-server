package com.spy.server.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.spy.server.model.domain.CouponOrder;
import com.spy.server.service.CouponOrderService;
import com.spy.server.mapper.CouponOrderMapper;
import org.springframework.stereotype.Service;

/**
* @author OUC
* @description 针对表【coupon_order(优惠券订单表)】的数据库操作Service实现
* @createDate 2026-03-20 19:51:30
*/
@Service
public class CouponOrderServiceImpl extends ServiceImpl<CouponOrderMapper, CouponOrder>
    implements CouponOrderService{

}




