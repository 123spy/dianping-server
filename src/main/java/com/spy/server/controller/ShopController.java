package com.spy.server.controller;

import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.spy.server.annotation.AuthCheck;
import com.spy.server.common.BaseResponse;
import com.spy.server.common.DeleteRequest;
import com.spy.server.common.ErrorCode;
import com.spy.server.constant.CommonConstant;
import com.spy.server.constant.UserConstant;
import com.spy.server.exception.BusinessException;
import com.spy.server.model.domain.Shop;
import com.spy.server.model.domain.User;
import com.spy.server.model.dto.shop.ShopAddRequest;
import com.spy.server.model.dto.shop.ShopNearQueryRequest;
import com.spy.server.model.dto.shop.ShopQueryRequest;
import com.spy.server.model.dto.shop.ShopUpdateRequest;
import com.spy.server.model.vo.ShopVO;
import com.spy.server.service.ShopService;
import com.spy.server.service.UserService;
import com.spy.server.utils.ResultUtil;
import com.spy.server.utils.ShopGeoRedisKeyUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.GeoResult;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Point;
import org.springframework.data.geo.Distance;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/shop")
@Slf4j
public class ShopController {

    private static final String SHOP_GET_VO_CACHE_PREFIX = "dianping:shop:get:vo:";
    private static final String SHOP_LIST_VO_CACHE_PREFIX = "dianping:shop:list:page:vo:";
    private static final String SHOP_RATING_RANKING_KEY = "ranking:shop:rating";

    @Resource
    private ShopService shopService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private RedisTemplate redisTemplate;

    @Autowired
    private UserService userService;

    private final ConcurrentHashMap<String, Object> lockHotMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Object> lockVoMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Object> lockMap = new ConcurrentHashMap<>();

    @PostMapping("/near")
    public BaseResponse<List<ShopVO>> getNearShop(@RequestBody ShopNearQueryRequest shopNearQueryRequest,
                                                  HttpServletRequest request) {
        BigDecimal longitude = shopNearQueryRequest.getLongitude();
        BigDecimal latitude = shopNearQueryRequest.getLatitude();
        Long distance = shopNearQueryRequest.getDistance();

        if (longitude == null || latitude == null || distance == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Invalid location parameters");
        }

        GeoResults<RedisGeoCommands.GeoLocation<String>> results = stringRedisTemplate.opsForGeo().search(
                ShopGeoRedisKeyUtil.SHOP_GEO_KEY,
                new Circle(
                        new Point(longitude.doubleValue(), latitude.doubleValue()),
                        new Distance(distance)
                )
        );

        if (results == null || results.getContent() == null || results.getContent().isEmpty()) {
            return ResultUtil.success(new ArrayList<>());
        }

        List<Long> shopIdList = new ArrayList<>();
        for (GeoResult<RedisGeoCommands.GeoLocation<String>> result : results.getContent()) {
            String shopIdStr = result.getContent().getName();
            if (shopIdStr != null) {
                shopIdList.add(Long.valueOf(shopIdStr));
            }
        }

        List<Shop> shopList = shopService.listByIds(shopIdList);
        if (shopList == null || shopList.isEmpty()) {
            return ResultUtil.success(new ArrayList<>());
        }

        Map<Long, Shop> shopMap = shopList.stream().collect(Collectors.toMap(Shop::getId, shop -> shop));
        List<ShopVO> shopVOList = new ArrayList<>();
        for (Long shopId : shopIdList) {
            Shop shop = shopMap.get(shopId);
            if (shop != null) {
                shopVOList.add(shopService.getShopVO(shop, request));
            }
        }

        return ResultUtil.success(shopVOList);
    }

    @GetMapping("/hot/rating")
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse<List<ShopVO>> getHotRatingShop(HttpServletRequest request) {
        String key = SHOP_RATING_RANKING_KEY;
        Set<ZSetOperations.TypedTuple<Object>> top10 = redisTemplate.opsForZSet().reverseRangeWithScores(key, 0, 9);

        if (top10 != null && !top10.isEmpty()) {
            return ResultUtil.success(buildHotRatingShopVOList(top10, request));
        }

        Object lock = lockHotMap.computeIfAbsent(key, k -> new Shop());

        synchronized (lock) {
            try {
                top10 = redisTemplate.opsForZSet().reverseRangeWithScores(key, 0, 9);
                if (top10 != null && !top10.isEmpty()) {
                    return ResultUtil.success(buildHotRatingShopVOList(top10, request));
                }

                ShopQueryRequest shopQueryRequest = new ShopQueryRequest();
                shopQueryRequest.setCurrent(1);
                shopQueryRequest.setPageSize(10);
                shopQueryRequest.setAuditStatus(1);
                shopQueryRequest.setBusinessStatus(1);
                shopQueryRequest.setSortField("avgScore");
                shopQueryRequest.setSortOrder(CommonConstant.SORT_ORDER_DESC);

                Wrapper<Shop> queryWrapper = shopService.getQueryWrapper(shopQueryRequest);
                Page<Shop> shopPage = shopService.page(new Page<>(1, 10), queryWrapper);
                List<Shop> dataFromMySQL = shopPage.getRecords();
                List<ShopVO> shopVO = shopService.getShopVO(dataFromMySQL, request);

                redisTemplate.delete(key);
                dataFromMySQL.forEach(shop -> {
                    if (shop.getAvgScore() != null) {
                        redisTemplate.opsForZSet().add(key, shop.getId(), shop.getAvgScore().doubleValue());
                    }
                });
                int randomTtl = ThreadLocalRandom.current().nextInt(60);
                redisTemplate.expire(key, 60 + randomTtl, TimeUnit.SECONDS);

                return ResultUtil.success(shopVO);
            } finally {
                lockHotMap.remove(key, lock);
            }
        }
    }

    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addShop(@RequestBody ShopAddRequest shopAddRequest) {
        if (shopAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long id = shopService.addShop(shopAddRequest);
        Shop shop = shopService.getById(id);
        refreshShopGeo(shop);
        clearShopListCache();
        refreshShopRatingRanking(shop);
        return ResultUtil.success(id);
    }

    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse<Boolean> deleteShop(@RequestBody DeleteRequest deleteRequest) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        Shop oldShop = shopService.getById(deleteRequest.getId());
        boolean result = shopService.removeById(deleteRequest.getId());
        if (!result) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        if (oldShop != null) {
            removeShopGeo(oldShop.getId());
        }
        clearShopDetailCache(deleteRequest.getId());
        clearShopListCache();
        removeShopRatingRanking(deleteRequest.getId());

        return ResultUtil.success(true);
    }

    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse<Boolean> updateShop(@RequestBody ShopUpdateRequest shopUpdateRequest, HttpServletRequest request) {
        if (shopUpdateRequest == null || shopUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Boolean result = shopService.updateShop(shopUpdateRequest);
        if (!result) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        Shop shop = shopService.getById(shopUpdateRequest.getId());
        refreshShopGeo(shop);
        clearShopDetailCache(shopUpdateRequest.getId());
        clearShopListCache();
        refreshShopRatingRanking(shop);

        return ResultUtil.success(true);
    }

    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Shop> getShopById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Shop shop = shopService.getById(id);
        if (shop == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtil.success(shop);
    }

    @GetMapping("/get/vo")
    public BaseResponse<ShopVO> getShopVOById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        String key = SHOP_GET_VO_CACHE_PREFIX + id;
        Shop data = (Shop) redisTemplate.opsForValue().get(key);

        if (data != null) {
            if (data.getId() == null) {
                throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
            }
            ShopVO shopVO = shopService.getShopVO(data, request);
            return ResultUtil.success(shopVO);
        }

        Object lock = lockVoMap.computeIfAbsent(key, k -> new Object());

        synchronized (lock) {
            try {
                Shop shop = (Shop) redisTemplate.opsForValue().get(key);
                if (shop != null) {
                    if (shop.getId() == null) {
                        throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
                    }
                    ShopVO shopVO = shopService.getShopVO(shop, request);
                    return ResultUtil.success(shopVO);
                }

                Shop result = shopService.getById(id);
                if (result == null) {
                    Shop nullShopData = new Shop();
                    redisTemplate.opsForValue().set(key, nullShopData, 60, TimeUnit.SECONDS);
                    throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
                }

                int randomTtl = ThreadLocalRandom.current().nextInt(60);
                redisTemplate.opsForValue().set(key, result, 60 + randomTtl, TimeUnit.SECONDS);

                ShopVO shopVO = shopService.getShopVO(result, request);
                return ResultUtil.success(shopVO);
            } finally {
                lockVoMap.remove(key, lock);
            }
        }
    }

    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Shop>> listShopByPage(@RequestBody ShopQueryRequest shopQueryRequest, HttpServletRequest request) {
        if (shopQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        int current = shopQueryRequest.getCurrent();
        int pageSize = shopQueryRequest.getPageSize();
        if (pageSize > 100) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Page size is too large");
        }
        Page<Shop> shopPage = shopService.page(new Page<>(current, pageSize), shopService.getQueryWrapper(shopQueryRequest));
        return ResultUtil.success(shopPage);
    }

    @PostMapping("/list/page/vo")
    public BaseResponse<Page<ShopVO>> listShopVOByPage(@RequestBody ShopQueryRequest shopQueryRequest,
                                                       HttpServletRequest request) {
        if (shopQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        User loginUser = userService.getLoginUserAllowNull(request);
        Long loginUserId = loginUser == null ? 0L : loginUser.getId();
        String queryJson = JSONUtil.toJsonStr(shopQueryRequest);
        String key = SHOP_LIST_VO_CACHE_PREFIX + loginUserId + ":" + DigestUtil.md5Hex(queryJson);

        Page<ShopVO> cachedResult = (Page<ShopVO>) redisTemplate.opsForValue().get(key);
        if (cachedResult != null) {
            return ResultUtil.success(cachedResult);
        }

        Object lock = lockMap.computeIfAbsent(key, k -> new Object());

        synchronized (lock) {
            try {
                cachedResult = (Page<ShopVO>) redisTemplate.opsForValue().get(key);
                if (cachedResult != null) {
                    return ResultUtil.success(cachedResult);
                }

                Page<ShopVO> result = shopService.listShopVOByPage(shopQueryRequest, request);
                if (result == null) {
                    Page<ShopVO> emptyPage = new Page<>();
                    emptyPage.setRecords(Collections.emptyList());
                    redisTemplate.opsForValue().set(key, emptyPage, 60, TimeUnit.SECONDS);
                    return ResultUtil.success(emptyPage);
                }

                int randomTtl = ThreadLocalRandom.current().nextInt(60);
                redisTemplate.opsForValue().set(key, result, 60 + randomTtl, TimeUnit.SECONDS);
                return ResultUtil.success(result);
            } finally {
                lockMap.remove(key, lock);
            }
        }
    }

    private List<ShopVO> buildHotRatingShopVOList(Set<ZSetOperations.TypedTuple<Object>> top10, HttpServletRequest request) {
        ArrayList<ShopVO> shopVOArrayList = new ArrayList<>(top10.size());
        for (ZSetOperations.TypedTuple<Object> objectTypedTuple : top10) {
            Long shopId = Long.valueOf(objectTypedTuple.getValue().toString());
            Shop shop = shopService.getById(shopId);
            if (shop != null) {
                shopVOArrayList.add(shopService.getShopVO(shop, request));
            }
        }
        return shopVOArrayList;
    }

    private void clearShopDetailCache(Long shopId) {
        if (shopId == null || shopId <= 0) {
            return;
        }
        redisTemplate.delete(SHOP_GET_VO_CACHE_PREFIX + shopId);
    }

    private void clearShopListCache() {
        Set<String> keys = redisTemplate.keys(SHOP_LIST_VO_CACHE_PREFIX + "*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    private void removeShopRatingRanking(Long shopId) {
        if (shopId == null || shopId <= 0) {
            return;
        }
        redisTemplate.opsForZSet().remove(SHOP_RATING_RANKING_KEY, shopId);
    }

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

    private void refreshShopGeo(Shop shop) {
        if (shop == null || shop.getId() == null) {
            return;
        }
        if (shop.getLongitude() == null || shop.getLatitude() == null) {
            removeShopGeo(shop.getId());
            return;
        }
        stringRedisTemplate.opsForGeo().add(
                ShopGeoRedisKeyUtil.SHOP_GEO_KEY,
                new Point(shop.getLongitude().doubleValue(), shop.getLatitude().doubleValue()),
                shop.getId().toString()
        );
    }

    private void removeShopGeo(Long shopId) {
        if (shopId == null || shopId <= 0) {
            return;
        }
        stringRedisTemplate.opsForGeo().remove(ShopGeoRedisKeyUtil.SHOP_GEO_KEY, shopId.toString());
    }
}
