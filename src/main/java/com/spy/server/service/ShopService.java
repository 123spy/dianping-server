package com.spy.server.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.spy.server.model.domain.Shop;
import com.baomidou.mybatisplus.extension.service.IService;
import com.spy.server.model.dto.shop.ShopAddRequest;
import com.spy.server.model.dto.shop.ShopQueryRequest;
import com.spy.server.model.dto.shop.ShopUpdateRequest;
import com.spy.server.model.vo.ShopVO;

import java.util.List;

/**
* @author OUC
* @description 针对表【shop(店铺表)】的数据库操作Service
 * @createDate 2026-03-22 13:49:43
*/
public interface ShopService extends IService<Shop> {
    ShopVO getShopVO(Shop shop);

    Long addShop(ShopAddRequest shopAddRequest);

    Boolean updateShop(ShopUpdateRequest shopUpdateRequest);

    Wrapper<Shop> getQueryWrapper(ShopQueryRequest shopQueryRequest);

    List<ShopVO> getShopVO(List<Shop> records);

    Page<ShopVO> listShopVOByPage(ShopQueryRequest shopQueryRequest);
}
