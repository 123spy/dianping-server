package com.spy.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.spy.server.common.ErrorCode;
import com.spy.server.constant.CommonConstant;
import com.spy.server.exception.BusinessException;
import com.spy.server.model.domain.Coupon;
import com.spy.server.mapper.CouponMapper;
import com.spy.server.model.domain.Shop;
import com.spy.server.model.domain.Coupon;
import com.spy.server.model.dto.coupon.CouponAddRequest;
import com.spy.server.model.dto.coupon.CouponQueryRequest;
import com.spy.server.model.dto.coupon.CouponUpdateRequest;
import com.spy.server.model.vo.CouponVO;
import com.spy.server.model.vo.CouponVO;
import com.spy.server.service.CouponService;
import com.spy.server.service.ShopService;
import com.spy.server.utils.SqlUtil;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
* @author OUC
* @description 针对表【coupon(优惠券表（券模板）)】的数据库操作Service实现
 * @createDate 2026-03-22 13:49:36
*/
@Service
public class CouponServiceImpl extends ServiceImpl<CouponMapper, Coupon>
        implements CouponService {

    @Resource
    private ShopService shopService;

    @Resource
    private RedisTemplate redisTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addCoupon(CouponAddRequest couponAddRequest) {

        // todo 针对不同的优惠券，要做不同的权限校验，但是因为是初期的版本，就先这样
        Long shopId = couponAddRequest.getShopId();
        if (shopId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Shop shop = shopService.getById(shopId);
        if (shop == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "店铺不存在");
        }


        String title = couponAddRequest.getTitle();
        if (StringUtils.isBlank(title) || title.length() > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "优惠券名称非法");
        }


        String description = couponAddRequest.getDescription();


        Integer type = couponAddRequest.getType();
        if (type == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "优惠券类型为空");
        }
        if (type != 0 && type != 1 && type != 2) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "优惠券类型非法");
        }


        BigDecimal price = couponAddRequest.getPrice();
        if (price == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "优惠券的价格为空");
        }

        BigDecimal discountPrice = couponAddRequest.getDiscountPrice();
        if (discountPrice == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "优惠券的折扣价格为空");
        }
        Integer stock = couponAddRequest.getStock();
        if (stock == null || stock < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "优惠券的库存数据非法");
        }

        // 这个字段就不校验了，主要是考虑到，开始时间设置为null，就代表随时都ok
        Date availableStartTime = couponAddRequest.getAvailableStartTime();

        // 该字段也可以不限制，代表没有结束事件
        Date availableEndTime = couponAddRequest.getAvailableEndTime();

        // 开始使用事件设置为null，就代表随时可以开始使用
        Date useStartTime = couponAddRequest.getUseStartTime();

        // null就代表，永久可以使用它
        Date useEndTime = couponAddRequest.getUseEndTime();

        Integer status = couponAddRequest.getStatus();
        if (status != 0 && status != 1 && status != 2) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "优惠券的状态错误");
        }

        Coupon coupon = new Coupon();
        BeanUtils.copyProperties(couponAddRequest, coupon);

        boolean result = this.save(coupon);
        if (!result) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "系统错误，优惠券创建失败");
        }
        Long couponId = coupon.getId();
        // 还需要存储一下redis。
        String stockKey = "coupon:stock:" + couponId;
        redisTemplate.opsForValue().set(stockKey, stock);
        return couponId;
    }

    @Override
    public Boolean updateCoupon(CouponUpdateRequest couponUpdateRequest) {
        Long id = couponUpdateRequest.getId();

        Long shopId = couponUpdateRequest.getShopId();
        if (shopId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Shop shop = shopService.getById(shopId);
        if (shop == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "店铺不存在");
        }


        String title = couponUpdateRequest.getTitle();
        if (StringUtils.isBlank(title) || title.length() > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "优惠券名称非法");
        }


        String description = couponUpdateRequest.getDescription();


        Integer type = couponUpdateRequest.getType();
        if (type == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "优惠券类型为空");
        }
        if (type != 0 && type != 1 && type != 2) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "优惠券类型非法");
        }


        BigDecimal price = couponUpdateRequest.getPrice();
        if (price == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "优惠券的价格为空");
        }

        BigDecimal discountPrice = couponUpdateRequest.getDiscountPrice();
        if (discountPrice == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "优惠券的折扣价格为空");
        }
        Integer stock = couponUpdateRequest.getStock();
        if (stock == null || stock < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "优惠券的库存数据非法");
        }

        // 这个字段就不校验了，主要是考虑到，开始时间设置为null，就代表随时都ok
        Date availableStartTime = couponUpdateRequest.getAvailableStartTime();

        // 该字段也可以不限制，代表没有结束事件
        Date availableEndTime = couponUpdateRequest.getAvailableEndTime();

        // 开始使用事件设置为null，就代表随时可以开始使用
        Date useStartTime = couponUpdateRequest.getUseStartTime();

        // null就代表，永久可以使用它
        Date useEndTime = couponUpdateRequest.getUseEndTime();

        Integer status = couponUpdateRequest.getStatus();
        if (status != 0 && status != 1 && status != 2) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "优惠券的状态错误");
        }

        Coupon coupon = new Coupon();
        BeanUtils.copyProperties(couponUpdateRequest, coupon);

        boolean result = this.updateById(coupon);
        if (!result) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "系统错误，优惠券更新失败");
        }
        return null;
    }

    @Override
    public CouponVO getCouponVO(Coupon coupon) {
        CouponVO couponVO = new CouponVO();
        if (coupon == null) {
            return couponVO;
        }
        BeanUtils.copyProperties(coupon, couponVO);
        return couponVO;
    }

    @Override
    public Wrapper<Coupon> getQueryWrapper(CouponQueryRequest couponQueryRequest) {
        if (couponQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        Long id = couponQueryRequest.getId();
        Long shopId = couponQueryRequest.getShopId();
        String title = couponQueryRequest.getTitle();
        String description = couponQueryRequest.getDescription();
        Integer type = couponQueryRequest.getType();
        BigDecimal price = couponQueryRequest.getPrice();
        BigDecimal discountPrice = couponQueryRequest.getDiscountPrice();
        Integer stock = couponQueryRequest.getStock();
        Date availableStartTime = couponQueryRequest.getAvailableStartTime();
        Date availableEndTime = couponQueryRequest.getAvailableEndTime();
        Date useStartTime = couponQueryRequest.getUseStartTime();
        Date useEndTime = couponQueryRequest.getUseEndTime();
        Integer status = couponQueryRequest.getStatus();
        Date createTime = couponQueryRequest.getCreateTime();
        Date updateTime = couponQueryRequest.getUpdateTime();
        Integer isDelete = couponQueryRequest.getIsDelete();
        int current = couponQueryRequest.getCurrent();
        int pageSize = couponQueryRequest.getPageSize();
        String searchText = couponQueryRequest.getSearchText();
        String sortField = couponQueryRequest.getSortField();
        String sortOrder = couponQueryRequest.getSortOrder();

        QueryWrapper<Coupon> queryWrapper = new QueryWrapper<>();

        queryWrapper.eq(id != null, "id", id);
        queryWrapper.eq(shopId != null, "shopId", shopId);
        queryWrapper.like(StringUtils.isNotBlank(title), "title", title);
        queryWrapper.like(StringUtils.isNotBlank(description), "description", description);
        queryWrapper.eq(type != null, "type", type);
        queryWrapper.eq(stock != null, "stock", stock);
        queryWrapper.eq(status != null, "status", status);

        if (StringUtils.isNotBlank(searchText)) {
            queryWrapper.and(wrapper ->
                    wrapper.like("title", searchText)
                            .or()
                            .like("description", searchText)
            );
        }

        queryWrapper.orderBy(
                SqlUtil.validSortField(sortField),
                CommonConstant.SORT_ORDER_ASC.equals(sortOrder),
                sortField
        );
        return queryWrapper;
    }

    @Override
    public Page<CouponVO> listCouponVOByPage(CouponQueryRequest couponQueryRequest) {
        int current = couponQueryRequest.getCurrent();
        int pageSize = couponQueryRequest.getPageSize();
        Page<Coupon> couponPage = this.page(new Page<>(current, pageSize), this.getQueryWrapper(couponQueryRequest));
        Page<com.spy.server.model.vo.CouponVO> couponVOPage = new Page<>(current, pageSize, couponPage.getTotal());
        List<CouponVO> couponVoList = this.getCouponVO(couponPage.getRecords());
        couponVOPage.setRecords(couponVoList);
        return couponVOPage;
    }

    @Override
    public List<CouponVO> getCouponVO(List<Coupon> records) {
        if (CollectionUtils.isEmpty(records)) {
            return new ArrayList<>();
        }
        List<CouponVO> couponVOList = records.stream().map(coupon -> {
            return getCouponVO(coupon);
        }).collect(Collectors.toList());
        return couponVOList;
    }
}




