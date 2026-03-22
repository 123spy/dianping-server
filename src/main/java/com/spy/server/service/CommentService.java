package com.spy.server.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.spy.server.common.DeleteRequest;
import com.spy.server.model.domain.Comment;
import com.spy.server.model.dto.comment.CommentAddRequest;
import com.spy.server.model.dto.comment.CommentQueryRequest;
import com.spy.server.model.dto.comment.CommentUpdateRequest;
import com.spy.server.model.vo.CommentVO;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface CommentService extends IService<Comment> {

    CommentVO getCommentVO(Comment comment);

    List<CommentVO> getCommentVO(List<Comment> records);

    Long addComment(CommentAddRequest commentAddRequest);

    Long submitComment(CommentAddRequest commentAddRequest);

    Boolean updateComment(CommentUpdateRequest commentUpdateRequest);

    Boolean revokeComment(DeleteRequest deleteRequest, HttpServletRequest request);

    Boolean adminDeleteComment(Long id);

    Wrapper<Comment> getQueryWrapper(CommentQueryRequest commentQueryRequest);

    Page<CommentVO> listCommentVOByPage(CommentQueryRequest commentQueryRequest);

    void recalculateCommentCount(Long shopId);
}