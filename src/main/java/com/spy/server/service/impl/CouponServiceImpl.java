package com.spy.server.service.impl;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.spy.server.model.domain.Coupon;
import com.spy.server.mapper.CouponMapper;
import org.springframework.stereotype.Service;

/**
* @author OUC
* @description 针对表【coupon(优惠券表（券模板）)】的数据库操作Service实现
 * @createDate 2026-03-22 13:49:36
*/
@Service
public class CouponServiceImpl extends ServiceImpl<CouponMapper, Coupon>
        implements IService<Coupon> {

}




