package com.spy.server.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.spy.server.common.DeleteRequest;
import com.spy.server.model.domain.Favorite;
import com.baomidou.mybatisplus.extension.service.IService;
import com.spy.server.model.vo.FavoriteVO;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
* @author OUC
* @description 针对表【favorite(收藏表)】的数据库操作Service
 * @createDate 2026-03-22 13:49:41
*/
public interface FavoriteService extends IService<Favorite> {
    FavoriteVO getFavoriteVO(Favorite favorite);

    Long addFavorite(com.spy.server.model.dto.favorite.FavoriteAddRequest favoriteAddRequest);

    Boolean updateFavorite(com.spy.server.model.dto.favorite.FavoriteUpdateRequest favoriteUpdateRequest);

    Wrapper<Favorite> getQueryWrapper(com.spy.server.model.dto.favorite.FavoriteQueryRequest favoriteQueryRequest);

    List<FavoriteVO> getFavoriteVO(List<Favorite> records);

    Page<FavoriteVO> listFavoriteVOByPage(com.spy.server.model.dto.favorite.FavoriteQueryRequest favoriteQueryRequest);

    Boolean deleteFavorite(DeleteRequest deleteRequest, HttpServletRequest request);
}
