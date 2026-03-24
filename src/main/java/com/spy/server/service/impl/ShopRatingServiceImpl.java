package com.spy.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.spy.server.common.DeleteRequest;
import com.spy.server.common.ErrorCode;
import com.spy.server.constant.CommonConstant;
import com.spy.server.exception.BusinessException;
import com.spy.server.model.domain.ShopRating;
import com.spy.server.model.domain.Shop;
import com.spy.server.model.domain.User;
import com.spy.server.model.dto.shoprating.ShopRatingAddRequest;
import com.spy.server.model.dto.shoprating.ShopRatingQueryRequest;
import com.spy.server.model.dto.shoprating.ShopRatingUpdateRequest;
import com.spy.server.model.vo.ShopRatingVO;
import com.spy.server.model.vo.ShopVO;
import com.spy.server.model.vo.UserVO;
import com.spy.server.service.ShopRatingService;
import com.spy.server.mapper.ShopRatingMapper;
import com.spy.server.service.ShopService;
import com.spy.server.service.UserService;
import com.spy.server.utils.SqlUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
* @author OUC
* @description 针对表【shop_rating(店铺评分表)】的数据库操作Service实现
* @createDate 2026-03-22 13:49:45
*/
@Service
public class ShopRatingServiceImpl extends ServiceImpl<ShopRatingMapper, ShopRating>
    implements ShopRatingService{
    @Resource
    private UserService userService;

    @Resource
    private ShopService shopService;

    @Override
    public ShopRatingVO getShopRatingVO(ShopRating shopRating, HttpServletRequest request) {
        if (shopRating == null) {
            return null;
        }

        ShopRatingVO shopRatingVO = new ShopRatingVO();
        BeanUtils.copyProperties(shopRating, shopRatingVO);

        User user = userService.getById(shopRating.getUserId());
        if (user != null) {
            UserVO userVO = userService.getUserVO(user);
            shopRatingVO.setUserVO(userVO);
        }

        Shop shop = shopService.getById(shopRating.getShopId());
        if (shop != null) {
            ShopVO shopVO = shopService.getShopVO(shop, request);
            shopRatingVO.setShopVO(shopVO);
        }

        return shopRatingVO;
    }

    @Override
    public List<ShopRatingVO> getShopRatingVO(List<ShopRating> records, HttpServletRequest request) {
        if (CollectionUtils.isEmpty(records)) {
            return new ArrayList<>();
        }

        Set<Long> userIdSet = records.stream()
                .map(ShopRating::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Set<Long> shopIdSet = records.stream()
                .map(ShopRating::getShopId)
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

        return records.stream().map(shopRating -> {
            ShopRatingVO shopRatingVO = new ShopRatingVO();
            BeanUtils.copyProperties(shopRating, shopRatingVO);
            shopRatingVO.setUserVO(finalUserVOMap.get(shopRating.getUserId()));
            shopRatingVO.setShopVO(finalShopVOMap.get(shopRating.getShopId()));
            return shopRatingVO;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addShopRating(ShopRatingAddRequest shopRatingAddRequest) {
        return saveShopRating(shopRatingAddRequest);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long submitShopRating(ShopRatingAddRequest shopRatingAddRequest) {
        return saveShopRating(shopRatingAddRequest);
    }

    private Long saveShopRating(ShopRatingAddRequest shopRatingAddRequest) {
        if (shopRatingAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        Long userId = shopRatingAddRequest.getUserId();
        Long shopId = shopRatingAddRequest.getShopId();

        if (userId == null || userId <= 0 || shopId == null || shopId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户或店铺参数错误");
        }

        if (userService.getById(userId) == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在");
        }

        if (shopService.getById(shopId) == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "店铺不存在");
        }

        QueryWrapper<ShopRating> existsWrapper = new QueryWrapper<>();
        existsWrapper.eq("userId", userId)
                .eq("shopId", shopId)
                .eq("isDelete", 0);

        ShopRating exists = this.getOne(existsWrapper);
        if (exists != null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "已经评价过该店铺");
        }

        ShopRating shopRating = new ShopRating();
        Integer score = shopRatingAddRequest.getScore();
        if (score == null || score < 0 || score > 5) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "评分非法");
        }
        BeanUtils.copyProperties(shopRatingAddRequest, shopRating);

        boolean saved = this.save(shopRating);
        if (!saved) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "评价失败");
        }

        recalculateShopRatingCount(shopId);
        return shopRating.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateShopRating(ShopRatingUpdateRequest shopRatingUpdateRequest) {
        if (shopRatingUpdateRequest == null || shopRatingUpdateRequest.getId() == null || shopRatingUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        ShopRating oldShopRating = this.getById(shopRatingUpdateRequest.getId());
        if (oldShopRating == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "评价不存在");
        }

        Long userId = shopRatingUpdateRequest.getUserId();
        if (userId != null && userService.getById(userId) == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在");
        }

        Long newShopId = shopRatingUpdateRequest.getShopId();
        if (newShopId != null && shopService.getById(newShopId) == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "店铺不存在");
        }

        Long finalUserId = userId != null ? userId : oldShopRating.getUserId();
        Long finalShopId = newShopId != null ? newShopId : oldShopRating.getShopId();

        QueryWrapper<ShopRating> existsWrapper = new QueryWrapper<>();
        existsWrapper.eq("userId", finalUserId)
                .eq("shopId", finalShopId)
                .eq("isDelete", 0)
                .ne("id", oldShopRating.getId());

        ShopRating duplicate = this.getOne(existsWrapper);
        if (duplicate != null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "该评价关系已存在");
        }

        ShopRating shopRating = new ShopRating();
        BeanUtils.copyProperties(shopRatingUpdateRequest, shopRating);

        boolean updated = this.updateById(shopRating);
        if (!updated) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新失败");
        }

        Long oldShopId = oldShopRating.getShopId();
        recalculateShopRatingCount(oldShopId);
        if (!Objects.equals(oldShopId, finalShopId)) {
            recalculateShopRatingCount(finalShopId);
        }

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean revokeShopRating(DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        User loginUser = userService.getLoginUser(request);

        ShopRating shopRating = this.getById(deleteRequest.getId());
        if (shopRating == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "评价不存在");
        }
        if (!Objects.equals(shopRating.getUserId(), loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限取消评分");
        }

        boolean result = this.removeById(deleteRequest.getId());
        if (!result) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "取消评分失败");
        }

        recalculateShopRatingCount(shopRating.getShopId());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean adminDeleteShopRating(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        ShopRating shopRating = this.getById(id);
        if (shopRating == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "评分不存在");
        }

        boolean result = this.removeById(id);
        if (!result) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "删除失败");
        }

        recalculateShopRatingCount(shopRating.getShopId());
        return true;
    }

    @Override
    public Wrapper<ShopRating> getQueryWrapper(ShopRatingQueryRequest shopRatingQueryRequest) {
        if (shopRatingQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        Long id = shopRatingQueryRequest.getId();
        Long userId = shopRatingQueryRequest.getUserId();
        Long shopId = shopRatingQueryRequest.getShopId();
        Date createTime = shopRatingQueryRequest.getCreateTime();
        Date updateTime = shopRatingQueryRequest.getUpdateTime();
        String sortField = shopRatingQueryRequest.getSortField();
        String sortOrder = shopRatingQueryRequest.getSortOrder();

        QueryWrapper<ShopRating> queryWrapper = new QueryWrapper<>();
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
    public Page<ShopRatingVO> listShopRatingVOByPage(ShopRatingQueryRequest shopRatingQueryRequest, HttpServletRequest request) {
        int current = shopRatingQueryRequest.getCurrent();
        int pageSize = shopRatingQueryRequest.getPageSize();

        Page<ShopRating> shopRatingPage = this.page(new Page<>(current, pageSize), this.getQueryWrapper(shopRatingQueryRequest));
        Page<ShopRatingVO> shopRatingVOPage = new Page<>(current, pageSize, shopRatingPage.getTotal());
        shopRatingVOPage.setRecords(this.getShopRatingVO(shopRatingPage.getRecords(), request));
        return shopRatingVOPage;
    }

    @Override
    public void recalculateShopRatingCount(Long shopId) {
        if (shopId == null || shopId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "店铺参数错误");
        }

        Shop shop = shopService.getById(shopId);
        if (shop == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "店铺不存在");
        }

        QueryWrapper<ShopRating> wrapper = new QueryWrapper<>();
        wrapper.eq("shopId", shopId);
        wrapper.eq("isDelete", 0);

        List<ShopRating> ratingList = this.list(wrapper);

        // todo

        shop.setRatingCount((int) ratingList.size());
        // 计算分数
        if (ratingList.size() != 0) {
            double sumScore = 0;
            for (ShopRating shopRating : ratingList) {
                sumScore += shopRating.getScore();
            }
            BigDecimal avgScore = new BigDecimal(sumScore / ratingList.size());
            shop.setAvgScore(avgScore);
        } else {
            shop.setAvgScore(BigDecimal.ZERO);
        }

        boolean result = shopService.updateById(shop);
        if (!result) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "重算评分失败");
        }
    }
}




