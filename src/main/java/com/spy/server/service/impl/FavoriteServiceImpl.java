package com.spy.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.spy.server.common.ErrorCode;
import com.spy.server.constant.CommonConstant;
import com.spy.server.exception.BusinessException;
import com.spy.server.model.domain.*;
import com.spy.server.model.domain.Favorite;
import com.spy.server.model.dto.favorite.FavoriteAddRequest;
import com.spy.server.model.dto.favorite.FavoriteQueryRequest;
import com.spy.server.model.dto.favorite.FavoriteUpdateRequest;
import com.spy.server.model.vo.FavoriteVO;
import com.spy.server.model.vo.ShopVO;
import com.spy.server.model.vo.UserVO;
import com.spy.server.service.CategoryService;
import com.spy.server.service.FavoriteService;
import com.spy.server.mapper.FavoriteMapper;
import com.spy.server.service.ShopService;
import com.spy.server.service.UserService;
import com.spy.server.utils.SqlUtil;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
* @author OUC
* @description 针对表【favorite(收藏表)】的数据库操作Service实现
* @createDate 2026-03-20 19:51:32
*/
@Service
public class FavoriteServiceImpl extends ServiceImpl<FavoriteMapper, Favorite>
    implements FavoriteService{


    private final Gson gson = new Gson();

    @Resource
    private UserService userService;

    @Resource
    private ShopService shopService;

    @Override
    public FavoriteVO getFavoriteVO(Favorite favorite) {
        FavoriteVO favoriteVO = new FavoriteVO();
        if(favorite==null){
            return favoriteVO;
        }
        BeanUtils.copyProperties(favorite,favoriteVO);

        UserVO userVO = userService.getUserVO(userService.getById(favorite.getUserId()));
        favoriteVO.setUserVO(userVO);

        ShopVO shopVO = shopService.getShopVO(shopService.getById(favorite.getShopId()));
        favoriteVO.setShopVO(shopVO);

        return favoriteVO;
    }

    @Override
    public Long addFavorite(FavoriteAddRequest favoriteAddRequest) {
        Favorite favorite = new Favorite();
        BeanUtils.copyProperties(favoriteAddRequest, favorite);

        Long userId = favoriteAddRequest.getUserId();
        User user = userService.getById(userId);
        if(user == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在");
        }

        Long shopId = favoriteAddRequest.getShopId();

        Shop shop = shopService.getById(shopId);
        if(shop == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "商品不存在");
        }

        // 插入数据
        this.save(favorite);
        return favorite.getId();

    }

    @Override
    public Boolean updateFavorite(FavoriteUpdateRequest favoriteUpdateRequest) {
        Favorite oldFavorite = this.getById(favoriteUpdateRequest.getId());
        if(oldFavorite == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        Favorite favorite = new Favorite();

        BeanUtils.copyProperties(oldFavorite, favorite);

        Long userId = favoriteUpdateRequest.getUserId();
        User user = userService.getById(userId);
        if(user == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在");
        }

        Long shopId = favoriteUpdateRequest.getShopId();

        Shop shop = shopService.getById(shopId);
        if(shop == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "商品不存在");
        }

        // 插入数据
        boolean result = this.updateById(favorite);
        return result;

    }

    @Override
    public Wrapper<Favorite> getQueryWrapper(FavoriteQueryRequest favoriteQueryRequest) {
        if(favoriteQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        Long id = favoriteQueryRequest.getId();
        Long userId = favoriteQueryRequest.getUserId();
        Long shopId = favoriteQueryRequest.getShopId();
        Date createTime = favoriteQueryRequest.getCreateTime();
        Date updateTime = favoriteQueryRequest.getUpdateTime();
        int current = favoriteQueryRequest.getCurrent();
        int pageSize = favoriteQueryRequest.getPageSize();
        String searchText = favoriteQueryRequest.getSearchText();
        String sortField = favoriteQueryRequest.getSortField();
        String sortOrder = favoriteQueryRequest.getSortOrder();



        QueryWrapper<Favorite> queryWrapper = new QueryWrapper<>();

        queryWrapper.eq(id != null, "id", id);
        queryWrapper.eq(userId != null, "userId", userId);
        queryWrapper.eq(shopId != null, "shopId", shopId);

        queryWrapper.orderBy(SqlUtil.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC), sortField);
        return queryWrapper;
    }

    @Override
    public List<FavoriteVO> getFavoriteVO(List<Favorite> records) {
        if (CollectionUtils.isEmpty(records)) {
            return new ArrayList<>();
        }
        List<FavoriteVO> favoriteVOList = records.stream().map(favorite -> {
            return getFavoriteVO(favorite);
        }).collect(Collectors.toList());
        return favoriteVOList;
    }

    @Override
    public Page<FavoriteVO> listFavoriteVOByPage(FavoriteQueryRequest favoriteQueryRequest) {
        int current = favoriteQueryRequest.getCurrent();
        int pageSize = favoriteQueryRequest.getPageSize();
        Page<Favorite> favoritePage = this.page(new Page<>(current, pageSize), this.getQueryWrapper(favoriteQueryRequest));
        Page<FavoriteVO> favoriteVOPage = new Page<>(current, pageSize, favoritePage.getTotal());
        List<FavoriteVO> favoriteVoList = this.getFavoriteVO(favoritePage.getRecords());
        favoriteVOPage.setRecords(favoriteVoList);
        return favoriteVOPage;
    }
    
}




