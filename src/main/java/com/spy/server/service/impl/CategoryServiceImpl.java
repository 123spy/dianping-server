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
import com.spy.server.model.domain.Category;
import com.spy.server.model.domain.User;
import com.spy.server.model.dto.category.CategoryAddRequest;
import com.spy.server.model.dto.category.CategoryQueryRequest;
import com.spy.server.model.dto.category.CategoryUpdateRequest;
import com.spy.server.model.vo.CategoryVO;
import com.spy.server.model.vo.UserVO;
import com.spy.server.service.CategoryService;
import com.spy.server.mapper.CategoryMapper;
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
 * @description 针对表【category(分类分类表)】的数据库操作Service实现
* @createDate 2026-03-20 19:51:22
*/
@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category>
    implements CategoryService{

    @Override
    public CategoryVO getCategoryVO(Category category) {
        CategoryVO categoryVO = new CategoryVO();
        if (category == null) {
            return categoryVO;
        }
        BeanUtils.copyProperties(category, categoryVO);

        return categoryVO;
    }

    @Override
    public Long addCategory(CategoryAddRequest categoryAddRequest) {
        if (categoryAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        if (StringUtils.isBlank(categoryAddRequest.getName())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "分类名称为空");
        }

        String name = categoryAddRequest.getName();
        if (name.length() > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "分类名称过长");
        }

        Category category = new Category();
        BeanUtils.copyProperties(categoryAddRequest, category);

        boolean result = this.save(category);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "新增分类失败");
        }

        return category.getId();
    }

    @Override
    public Boolean updateCategory(CategoryUpdateRequest req) {
        if (req == null || req.getId() == null || req.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        Category oldCategory = this.getById(req.getId());
        if (oldCategory == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "分类不存在");
        }

        Category updateCategory = new Category();
        updateCategory.setId(req.getId());


        String name = req.getName();
        if (StringUtils.isNotBlank(req.getName())) {
            if (name.length() > 20) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "分类名称过长");
            }
            updateCategory.setName(req.getName().trim());
        }

        Integer sort = req.getSort();
        if (req.getSort() != null) {
            updateCategory.setSort(sort);
        }

        return this.updateById(updateCategory);
    }

    @Override
    public Wrapper<Category> getQueryWrapper(CategoryQueryRequest categoryQueryRequest) {
        if (categoryQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        Long id = categoryQueryRequest.getId();
        String name = categoryQueryRequest.getName();
        Integer sort = categoryQueryRequest.getSort();
        Date createTime = categoryQueryRequest.getCreateTime();
        Date updateTime = categoryQueryRequest.getUpdateTime();
        int current = categoryQueryRequest.getCurrent();
        int pageSize = categoryQueryRequest.getPageSize();
        String searchText = categoryQueryRequest.getSearchText();
        String sortField = categoryQueryRequest.getSortField();
        String sortOrder = categoryQueryRequest.getSortOrder();


        QueryWrapper<Category> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(id != null, "id", id);
        queryWrapper.eq(sort != null, "sort", sort);

        queryWrapper.like(StringUtils.isNotBlank(name), "name", name);
        queryWrapper.like(StringUtils.isNotBlank(searchText), "name", searchText);

        queryWrapper.orderBy(
                SqlUtil.validSortField(sortField),
                CommonConstant.SORT_ORDER_ASC.equals(sortOrder),
                sortField
        );
        return queryWrapper;
    }

    @Override
    public List<CategoryVO> getCategoryVO(List<Category> records) {
        if (CollectionUtils.isEmpty(records)) {
            return new ArrayList<>();
        }
        List<CategoryVO> categoryVOList = records.stream().map(category -> {
            return getCategoryVO(category);
        }).collect(Collectors.toList());
        return categoryVOList;
    }

    @Override
    public Page<CategoryVO> listCategoryVOByPage(CategoryQueryRequest categoryQueryRequest) {
        int current = categoryQueryRequest.getCurrent();
        int pageSize = categoryQueryRequest.getPageSize();
        Page<Category> categoryPage = this.page(new Page<>(current, pageSize), this.getQueryWrapper(categoryQueryRequest));
        Page<CategoryVO> categoryVOPage = new Page<>(current, pageSize, categoryPage.getTotal());
        List<CategoryVO> categoryVoList = this.getCategoryVO(categoryPage.getRecords());
        categoryVOPage.setRecords(categoryVoList);
        return categoryVOPage;
    }

}




