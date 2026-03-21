package com.spy.server.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.spy.server.model.domain.Favorite;
import com.baomidou.mybatisplus.extension.service.IService;
import com.spy.server.model.domain.Favorite;
import com.spy.server.model.dto.favorite.FavoriteAddRequest;
import com.spy.server.model.dto.favorite.FavoriteQueryRequest;
import com.spy.server.model.dto.favorite.FavoriteUpdateRequest;
import com.spy.server.model.vo.FavoriteVO;

import java.util.List;

/**
* @author OUC
* @description 针对表【favorite(收藏表)】的数据库操作Service
* @createDate 2026-03-20 19:51:32
*/
public interface FavoriteService extends IService<Favorite> {
    FavoriteVO getFavoriteVO(Favorite favorite);

    Long addFavorite(FavoriteAddRequest favoriteAddRequest);

    Boolean updateFavorite(FavoriteUpdateRequest favoriteUpdateRequest);

    Wrapper<Favorite> getQueryWrapper(FavoriteQueryRequest favoriteQueryRequest);

    List<FavoriteVO> getFavoriteVO(List<Favorite> records);

    Page<FavoriteVO> listFavoriteVOByPage(FavoriteQueryRequest favoriteQueryRequest);
}
