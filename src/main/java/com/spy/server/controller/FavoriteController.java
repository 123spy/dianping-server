package com.spy.server.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.spy.server.annotation.AuthCheck;
import com.spy.server.common.BaseResponse;
import com.spy.server.common.DeleteRequest;
import com.spy.server.common.ErrorCode;
import com.spy.server.constant.UserConstant;
import com.spy.server.exception.BusinessException;
import com.spy.server.model.domain.Favorite;
import com.spy.server.model.domain.User;
import com.spy.server.model.dto.favorite.FavoriteAddRequest;
import com.spy.server.model.dto.favorite.FavoriteSubmitRequest;
import com.spy.server.model.dto.favorite.FavoriteQueryRequest;
import com.spy.server.model.dto.favorite.FavoriteUpdateRequest;
import com.spy.server.model.vo.FavoriteVO;
import com.spy.server.service.FavoriteService;
import com.spy.server.service.UserService;
import com.spy.server.utils.ResultUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/favorite")
@Slf4j
public class FavoriteController {

    @Resource
    private FavoriteService favoriteService;

    @Resource
    private UserService userService;

    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addFavorite(@RequestBody FavoriteAddRequest favoriteAddRequest) {
        if (favoriteAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long id = favoriteService.addFavorite(favoriteAddRequest);
        return ResultUtil.success(id);
    }

    @PostMapping("/submit")
    public BaseResponse<Long> submitFavorite(@RequestBody FavoriteAddRequest favoriteAddRequest, HttpServletRequest request) {
        if (favoriteAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        favoriteAddRequest.setUserId(loginUser.getId());
        Long id = favoriteService.submitFavorite(favoriteAddRequest);
        return ResultUtil.success(id);
    }

    @PostMapping("/revoke")
    public BaseResponse<Boolean> revokeFavorite(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = favoriteService.revokeFavorite(deleteRequest, request);
        return ResultUtil.success(result);
    }

    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteFavorite(@RequestBody DeleteRequest deleteRequest) {
        if (deleteRequest == null || deleteRequest.getId() == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = favoriteService.adminDeleteFavorite(deleteRequest.getId());
        return ResultUtil.success(result);
    }

    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateFavorite(@RequestBody FavoriteUpdateRequest favoriteUpdateRequest, HttpServletRequest request) {
        if (favoriteUpdateRequest == null || favoriteUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Favorite oldFavorite = favoriteService.getById(favoriteUpdateRequest.getId());
        if (oldFavorite == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Boolean result = favoriteService.updateFavorite(favoriteUpdateRequest);

        return ResultUtil.success(result);
    }

    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Favorite> getFavoriteById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Favorite favorite = favoriteService.getById(id);
        if (favorite == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtil.success(favorite);
    }

    @GetMapping("/get/vo")
    public BaseResponse<FavoriteVO> getFavoriteVOById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Favorite favorite = favoriteService.getById(id);
        if (favorite == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtil.success(favoriteService.getFavoriteVO(favorite));
    }

    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Favorite>> listFavoriteByPage(@RequestBody FavoriteQueryRequest favoriteQueryRequest, HttpServletRequest request) {
        if (favoriteQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        int current = favoriteQueryRequest.getCurrent();
        int pageSize = favoriteQueryRequest.getPageSize();
        Page<Favorite> favoritePage = favoriteService.page(new Page<>(current, pageSize), favoriteService.getQueryWrapper(favoriteQueryRequest));
        return ResultUtil.success(favoritePage);
    }

    @PostMapping("/list/page/vo")
    public BaseResponse<Page<FavoriteVO>> listFavoriteVOByPage(@RequestBody FavoriteQueryRequest favoriteQueryRequest, HttpServletRequest request) {
        if (favoriteQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Page<FavoriteVO> favoriteVOPage = favoriteService.listFavoriteVOByPage(favoriteQueryRequest);
        return ResultUtil.success(favoriteVOPage);
    }
}
