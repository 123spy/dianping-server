package com.spy.server.model.vo;

import lombok.Data;

import java.util.Date;

@Data
public class UserCouponVO {

    private Long id;

    private Long userId;

    private UserVO userVO;

    private Long couponId;

    private CouponVO couponVO;

    private Long orderId;

    private String orderNo;

    private ShopVO shopVO;

    private String code;

    private Integer status;

    private Date obtainTime;

    private Date useTime;

    private Date expireTime;

    private Date createTime;

    private Date updateTime;
}
