package com.spy.server.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.spy.server.common.DeleteRequest;
import com.spy.server.model.domain.Favorite;
import com.spy.server.model.dto.favorite.FavoriteAddRequest;
import com.spy.server.model.dto.favorite.FavoriteQueryRequest;
import com.spy.server.model.dto.favorite.FavoriteUpdateRequest;
import com.spy.server.model.vo.FavoriteVO;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface FavoriteService extends IService<Favorite> {

    FavoriteVO getFavoriteVO(Favorite favorite, HttpServletRequest request);

    List<FavoriteVO> getFavoriteVO(List<Favorite> records, HttpServletRequest request);

    Long addFavorite(FavoriteAddRequest favoriteAddRequest);

    Long submitFavorite(FavoriteAddRequest favoriteAddRequest);

    Boolean updateFavorite(FavoriteUpdateRequest favoriteUpdateRequest);

    Boolean revokeFavorite(DeleteRequest deleteRequest, HttpServletRequest request);

    Boolean adminDeleteFavorite(Long id);

    Wrapper<Favorite> getQueryWrapper(FavoriteQueryRequest favoriteQueryRequest);

    Page<FavoriteVO> listFavoriteVOByPage(FavoriteQueryRequest favoriteQueryRequest, HttpServletRequest request);

    void recalculateFavoriteCount(Long shopId);
}