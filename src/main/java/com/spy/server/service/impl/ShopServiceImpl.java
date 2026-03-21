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
import com.spy.server.model.domain.Shop;
import com.spy.server.model.domain.User;
import com.spy.server.model.dto.shop.ShopAddRequest;
import com.spy.server.model.dto.shop.ShopQueryRequest;
import com.spy.server.model.dto.shop.ShopUpdateRequest;
import com.spy.server.model.vo.ShopVO;
import com.spy.server.model.vo.UserVO;
import com.spy.server.service.CategoryService;
import com.spy.server.service.ShopService;
import com.spy.server.mapper.ShopMapper;
import com.spy.server.service.UserService;
import com.spy.server.utils.AccountUtil;
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
* @createDate 2026-03-20 19:51:35
*/
@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop>
    implements ShopService{

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
        List list = gson.fromJson(tags, List.class);
        shopVO.setTags(list);

        return shopVO;
    }

    @Override
    public Long addShop(ShopAddRequest shopAddRequest) {
        Shop shop = new Shop();
        BeanUtils.copyProperties(shopAddRequest, shop);

        Long managerId = shopAddRequest.getManagerId();
        User manager = userService.getById(managerId);
        if(manager == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在");
        }

        String name = shopAddRequest.getName();
        String description = shopAddRequest.getDescription();
        List<String> tags = shopAddRequest.getTags();
        String tagJson = gson.toJson(tags);
        shop.setTags(tagJson);

        Long categoryId = shopAddRequest.getCategoryId();
        Category category = categoryService.getById(categoryId);
        if(category == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "分类不存在");
        }

        BigDecimal longitude = shopAddRequest.getLongitude();
        BigDecimal latitude = shopAddRequest.getLatitude();

        String address = shopAddRequest.getAddress();
        String city = shopAddRequest.getCity();
        Integer businessStatus = shopAddRequest.getBusinessStatus();
        Integer auditStatus = shopAddRequest.getAuditStatus();
        BigDecimal avgScore = shopAddRequest.getAvgScore();
        Integer commentCount = shopAddRequest.getCommentCount();
        Integer favoriteCount = shopAddRequest.getFavoriteCount();
        Integer viewCount = shopAddRequest.getViewCount();

        // todo 这里这样加锁真的可以吗？
        String point = longitude.toString() + "," + latitude.toString();

        // todo 怎么加锁

        // 插入数据
        this.save(shop);
        return shop.getId();

    }

    @Override
    public Boolean updateShop(ShopUpdateRequest shopUpdateRequest) {
        Shop oldShop = this.getById(shopUpdateRequest.getId());
        if(oldShop == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        Shop shop = new Shop();

        BeanUtils.copyProperties(oldShop, shop);

        Long managerId = shopUpdateRequest.getManagerId();
        if(managerId != null) {
            User manager = userService.getById(managerId);
            if(manager == null){
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在");
            }
        }


        String name = shopUpdateRequest.getName();
        String description = shopUpdateRequest.getDescription();
        List<String> tags = shopUpdateRequest.getTags();
        String tagJson = gson.toJson(tags);
        shop.setTags(tagJson);


        Long categoryId = shopUpdateRequest.getCategoryId();
        if(categoryId != null) {
            Category category = categoryService.getById(categoryId);
            if(category == null){
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "分类不存在");
            }
        }


        BigDecimal longitude = shopUpdateRequest.getLongitude();
        BigDecimal latitude = shopUpdateRequest.getLatitude();
        String address = shopUpdateRequest.getAddress();
        String city = shopUpdateRequest.getCity();
        Integer businessStatus = shopUpdateRequest.getBusinessStatus();
        Integer auditStatus = shopUpdateRequest.getAuditStatus();
        BigDecimal avgScore = shopUpdateRequest.getAvgScore();
        Integer commentCount = shopUpdateRequest.getCommentCount();
        Integer favoriteCount = shopUpdateRequest.getFavoriteCount();
        Integer viewCount = shopUpdateRequest.getViewCount();

        // 插入数据
        boolean result = this.updateById(shop);
        return result;

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

        if (StringUtils.isNotBlank(searchText)) {
            queryWrapper.and(wrapper ->
                    wrapper.like("name", searchText)
                            .or()
                            .like("description", searchText)
                            .or()
                            .like("tags", searchText)
            );
        }

        queryWrapper.orderBy(SqlUtil.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC), sortField);
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




