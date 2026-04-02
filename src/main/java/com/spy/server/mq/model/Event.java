package com.spy.server.mq.model;

import lombok.Data;

@Data
public class Event {

    // 重试次数
    private int retryCount;
}
