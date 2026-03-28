package com.spy.server.controller;

import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
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
import com.spy.server.model.dto.shop.*;
import com.spy.server.model.vo.ShopVO;
import com.spy.server.service.ShopService;
import com.spy.server.service.UserService;
import com.spy.server.utils.ResultUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/shop")
@Slf4j
public class ShopController {

    @Resource
    private ShopService shopService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private RedisTemplate redisTemplate;
    @Autowired
    private UserService userService;

    private final ConcurrentHashMap<String, Object> lockHotMap = new ConcurrentHashMap<>();

    // 评分榜单
    @GetMapping("/hot/rating")
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse<List<ShopVO>> getHotRatingShop(HttpServletRequest request) {

        String key = "ranking:shop:rating";
        // 第一步去缓存查询
        Set<ZSetOperations.TypedTuple<Object>> top10 =
                redisTemplate.opsForZSet().reverseRangeWithScores(key, 0, 9);

        // 缓存存在，那么就返回结果
        if(top10.size() != 0) {
            ArrayList<ShopVO> shopVOArrayList = new ArrayList<>(top10.size());
            for (ZSetOperations.TypedTuple<Object> objectTypedTuple : top10) {
                Long shopId = Long.valueOf(objectTypedTuple.getValue().toString());
                double score = objectTypedTuple.getScore();
                BigDecimal avgScore = new BigDecimal(score);
                shopVOArrayList.add(shopService.getShopVO(shopService.getById(shopId), request));
            }
            return ResultUtil.success(shopVOArrayList);
        }

        // 如果数据不存在，就去mysql中查询。

        // 生成锁
        Object lock = lockHotMap.computeIfAbsent(key, k -> new Shop());

        synchronized (lock) {
            try {
                // 再获取一遍
                top10 =
                        redisTemplate.opsForZSet().reverseRangeWithScores(key, 0, 9);
                if(top10.size() != 0) {
                    ArrayList<ShopVO> shopVOArrayList = new ArrayList<>(top10.size());
                    for (ZSetOperations.TypedTuple<Object> objectTypedTuple : top10) {
                        Long shopId = (Long) objectTypedTuple.getValue();
                        double score = objectTypedTuple.getScore();
                        BigDecimal avgScore = new BigDecimal(score);
                        shopVOArrayList.add(shopService.getShopVO(shopService.getById(shopId), request));
                    }
                    return ResultUtil.success(shopVOArrayList);
                }

                // 去数据库查询
                ShopQueryRequest shopQueryRequest = new ShopQueryRequest();
                shopQueryRequest.setCurrent(1);
                shopQueryRequest.setPageSize(10);
                shopQueryRequest.setSortField("avgScore");
                shopQueryRequest.setSortOrder(CommonConstant.SORT_ORDER_DESC);

                Wrapper<Shop> queryWrapper = shopService.getQueryWrapper(shopQueryRequest);
                List<Shop> dataFromMySQL = shopService.list(queryWrapper);
                List<ShopVO> shopVO = shopService.getShopVO(dataFromMySQL, request);

                // 异步写入缓存
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    dataFromMySQL.forEach(shop -> {
                        Boolean result = redisTemplate.opsForZSet().add(key, shop.getId(), shop.getAvgScore().doubleValue());
                    });
                });

                return ResultUtil.success(shopVO);
            } finally {
                lockHotMap.remove(key, lock);
            }
        }
    }

    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addShop(@RequestBody ShopAddRequest shopAddRequest) {
        // 校验
        if (shopAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long id = shopService.addShop(shopAddRequest);
        return ResultUtil.success(id);
    }

    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse<Boolean> deleteShop(@RequestBody DeleteRequest deleteRequest) {
        // 1. 校验
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 2. 删除
        boolean result = shopService.removeById(deleteRequest.getId());
        if (!result) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 删除redis缓存中的数据
        String key = "dianping:shop:get:vo:" + deleteRequest.getId();
        Shop shop = (Shop) redisTemplate.opsForValue().get(key);
        if(shop != null) {
            Boolean deleteRedisDataResult = redisTemplate.delete(key);
            if (!deleteRedisDataResult) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR);
            }


        }

        // 删除评分榜单
        Long count = redisTemplate.opsForZSet().remove(key, shop.getId());

        // 可以加延迟双删，进一步加强安全性
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

        // 删除redis缓存中的数据
        String key = "dianping:shop:get:vo:" + shopUpdateRequest.getId();
        Shop shop = (Shop) redisTemplate.opsForValue().get(key);
        if(shop != null){
            Boolean deleteRedisDataResult = redisTemplate.delete(key);
            if (!deleteRedisDataResult) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR);
            }
        }

        // 删除评分榜单
        Long count = redisTemplate.opsForZSet().remove(key, shop.getId());

        // 可以加延迟双删，进一步加强安全性
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

    private final ConcurrentHashMap<String, Object> lockVoMap = new ConcurrentHashMap<>();

    @GetMapping("/get/vo")
    public BaseResponse<ShopVO> getShopVOById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 检查一下，是否存在。
        String key = "dianping:shop:get:vo:" + id;
        Shop data = (Shop) redisTemplate.opsForValue().get(key);

        if (data != null) {
            ShopVO shopVO = shopService.getShopVO(data, request);
            return ResultUtil.success(shopVO);
        }

        // 获取锁
        Object lock = lockVoMap.computeIfAbsent(key, k -> new Object());

        synchronized (lock) {
            try {
                // 双重检查
                Shop shop = (Shop) redisTemplate.opsForValue().get(key);
                if (shop != null) {
                    ShopVO shopVO = shopService.getShopVO(shop, request);
                    return ResultUtil.success(shopVO);
                }


                // 查询数据库
                Shop result = shopService.getById(id);
                if (result == null) {
                    // 如果未空数据，也要写入缓存中
                    Shop nullSHopData = new Shop();
                    redisTemplate.opsForValue().set(key, nullSHopData, 60, TimeUnit.SECONDS);
                    ShopVO shopVO = shopService.getShopVO(data, request);
                    return ResultUtil.success(shopVO);
                }

                // 到这里就代表，数据是正常的。那就写入缓存，然后返回数据
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
        if(pageSize > 100) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "分页查询过大");
        }
        Page<Shop> shopPage = shopService.page(new Page<>(current, pageSize), shopService.getQueryWrapper(shopQueryRequest));
        return ResultUtil.success(shopPage);
    }


    // ConcurrentHashMap线程安全的并发容器
    // put， get, remove，containsKey, putIfAbsent（查看一下是否存在，如果存在，就不覆盖，如果没有，那就用新的数值覆盖过去）, computeIfAbsent（查看一下数据是否存在，如果存在就返回，反之就按照函数中指定的规则生成一个覆盖进去并放回回来）
    private final ConcurrentHashMap<String, Object> lockMap = new ConcurrentHashMap<>();

    @PostMapping("/list/page/vo")
    public BaseResponse<Page<ShopVO>> listShopVOByPage(@RequestBody ShopQueryRequest shopQueryRequest,
                                                       HttpServletRequest request) {
        if (shopQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        String queryJson = JSONUtil.toJsonStr(shopQueryRequest);
        String key = "dianping:shop:list:page:vo:" + DigestUtil.md5Hex(queryJson);

        // 1. 先查缓存
        Page<ShopVO> cachedResult = (Page<ShopVO>) redisTemplate.opsForValue().get(key);
        if (cachedResult != null) {
            return ResultUtil.success(cachedResult);
        }

        Object lock = lockMap.computeIfAbsent(key, k -> new Object());

        synchronized (lock) {
            try {
                // 2. 双检
                cachedResult = (Page<ShopVO>) redisTemplate.opsForValue().get(key);
                if (cachedResult != null) {
                    return ResultUtil.success(cachedResult);
                }

                // 3. 查数据库
                Page<ShopVO> result = shopService.listShopVOByPage(shopQueryRequest, request);

                // 4. 写缓存
                if (result == null) {
                    // 如果你的 redisTemplate 不适合存 null，可以换成一个空对象
                    Page<ShopVO> emptyPage = new Page<>();
                    emptyPage.setRecords(Collections.emptyList());
                    redisTemplate.opsForValue().set(key, emptyPage, 60, TimeUnit.SECONDS);
                    return ResultUtil.success(emptyPage);
                }

                int randomTtl = ThreadLocalRandom.current().nextInt(60);
                redisTemplate.opsForValue().set(key, result, 60 + randomTtl, TimeUnit.SECONDS);

                return ResultUtil.success(result);
            } finally {
                // 最后这里，为什么要删除，这个删除的合法性
                // 首先第一点就是，如果在concurrentHashMap一直在插入kv，但是从不删除，一旦请求过多，那么内存的使用就会变大，最后导致一个OOM的效果
                // 这一步操作的合理性，他控制的是一段时间内的并。比如说在这1分钟内，最开始就有500个请求一起过来。此时，redis中没有属于，map中也没有lock的数据
                // 但是此时，第一个线程抢到了，他把锁创建了出来，放入到了map中，然后从数据库中请求到了数据，然后插入到缓存中。最后他把这个map删除掉了。
                lockMap.remove(key, lock);
            }
        }
    }
}
