package com.spy.server.model.dto.couponorder;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class CouponOrderUpdateRequest implements Serializable {

    private Long id;

    private Long userId;

    private Long shopId;

    private Long couponId;

    private String orderNo;

    private BigDecimal totalAmount;

    private BigDecimal payAmount;

    private Integer status;

    private Date payTime;

    private String payType;

    private Date cancelTime;

    private Date finishTime;

    private static final long serialVersionUID = 1L;
}
