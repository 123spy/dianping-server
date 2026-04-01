package com.spy.server.mq.model;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class CouponOrderEvent implements Serializable {

    private Long couponId;

    private Long userId;

    private LocalDateTime eventTime;
}
