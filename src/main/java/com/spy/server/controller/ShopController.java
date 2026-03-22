package com.spy.server.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.spy.server.annotation.AuthCheck;
import com.spy.server.common.BaseResponse;
import com.spy.server.common.DeleteRequest;
import com.spy.server.common.ErrorCode;
import com.spy.server.constant.UserConstant;
import com.spy.server.exception.BusinessException;
import com.spy.server.model.domain.Shop;
import com.spy.server.model.dto.shop.*;
import com.spy.server.model.vo.ShopVO;
import com.spy.server.service.ShopService;
import com.spy.server.utils.ResultUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/shop")
@Slf4j
public class ShopController {

    @Resource
    private ShopService shopService;

    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addShop(@RequestBody ShopAddRequest shopAddRequest) {
        // 校验
        if (shopAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long id = shopService.addShop(shopAddRequest);
        return ResultUtil.success(id);
    }

    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteShop(@RequestBody DeleteRequest deleteRequest) {
        // 1. 校验
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 2. 删除
        boolean result = shopService.removeById(deleteRequest.getId());
        if (!result) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return ResultUtil.success(true);
    }

    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateShop(@RequestBody ShopUpdateRequest shopUpdateRequest, HttpServletRequest request) {
        if (shopUpdateRequest == null || shopUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Boolean result = shopService.updateShop(shopUpdateRequest);

        return ResultUtil.success(result);
    }

    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Shop> getShopById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Shop shop = shopService.getById(id);
        if (shop == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtil.success(shop);
    }

    @GetMapping("/get/vo")
    public BaseResponse<ShopVO> getShopVOById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Shop shop = shopService.getById(id);
        if (shop == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtil.success(shopService.getShopVO(shop));
    }

    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Shop>> listShopByPage(@RequestBody ShopQueryRequest shopQueryRequest, HttpServletRequest request) {
        if (shopQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        int current = shopQueryRequest.getCurrent();
        int pageSize = shopQueryRequest.getPageSize();
        if(pageSize > 100) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "分页查询过大");
        }
        Page<Shop> shopPage = shopService.page(new Page<>(current, pageSize), shopService.getQueryWrapper(shopQueryRequest));
        return ResultUtil.success(shopPage);
    }

    @PostMapping("/list/page/vo")
    public BaseResponse<Page<ShopVO>> listShopVOByPage(@RequestBody ShopQueryRequest shopQueryRequest, HttpServletRequest request) {
        if (shopQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Page<ShopVO> shopVOPage = shopService.listShopVOByPage(shopQueryRequest);
        return ResultUtil.success(shopVOPage);
    }
}
