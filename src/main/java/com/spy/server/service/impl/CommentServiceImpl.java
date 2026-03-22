package com.spy.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.spy.server.common.DeleteRequest;
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
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
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

        User user = userService.getById(comment.getUserId());
        if (user != null) {
            UserVO userVO = userService.getUserVO(user);
            commentVO.setUserVO(userVO);
        }
        return commentVO;
    }

    @Override
    public List<CommentVO> getCommentVO(List<Comment> records) {
        if (CollectionUtils.isEmpty(records)) {
            return new ArrayList<>();
        }

        Set<Long> userIdSet = records.stream()
                .map(Comment::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, UserVO> userVOMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(userIdSet)) {
            List<User> userList = userService.listByIds(userIdSet);
            userVOMap = userList.stream().collect(Collectors.toMap(
                    User::getId,
                    user -> userService.getUserVO(user),
                    (a, b) -> a
            ));
        }

        Map<Long, UserVO> finalUserVOMap = userVOMap;
        return records.stream().map(comment -> {
            CommentVO commentVO = new CommentVO();
            BeanUtils.copyProperties(comment, commentVO);
            commentVO.setUserVO(finalUserVOMap.get(comment.getUserId()));
            return commentVO;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addComment(CommentAddRequest commentAddRequest) {
        return saveComment(commentAddRequest);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long submitComment(CommentAddRequest commentAddRequest) {
        return saveComment(commentAddRequest);
    }

    private Long saveComment(CommentAddRequest commentAddRequest) {
        if (commentAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        Long userId = commentAddRequest.getUserId();
        Long shopId = commentAddRequest.getShopId();

        if (userId == null || userId <= 0 || shopId == null || shopId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户或店铺参数错误");
        }
        if (StringUtils.isBlank(commentAddRequest.getContent())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "评论内容不能为空");
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

        recalculateCommentCount(shopId);
        return comment.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateComment(CommentUpdateRequest commentUpdateRequest) {
        if (commentUpdateRequest == null || commentUpdateRequest.getId() == null || commentUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        Comment oldComment = this.getById(commentUpdateRequest.getId());
        if (oldComment == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "评论不存在");
        }

        Long userId = commentUpdateRequest.getUserId();
        if (userId != null && userService.getById(userId) == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在");
        }

        Long newShopId = commentUpdateRequest.getShopId();
        if (newShopId != null && shopService.getById(newShopId) == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "店铺不存在");
        }

        if (commentUpdateRequest.getContent() != null && StringUtils.isBlank(commentUpdateRequest.getContent())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "评论内容不能为空");
        }

        Comment comment = new Comment();
        BeanUtils.copyProperties(commentUpdateRequest, comment);

        boolean updated = this.updateById(comment);
        if (!updated) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新失败");
        }

        Long oldShopId = oldComment.getShopId();
        Long finalNewShopId = newShopId != null ? newShopId : oldShopId;

        recalculateCommentCount(oldShopId);
        if (!Objects.equals(oldShopId, finalNewShopId)) {
            recalculateCommentCount(finalNewShopId);
        }

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean revokeComment(DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        User loginUser = userService.getLoginUser(request);
        Comment comment = this.getById(deleteRequest.getId());
        if (comment == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "评论不存在");
        }
        if (!Objects.equals(comment.getUserId(), loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限删除评论");
        }

        boolean result = this.removeById(deleteRequest.getId());
        if (!result) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "删除失败");
        }

        recalculateCommentCount(comment.getShopId());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean adminDeleteComment(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        Comment comment = this.getById(id);
        if (comment == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "评论不存在");
        }

        boolean result = this.removeById(id);
        if (!result) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "删除失败");
        }

        recalculateCommentCount(comment.getShopId());
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
    public Page<CommentVO> listCommentVOByPage(CommentQueryRequest commentQueryRequest) {
        int current = commentQueryRequest.getCurrent();
        int pageSize = commentQueryRequest.getPageSize();

        Page<Comment> commentPage = this.page(new Page<>(current, pageSize), this.getQueryWrapper(commentQueryRequest));
        Page<CommentVO> commentVOPage = new Page<>(current, pageSize, commentPage.getTotal());
        commentVOPage.setRecords(this.getCommentVO(commentPage.getRecords()));
        return commentVOPage;
    }

    @Override
    public void recalculateCommentCount(Long shopId) {
        if (shopId == null || shopId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "店铺参数错误");
        }

        Shop shop = shopService.getById(shopId);
        if (shop == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "店铺不存在");
        }

        QueryWrapper<Comment> wrapper = new QueryWrapper<>();
        wrapper.eq("shopId", shopId);
        wrapper.eq("isDelete", 0);
        wrapper.eq("status", 0);

        long count = this.count(wrapper);
        shop.setCommentCount((int) count);

        boolean result = shopService.updateById(shop);
        if (!result) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "重算评论数失败");
        }
    }
}