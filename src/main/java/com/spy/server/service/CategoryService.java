package com.spy.server.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.spy.server.model.domain.Category;
import com.baomidou.mybatisplus.extension.service.IService;
import com.spy.server.model.domain.Category;
import com.spy.server.model.dto.category.CategoryAddRequest;
import com.spy.server.model.dto.category.CategoryQueryRequest;
import com.spy.server.model.dto.category.CategoryUpdateRequest;
import com.spy.server.model.vo.CategoryVO;

import java.util.List;

/**
* @author OUC
* @description 针对表【category(店铺分类表)】的数据库操作Service
* @createDate 2026-03-20 19:51:22
*/
public interface CategoryService extends IService<Category> {
    CategoryVO getCategoryVO(Category category);

    Long addCategory(CategoryAddRequest categoryAddRequest);

    Boolean updateCategory(CategoryUpdateRequest categoryUpdateRequest);

    Wrapper<Category> getQueryWrapper(CategoryQueryRequest categoryQueryRequest);

    List<CategoryVO> getCategoryVO(List<Category> records);

    Page<CategoryVO> listCategoryVOByPage(CategoryQueryRequest categoryQueryRequest);
}
