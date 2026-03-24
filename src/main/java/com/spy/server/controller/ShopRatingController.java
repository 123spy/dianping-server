package com.spy.server.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.spy.server.annotation.AuthCheck;
import com.spy.server.common.BaseResponse;
import com.spy.server.common.DeleteRequest;
import com.spy.server.common.ErrorCode;
import com.spy.server.constant.UserConstant;
import com.spy.server.exception.BusinessException;
import com.spy.server.model.domain.ShopRating;
import com.spy.server.model.domain.User;
import com.spy.server.model.dto.shoprating.ShopRatingAddRequest;
import com.spy.server.model.dto.shoprating.ShopRatingQueryRequest;
import com.spy.server.model.dto.shoprating.ShopRatingUpdateRequest;
import com.spy.server.model.vo.ShopRatingVO;
import com.spy.server.service.ShopRatingService;
import com.spy.server.service.UserService;
import com.spy.server.utils.ResultUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/shopRating")
@Slf4j
public class ShopRatingController {

    @Resource
    private ShopRatingService shopRatingService;

    @Resource
    private UserService userService;

    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addShopRating(@RequestBody ShopRatingAddRequest shopRatingAddRequest) {
        if (shopRatingAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long id = shopRatingService.addShopRating(shopRatingAddRequest);
        return ResultUtil.success(id);
    }

    @PostMapping("/submit")
    public BaseResponse<Long> submitShopRating(@RequestBody ShopRatingAddRequest shopRatingAddRequest, HttpServletRequest request) {
        if (shopRatingAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        shopRatingAddRequest.setUserId(loginUser.getId());
        Long id = shopRatingService.submitShopRating(shopRatingAddRequest);
        return ResultUtil.success(id);
    }

    @PostMapping("/revoke")
    public BaseResponse<Boolean> revokeShopRating(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = shopRatingService.revokeShopRating(deleteRequest, request);
        return ResultUtil.success(result);
    }

    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteShopRating(@RequestBody DeleteRequest deleteRequest) {
        if (deleteRequest == null || deleteRequest.getId() == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = shopRatingService.adminDeleteShopRating(deleteRequest.getId());
        return ResultUtil.success(result);
    }

    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateShopRating(@RequestBody ShopRatingUpdateRequest shopRatingUpdateRequest, HttpServletRequest request) {
        if (shopRatingUpdateRequest == null || shopRatingUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        ShopRating oldShopRating = shopRatingService.getById(shopRatingUpdateRequest.getId());
        if (oldShopRating == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Boolean result = shopRatingService.updateShopRating(shopRatingUpdateRequest);

        return ResultUtil.success(result);
    }

    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<ShopRating> getShopRatingById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        ShopRating shopRating = shopRatingService.getById(id);
        if (shopRating == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtil.success(shopRating);
    }

    @GetMapping("/get/vo")
    public BaseResponse<ShopRatingVO> getShopRatingVOById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        ShopRating shopRating = shopRatingService.getById(id);
        if (shopRating == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtil.success(shopRatingService.getShopRatingVO(shopRating, request));
    }

    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<ShopRating>> listShopRatingByPage(@RequestBody ShopRatingQueryRequest shopRatingQueryRequest, HttpServletRequest request) {
        if (shopRatingQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        int current = shopRatingQueryRequest.getCurrent();
        int pageSize = shopRatingQueryRequest.getPageSize();
        Page<ShopRating> shopRatingPage = shopRatingService.page(new Page<>(current, pageSize), shopRatingService.getQueryWrapper(shopRatingQueryRequest));
        return ResultUtil.success(shopRatingPage);
    }

    @PostMapping("/list/page/vo")
    public BaseResponse<Page<ShopRatingVO>> listShopRatingVOByPage(@RequestBody ShopRatingQueryRequest shopRatingQueryRequest, HttpServletRequest request) {
        if (shopRatingQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Page<ShopRatingVO> shopRatingVOPage = shopRatingService.listShopRatingVOByPage(shopRatingQueryRequest, request);
        return ResultUtil.success(shopRatingVOPage);
    }
}
