package com.spy.server.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.spy.server.annotation.AuthCheck;
import com.spy.server.common.BaseResponse;
import com.spy.server.common.DeleteRequest;
import com.spy.server.common.ErrorCode;
import com.spy.server.constant.UserConstant;
import com.spy.server.exception.BusinessException;
import com.spy.server.model.domain.Comment;
import com.spy.server.model.domain.User;
import com.spy.server.model.dto.comment.CommentAddRequest;
import com.spy.server.model.dto.comment.CommentQueryRequest;
import com.spy.server.model.dto.comment.CommentSubmitRequest;
import com.spy.server.model.dto.comment.CommentUpdateRequest;
import com.spy.server.model.vo.CommentVO;
import com.spy.server.service.CommentService;
import com.spy.server.service.UserService;
import com.spy.server.utils.ResultUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/comment")
@Slf4j
public class CommentController {

    @Resource
    private CommentService commentService;

    @Resource
    private UserService userService;

    /**
     * 普通用户提交评论
     *
     * @param commentSubmitRequest
     * @param request
     * @return
     */
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
        commentAddRequest.setLikeCount(0);
        // todo 这里应该设置为审核状态，但是为了前期方便调试，就直接设置为0
        commentAddRequest.setStatus(0);

        Long id = commentService.submitComment(commentAddRequest);

        return ResultUtil.success(id);
    }

    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteComment(@RequestBody DeleteRequest deleteRequest) {
        if (deleteRequest == null || deleteRequest.getId() == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = commentService.adminDeleteComment(deleteRequest.getId());
        return ResultUtil.success(result);
    }

    @PostMapping("/revoke")
    public BaseResponse<Boolean> revokeComment(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = commentService.revokeComment(deleteRequest, request);
        return ResultUtil.success(result);
    }


    /**
     * 管理员添加评论
     * @param commentAddRequest
     * @param request
     * @return
     */
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



    /**
     * 管理员更新评论
     * @param commentUpdateRequest
     * @param request
     * @return
     */
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

    /**
     * 管理员获取信息评论
     * @param id
     * @param request
     * @return
     */
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

    /**
     * 普通用户获取评论信息
     * @param id
     * @param request
     * @return
     */
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

    /**
     * 管理员获取评论页
     * @param commentQueryRequest
     * @param request
     * @return
     */
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

    /**
     * 普通用户获取评论页
     * @param commentQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<CommentVO>> listCommentVOByPage(@RequestBody CommentQueryRequest commentQueryRequest, HttpServletRequest request) {
        if (commentQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Page<CommentVO> commentVOPage = commentService.listCommentVOByPage(commentQueryRequest);
        return ResultUtil.success(commentVOPage);
    }
}
