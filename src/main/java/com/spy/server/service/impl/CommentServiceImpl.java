package com.spy.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.spy.server.common.ErrorCode;
import com.spy.server.constant.CommonConstant;
import com.spy.server.exception.BusinessException;
import com.spy.server.mapper.CommentMapper;
import com.spy.server.model.domain.Comment;
import com.spy.server.model.domain.Shop;
import com.spy.server.model.domain.User;
import com.spy.server.model.dto.comment.CommentAddRequest;
import com.spy.server.model.dto.comment.CommentQueryRequest;
import com.spy.server.model.dto.comment.CommentUpdateRequest;
import com.spy.server.model.vo.CommentVO;
import com.spy.server.model.vo.UserVO;
import com.spy.server.service.CommentService;
import com.spy.server.service.ShopService;
import com.spy.server.service.UserService;
import com.spy.server.utils.SqlUtil;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {

    @Resource
    private UserService userService;

    @Resource
    private ShopService shopService;

    @Override
    public CommentVO getCommentVO(Comment comment) {
        if (comment == null) {
            return null;
        }
        CommentVO commentVO = new CommentVO();
        BeanUtils.copyProperties(comment, commentVO);

        UserVO userVO = userService.getUserVO(userService.getById(comment.getUserId()));
        commentVO.setUserVO(userVO);
        return commentVO;
    }

    @Override
    public Long addComment(CommentAddRequest commentAddRequest) {
        if (commentAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        Long userId = commentAddRequest.getUserId();
        Long shopId = commentAddRequest.getShopId();
        Integer score = commentAddRequest.getScore();

        if (userId == null || userId <= 0 || shopId == null || shopId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户或店铺参数错误");
        }
        if (StringUtils.isBlank(commentAddRequest.getContent())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "评论内容不能为空");
        }
        if (score == null || score < 1 || score > 5) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "评分必须在 1 到 5 之间");
        }

        User user = userService.getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在");
        }

        Shop shop = shopService.getById(shopId);
        if (shop == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "店铺不存在");
        }

        Comment comment = new Comment();
        BeanUtils.copyProperties(commentAddRequest, comment);

        if (comment.getLikeCount() == null) {
            comment.setLikeCount(0);
        }
        if (comment.getStatus() == null) {
            comment.setStatus(0);
        }

        boolean saved = this.save(comment);
        if (!saved) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "评论失败");
        }
        return comment.getId();
    }

    @Override
    public Boolean updateComment(CommentUpdateRequest commentUpdateRequest) {
        if (commentUpdateRequest == null || commentUpdateRequest.getId() == null || commentUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        Comment oldComment = this.getById(commentUpdateRequest.getId());
        if (oldComment == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "评论不存在");
        }

        Long userId = commentUpdateRequest.getUserId();
        Long shopId = commentUpdateRequest.getShopId();
        Integer score = commentUpdateRequest.getScore();

        if (userId == null || userId <= 0 || shopId == null || shopId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户或店铺参数错误");
        }
        if (StringUtils.isBlank(commentUpdateRequest.getContent())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "评论内容不能为空");
        }
        if (score == null || score < 1 || score > 5) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "评分必须在 1 到 5 之间");
        }

        if (userService.getById(userId) == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在");
        }
        if (shopService.getById(shopId) == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "店铺不存在");
        }

        Comment comment = new Comment();
        BeanUtils.copyProperties(commentUpdateRequest, comment); // 关键修复点

        boolean updated = this.updateById(comment);
        if (!updated) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新失败");
        }
        return true;
    }

    @Override
    public Wrapper<Comment> getQueryWrapper(CommentQueryRequest commentQueryRequest) {
        if (commentQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        Long id = commentQueryRequest.getId();
        Long userId = commentQueryRequest.getUserId();
        Long shopId = commentQueryRequest.getShopId();
        String content = commentQueryRequest.getContent();
        Integer score = commentQueryRequest.getScore();
        Integer likeCount = commentQueryRequest.getLikeCount();
        Integer status = commentQueryRequest.getStatus();
        Date createTime = commentQueryRequest.getCreateTime();
        Date updateTime = commentQueryRequest.getUpdateTime();
        String searchText = commentQueryRequest.getSearchText();
        String sortField = commentQueryRequest.getSortField();
        String sortOrder = commentQueryRequest.getSortOrder();

        QueryWrapper<Comment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(id != null, "id", id);
        queryWrapper.eq(userId != null, "userId", userId);
        queryWrapper.eq(shopId != null, "shopId", shopId);
        queryWrapper.eq(score != null, "score", score);
        queryWrapper.eq(likeCount != null, "likeCount", likeCount);
        queryWrapper.eq(status != null, "status", status);
        queryWrapper.like(StringUtils.isNotBlank(content), "content", content);
        queryWrapper.like(StringUtils.isNotBlank(searchText), "content", searchText);
        queryWrapper.ge(createTime != null, "createTime", createTime);
        queryWrapper.ge(updateTime != null, "updateTime", updateTime);
        queryWrapper.eq("isDelete", 0);

        boolean asc = CommonConstant.SORT_ORDER_ASC.equals(sortOrder);
        queryWrapper.orderBy(SqlUtil.validSortField(sortField), asc, sortField);
        return queryWrapper;
    }

    @Override
    public List<CommentVO> getCommentVO(List<Comment> records) {
        if (CollectionUtils.isEmpty(records)) {
            return new ArrayList<>();
        }
        return records.stream().map(this::getCommentVO).collect(Collectors.toList());
    }

    @Override
    public Page<CommentVO> listCommentVOByPage(CommentQueryRequest commentQueryRequest) {
        int current = commentQueryRequest.getCurrent();
        int pageSize = commentQueryRequest.getPageSize();

        Page<Comment> commentPage = this.page(new Page<>(current, pageSize), this.getQueryWrapper(commentQueryRequest));
        Page<CommentVO> commentVOPage = new Page<>(current, pageSize, commentPage.getTotal());
        commentVOPage.setRecords(this.getCommentVO(commentPage.getRecords()));
        return commentVOPage;
    }
}