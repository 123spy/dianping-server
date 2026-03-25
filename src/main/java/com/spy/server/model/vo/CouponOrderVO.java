package com.spy.server.model.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class CouponOrderVO {

    private Long id;

    private String orderNo;

    private Long userId;

    private UserVO userVO;

    private Long shopId;

    private ShopVO shopVO;

    private Long couponId;

    private CouponVO couponVO;

    private Long userCouponId;

    private BigDecimal totalAmount;

    private BigDecimal payAmount;

    private Integer status;

    private Date payTime;

    private String payType;

    private Date cancelTime;

    private Date finishTime;

    private Date createTime;

    private Date updateTime;
}
