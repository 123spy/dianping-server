package com.spy.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.spy.server.common.DeleteRequest;
import com.spy.server.common.ErrorCode;
import com.spy.server.constant.CommonConstant;
import com.spy.server.exception.BusinessException;
import com.spy.server.mapper.FavoriteMapper;
import com.spy.server.model.domain.Favorite;
import com.spy.server.model.domain.Shop;
import com.spy.server.model.domain.User;
import com.spy.server.model.dto.favorite.FavoriteAddRequest;
import com.spy.server.model.dto.favorite.FavoriteQueryRequest;
import com.spy.server.model.dto.favorite.FavoriteUpdateRequest;
import com.spy.server.model.vo.FavoriteVO;
import com.spy.server.model.vo.ShopVO;
import com.spy.server.model.vo.UserVO;
import com.spy.server.service.FavoriteService;
import com.spy.server.service.ShopService;
import com.spy.server.service.UserService;
import com.spy.server.utils.SqlUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class FavoriteServiceImpl extends ServiceImpl<FavoriteMapper, Favorite> implements FavoriteService {

    @Resource
    private UserService userService;

    @Lazy
    @Resource
    private ShopService shopService;

    @Override
    public FavoriteVO getFavoriteVO(Favorite favorite, HttpServletRequest request) {
        if (favorite == null) {
            return null;
        }

        FavoriteVO favoriteVO = new FavoriteVO();
        BeanUtils.copyProperties(favorite, favoriteVO);

        User user = userService.getById(favorite.getUserId());
        if (user != null) {
            UserVO userVO = userService.getUserVO(user);
            favoriteVO.setUserVO(userVO);
        }

        Shop shop = shopService.getById(favorite.getShopId());
        if (shop != null) {
            ShopVO shopVO = shopService.getShopVO(shop, request);
            favoriteVO.setShopVO(shopVO);
        }

        return favoriteVO;
    }

    @Override
    public List<FavoriteVO> getFavoriteVO(List<Favorite> records, HttpServletRequest request) {
        if (CollectionUtils.isEmpty(records)) {
            return new ArrayList<>();
        }

        Set<Long> userIdSet = records.stream()
                .map(Favorite::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Set<Long> shopIdSet = records.stream()
                .map(Favorite::getShopId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, UserVO> userVOMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(userIdSet)) {
            List<User> userList = userService.listByIds(userIdSet);
            userVOMap = userList.stream().collect(Collectors.toMap(
                    User::getId,
                    user -> userService.getUserVO(user),
                    (a, b) -> a
            ));
        }

        Map<Long, ShopVO> shopVOMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(shopIdSet)) {
            List<Shop> shopList = shopService.listByIds(shopIdSet);
            shopVOMap = shopList.stream().collect(Collectors.toMap(
                    Shop::getId,
                    shop -> shopService.getShopVO(shop, request),
                    (a, b) -> a
            ));
        }

        Map<Long, UserVO> finalUserVOMap = userVOMap;
        Map<Long, ShopVO> finalShopVOMap = shopVOMap;

        return records.stream().map(favorite -> {
            FavoriteVO favoriteVO = new FavoriteVO();
            BeanUtils.copyProperties(favorite, favoriteVO);
            favoriteVO.setUserVO(finalUserVOMap.get(favorite.getUserId()));
            favoriteVO.setShopVO(finalShopVOMap.get(favorite.getShopId()));
            return favoriteVO;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addFavorite(FavoriteAddRequest favoriteAddRequest) {
        return saveFavorite(favoriteAddRequest);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long submitFavorite(FavoriteAddRequest favoriteAddRequest) {
        return saveFavorite(favoriteAddRequest);
    }

    private Long saveFavorite(FavoriteAddRequest favoriteAddRequest) {
        if (favoriteAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        Long userId = favoriteAddRequest.getUserId();
        Long shopId = favoriteAddRequest.getShopId();

        if (userId == null || userId <= 0 || shopId == null || shopId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户或店铺参数错误");
        }

        if (userService.getById(userId) == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在");
        }

        if (shopService.getById(shopId) == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "店铺不存在");
        }

        QueryWrapper<Favorite> existsWrapper = new QueryWrapper<>();
        existsWrapper.eq("userId", userId)
                .eq("shopId", shopId)
                .eq("isDelete", 0);

        Favorite exists = this.getOne(existsWrapper);
        if (exists != null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "已经收藏过该店铺");
        }

        Favorite favorite = new Favorite();
        BeanUtils.copyProperties(favoriteAddRequest, favorite);

        boolean saved = this.save(favorite);
        if (!saved) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "收藏失败");
        }

        recalculateFavoriteCount(shopId);
        return favorite.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateFavorite(FavoriteUpdateRequest favoriteUpdateRequest) {
        if (favoriteUpdateRequest == null || favoriteUpdateRequest.getId() == null || favoriteUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        Favorite oldFavorite = this.getById(favoriteUpdateRequest.getId());
        if (oldFavorite == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "收藏不存在");
        }

        Long userId = favoriteUpdateRequest.getUserId();
        if (userId != null && userService.getById(userId) == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在");
        }

        Long newShopId = favoriteUpdateRequest.getShopId();
        if (newShopId != null && shopService.getById(newShopId) == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "店铺不存在");
        }

        Long finalUserId = userId != null ? userId : oldFavorite.getUserId();
        Long finalShopId = newShopId != null ? newShopId : oldFavorite.getShopId();

        QueryWrapper<Favorite> existsWrapper = new QueryWrapper<>();
        existsWrapper.eq("userId", finalUserId)
                .eq("shopId", finalShopId)
                .eq("isDelete", 0)
                .ne("id", oldFavorite.getId());

        Favorite duplicate = this.getOne(existsWrapper);
        if (duplicate != null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "该收藏关系已存在");
        }

        Favorite favorite = new Favorite();
        BeanUtils.copyProperties(favoriteUpdateRequest, favorite);

        boolean updated = this.updateById(favorite);
        if (!updated) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新失败");
        }

        Long oldShopId = oldFavorite.getShopId();
        recalculateFavoriteCount(oldShopId);
        if (!Objects.equals(oldShopId, finalShopId)) {
            recalculateFavoriteCount(finalShopId);
        }

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean revokeFavorite(DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        User loginUser = userService.getLoginUser(request);

        Favorite favorite = this.getById(deleteRequest.getId());
        if (favorite == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "收藏不存在");
        }
        if (!Objects.equals(favorite.getUserId(), loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限取消收藏");
        }

        boolean result = this.removeById(deleteRequest.getId());
        if (!result) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "取消收藏失败");
        }

        recalculateFavoriteCount(favorite.getShopId());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean adminDeleteFavorite(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        Favorite favorite = this.getById(id);
        if (favorite == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "收藏不存在");
        }

        boolean result = this.removeById(id);
        if (!result) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "删除失败");
        }

        recalculateFavoriteCount(favorite.getShopId());
        return true;
    }

    @Override
    public Wrapper<Favorite> getQueryWrapper(FavoriteQueryRequest favoriteQueryRequest) {
        if (favoriteQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        Long id = favoriteQueryRequest.getId();
        Long userId = favoriteQueryRequest.getUserId();
        Long shopId = favoriteQueryRequest.getShopId();
        Date createTime = favoriteQueryRequest.getCreateTime();
        Date updateTime = favoriteQueryRequest.getUpdateTime();
        String sortField = favoriteQueryRequest.getSortField();
        String sortOrder = favoriteQueryRequest.getSortOrder();

        QueryWrapper<Favorite> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(id != null, "id", id);
        queryWrapper.eq(userId != null, "userId", userId);
        queryWrapper.eq(shopId != null, "shopId", shopId);
        queryWrapper.ge(createTime != null, "createTime", createTime);
        queryWrapper.ge(updateTime != null, "updateTime", updateTime);
        queryWrapper.eq("isDelete", 0);

        boolean asc = CommonConstant.SORT_ORDER_ASC.equals(sortOrder);
        queryWrapper.orderBy(SqlUtil.validSortField(sortField), asc, sortField);
        return queryWrapper;
    }

    @Override
    public Page<FavoriteVO> listFavoriteVOByPage(FavoriteQueryRequest favoriteQueryRequest, HttpServletRequest request) {
        int current = favoriteQueryRequest.getCurrent();
        int pageSize = favoriteQueryRequest.getPageSize();

        Page<Favorite> favoritePage = this.page(new Page<>(current, pageSize), this.getQueryWrapper(favoriteQueryRequest));
        Page<FavoriteVO> favoriteVOPage = new Page<>(current, pageSize, favoritePage.getTotal());
        favoriteVOPage.setRecords(this.getFavoriteVO(favoritePage.getRecords(), request));
        return favoriteVOPage;
    }

    @Override
    public void recalculateFavoriteCount(Long shopId) {
        if (shopId == null || shopId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "店铺参数错误");
        }

        Shop shop = shopService.getById(shopId);
        if (shop == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "店铺不存在");
        }

        QueryWrapper<Favorite> wrapper = new QueryWrapper<>();
        wrapper.eq("shopId", shopId);
        wrapper.eq("isDelete", 0);

        long count = this.count(wrapper);
        shop.setFavoriteCount((int) count);

        boolean result = shopService.updateById(shop);
        if (!result) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "重算收藏数失败");
        }
    }
}