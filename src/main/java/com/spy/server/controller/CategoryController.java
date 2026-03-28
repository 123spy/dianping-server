package com.spy.server.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.spy.server.annotation.AuthCheck;
import com.spy.server.common.BaseResponse;
import com.spy.server.common.DeleteRequest;
import com.spy.server.common.ErrorCode;
import com.spy.server.constant.UserConstant;
import com.spy.server.exception.BusinessException;
import com.spy.server.model.domain.Category;
import com.spy.server.model.dto.category.CategoryAddRequest;
import com.spy.server.model.dto.category.CategoryQueryRequest;
import com.spy.server.model.dto.category.CategoryUpdateRequest;
import com.spy.server.model.vo.CategoryVO;
import com.spy.server.service.CategoryService;
import com.spy.server.utils.ResultUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/category")
@Slf4j
public class CategoryController {

    @Resource
    private CategoryService categoryService;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addCategory(@RequestBody CategoryAddRequest categoryAddRequest) {
        // 校验
        if (categoryAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long id = categoryService.addCategory(categoryAddRequest);
        return ResultUtil.success(id);
    }

    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteCategory(@RequestBody DeleteRequest deleteRequest) {
        // 1. 校验
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 2. 删除
        boolean result = categoryService.removeById(deleteRequest.getId());
        if (!result) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return ResultUtil.success(true);
    }

    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateCategory(@RequestBody CategoryUpdateRequest categoryUpdateRequest, HttpServletRequest request) {
        if (categoryUpdateRequest == null || categoryUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Boolean result = categoryService.updateCategory(categoryUpdateRequest);

        return ResultUtil.success(result);
    }

    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Category> getCategoryById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Category category = categoryService.getById(id);
        if (category == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtil.success(category);
    }

    @GetMapping("/get/vo")
    public BaseResponse<CategoryVO> getCategoryVOById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Category category = categoryService.getById(id);
        if (category == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtil.success(categoryService.getCategoryVO(category));
    }

    @GetMapping("/get/all")
    public BaseResponse<List<CategoryVO>> getCategoryVOByAll(HttpServletRequest request) {
        List<Category> categoryList = categoryService.list();
        return ResultUtil.success(categoryList);
    }

    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Category>> listCategoryByPage(@RequestBody CategoryQueryRequest categoryQueryRequest, HttpServletRequest request) {
        if (categoryQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        int current = categoryQueryRequest.getCurrent();
        int pageSize = categoryQueryRequest.getPageSize();
        if (pageSize > 100) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "分页查询过大");
        }
        Page<Category> categoryPage = categoryService.page(new Page<>(current, pageSize), categoryService.getQueryWrapper(categoryQueryRequest));
        return ResultUtil.success(categoryPage);
    }

    @PostMapping("/list/page/vo")
    public BaseResponse<Page<CategoryVO>> listCategoryVOByPage(@RequestBody CategoryQueryRequest categoryQueryRequest, HttpServletRequest request) {
        if (categoryQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Page<CategoryVO> categoryVOPage = categoryService.listCategoryVOByPage(categoryQueryRequest);
        return ResultUtil.success(categoryVOPage);
    }
}
