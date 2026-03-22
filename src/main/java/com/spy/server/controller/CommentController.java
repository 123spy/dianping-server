package com.spy.server.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.spy.server.annotation.AuthCheck;
import com.spy.server.common.BaseResponse;
import com.spy.server.common.DeleteRequest;
import com.spy.server.common.ErrorCode;
import com.spy.server.constant.UserConstant;
import com.spy.server.exception.BusinessException;
import com.spy.server.model.domain.Comment;
import com.spy.server.model.domain.Shop;
import com.spy.server.model.domain.User;
import com.spy.server.model.dto.comment.CommentAddRequest;
import com.spy.server.model.dto.comment.CommentQueryRequest;
import com.spy.server.model.dto.comment.CommentSubmitRequest;
import com.spy.server.model.dto.comment.CommentUpdateRequest;
import com.spy.server.model.vo.CommentVO;
import com.spy.server.service.CommentService;
import com.spy.server.service.ShopService;
import com.spy.server.service.UserService;
import com.spy.server.utils.ResultUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/comment")
@Slf4j
public class CommentController {

    @Resource
    private CommentService commentService;

    @Resource
    private UserService userService;

    @Resource
    private ShopService shopService;

    @PostMapping("/submit")
    public BaseResponse<Long> submitComment(@RequestBody CommentSubmitRequest commentSubmitRequest,
                                            HttpServletRequest request) {
        if (commentSubmitRequest == null || commentSubmitRequest.getShopId() == null || commentSubmitRequest.getShopId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        User loginUser = userService.getLoginUser(request);
        CommentAddRequest commentAddRequest = new CommentAddRequest();

        commentAddRequest.setUserId(loginUser.getId());
        commentAddRequest.setShopId(commentSubmitRequest.getShopId());
        commentAddRequest.setContent(commentSubmitRequest.getContent());
        commentAddRequest.setScore(commentSubmitRequest.getScore());
        commentAddRequest.setLikeCount(0);
        // todo 这里应该设置为审核状态，但是为了前期方便调试，就直接设置为0
        commentAddRequest.setStatus(0);

        Long id = commentService.submitComment(commentAddRequest);

        return ResultUtil.success(id);
    }

    @PostMapping("/delete/my")
    public BaseResponse<Boolean> deleteMyComment(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        // 1. 校验
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);

        Boolean result = commentService.deleteMyComment(deleteRequest, request);


        return ResultUtil.success(true);
    }


    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addComment(@RequestBody CommentAddRequest commentAddRequest,
                                         HttpServletRequest request) {
        if (commentAddRequest == null || commentAddRequest.getShopId() == null || commentAddRequest.getShopId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        Long id = commentService.addComment(commentAddRequest);
        return ResultUtil.success(id);
    }

    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteComment(@RequestBody DeleteRequest deleteRequest) {
        // 1. 校验
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 2. 删除
        boolean result = commentService.removeById(deleteRequest.getId());
        if (!result) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return ResultUtil.success(true);
    }

    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateComment(@RequestBody CommentUpdateRequest commentUpdateRequest, HttpServletRequest request) {
        if (commentUpdateRequest == null || commentUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Comment oldComment = commentService.getById(commentUpdateRequest.getId());
        if (oldComment == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Boolean result = commentService.updateComment(commentUpdateRequest);

        return ResultUtil.success(result);
    }

    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Comment> getCommentById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Comment comment = commentService.getById(id);
        if (comment == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtil.success(comment);
    }

    @GetMapping("/get/vo")
    public BaseResponse<CommentVO> getCommentVOById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Comment comment = commentService.getById(id);
        if (comment == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtil.success(commentService.getCommentVO(comment));
    }

    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Comment>> listCommentByPage(@RequestBody CommentQueryRequest commentQueryRequest, HttpServletRequest request) {
        if (commentQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        int current = commentQueryRequest.getCurrent();
        int pageSize = commentQueryRequest.getPageSize();
        Page<Comment> commentPage = commentService.page(new Page<>(current, pageSize), commentService.getQueryWrapper(commentQueryRequest));
        return ResultUtil.success(commentPage);
    }

    @PostMapping("/list/page/vo")
    public BaseResponse<Page<CommentVO>> listCommentVOByPage(@RequestBody CommentQueryRequest commentQueryRequest, HttpServletRequest request) {
        if (commentQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Page<CommentVO> commentVOPage = commentService.listCommentVOByPage(commentQueryRequest);
        return ResultUtil.success(commentVOPage);
    }
}
