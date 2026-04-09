package com.spy.server.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.spy.server.model.domain.ShopMedia;
import com.spy.server.model.vo.ShopMediaVO;

import java.util.List;

public interface ShopMediaService extends IService<ShopMedia> {

    List<ShopMedia> listByShopId(Long shopId);

    List<ShopMedia> listByShopIds(List<Long> shopIds);

    List<ShopMedia> listByShopIdAndType(Long shopId, Integer type);

    long countByShopIdAndType(Long shopId, Integer type);

    List<ShopMediaVO> getShopMediaVOList(List<ShopMedia> shopMediaList);
}
