package com.spy.server.job;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.spy.server.model.domain.Shop;
import com.spy.server.service.ShopService;
import com.spy.server.utils.ShopGeoRedisKeyUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Component
@Slf4j
public class IncrementSync {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private ShopService shopService;

    @Scheduled(fixedDelay = 4 * 60 * 60 * 1000)
    public void syncIncrementalShopGeo() {
        String lastSyncTimeStr = stringRedisTemplate.opsForValue().get(ShopGeoRedisKeyUtil.SHOP_GEO_LAST_SYNC_KEY);

        LocalDateTime lastSyncTime = StringUtils.isBlank(lastSyncTimeStr)
                ? LocalDateTime.now().minusHours(4)
                : LocalDateTime.parse(lastSyncTimeStr);
        LocalDateTime currentSyncTime = LocalDateTime.now();

        List<Shop> changedShops = shopService.list(
                new LambdaQueryWrapper<Shop>()
                        .gt(Shop::getUpdateTime, toDate(lastSyncTime))
                        .le(Shop::getUpdateTime, toDate(currentSyncTime))
        );

        if (changedShops == null || changedShops.isEmpty()) {
            stringRedisTemplate.opsForValue().set(ShopGeoRedisKeyUtil.SHOP_GEO_LAST_SYNC_KEY, currentSyncTime.toString());
            log.info("店铺 GEO 增量同步完成：本次无变更数据，GEO键={}", ShopGeoRedisKeyUtil.SHOP_GEO_KEY);
            return;
        }

        int updatedCount = 0;
        int removedCount = 0;
        for (Shop shop : changedShops) {
            if (shop.getId() == null) {
                continue;
            }
            String shopId = shop.getId().toString();
            if (shop.getLongitude() == null || shop.getLatitude() == null) {
                Long removed = stringRedisTemplate.opsForGeo().remove(ShopGeoRedisKeyUtil.SHOP_GEO_KEY, shopId);
                if (removed != null && removed > 0) {
                    removedCount += removed.intValue();
                }
                continue;
            }
            stringRedisTemplate.opsForGeo().add(
                    ShopGeoRedisKeyUtil.SHOP_GEO_KEY,
                    new Point(shop.getLongitude().doubleValue(), shop.getLatitude().doubleValue()),
                    shopId
            );
            updatedCount++;
        }

        stringRedisTemplate.opsForValue().set(ShopGeoRedisKeyUtil.SHOP_GEO_LAST_SYNC_KEY, currentSyncTime.toString());
        log.info("店铺 GEO 增量同步完成：GEO键={}，变更数量={}，更新数量={}，移除数量={}",
                ShopGeoRedisKeyUtil.SHOP_GEO_KEY, changedShops.size(), updatedCount, removedCount);
    }

    private Date toDate(LocalDateTime time) {
        return Date.from(time.atZone(ZoneId.systemDefault()).toInstant());
    }
}
