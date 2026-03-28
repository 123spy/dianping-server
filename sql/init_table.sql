CREATE DATABASE IF NOT EXISTS dianping_project
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE dianping_project;

SET NAMES utf8mb4;

-- =========================
-- 1. 用户表
-- =========================
DROP TABLE IF EXISTS user;
CREATE TABLE user (
                      id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 id',
                      userName VARCHAR(64) NOT NULL DEFAULT '' COMMENT '用户名',
                      userAccount VARCHAR(64) NOT NULL COMMENT '用户账号',
                      userPassword VARCHAR(255) NOT NULL COMMENT '用户密码（加密存储）',
                      userPhone VARCHAR(20) DEFAULT NULL COMMENT '用户手机号',
                      avatar VARCHAR(512) DEFAULT NULL COMMENT '用户头像',
                      userProfile VARCHAR(512) DEFAULT NULL COMMENT '用户简介',
                      userRole VARCHAR(20) NOT NULL DEFAULT 'user' COMMENT '用户角色：user/manager/admin',
                      status TINYINT NOT NULL DEFAULT 0 COMMENT '账号状态：0-正常 1-封禁',
                      createTime DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                      updateTime DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                      isDelete TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删 1-已删',
                      PRIMARY KEY (id),
                      UNIQUE KEY uk_userAccount (userAccount),
                      UNIQUE KEY uk_userPhone (userPhone),
                      KEY idx_userRole (userRole),
                      KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';


-- =========================
-- 2. 店铺分类表
-- =========================
DROP TABLE IF EXISTS category;
CREATE TABLE category (
                          id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 id',
                          name VARCHAR(64) NOT NULL COMMENT '分类名称',
                          sort INT NOT NULL DEFAULT 0 COMMENT '排序值，越大越靠前',
                          createTime DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                          updateTime DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                          isDelete TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删 1-已删',
                          PRIMARY KEY (id),
                          UNIQUE KEY uk_name (name),
                          KEY idx_sort (sort)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='店铺分类表';


-- =========================
-- 3. 店铺表
-- =========================
DROP TABLE IF EXISTS shop;
CREATE TABLE shop (
                      id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 id',
                      managerId BIGINT DEFAULT NULL COMMENT '店长/店铺管理员 id，对应 user.id',
                      name VARCHAR(128) NOT NULL COMMENT '店铺名称',
                      description VARCHAR(1024) DEFAULT NULL COMMENT '店铺介绍',
                      tags VARCHAR(512) DEFAULT NULL COMMENT '店铺标签，逗号分隔，如 火锅,夜宵,热门',
                      categoryId BIGINT NOT NULL COMMENT '店铺分类 id',
                      longitude DECIMAL(10,6) NOT NULL COMMENT '经度',
                      latitude DECIMAL(10,6) NOT NULL COMMENT '纬度',
                      address VARCHAR(255) NOT NULL COMMENT '店铺地址',
                      city VARCHAR(64) NOT NULL COMMENT '店铺所在城市',
                      businessStatus TINYINT NOT NULL DEFAULT 1 COMMENT '营业状态：0-歇业 1-营业',
                      auditStatus TINYINT NOT NULL DEFAULT 0 COMMENT '审核状态：0-审核中 1-审核通过 2-审核失败',
                      avgScore DECIMAL(3,2) NOT NULL DEFAULT 0.00 COMMENT '店铺平均分，范围建议 0.00-5.00',
                      ratingCount INT NOT NULL DEFAULT 0 COMMENT '评分人数',
                      commentCount INT NOT NULL DEFAULT 0 COMMENT '评论数',
                      favoriteCount INT NOT NULL DEFAULT 0 COMMENT '收藏数',
                      viewCount INT NOT NULL DEFAULT 0 COMMENT '浏览量',
                      createTime DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                      updateTime DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                      isDelete TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删 1-已删',
                      PRIMARY KEY (id),
                      KEY idx_managerId (managerId),
                      KEY idx_categoryId (categoryId),
                      KEY idx_city (city),
                      KEY idx_businessStatus (businessStatus),
                      KEY idx_auditStatus (auditStatus),
                      KEY idx_avgScore (avgScore),
                      KEY idx_ratingCount (ratingCount),
                      KEY idx_commentCount (commentCount),
                      KEY idx_favoriteCount (favoriteCount),
                      KEY idx_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='店铺表';


-- =========================
-- 4. 评论表
-- 说明：评论和评分拆开，这里只存评论内容
-- =========================
DROP TABLE IF EXISTS comment;
CREATE TABLE comment (
                         id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 id',
                         userId BIGINT NOT NULL COMMENT '用户 id',
                         shopId BIGINT NOT NULL COMMENT '店铺 id',
                         content VARCHAR(2000) NOT NULL COMMENT '评论内容',
                         likeCount INT NOT NULL DEFAULT 0 COMMENT '点赞数',
                         status TINYINT NOT NULL DEFAULT 0 COMMENT '评论状态：0-正常 1-隐藏/删除 2-待审核',
                         createTime DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                         updateTime DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                         isDelete TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删 1-已删',
                         PRIMARY KEY (id),
                         KEY idx_userId (userId),
                         KEY idx_shopId (shopId),
                         KEY idx_status (status),
                         KEY idx_createTime (createTime)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论表';


-- =========================
-- 5. 店铺评分表
-- 说明：一个用户对一个店铺只能有一条有效评分
-- =========================
DROP TABLE IF EXISTS shop_rating;
CREATE TABLE shop_rating
(
    id         BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键 id',
    userId     BIGINT   NOT NULL COMMENT '用户 id',
    shopId     BIGINT   NOT NULL COMMENT '店铺 id',
    score      TINYINT  NOT NULL COMMENT '评分，1-5',
    createTime DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updateTime DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    isDelete   TINYINT  NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删 1-已删',
    PRIMARY KEY (id),
    KEY idx_shopId (shopId),
    KEY idx_score (score),
    KEY idx_createTime (createTime)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='店铺评分表';


-- =========================
-- 6. 收藏表
-- =========================
DROP TABLE IF EXISTS favorite;
CREATE TABLE favorite (
                          id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 id',
                          userId BIGINT NOT NULL COMMENT '用户 id',
                          shopId BIGINT NOT NULL COMMENT '店铺 id',
                          createTime DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                          updateTime DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                          isDelete TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删 1-已删',
                          PRIMARY KEY (id),
                          KEY idx_shopId (shopId)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收藏表';


-- =========================
-- 7. 优惠券表（券模板）
-- =========================
DROP TABLE IF EXISTS coupon;
CREATE TABLE coupon (
                        id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 id',
                        shopId BIGINT NOT NULL COMMENT '店铺 id',
                        title VARCHAR(128) NOT NULL COMMENT '优惠券标题',
                        description VARCHAR(1024) DEFAULT NULL COMMENT '优惠券描述',
                        type TINYINT NOT NULL DEFAULT 0 COMMENT '优惠券类型：0-普通券 1-团购券 2-秒杀券',
                        price DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '原价',
                        discountPrice DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '优惠价/折后价',
                        stock INT NOT NULL DEFAULT 0 COMMENT '库存',
                        availableStartTime DATETIME DEFAULT NULL COMMENT '可领取开始时间',
                        availableEndTime DATETIME DEFAULT NULL COMMENT '可领取结束时间',
                        useStartTime DATETIME DEFAULT NULL COMMENT '可使用开始时间',
                        useEndTime DATETIME DEFAULT NULL COMMENT '可使用结束时间',
                        status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-下架 1-上架 2-审核中',
                        createTime DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                        updateTime DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                        isDelete TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删 1-已删',
                        PRIMARY KEY (id),
                        KEY idx_shopId (shopId),
                        KEY idx_type (type),
                        KEY idx_status (status),
                        KEY idx_availableStartTime (availableStartTime),
                        KEY idx_availableEndTime (availableEndTime)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='优惠券表（券模板）';


-- =========================
-- 8. 订单表
-- =========================
DROP TABLE IF EXISTS coupon_order;
CREATE TABLE coupon_order (
                              id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 id',
                              orderNo VARCHAR(64) NOT NULL COMMENT '订单号',
                              userId BIGINT NOT NULL COMMENT '用户 id',
                              shopId BIGINT NOT NULL COMMENT '店铺 id',
                              couponId BIGINT NOT NULL COMMENT '优惠券 id',
                              totalAmount DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '订单总金额',
                              payAmount DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '实付金额',
                              payType varchar(20) NOT NULL COMMENT '支付方式',
                              status TINYINT NOT NULL DEFAULT 0 COMMENT '订单状态：0-待支付 1-已支付 2-已取消 3-已完成 4-已退款',
                              payTime DATETIME DEFAULT NULL COMMENT '支付时间',
                              cancelTime DATETIME DEFAULT NULL COMMENT '取消时间',
                              finishTime DATETIME DEFAULT NULL COMMENT '完成时间',
                              createTime DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                              updateTime DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                              isDelete TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删 1-已删',
                              PRIMARY KEY (id),
                              UNIQUE KEY uk_orderNo (orderNo),
                              KEY idx_userId (userId),
                              KEY idx_shopId (shopId),
                              KEY idx_couponId (couponId),
                              KEY idx_status (status),
                              KEY idx_createTime (createTime)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='优惠券订单表';


-- =========================
-- 9. 用户拥有的券表
-- =========================
DROP TABLE IF EXISTS user_coupon;
CREATE TABLE user_coupon (
                             id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 id',
                             userId BIGINT NOT NULL COMMENT '用户 id',
                             couponId BIGINT NOT NULL COMMENT '优惠券 id',
                             orderId BIGINT DEFAULT NULL COMMENT '关联订单 id',
                             code VARCHAR(64) NOT NULL COMMENT '券码/核销码',
                             status TINYINT NOT NULL DEFAULT 0 COMMENT '用户券状态：0-未使用 1-已使用 2-已过期 3-已退款',
                             obtainTime DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '获得时间',
                             useTime DATETIME DEFAULT NULL COMMENT '使用时间',
                             expireTime DATETIME DEFAULT NULL COMMENT '过期时间',
                             createTime DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                             updateTime DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                             isDelete TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删 1-已删',
                             PRIMARY KEY (id),
                             UNIQUE KEY uk_code (code),
                             KEY idx_userId (userId),
                             KEY idx_couponId (couponId),
                             KEY idx_orderId (orderId),
                             KEY idx_status (status),
                             KEY idx_expireTime (expireTime)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户拥有的优惠券表';

-- =========================
-- 数据初始化说明
-- 执行完本文件建表后，再执行 server/sql/init_data.sql 导入初始化与压测数据。
-- =========================

