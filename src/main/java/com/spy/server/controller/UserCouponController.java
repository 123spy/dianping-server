package com.spy.server.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.spy.server.annotation.AuthCheck;
import com.spy.server.common.BaseResponse;
import com.spy.server.common.DeleteRequest;
import com.spy.server.common.ErrorCode;
import com.spy.server.constant.UserConstant;
import com.spy.server.exception.BusinessException;
import com.spy.server.model.domain.User;
import com.spy.server.model.domain.UserCoupon;
import com.spy.server.model.dto.usercoupon.UserCouponAddRequest;
import com.spy.server.model.dto.usercoupon.UserCouponQueryRequest;
import com.spy.server.model.dto.usercoupon.UserCouponUpdateRequest;
import com.spy.server.model.vo.UserCouponVO;
import com.spy.server.service.UserCouponService;
import com.spy.server.service.UserService;
import com.spy.server.utils.ResultUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/userCoupon")
@Slf4j
public class UserCouponController {

    @Resource
    private UserCouponService userCouponService;

    @Resource
    private UserService userService;

    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addUserCoupon(@RequestBody UserCouponAddRequest userCouponAddRequest) {
        if (userCouponAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long id = userCouponService.addUserCoupon(userCouponAddRequest);
        return ResultUtil.success(id);
    }

    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteUserCoupon(@RequestBody DeleteRequest deleteRequest) {
        if (deleteRequest == null || deleteRequest.getId() == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Boolean result = userCouponService.adminDeleteUserCoupon(deleteRequest.getId());
        return ResultUtil.success(result);
    }

    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateUserCoupon(@RequestBody UserCouponUpdateRequest userCouponUpdateRequest) {
        if (userCouponUpdateRequest == null || userCouponUpdateRequest.getId() == null || userCouponUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        UserCoupon oldUserCoupon = userCouponService.getById(userCouponUpdateRequest.getId());
        if (oldUserCoupon == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        Boolean result = userCouponService.updateUserCoupon(userCouponUpdateRequest);
        return ResultUtil.success(result);
    }

    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<UserCoupon> getUserCouponById(long id) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        UserCoupon userCoupon = userCouponService.getById(id);
        if (userCoupon == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtil.success(userCoupon);
    }

    @GetMapping("/get/vo")
    public BaseResponse<UserCouponVO> getUserCouponVOById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        UserCoupon userCoupon = userCouponService.getById(id);
        if (userCoupon == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        if (!isAdmin(loginUser) && !loginUser.getId().equals(userCoupon.getUserId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        return ResultUtil.success(userCouponService.getUserCouponVO(userCoupon, request));
    }

    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<UserCoupon>> listUserCouponByPage(@RequestBody UserCouponQueryRequest userCouponQueryRequest) {
        if (userCouponQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        int current = userCouponQueryRequest.getCurrent();
        int pageSize = userCouponQueryRequest.getPageSize();
        Page<UserCoupon> userCouponPage = userCouponService.page(
                new Page<>(current, pageSize),
                userCouponService.getQueryWrapper(userCouponQueryRequest)
        );
        return ResultUtil.success(userCouponPage);
    }

    @PostMapping("/list/page/vo")
    public BaseResponse<Page<UserCouponVO>> listUserCouponVOByPage(@RequestBody UserCouponQueryRequest userCouponQueryRequest,
                                                                   HttpServletRequest request) {
        if (userCouponQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        if (!isAdmin(loginUser)) {
            userCouponQueryRequest.setUserId(loginUser.getId());
        }
        Page<UserCouponVO> userCouponVOPage = userCouponService.listUserCouponVOByPage(userCouponQueryRequest, request);
        return ResultUtil.success(userCouponVOPage);
    }

    private boolean isAdmin(User loginUser) {
        return loginUser != null && UserConstant.ADMIN_ROLE.equals(loginUser.getUserRole());
    }
}
