package com.spy.server.model.dto.couponorder;

import com.spy.server.common.PageRequest;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class CouponOrderQueryRequest extends PageRequest implements Serializable {

    private String searchText;

    private Long id;

    private Long userId;

    private Long shopId;

    private Long couponId;

    private String orderNo;

    private BigDecimal totalAmount;

    private BigDecimal payAmount;

    private Integer status;

    private String payType;

    private Date payTime;

    private Date cancelTime;

    private Date finishTime;

    private Date createTime;

    private Date updateTime;

    private static final long serialVersionUID = 1L;
}
