package com.spy.server.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.spy.server.annotation.AuthCheck;
import com.spy.server.common.BaseResponse;
import com.spy.server.common.DeleteRequest;
import com.spy.server.common.ErrorCode;
import com.spy.server.constant.UserConstant;
import com.spy.server.exception.BusinessException;
import com.spy.server.model.domain.Shop;
import com.spy.server.model.domain.ShopRating;
import com.spy.server.model.domain.User;
import com.spy.server.model.dto.shoprating.ShopRatingAddRequest;
import com.spy.server.model.dto.shoprating.ShopRatingQueryRequest;
import com.spy.server.model.dto.shoprating.ShopRatingUpdateRequest;
import com.spy.server.model.vo.ShopRatingVO;
import com.spy.server.mq.model.ShopRatingChangedEvent;
import com.spy.server.mq.producer.ShopRatingEventProducer;
import com.spy.server.service.ShopRatingService;
import com.spy.server.service.ShopService;
import com.spy.server.service.UserService;
import com.spy.server.utils.ResultUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/shopRating")
@Slf4j
public class ShopRatingController {

    private static final String SHOP_GET_VO_CACHE_PREFIX = "dianping:shop:get:vo:";
    private static final String SHOP_LIST_VO_CACHE_PREFIX = "dianping:shop:list:page:vo:";
    private static final String SHOP_RATING_RANKING_KEY = "ranking:shop:rating";

    @Resource
    private ShopRatingService shopRatingService;

    @Resource
    private UserService userService;

    @Resource
    private RedisTemplate redisTemplate;
    @Autowired
    private ShopService shopService;
    @Autowired
    private ShopRatingEventProducer shopRatingEventProducer;

    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addShopRating(@RequestBody ShopRatingAddRequest shopRatingAddRequest) {
        if (shopRatingAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long id = shopRatingService.addShopRating(shopRatingAddRequest);
        Shop shop = shopService.getById(shopRatingAddRequest.getShopId());
        clearShopRelatedCache(shopRatingAddRequest.getShopId());
        refreshShopRatingRanking(shop);
        return ResultUtil.success(id);
    }

    @PostMapping("/submit")
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse<Long> submitShopRating(@RequestBody ShopRatingAddRequest shopRatingAddRequest, HttpServletRequest request) {
        if (shopRatingAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        shopRatingAddRequest.setUserId(loginUser.getId());
        Long id = shopRatingService.submitShopRating(shopRatingAddRequest);

        // 异步更新
        ShopRatingChangedEvent event = new ShopRatingChangedEvent();
        event.setShopId(shopRatingAddRequest.getShopId());
        event.setRatingId(id);
        event.setAction("SUBMIT");
        event.setEventTime(LocalDateTime.now());
        event.setRetryCount(0);

        shopRatingEventProducer.sendShopRatingChangedEvent(event);

//        Shop shop = shopService.getById(shopRatingAddRequest.getShopId());
//        clearShopRelatedCache(shopRatingAddRequest.getShopId());
//        refreshShopRatingRanking(shop);
        return ResultUtil.success(id);
    }

    @PostMapping("/revoke")
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse<Boolean> revokeShopRating(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        ShopRating shopRating = shopRatingService.getById(deleteRequest.getId());

        boolean result = shopRatingService.revokeShopRating(deleteRequest, request);

        if (shopRating != null) {
            Shop shop = shopService.getById(shopRating.getShopId());
            clearShopRelatedCache(shopRating.getShopId());
            refreshShopRatingRanking(shop);
        }
        return ResultUtil.success(result);
    }

    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse<Boolean> deleteShopRating(@RequestBody DeleteRequest deleteRequest) {
        if (deleteRequest == null || deleteRequest.getId() == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        ShopRating oldShopRating = shopRatingService.getById(deleteRequest.getId());
        boolean result = shopRatingService.adminDeleteShopRating(deleteRequest.getId());
        if (result && oldShopRating != null) {
            Shop shop = shopService.getById(oldShopRating.getShopId());
            clearShopRelatedCache(oldShopRating.getShopId());
            refreshShopRatingRanking(shop);
        }
        return ResultUtil.success(result);
    }

    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse<Boolean> updateShopRating(@RequestBody ShopRatingUpdateRequest shopRatingUpdateRequest, HttpServletRequest request) {
        if (shopRatingUpdateRequest == null || shopRatingUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        ShopRating oldShopRating = shopRatingService.getById(shopRatingUpdateRequest.getId());
        if (oldShopRating == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Boolean result = shopRatingService.updateShopRating(shopRatingUpdateRequest);

        ShopRating newShopRating = shopRatingService.getById(shopRatingUpdateRequest.getId());
        if (oldShopRating.getShopId() != null) {
            clearShopRelatedCache(oldShopRating.getShopId());
            refreshShopRatingRanking(shopService.getById(oldShopRating.getShopId()));
        }
        if (newShopRating != null && newShopRating.getShopId() != null
                && !newShopRating.getShopId().equals(oldShopRating.getShopId())) {
            clearShopRelatedCache(newShopRating.getShopId());
            refreshShopRatingRanking(shopService.getById(newShopRating.getShopId()));
        }

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

    // 清理缓存操作
    private void clearShopRelatedCache(Long shopId) {
        if (shopId == null || shopId <= 0) {
            return;
        }
        redisTemplate.delete(SHOP_GET_VO_CACHE_PREFIX + shopId);
        Set<String> keys = redisTemplate.keys(SHOP_LIST_VO_CACHE_PREFIX + "*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    // 更新商店分数
    private void refreshShopRatingRanking(Shop shop) {
        if (shop == null || shop.getId() == null) {
            return;
        }
        Integer auditStatus = shop.getAuditStatus();
        Integer businessStatus = shop.getBusinessStatus();
        if (auditStatus != null && auditStatus == 1
                && businessStatus != null && businessStatus == 1
                && shop.getAvgScore() != null) {
            redisTemplate.opsForZSet().add(SHOP_RATING_RANKING_KEY, shop.getId(), shop.getAvgScore().doubleValue());
            int randomTtl = ThreadLocalRandom.current().nextInt(60);
            redisTemplate.expire(SHOP_RATING_RANKING_KEY, 60 + randomTtl, TimeUnit.SECONDS);
        } else {
            redisTemplate.opsForZSet().remove(SHOP_RATING_RANKING_KEY, shop.getId());
        }
    }
}
