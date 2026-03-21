package com.spy.server.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.spy.server.model.domain.Comment;
import com.baomidou.mybatisplus.extension.service.IService;
import com.spy.server.model.domain.Comment;
import com.spy.server.model.dto.comment.CommentAddRequest;
import com.spy.server.model.dto.comment.CommentQueryRequest;
import com.spy.server.model.dto.comment.CommentUpdateRequest;
import com.spy.server.model.vo.CommentVO;

import java.util.List;

/**
* @author OUC
* @description 针对表【comment(评论表)】的数据库操作Service
* @createDate 2026-03-20 19:51:25
*/
public interface CommentService extends IService<Comment> {
    CommentVO getCommentVO(Comment comment);

    Long addComment(CommentAddRequest commentAddRequest);

    Boolean updateComment(CommentUpdateRequest commentUpdateRequest);

    Wrapper<Comment> getQueryWrapper(CommentQueryRequest commentQueryRequest);

    List<CommentVO> getCommentVO(List<Comment> records);

    Page<CommentVO> listCommentVOByPage(CommentQueryRequest commentQueryRequest);
}
