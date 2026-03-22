package com.spy.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.spy.server.common.DeleteRequest;
import com.spy.server.common.ErrorCode;
import com.spy.server.constant.CommonConstant;
import com.spy.server.exception.BusinessException;
import com.spy.server.model.domain.Favorite;
import com.spy.server.mapper.FavoriteMapper;
import com.spy.server.model.domain.Shop;
import com.spy.server.model.domain.User;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author OUC
 * @description 针对表【favorite(收藏表)】的数据库操作Service实现
 * @createDate 2026-03-22 13:49:40
 */
@Service
public class FavoriteServiceImpl extends ServiceImpl<FavoriteMapper, Favorite>
        implements FavoriteService {
    @Resource
    private UserService userService;

    @Resource
    private ShopService shopService;

    @Override
    public FavoriteVO getFavoriteVO(Favorite favorite) {
        if (favorite == null) {
            return null;
        }
        FavoriteVO favoriteVO = new FavoriteVO();
        BeanUtils.copyProperties(favorite, favoriteVO);

        UserVO userVO = userService.getUserVO(userService.getById(favorite.getUserId()));
        favoriteVO.setUserVO(userVO);

        ShopVO shopVO = shopService.getShopVO(shopService.getById(favorite.getShopId()));
        favoriteVO.setShopVO(shopVO);
        return favoriteVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addFavorite(com.spy.server.model.dto.favorite.FavoriteAddRequest favoriteAddRequest) {
        if (favoriteAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        Long userId = favoriteAddRequest.getUserId();
        Long shopId = favoriteAddRequest.getShopId();
        if (userId == null || userId <= 0 || shopId == null || shopId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户或店铺参数错误");
        }

        User user = userService.getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在");
        }

        Shop shop = shopService.getById(shopId);
        if (shop == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "店铺不存在");
        }

        boolean exists = this.lambdaQuery()
                .eq(Favorite::getUserId, userId)
                .eq(Favorite::getShopId, shopId)
                .exists();
        if (exists) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "请勿重复收藏");
        }

        Favorite favorite = new Favorite();
        BeanUtils.copyProperties(favoriteAddRequest, favorite);

        boolean saved = this.save(favorite);
        if (!saved) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "收藏失败");
        }

        calculateCount(shopService.getById(shopId));
        return favorite.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateFavorite(com.spy.server.model.dto.favorite.FavoriteUpdateRequest favoriteUpdateRequest) {
        if (favoriteUpdateRequest == null || favoriteUpdateRequest.getId() == null || favoriteUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        Favorite oldFavorite = this.getById(favoriteUpdateRequest.getId());
        if (oldFavorite == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "收藏记录不存在");
        }

        Long userId = favoriteUpdateRequest.getUserId();
        Long shopId = favoriteUpdateRequest.getShopId();
        if (userId == null || userId <= 0 || shopId == null || shopId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户或店铺参数错误");
        }

        if (userService.getById(userId) == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在");
        }
        if (shopService.getById(shopId) == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "店铺不存在");
        }

        boolean duplicate = this.lambdaQuery()
                .eq(Favorite::getUserId, userId)
                .eq(Favorite::getShopId, shopId)
                .ne(Favorite::getId, favoriteUpdateRequest.getId())
                .exists();
        if (duplicate) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "该收藏关系已存在");
        }

        Favorite favorite = new Favorite();
        BeanUtils.copyProperties(favoriteUpdateRequest, favorite); // 关键修复点

        boolean updated = this.updateById(favorite);
        if (!updated) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新失败");
        }

        calculateCount(shopService.getById(oldFavorite.getShopId()));
        return true;
    }

    @Override
    public Wrapper<Favorite> getQueryWrapper(com.spy.server.model.dto.favorite.FavoriteQueryRequest favoriteQueryRequest) {
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
    public List<FavoriteVO> getFavoriteVO(List<Favorite> records) {
        if (CollectionUtils.isEmpty(records)) {
            return new ArrayList<>();
        }
        return records.stream().map(this::getFavoriteVO).collect(Collectors.toList());
    }

    @Override
    public Page<FavoriteVO> listFavoriteVOByPage(com.spy.server.model.dto.favorite.FavoriteQueryRequest favoriteQueryRequest) {
        int current = favoriteQueryRequest.getCurrent();
        int pageSize = favoriteQueryRequest.getPageSize();

        Page<Favorite> favoritePage = this.page(new Page<>(current, pageSize), this.getQueryWrapper(favoriteQueryRequest));
        Page<FavoriteVO> favoriteVOPage = new Page<>(current, pageSize, favoritePage.getTotal());
        favoriteVOPage.setRecords(this.getFavoriteVO(favoritePage.getRecords()));
        return favoriteVOPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteFavorite(DeleteRequest deleteRequest, HttpServletRequest request) {
        Favorite favorite = this.getById(deleteRequest.getId());
        if (favorite == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "评论不存在");
        }
        // 2. 删除
        boolean result = this.removeById(deleteRequest.getId());
        if (!result) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Shop shop = shopService.getById(favorite.getShopId());
        calculateCount(shop);
        return result;
    }

    private void calculateCount(Shop shop) {
        if (shop == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "店铺不存在");
        }
        shop = shopService.getById(shop.getId());

        // 还需要重新计算商店的评论数
        shop.setFavoriteCount(shop.getFavoriteCount() + 1);

        boolean result = shopService.updateById(shop);
        if (!result) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "计算失败");
        }
    }
}




