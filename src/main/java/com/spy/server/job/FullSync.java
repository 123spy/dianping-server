package com.spy.server.job;

import com.spy.server.model.domain.Shop;
import com.spy.server.service.ShopService;
import com.spy.server.utils.ShopGeoRedisKeyUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
public class FullSync implements CommandLineRunner {

    private final ShopService shopService;
    private final StringRedisTemplate stringRedisTemplate;

    public FullSync(ShopService shopService, StringRedisTemplate stringRedisTemplate) {
        this.shopService = shopService;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public void run(String... args) {
        List<Shop> shopList = shopService.list();
        List<RedisGeoCommands.GeoLocation<String>> locations = new ArrayList<>();

        for (Shop shop : shopList) {
            if (shop.getId() == null || shop.getLongitude() == null || shop.getLatitude() == null) {
                continue;
            }
            locations.add(new RedisGeoCommands.GeoLocation<>(
                    shop.getId().toString(),
                    new Point(shop.getLongitude().doubleValue(), shop.getLatitude().doubleValue())
            ));
        }

        Set<String> oldKeys = stringRedisTemplate.keys(ShopGeoRedisKeyUtil.SHOP_GEO_KEY_PATTERN);
        if (oldKeys != null && !oldKeys.isEmpty()) {
            stringRedisTemplate.delete(oldKeys);
        }
        if (!locations.isEmpty()) {
            stringRedisTemplate.opsForGeo().add(ShopGeoRedisKeyUtil.SHOP_GEO_KEY, locations);
        }
        stringRedisTemplate.opsForValue().set(ShopGeoRedisKeyUtil.SHOP_GEO_LAST_SYNC_KEY, LocalDateTime.now().toString());

        log.info("店铺 GEO 全量同步完成：GEO键={}，同步数量={}", ShopGeoRedisKeyUtil.SHOP_GEO_KEY, locations.size());
    }
}
