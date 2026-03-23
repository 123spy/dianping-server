package com.spy.server.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.spy.server.common.DeleteRequest;
import com.spy.server.model.domain.ShopRating;
import com.spy.server.model.domain.ShopRating;
import com.baomidou.mybatisplus.extension.service.IService;
import com.spy.server.model.dto.shoprating.ShopRatingAddRequest;
import com.spy.server.model.dto.shoprating.ShopRatingQueryRequest;
import com.spy.server.model.dto.shoprating.ShopRatingUpdateRequest;
import com.spy.server.model.vo.ShopRatingVO;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
* @author OUC
* @description 针对表【shop_rating(店铺评分表)】的数据库操作Service
* @createDate 2026-03-22 13:49:45
*/
public interface ShopRatingService extends IService<ShopRating> {
    ShopRatingVO getShopRatingVO(ShopRating shopRating);

    List<ShopRatingVO> getShopRatingVO(List<ShopRating> records);

    Long addShopRating(ShopRatingAddRequest shopRatingAddRequest);

    Long submitShopRating(ShopRatingAddRequest shopRatingAddRequest);

    Boolean updateShopRating(ShopRatingUpdateRequest shopRatingUpdateRequest);

    Boolean revokeShopRating(DeleteRequest deleteRequest, HttpServletRequest request);

    Boolean adminDeleteShopRating(Long id);

    Wrapper<ShopRating> getQueryWrapper(ShopRatingQueryRequest shopRatingQueryRequest);

    Page<ShopRatingVO> listShopRatingVOByPage(ShopRatingQueryRequest shopRatingQueryRequest);

    void recalculateShopRatingCount(Long shopId);
}
