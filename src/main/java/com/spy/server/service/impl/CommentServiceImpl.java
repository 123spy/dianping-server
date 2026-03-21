package com.spy.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.spy.server.common.ErrorCode;
import com.spy.server.constant.CommonConstant;
import com.spy.server.exception.BusinessException;
import com.spy.server.model.domain.Shop;
import com.spy.server.model.domain.Comment;
import com.spy.server.model.domain.Comment;
import com.spy.server.model.domain.User;
import com.spy.server.model.dto.comment.CommentAddRequest;
import com.spy.server.model.dto.comment.CommentQueryRequest;
import com.spy.server.model.dto.comment.CommentUpdateRequest;
import com.spy.server.model.vo.CommentVO;
import com.spy.server.model.vo.UserVO;
import com.spy.server.service.ShopService;
import com.spy.server.service.CommentService;
import com.spy.server.mapper.CommentMapper;
import com.spy.server.service.UserService;
import com.spy.server.utils.SqlUtil;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
* @author OUC
* @description 针对表【comment(评论表)】的数据库操作Service实现
* @createDate 2026-03-20 19:51:25
*/
@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment>
    implements CommentService{

    private final Gson gson = new Gson();

    @Resource
    private UserService userService;

    @Resource
    private ShopService shopService;

    @Override
    public CommentVO getCommentVO(Comment comment) {
        CommentVO commentVO = new CommentVO();
        if(comment==null){
            return commentVO;
        }
        BeanUtils.copyProperties(comment,commentVO);

        UserVO userVO = userService.getUserVO(userService.getById(comment.getUserId()));
        commentVO.setUserVO(userVO);

        return commentVO;
    }

    @Override
    public Long addComment(CommentAddRequest commentAddRequest) {
        Comment comment = new Comment();
        BeanUtils.copyProperties(commentAddRequest, comment);
        User user = userService.getById(comment.getUserId());
        Shop shop = shopService.getById(comment.getShopId());
        if(user==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在");
        }
        if(shop==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "店铺不存在");
        }
        // 插入数据
        this.save(comment);
        return comment.getId();

    }

    @Override
    public Boolean updateComment(CommentUpdateRequest commentUpdateRequest) {
        Comment oldComment = this.getById(commentUpdateRequest.getId());
        if(oldComment == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        Comment comment = new Comment();

        BeanUtils.copyProperties(oldComment, comment);

        User user = userService.getById(comment.getUserId());
        Shop shop = shopService.getById(comment.getShopId());
        if(user==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在");
        }
        if(shop==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "店铺不存在");
        }

        // 插入数据
        boolean result = this.updateById(comment);
        return result;

    }

    @Override
    public Wrapper<Comment> getQueryWrapper(CommentQueryRequest commentQueryRequest) {
        if(commentQueryRequest == null) {
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
        int current = commentQueryRequest.getCurrent();
        int pageSize = commentQueryRequest.getPageSize();
        String searchText = commentQueryRequest.getSearchText();
        String sortField = commentQueryRequest.getSortField();
        String sortOrder = commentQueryRequest.getSortOrder();



        QueryWrapper<Comment> queryWrapper = new QueryWrapper<>();

        queryWrapper.eq(id != null, "id", id);
        queryWrapper.eq(userId != null, "userId", userId);
        queryWrapper.eq(shopId != null, "shopId", shopId);

        if (StringUtils.isNotBlank(searchText)) {
            queryWrapper.and(wrapper ->
                    wrapper.like("content", searchText)
            );
        }

        queryWrapper.orderBy(SqlUtil.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC), sortField);
        return queryWrapper;
    }

    @Override
    public List<CommentVO> getCommentVO(List<Comment> records) {
        if (CollectionUtils.isEmpty(records)) {
            return new ArrayList<>();
        }
        List<CommentVO> commentVOList = records.stream().map(comment -> {
            return getCommentVO(comment);
        }).collect(Collectors.toList());
        return commentVOList;
    }

    @Override
    public Page<CommentVO> listCommentVOByPage(CommentQueryRequest commentQueryRequest) {
        int current = commentQueryRequest.getCurrent();
        int pageSize = commentQueryRequest.getPageSize();
        Page<Comment> commentPage = this.page(new Page<>(current, pageSize), this.getQueryWrapper(commentQueryRequest));
        Page<CommentVO> commentVOPage = new Page<>(current, pageSize, commentPage.getTotal());
        List<CommentVO> commentVoList = this.getCommentVO(commentPage.getRecords());
        commentVOPage.setRecords(commentVoList);
        return commentVOPage;
    }
}




