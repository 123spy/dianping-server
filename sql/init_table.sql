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
    UNIQUE KEY uk_user_shop (userId, shopId),
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
                          UNIQUE KEY uk_user_shop (userId, shopId),
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
-- 10. 初始化用户数据
-- =========================
INSERT INTO user
(userName, userAccount, userPassword, userPhone, avatar, userProfile, userRole, status)
VALUES
    ('张三', 'zhangsan', '123456', '13800000001', 'https://example.com/avatar1.jpg', '喜欢探店和火锅', 'user', 0),
    ('李四', 'lisi', '123456', '13800000002', 'https://example.com/avatar2.jpg', '奶茶爱好者', 'user', 0),
    ('王店长', 'manager01', '123456', '13800000003', 'https://example.com/avatar3.jpg', '负责店铺运营', 'manager', 0),
    ('赵店长', 'manager02', '123456', '13800000004', 'https://example.com/avatar4.jpg', '擅长活动策划', 'manager', 0),
    ('管理员', 'admin', '123456', '13800000005', 'https://example.com/avatar5.jpg', '系统管理员', 'admin', 0);


-- =========================
-- 11. 初始化分类数据
-- 这里改成越大越靠前
-- =========================
INSERT INTO category
(name, sort)
VALUES ('火锅', 100),
       ('烧烤', 90),
       ('奶茶', 80),
       ('咖啡', 70),
       ('快餐', 60),
       ('甜品', 50);


-- =========================
-- 12. 初始化店铺数据
-- =========================
INSERT INTO shop
(managerId, name, description, tags, categoryId, longitude, latitude, address, city,
 businessStatus, auditStatus, avgScore, ratingCount, commentCount, favoriteCount, viewCount)
VALUES
    (3, '蜀香火锅', '主打川味牛油火锅，适合朋友聚餐', '火锅,聚餐,热门', 1,
     116.397128, 39.916527, '北京市朝阳区建国路88号', '北京',
     1, 1, 4.50, 2, 2, 2, 120),

    (4, '深夜烧烤摊', '营业到凌晨两点的烧烤小店', '烧烤,夜宵,人气', 2,
     116.407128, 39.926527, '北京市海淀区中关村大街100号', '北京',
     1, 1, 4.00, 1, 1, 1, 80),

    (3, '甜心奶茶', '招牌杨枝甘露和芝士葡萄', '奶茶,饮品,学生党', 3,
     121.473701, 31.230416, '上海市浦东新区世纪大道200号', '上海',
     1, 1, 5.00, 1, 1, 2, 150),

    (4, '慢时光咖啡馆', '适合学习办公的安静咖啡店', '咖啡,安静,下午茶', 4,
     121.483701, 31.240416, '上海市徐汇区漕溪北路66号', '上海',
     1, 1, 0.00, 0, 0, 0, 60),

    (3, '老街快餐', '平价便捷，出餐快', '快餐,便宜,午餐', 5,
     113.264385, 23.129112, '广州市天河区体育西路10号', '广州',
     1, 1, 0.00, 0, 0, 0, 40),

    (4, '樱花甜品屋', '主打蛋糕和双皮奶', '甜品,约会,下午茶', 6,
     113.274385, 23.139112, '广州市越秀区北京路66号', '广州',
     1, 1, 0.00, 0, 0, 1, 70);


-- =========================
-- 13. 初始化评论数据
-- 评论不再带 score
-- =========================
INSERT INTO comment
    (userId, shopId, content, likeCount, status)
VALUES (1, 1, '锅底很香，牛肉很新鲜，环境也不错。', 3, 0),
       (2, 1, '味道不错，就是排队有点久。', 1, 0),
       (1, 2, '烤串分量足，适合夜宵。', 2, 0),
       (2, 3, '奶茶很好喝，性价比高。', 5, 0),
       (1, 1, '这次和朋友来吃，服务态度也挺好。', 0, 0);


-- =========================
-- 14. 初始化评分数据
-- 一个用户对一个店铺只能有一条评分
-- =========================
INSERT INTO shop_rating
    (userId, shopId, score)
VALUES (1, 1, 5),
       (2, 1, 4),
       (1, 2, 4),
       (2, 3, 5);


-- =========================
-- 15. 初始化收藏数据
-- =========================
INSERT INTO favorite
(userId, shopId)
VALUES
    (1, 1),
    (1, 3),
    (2, 1),
    (2, 2),
    (2, 3),
    (1, 6);


-- =========================
-- 16. 初始化优惠券数据
-- =========================
INSERT INTO coupon
(shopId, title, description, type, price, discountPrice, stock,
 availableStartTime, availableEndTime, useStartTime, useEndTime, status)
VALUES
    (1, '双人火锅套餐', '包含锅底、肥牛、毛肚、蔬菜拼盘', 1, 168.00, 99.00, 100,
     '2026-03-01 00:00:00', '2026-12-31 23:59:59',
     '2026-03-01 00:00:00', '2026-12-31 23:59:59', 1),

    (1, '100元代金券', '满200元可用，不与其他优惠同享', 0, 100.00, 88.00, 200,
     '2026-03-01 00:00:00', '2026-12-31 23:59:59',
     '2026-03-01 00:00:00', '2026-12-31 23:59:59', 1),

    (2, '烧烤夜宵套餐', '适合2-3人夜宵聚会', 1, 128.00, 79.00, 80,
     '2026-03-01 00:00:00', '2026-10-31 23:59:59',
     '2026-03-01 00:00:00', '2026-10-31 23:59:59', 1),

    (3, '奶茶买二送一券', '限指定饮品使用', 0, 30.00, 20.00, 300,
     '2026-03-01 00:00:00', '2026-09-30 23:59:59',
     '2026-03-01 00:00:00', '2026-09-30 23:59:59', 1),

    (6, '甜品双人下午茶', '含两份甜品和两杯饮品', 1, 88.00, 59.00, 50,
     '2026-03-01 00:00:00', '2026-12-31 23:59:59',
     '2026-03-01 00:00:00', '2026-12-31 23:59:59', 1),

    (1, '限时秒杀火锅券', '工作日限定秒杀券', 2, 168.00, 49.90, 20,
     '2026-03-20 00:00:00', '2026-04-30 23:59:59',
     '2026-03-20 00:00:00', '2026-05-31 23:59:59', 1);


-- =========================
-- 17. 初始化订单数据
-- =========================
INSERT INTO coupon_order
(orderNo, userId, shopId, couponId, totalAmount, payAmount, status, payTime, cancelTime, finishTime)
VALUES
    ('ORD202603200001', 1, 1, 1, 168.00, 99.00, 1, '2026-03-20 10:00:00', NULL, NULL),
    ('ORD202603200002', 2, 2, 3, 128.00, 79.00, 3, '2026-03-20 11:00:00', NULL, '2026-03-21 18:30:00'),
    ('ORD202603200003', 1, 3, 4, 30.00, 20.00, 2, NULL, '2026-03-20 12:00:00', NULL),
    ('ORD202603200004', 2, 6, 5, 88.00, 59.00, 1, '2026-03-20 13:00:00', NULL, NULL);


-- =========================
-- 18. 初始化用户拥有的券数据
-- =========================
INSERT INTO user_coupon
(userId, couponId, orderId, code, status, obtainTime, useTime, expireTime)
VALUES
    (1, 1, 1, 'CODE202603200001', 0, '2026-03-20 10:00:00', NULL, '2026-12-31 23:59:59'),
    (2, 3, 2, 'CODE202603200002', 1, '2026-03-20 11:00:00', '2026-03-21 18:30:00', '2026-10-31 23:59:59'),
    (2, 5, 4, 'CODE202603200003', 0, '2026-03-20 13:00:00', NULL, '2026-12-31 23:59:59');


-- =========================
-- 19. 可选：如果你想按真实数据重新刷新店铺统计，也可以执行下面这些
-- =========================
UPDATE shop
SET avgScore      = 4.50,
    ratingCount   = 2,
    commentCount  = 3,
    favoriteCount = 2
WHERE id = 1;
UPDATE shop
SET avgScore      = 4.00,
    ratingCount   = 1,
    commentCount  = 1,
    favoriteCount = 1
WHERE id = 2;
UPDATE shop
SET avgScore      = 5.00,
    ratingCount   = 1,
    commentCount  = 1,
    favoriteCount = 2
WHERE id = 3;
UPDATE shop
SET avgScore      = 0.00,
    ratingCount   = 0,
    commentCount  = 0,
    favoriteCount = 0
WHERE id = 4;
UPDATE shop
SET avgScore      = 0.00,
    ratingCount   = 0,
    commentCount  = 0,
    favoriteCount = 0
WHERE id = 5;
UPDATE shop
SET avgScore      = 0.00,
    ratingCount   = 0,
    commentCount  = 0,
    favoriteCount = 1
WHERE id = 6;