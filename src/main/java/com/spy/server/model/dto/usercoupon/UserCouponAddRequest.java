package com.spy.server.model.dto.usercoupon;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class UserCouponAddRequest implements Serializable {

    private Long userId;

    private Long couponId;

    private Long orderId;

    private String code;

    private Integer status;

    private Date obtainTime;

    private Date useTime;

    private Date expireTime;

    private static final long serialVersionUID = 1L;
}
