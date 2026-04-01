package com.spy.server.mq.model;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class ShopRatingChangedEvent implements Serializable {
    private Long shopId;

    private Long ratingId;

    private Long UserId;

    private String action;

    private LocalDateTime eventTime;
}
