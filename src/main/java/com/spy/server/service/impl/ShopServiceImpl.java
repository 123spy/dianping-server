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
import com.spy.server.model.domain.Category;
import com.spy.server.model.domain.Shop;
import com.spy.server.model.domain.User;
import com.spy.server.model.dto.shop.ShopAddRequest;
import com.spy.server.model.dto.shop.ShopQueryRequest;
import com.spy.server.model.dto.shop.ShopUpdateRequest;
import com.spy.server.model.vo.ShopVO;
import com.spy.server.model.vo.UserVO;
import com.spy.server.mapper.ShopMapper;
import com.spy.server.service.CategoryService;
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
* @description 针对表【shop(店铺表)】的数据库操作Service实现
 * @createDate 2026-03-22 13:49:43
*/
@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop>
        implements ShopService {

    private final Gson gson = new Gson();

    @Resource
    private UserService userService;

    @Resource
    private CategoryService categoryService;


    @Override
    public ShopVO getShopVO(Shop shop) {
        ShopVO shopVO = new ShopVO();
        if(shop==null){
            return shopVO;
        }
        BeanUtils.copyProperties(shop,shopVO);

        UserVO userVO = userService.getUserVO(userService.getById(shop.getManagerId()));
        shopVO.setUserVO(userVO);

        String tags = shop.getTags();
        if (StringUtils.isBlank(tags)) {
            shopVO.setTags(new ArrayList<>());
        } else {
            try {
                List list = gson.fromJson(tags, List.class);
                shopVO.setTags(list == null ? new ArrayList<>() : list);
            } catch (Exception e) {
                shopVO.setTags(new ArrayList<>());
            }
        }

        return shopVO;
    }

    @Override
    public Long addShop(ShopAddRequest shopAddRequest) {
        if (shopAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        Long managerId = shopAddRequest.getManagerId();
        String name = shopAddRequest.getName();
        String description = shopAddRequest.getDescription();
        List<String> tags = shopAddRequest.getTags();
        Long categoryId = shopAddRequest.getCategoryId();
        BigDecimal longitude = shopAddRequest.getLongitude();
        BigDecimal latitude = shopAddRequest.getLatitude();
        String address = shopAddRequest.getAddress();
        String city = shopAddRequest.getCity();
        Integer businessStatus = shopAddRequest.getBusinessStatus();
        Integer auditStatus = shopAddRequest.getAuditStatus();
        BigDecimal avgScore = shopAddRequest.getAvgScore();
        Integer ratingCount = shopAddRequest.getRatingCount();
        Integer commentCount = shopAddRequest.getCommentCount();
        Integer favoriteCount = shopAddRequest.getFavoriteCount();
        Integer viewCount = shopAddRequest.getViewCount();


        if (managerId == null || managerId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "店长参数错误");
        }
        User manager = userService.getById(managerId);
        if (manager == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "店长不存在");
        }

        if (StringUtils.isBlank(name)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "名称为空");
        }

        if (categoryId == null || categoryId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "类别参数错误");
        }
        Category category = categoryService.getById(categoryId);
        if (category == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "类别不存在");
        }

        Shop shop = new Shop();
        shop.setManagerId(managerId);
        shop.setName(name.trim());
        shop.setDescription(description);

        if (tags == null) {
            shop.setTags(gson.toJson(new ArrayList<>()));
        } else {
            shop.setTags(gson.toJson(tags));
        }

        shop.setCategoryId(categoryId);
        shop.setLongitude(longitude);
        shop.setLatitude(latitude);
        shop.setAddress(address);
        shop.setCity(city);
        shop.setBusinessStatus(businessStatus);
        shop.setAuditStatus(auditStatus);
        shop.setAvgScore(avgScore);
        shop.setRatingCount(ratingCount);
        shop.setCommentCount(commentCount);
        shop.setFavoriteCount(favoriteCount);
        shop.setViewCount(viewCount);

        boolean result = this.save(shop);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "新增店铺失败");
        }

        return shop.getId();
    }

    @Override
    public Boolean updateShop(ShopUpdateRequest req) {
        if (req == null || req.getId() == null || req.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        Shop oldShop = this.getById(req.getId());
        if (oldShop == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "店铺不存在");
        }

        Shop updateShop = new Shop();
        updateShop.setId(req.getId());

        if (req.getManagerId() != null) {
            User manager = userService.getById(req.getManagerId());
            if (manager == null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "店长不存在");
            }
            updateShop.setManagerId(req.getManagerId());
        }

        if (StringUtils.isNotBlank(req.getName())) {
            updateShop.setName(req.getName().trim());
        }

        if (StringUtils.isNotBlank(req.getDescription())) {
            updateShop.setDescription(req.getDescription().trim());
        }

        if (req.getTags() != null) {
            updateShop.setTags(gson.toJson(req.getTags()));
        }

        if (req.getCategoryId() != null) {
            Category category = categoryService.getById(req.getCategoryId());
            if (category == null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "分类不存在");
            }
            updateShop.setCategoryId(req.getCategoryId());
        }

        if (req.getLongitude() != null) {
            updateShop.setLongitude(req.getLongitude());
        }
        if (req.getLatitude() != null) {
            updateShop.setLatitude(req.getLatitude());
        }
        if (StringUtils.isNotBlank(req.getAddress())) {
            updateShop.setAddress(req.getAddress().trim());
        }
        if (StringUtils.isNotBlank(req.getCity())) {
            updateShop.setCity(req.getCity().trim());
        }
        if (req.getBusinessStatus() != null) {
            updateShop.setBusinessStatus(req.getBusinessStatus());
        }
        if (req.getAuditStatus() != null) {
            updateShop.setAuditStatus(req.getAuditStatus());
        }
        if (req.getAvgScore() != null) {
            updateShop.setAvgScore(req.getAvgScore());
        }
        if (req.getRatingCount() != null) {
            updateShop.setRatingCount(req.getRatingCount());
        }
        if (req.getCommentCount() != null) {
            updateShop.setCommentCount(req.getCommentCount());
        }
        if (req.getFavoriteCount() != null) {
            updateShop.setFavoriteCount(req.getFavoriteCount());
        }
        if (req.getViewCount() != null) {
            updateShop.setViewCount(req.getViewCount());
        }

        return this.updateById(updateShop);
    }

    @Override
    public Wrapper<Shop> getQueryWrapper(ShopQueryRequest shopQueryRequest) {
        if(shopQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        Long id = shopQueryRequest.getId();
        Long managerId = shopQueryRequest.getManagerId();
        String name = shopQueryRequest.getName();
        String description = shopQueryRequest.getDescription();
        String tags = shopQueryRequest.getTags();
        Long categoryId = shopQueryRequest.getCategoryId();
        BigDecimal longitude = shopQueryRequest.getLongitude();
        BigDecimal latitude = shopQueryRequest.getLatitude();
        String address = shopQueryRequest.getAddress();
        String city = shopQueryRequest.getCity();
        Integer businessStatus = shopQueryRequest.getBusinessStatus();
        Integer auditStatus = shopQueryRequest.getAuditStatus();
        BigDecimal avgScore = shopQueryRequest.getAvgScore();
        Integer ratingCount = shopQueryRequest.getRatingCount();
        Integer commentCount = shopQueryRequest.getCommentCount();
        Integer favoriteCount = shopQueryRequest.getFavoriteCount();
        Integer viewCount = shopQueryRequest.getViewCount();
        Date createTime = shopQueryRequest.getCreateTime();
        Date updateTime = shopQueryRequest.getUpdateTime();
        int current = shopQueryRequest.getCurrent();
        int pageSize = shopQueryRequest.getPageSize();
        String searchText = shopQueryRequest.getSearchText();
        String sortField = shopQueryRequest.getSortField();
        String sortOrder = shopQueryRequest.getSortOrder();


        QueryWrapper<Shop> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(id != null, "id", id);
        queryWrapper.eq(managerId != null, "managerId", managerId);
        queryWrapper.eq(categoryId != null, "categoryId", categoryId);
        queryWrapper.eq(businessStatus != null, "businessStatus", businessStatus);
        queryWrapper.eq(auditStatus != null, "auditStatus", auditStatus);

        queryWrapper.like(StringUtils.isNotBlank(name), "name", name);
        queryWrapper.like(StringUtils.isNotBlank(description), "description", description);
        queryWrapper.like(StringUtils.isNotBlank(tags), "tags", tags);
        queryWrapper.like(StringUtils.isNotBlank(address), "address", address);
        queryWrapper.like(StringUtils.isNotBlank(city), "city", city);

        if (StringUtils.isNotBlank(searchText)) {
            queryWrapper.and(wrapper ->
                    wrapper.like("name", searchText)
                            .or()
                            .like("description", searchText)
                            .or()
                            .like("tags", searchText)
                            .or()
                            .like("address", searchText)
                            .or()
                            .like("city", searchText)
            );
        }

        queryWrapper.orderBy(
                SqlUtil.validSortField(sortField),
                CommonConstant.SORT_ORDER_ASC.equals(sortOrder),
                sortField
        );
        return queryWrapper;
    }

    @Override
    public List<ShopVO> getShopVO(List<Shop> records) {
        if (CollectionUtils.isEmpty(records)) {
            return new ArrayList<>();
        }
        List<ShopVO> shopVOList = records.stream().map(shop -> {
            return getShopVO(shop);
        }).collect(Collectors.toList());
        return shopVOList;
    }

    @Override
    public Page<ShopVO> listShopVOByPage(ShopQueryRequest shopQueryRequest) {
        int current = shopQueryRequest.getCurrent();
        int pageSize = shopQueryRequest.getPageSize();
        Page<Shop> shopPage = this.page(new Page<>(current, pageSize), this.getQueryWrapper(shopQueryRequest));
        Page<ShopVO> shopVOPage = new Page<>(current, pageSize, shopPage.getTotal());
        List<ShopVO> shopVoList = this.getShopVO(shopPage.getRecords());
        shopVOPage.setRecords(shopVoList);
        return shopVOPage;
    }
}




