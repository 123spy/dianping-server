package com.spy.server.model.dto.usercoupon;

import com.spy.server.common.PageRequest;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class UserCouponQueryRequest extends PageRequest implements Serializable {

    private String searchText;

    private Long id;

    private Long userId;

    private Long couponId;

    private Long orderId;

    private String code;

    private Integer status;

    private Date obtainTime;

    private Date useTime;

    private Date expireTime;

    private Date createTime;

    private Date updateTime;

    private static final long serialVersionUID = 1L;
}
