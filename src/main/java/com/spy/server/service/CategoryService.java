package com.spy.server.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.spy.server.model.domain.Category;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author OUC
* @description 针对表【category(店铺分类表)】的数据库操作Service
 * @createDate 2026-03-22 13:49:31
*/
public interface CategoryService extends IService<Category> {
    com.spy.server.model.vo.CategoryVO getCategoryVO(Category category);

    Long addCategory(com.spy.server.model.dto.category.CategoryAddRequest categoryAddRequest);

    Boolean updateCategory(com.spy.server.model.dto.category.CategoryUpdateRequest categoryUpdateRequest);

    Wrapper<Category> getQueryWrapper(com.spy.server.model.dto.category.CategoryQueryRequest categoryQueryRequest);

    List<com.spy.server.model.vo.CategoryVO> getCategoryVO(List<Category> records);

    Page<com.spy.server.model.vo.CategoryVO> listCategoryVOByPage(com.spy.server.model.dto.category.CategoryQueryRequest categoryQueryRequest);
}
