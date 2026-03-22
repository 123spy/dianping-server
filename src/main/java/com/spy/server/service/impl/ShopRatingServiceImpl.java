package com.spy.server.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.spy.server.model.domain.ShopRating;
import com.spy.server.service.ShopRatingService;
import com.spy.server.mapper.ShopRatingMapper;
import org.springframework.stereotype.Service;

/**
* @author OUC
* @description 针对表【shop_rating(店铺评分表)】的数据库操作Service实现
* @createDate 2026-03-22 13:49:45
*/
@Service
public class ShopRatingServiceImpl extends ServiceImpl<ShopRatingMapper, ShopRating>
    implements ShopRatingService{

}




