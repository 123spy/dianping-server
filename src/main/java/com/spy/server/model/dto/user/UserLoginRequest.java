package com.spy.server.model.dto.user;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户表
 * @TableName user
 */
@Data
public class UserLoginRequest implements Serializable {
    /**
     * 用户账号
     */
    private String userAccount;

    /**
     * 用户密码（加密存储）
     */
    private String userPassword;
}