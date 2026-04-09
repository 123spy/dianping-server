package com.spy.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.spy.server.mapper.ShopMediaMapper;
import com.spy.server.model.domain.ShopMedia;
import com.spy.server.model.vo.ShopMediaVO;
import com.spy.server.service.ShopMediaService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class ShopMediaServiceImpl extends ServiceImpl<ShopMediaMapper, ShopMedia> implements ShopMediaService {

    @Override
    public List<ShopMedia> listByShopId(Long shopId) {
        if (shopId == null || shopId <= 0) {
            return new ArrayList<>();
        }
        QueryWrapper<ShopMedia> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("shopId", shopId);
        queryWrapper.orderByAsc("type", "sortNo", "id");
        return this.list(queryWrapper);
    }

    @Override
    public List<ShopMedia> listByShopIds(List<Long> shopIds) {
        if (shopIds == null || shopIds.isEmpty()) {
            return Collections.emptyList();
        }
        QueryWrapper<ShopMedia> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("shopId", shopIds);
        queryWrapper.orderByAsc("shopId", "type", "sortNo", "id");
        return this.list(queryWrapper);
    }

    @Override
    public List<ShopMedia> listByShopIdAndType(Long shopId, Integer type) {
        if (shopId == null || shopId <= 0 || type == null) {
            return new ArrayList<>();
        }
        QueryWrapper<ShopMedia> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("shopId", shopId);
        queryWrapper.eq("type", type);
        queryWrapper.orderByAsc("sortNo", "id");
        return this.list(queryWrapper);
    }

    @Override
    public long countByShopIdAndType(Long shopId, Integer type) {
        if (shopId == null || shopId <= 0 || type == null) {
            return 0;
        }
        QueryWrapper<ShopMedia> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("shopId", shopId);
        queryWrapper.eq("type", type);
        return this.count(queryWrapper);
    }

    @Override
    public List<ShopMediaVO> getShopMediaVOList(List<ShopMedia> shopMediaList) {
        if (shopMediaList == null || shopMediaList.isEmpty()) {
            return new ArrayList<>();
        }
        List<ShopMediaVO> result = new ArrayList<>(shopMediaList.size());
        for (ShopMedia shopMedia : shopMediaList) {
            ShopMediaVO shopMediaVO = new ShopMediaVO();
            BeanUtils.copyProperties(shopMedia, shopMediaVO);
            result.add(shopMediaVO);
        }
        return result;
    }
}
