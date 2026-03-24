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
-- 10. 初始化用户数据（生成更多用户）
-- =========================
INSERT INTO user
(userName, userAccount, userPassword, userPhone, avatar, userProfile, userRole, status)
VALUES
    ('张三', 'zhangsan', '123456', '13800000001', 'https://example.com/avatar1.jpg', '喜欢探店和火锅，美食爱好者', 'user', 0),
    ('李四', 'lisi', '123456', '13800000002', 'https://example.com/avatar2.jpg', '奶茶爱好者，甜品控', 'user', 0),
    ('王店长', 'manager01', '123456', '13800000003', 'https://example.com/avatar3.jpg', '负责店铺运营，10年餐饮经验', 'manager', 0),
    ('赵店长', 'manager02', '123456', '13800000004', 'https://example.com/avatar4.jpg', '擅长活动策划和营销', 'manager', 0),
    ('管理员', 'admin', '123456', '13800000005', 'https://example.com/avatar5.jpg', '系统管理员', 'admin', 0),
    ('王小明', 'wangxiaoming', '123456', '13800000006', 'https://example.com/avatar6.jpg', '大学生，喜欢尝试新店', 'user', 0),
    ('陈小红', 'chenxiaohong', '123456', '13800000007', 'https://example.com/avatar7.jpg', '美食博主，经常分享探店经历', 'user', 0),
    ('刘伟', 'liuwei', '123456', '13800000008', 'https://example.com/avatar8.jpg', '上班族，喜欢火锅和烧烤', 'user', 0),
    ('赵丽', 'zhaoli', '123456', '13800000009', 'https://example.com/avatar9.jpg', '下午茶爱好者，喜欢咖啡甜品', 'user', 0),
    ('孙鹏', 'sunpeng', '123456', '13800000010', 'https://example.com/avatar10.jpg', '健身达人，喜欢健康轻食', 'user', 0),
    ('周敏', 'zhoumin', '123456', '13800000011', 'https://example.com/avatar11.jpg', '宝妈，喜欢带孩子吃美食', 'user', 0),
    ('吴刚', 'wugang', '123456', '13800000012', 'https://example.com/avatar12.jpg', '夜宵爱好者，烧烤达人', 'user', 0),
    ('郑洁', 'zhengjie', '123456', '13800000013', 'https://example.com/avatar13.jpg', '甜品控，喜欢拍照打卡', 'user', 0),
    ('林涛', 'lintao', '123456', '13800000014', 'https://example.com/avatar14.jpg', '咖啡爱好者，手冲达人', 'user', 0),
    ('郭静', 'guojing', '123456', '13800000015', 'https://example.com/avatar15.jpg', '美食评论家，口味挑剔', 'user', 0);


-- =========================
-- 11. 初始化分类数据
-- =========================
INSERT INTO category
(name, sort)
VALUES
    ('火锅', 100),
    ('烧烤', 90),
    ('奶茶', 80),
    ('咖啡', 70),
    ('快餐', 60),
    ('甜品', 50),
    ('日料', 85),
    ('西餐', 75),
    ('川菜', 95),
    ('粤菜', 88);


-- =========================
-- 12. 初始化店铺数据（tags使用JSON数组格式）
-- =========================
INSERT INTO shop
(managerId, name, description, tags, categoryId, longitude, latitude, address, city,
 businessStatus, auditStatus, avgScore, ratingCount, commentCount, favoriteCount, viewCount)
VALUES
    (3, '蜀香火锅', '主打川味牛油火锅，适合朋友聚餐', '["火锅","聚餐","热门","麻辣"]', 1,
     116.397128, 39.916527, '北京市朝阳区建国路88号', '北京',
     1, 1, 4.50, 2, 2, 2, 120),

    (4, '深夜烧烤摊', '营业到凌晨两点的烧烤小店', '["烧烤","夜宵","人气","烤串"]', 2,
     116.407128, 39.926527, '北京市海淀区中关村大街100号', '北京',
     1, 1, 4.00, 1, 1, 1, 80),

    (3, '甜心奶茶', '招牌杨枝甘露和芝士葡萄', '["奶茶","饮品","学生党","网红"]', 3,
     121.473701, 31.230416, '上海市浦东新区世纪大道200号', '上海',
     1, 1, 5.00, 1, 1, 2, 150),

    (4, '慢时光咖啡馆', '适合学习办公的安静咖啡店', '["咖啡","安静","下午茶","办公"]', 4,
     121.483701, 31.240416, '上海市徐汇区漕溪北路66号', '上海',
     1, 1, 0.00, 0, 0, 0, 60),

    (3, '老街快餐', '平价便捷，出餐快', '["快餐","便宜","午餐","外卖"]', 5,
     113.264385, 23.129112, '广州市天河区体育西路10号', '广州',
     1, 1, 0.00, 0, 0, 0, 40),

    (4, '樱花甜品屋', '主打蛋糕和双皮奶', '["甜品","约会","下午茶","拍照"]', 6,
     113.274385, 23.139112, '广州市越秀区北京路66号', '广州',
     1, 1, 0.00, 0, 0, 1, 70),

    (3, '和风日料', '新鲜刺身，地道日本料理', '["日料","刺身","寿司","高端"]', 7,
     116.417128, 39.936527, '北京市朝阳区三里屯路33号', '北京',
     1, 1, 4.80, 5, 5, 3, 200),

    (4, '西堤牛排', '精选进口牛排，环境优雅', '["西餐","牛排","约会","浪漫"]', 8,
     121.493701, 31.250416, '上海市黄浦区南京东路300号', '上海',
     1, 1, 4.60, 3, 3, 2, 180),

    (3, '麻辣诱惑', '正宗川菜，麻辣鲜香', '["川菜","麻辣","聚餐","地道"]', 9,
     113.284385, 23.149112, '广州市天河区珠江新城10号', '广州',
     1, 1, 4.70, 4, 4, 2, 160),

    (4, '粤式茶餐厅', '地道粤菜，点心精致', '["粤菜","茶餐厅","点心","早茶"]', 10,
     113.294385, 23.159112, '广州市越秀区中山五路20号', '广州',
     1, 1, 4.50, 3, 3, 2, 140),

    (3, '喜茶', '网红奶茶，新品不断', '["奶茶","网红","打卡","果茶"]', 3,
     116.427128, 39.946527, '北京市朝阳区朝阳大悦城', '北京',
     1, 1, 4.90, 8, 8, 5, 300),

    (4, '星巴克', '经典咖啡，休闲空间', '["咖啡","连锁","商务","休闲"]', 4,
     121.503701, 31.260416, '上海市静安区南京西路1000号', '上海',
     1, 1, 4.70, 6, 6, 4, 250),

    (3, '海底捞火锅', '服务好，食材新鲜', '["火锅","服务好","聚餐","热门"]', 1,
     113.304385, 23.169112, '广州市天河区体育西路50号', '广州',
     1, 1, 4.95, 15, 15, 8, 500),

    (4, '奈雪的茶', '欧包+茶饮，品质保证', '["奶茶","欧包","网红","下午茶"]', 3,
     116.437128, 39.956527, '北京市海淀区五道口购物中心', '北京',
     1, 1, 4.85, 10, 10, 6, 400),

    (3, '必胜客', '披萨专家，家庭聚餐', '["西餐","披萨","家庭","快餐"]', 8,
     121.513701, 31.270416, '上海市浦东新区陆家嘴环路', '上海',
     1, 1, 4.40, 4, 4, 2, 120);


-- =========================
-- 13. 初始化评论数据（生成更多评论）
-- =========================
INSERT INTO comment
(userId, shopId, content, likeCount, status)
VALUES
    -- 蜀香火锅评论
    (1, 1, '锅底很香，牛肉很新鲜，环境也不错。', 3, 0),
    (2, 1, '味道不错，就是排队有点久。', 1, 0),
    (1, 1, '这次和朋友来吃，服务态度也挺好。', 0, 0),
    (6, 1, '推荐毛肚和鸭血，非常好吃！', 2, 0),
    (7, 1, '价格稍微有点贵，但是值得。', 1, 0),
    (8, 1, '环境很干净，菜品新鲜，会再来。', 2, 0),
    (15, 1, '麻辣锅底很正宗，喜欢吃辣的朋友不要错过', 4, 0),

    -- 深夜烧烤摊评论
    (1, 2, '烤串分量足，适合夜宵。', 2, 0),
    (8, 2, '烤茄子特别好吃，推荐！', 1, 0),
    (12, 2, '营业到很晚，适合加班后吃', 3, 0),
    (6, 2, '羊肉串很嫩，调味不错', 1, 0),
    (10, 2, '价格实惠，性价比高', 2, 0),

    -- 甜心奶茶评论
    (2, 3, '奶茶很好喝，性价比高。', 5, 0),
    (9, 3, '杨枝甘露是我喝过最好喝的', 2, 0),
    (13, 3, '珍珠很Q弹，甜度刚好', 1, 0),
    (7, 3, '芝士葡萄也很不错，奶盖很香', 3, 0),
    (11, 3, '外卖包装很好，送达及时', 1, 0),

    -- 慢时光咖啡馆评论
    (14, 4, '环境很安静，适合学习办公', 2, 0),
    (9, 4, '手冲咖啡很专业，豆子品质好', 1, 0),
    (4, 4, '甜品也不错，提拉米苏推荐', 0, 0),

    -- 老街快餐评论
    (10, 5, '出餐快，味道不错，适合上班族', 1, 0),
    (6, 5, '价格实惠，分量足', 0, 0),
    (8, 5, '卫生条件不错，经常来吃', 1, 0),

    -- 樱花甜品屋评论
    (13, 6, '双皮奶很正宗，甜而不腻', 2, 0),
    (9, 6, '蛋糕颜值高，适合拍照', 3, 0),
    (11, 6, '带孩子来吃，很喜欢', 1, 0),
    (7, 6, '芒果班戟很好吃，水果新鲜', 2, 0),

    -- 和风日料评论
    (7, 7, '刺身很新鲜，三文鱼厚切太满足了', 4, 0),
    (6, 7, '环境很有日本风情，服务也很周到', 2, 0),
    (8, 7, '价格偏高但品质确实好', 1, 0),
    (15, 7, '寿司师傅很专业，值得一试', 3, 0),
    (10, 7, '鳗鱼饭超级好吃，推荐！', 2, 0),
    (1, 7, '清酒选择很多，适合约会', 2, 0),

    -- 西堤牛排评论
    (9, 8, '牛排五分熟刚刚好，环境很适合约会', 3, 0),
    (14, 8, '服务很专业，会主动介绍菜品', 2, 0),
    (11, 8, '甜品也很棒，推荐提拉米苏', 1, 0),
    (7, 8, '性价比不错，团购很划算', 2, 0),

    -- 麻辣诱惑评论
    (8, 9, '水煮鱼太正宗了，麻辣过瘾', 3, 0),
    (12, 9, '夫妻肺片味道很地道', 2, 0),
    (10, 9, '喜欢吃辣的一定要来试试', 4, 0),
    (6, 9, '价格实惠，分量足', 1, 0),
    (15, 9, '毛血旺料很足，味道很正', 3, 0),

    -- 粤式茶餐厅评论
    (11, 10, '虾饺皇很鲜美，皮薄馅大', 2, 0),
    (13, 10, '烧鹅外酥里嫩，很地道', 3, 0),
    (7, 10, '早茶品种丰富，味道正宗', 2, 0),
    (9, 10, '奶茶很丝滑，菠萝油好吃', 1, 0),

    -- 喜茶评论
    (7, 11, '多肉葡萄永远的神，果肉超多', 5, 0),
    (6, 11, '新品不错，颜值高味道好', 3, 0),
    (13, 11, '排队人多但值得等', 4, 0),
    (2, 11, '芝芝莓莓也很好喝', 2, 0),
    (8, 11, '服务态度很好，会推荐新品', 2, 0),
    (1, 11, '店面装修很时尚，适合打卡', 3, 0),

    -- 星巴克评论
    (14, 12, '馥芮白是我的最爱，环境舒适', 2, 0),
    (9, 12, '咖啡品质稳定，服务态度好', 1, 0),
    (4, 12, '星冰乐夏天必点', 1, 0),
    (10, 12, '商务洽谈的好地方', 2, 0),

    -- 海底捞火锅评论
    (6, 13, '服务真的太好了，免费美甲很贴心', 5, 0),
    (8, 13, '食材新鲜，锅底选择多', 4, 0),
    (7, 13, '番茄锅底超级好喝', 5, 0),
    (10, 13, '等位的时候有零食和饮料，很人性化', 3, 0),
    (1, 13, '甩面表演很精彩，小朋友喜欢', 4, 0),
    (15, 13, '价格虽然贵但服务值得', 4, 0),
    (12, 13, '深夜去还有优惠，很划算', 3, 0),
    (11, 13, '生日还会送礼物，很温馨', 4, 0),

    -- 奈雪的茶评论
    (9, 14, '霸气橙子很好喝，欧包也超赞', 4, 0),
    (13, 14, '草莓魔法棒颜值高又好吃', 3, 0),
    (7, 14, '环境很舒适，适合闺蜜聚会', 3, 0),
    (2, 14, '茶饮品质好，不会太甜', 2, 0),
    (6, 14, '软欧包品种多，都很好吃', 2, 0),

    -- 必胜客评论
    (11, 15, '披萨料很足，芝心卷边好吃', 2, 0),
    (8, 15, '适合带孩子来吃，有儿童套餐', 1, 0),
    (10, 15, '下午茶套餐很划算', 1, 0),
    (14, 15, '服务态度好，上菜速度快', 1, 0);


-- =========================
-- 14. 初始化评分数据（生成更多评分）
-- =========================
INSERT INTO shop_rating
(userId, shopId, score)
VALUES
    -- 蜀香火锅评分
    (1, 1, 5),
    (2, 1, 4),
    (6, 1, 5),
    (7, 1, 4),
    (8, 1, 5),
    (15, 1, 4),

    -- 深夜烧烤摊评分
    (1, 2, 4),
    (8, 2, 4),
    (12, 2, 5),
    (6, 2, 3),
    (10, 2, 4),

    -- 甜心奶茶评分
    (2, 3, 5),
    (9, 3, 5),
    (13, 3, 5),
    (7, 3, 4),
    (11, 3, 4),

    -- 慢时光咖啡馆评分
    (14, 4, 4),
    (9, 4, 5),
    (4, 4, 4),

    -- 老街快餐评分
    (10, 5, 4),
    (6, 5, 3),
    (8, 5, 4),

    -- 樱花甜品屋评分
    (13, 6, 5),
    (9, 6, 4),
    (11, 6, 5),
    (7, 6, 4),

    -- 和风日料评分
    (7, 7, 5),
    (6, 7, 5),
    (8, 7, 4),
    (15, 7, 5),
    (10, 7, 5),
    (1, 7, 4),

    -- 西堤牛排评分
    (9, 8, 5),
    (14, 8, 5),
    (11, 8, 4),
    (7, 8, 4),

    -- 麻辣诱惑评分
    (8, 9, 5),
    (12, 9, 5),
    (10, 9, 5),
    (6, 9, 4),
    (15, 9, 4),

    -- 粤式茶餐厅评分
    (11, 10, 5),
    (13, 10, 4),
    (7, 10, 5),
    (9, 10, 4),

    -- 喜茶评分
    (7, 11, 5),
    (6, 11, 5),
    (13, 11, 5),
    (2, 11, 5),
    (8, 11, 4),
    (1, 11, 4),

    -- 星巴克评分
    (14, 12, 5),
    (9, 12, 4),
    (4, 12, 4),
    (10, 12, 5),

    -- 海底捞火锅评分
    (6, 13, 5),
    (8, 13, 5),
    (7, 13, 5),
    (10, 13, 5),
    (1, 13, 5),
    (15, 13, 5),
    (12, 13, 4),
    (11, 13, 5),

    -- 奈雪的茶评分
    (9, 14, 5),
    (13, 14, 5),
    (7, 14, 4),
    (2, 14, 5),
    (6, 14, 4),

    -- 必胜客评分
    (11, 15, 4),
    (8, 15, 4),
    (10, 15, 4),
    (14, 15, 5);


-- =========================
-- 15. 初始化收藏数据（生成更多收藏）
-- =========================
INSERT INTO favorite
(userId, shopId)
VALUES
    (1, 1),
    (1, 3),
    (1, 7),
    (1, 11),
    (1, 13),
    (2, 1),
    (2, 2),
    (2, 3),
    (2, 11),
    (2, 14),
    (6, 1),
    (6, 7),
    (6, 9),
    (6, 13),
    (7, 3),
    (7, 7),
    (7, 11),
    (7, 13),
    (8, 2),
    (8, 9),
    (8, 13),
    (9, 3),
    (9, 8),
    (9, 10),
    (9, 14),
    (10, 5),
    (10, 7),
    (10, 12),
    (10, 13),
    (11, 6),
    (11, 10),
    (11, 13),
    (12, 2),
    (12, 9),
    (12, 13),
    (13, 3),
    (13, 6),
    (13, 11),
    (13, 14),
    (14, 4),
    (14, 8),
    (14, 12),
    (15, 1),
    (15, 7),
    (15, 9),
    (15, 13);


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
     '2026-03-20 00:00:00', '2026-05-31 23:59:59', 1),
    (7, '单人日料套餐', '包含刺身拼盘、寿司、味增汤', 1, 188.00, 128.00, 50,
     '2026-03-01 00:00:00', '2026-12-31 23:59:59',
     '2026-03-01 00:00:00', '2026-12-31 23:59:59', 1),
    (8, '双人牛排套餐', '含两份牛排、沙拉、汤品', 1, 298.00, 198.00, 60,
     '2026-03-01 00:00:00', '2026-12-31 23:59:59',
     '2026-03-01 00:00:00', '2026-12-31 23:59:59', 1),
    (9, '川菜四人餐', '水煮鱼、夫妻肺片、宫保鸡丁等', 1, 268.00, 188.00, 40,
     '2026-03-01 00:00:00', '2026-12-31 23:59:59',
     '2026-03-01 00:00:00', '2026-12-31 23:59:59', 1),
    (11, '喜茶买一送一券', '指定新品买一送一', 0, 28.00, 14.00, 500,
     '2026-03-01 00:00:00', '2026-12-31 23:59:59',
     '2026-03-01 00:00:00', '2026-12-31 23:59:59', 1),
    (13, '海底捞88折券', '全场菜品88折', 0, 0.00, 0.00, 1000,
     '2026-03-01 00:00:00', '2026-12-31 23:59:59',
     '2026-03-01 00:00:00', '2026-12-31 23:59:59', 1),
    (14, '奈雪下午茶套餐', '霸气橙子+欧包', 1, 45.00, 29.90, 200,
     '2026-03-01 00:00:00', '2026-12-31 23:59:59',
     '2026-03-01 00:00:00', '2026-12-31 23:59:59', 1);


-- =========================
-- 17. 初始化订单数据
-- =========================
INSERT INTO coupon_order
(orderNo, userId, shopId, couponId, totalAmount, payAmount, payType, status, payTime, cancelTime, finishTime)
VALUES
    ('ORD202603200001', 1, 1, 1, 168.00, 99.00, 'wechat', 1, '2026-03-20 10:00:00', NULL, NULL),
    ('ORD202603200002', 2, 2, 3, 128.00, 79.00, 'alipay', 3, '2026-03-20 11:00:00', NULL, '2026-03-21 18:30:00'),
    ('ORD202603200003', 1, 3, 4, 30.00, 20.00, 'wechat', 2, NULL, '2026-03-20 12:00:00', NULL),
    ('ORD202603200004', 2, 6, 5, 88.00, 59.00, 'alipay', 1, '2026-03-20 13:00:00', NULL, NULL),
    ('ORD202603200005', 6, 7, 7, 188.00, 128.00, 'wechat', 1, '2026-03-21 14:00:00', NULL, NULL),
    ('ORD202603200006', 7, 8, 8, 298.00, 198.00, 'alipay', 3, '2026-03-21 15:00:00', NULL, '2026-03-22 12:00:00'),
    ('ORD202603200007', 8, 9, 9, 268.00, 188.00, 'wechat', 1, '2026-03-21 16:00:00', NULL, NULL),
    ('ORD202603200008', 9, 11, 10, 28.00, 14.00, 'alipay', 0, NULL, NULL, NULL),
    ('ORD202603200009', 10, 13, 11, 0.00, 0.00, 'wechat', 1, '2026-03-22 10:00:00', NULL, NULL),
    ('ORD202603200010', 11, 14, 12, 45.00, 29.90, 'alipay', 1, '2026-03-22 11:00:00', NULL, NULL),
    ('ORD202603200011', 12, 1, 2, 100.00, 88.00, 'wechat', 4, '2026-03-22 12:00:00', NULL, NULL),
    ('ORD202603200012', 13, 3, 4, 30.00, 20.00, 'alipay', 2, NULL, '2026-03-22 13:00:00', NULL),
    ('ORD202603200013', 14, 4, 1, 50.00, 50.00, 'wechat', 3, '2026-03-22 14:00:00', NULL, '2026-03-23 10:00:00'),
    ('ORD202603200014', 15, 7, 7, 188.00, 128.00, 'alipay', 1, '2026-03-23 15:00:00', NULL, NULL),
    ('ORD202603200015', 1, 13, 11, 0.00, 0.00, 'wechat', 1, '2026-03-23 16:00:00', NULL, NULL);

-- =========================
-- 18. 初始化用户拥有的券数据
-- =========================
INSERT INTO user_coupon
(userId, couponId, orderId, code, status, obtainTime, useTime, expireTime)
VALUES
    (1, 1, 1, 'CODE202603200001', 0, '2026-03-20 10:00:00', NULL, '2026-12-31 23:59:59'),
    (2, 3, 2, 'CODE202603200002', 1, '2026-03-20 11:00:00', '2026-03-21 18:30:00', '2026-10-31 23:59:59'),
    (2, 5, 4, 'CODE202603200003', 0, '2026-03-20 13:00:00', NULL, '2026-12-31 23:59:59'),
    (6, 7, 5, 'CODE202603200004', 0, '2026-03-21 14:00:00', NULL, '2026-12-31 23:59:59'),
    (7, 8, 6, 'CODE202603200005', 1, '2026-03-21 15:00:00', '2026-03-22 12:00:00', '2026-12-31 23:59:59'),
    (8, 9, 7, 'CODE202603200006', 0, '2026-03-21 16:00:00', NULL, '2026-12-31 23:59:59'),
    (9, 10, 8, 'CODE202603200007', 0, '2026-03-21 17:00:00', NULL, '2026-12-31 23:59:59'),
    (10, 11, 9, 'CODE202603200008', 0, '2026-03-22 10:00:00', NULL, '2026-12-31 23:59:59'),
    (11, 12, 10, 'CODE202603200009', 0, '2026-03-22 11:00:00', NULL, '2026-12-31 23:59:59'),
    (12, 2, 11, 'CODE202603200010', 1, '2026-03-22 12:00:00', '2026-03-23 14:00:00', '2026-12-31 23:59:59'),
    (13, 4, 12, 'CODE202603200011', 0, '2026-03-22 13:00:00', NULL, '2026-09-30 23:59:59'),
    (15, 7, 14, 'CODE202603200012', 0, '2026-03-23 15:00:00', NULL, '2026-12-31 23:59:59'),
    (1, 11, 15, 'CODE202603200013', 0, '2026-03-23 16:00:00', NULL, '2026-12-31 23:59:59');


-- =========================
-- 19. 更新店铺统计数据
-- =========================
UPDATE shop SET avgScore = 4.50, ratingCount = 6, commentCount = 7, favoriteCount = 3 WHERE id = 1;
UPDATE shop SET avgScore = 4.00, ratingCount = 5, commentCount = 5, favoriteCount = 2 WHERE id = 2;
UPDATE shop SET avgScore = 4.80, ratingCount = 5, commentCount = 5, favoriteCount = 4 WHERE id = 3;
UPDATE shop SET avgScore = 4.33, ratingCount = 3, commentCount = 3, favoriteCount = 1 WHERE id = 4;
UPDATE shop SET avgScore = 3.67, ratingCount = 3, commentCount = 3, favoriteCount = 1 WHERE id = 5;
UPDATE shop SET avgScore = 4.50, ratingCount = 4, commentCount = 4, favoriteCount = 2 WHERE id = 6;
UPDATE shop SET avgScore = 4.67, ratingCount = 6, commentCount = 6, favoriteCount = 4 WHERE id = 7;
UPDATE shop SET avgScore = 4.50, ratingCount = 4, commentCount = 4, favoriteCount = 3 WHERE id = 8;
UPDATE shop SET avgScore = 4.60, ratingCount = 5, commentCount = 5, favoriteCount = 3 WHERE id = 9;
UPDATE shop SET avgScore = 4.50, ratingCount = 4, commentCount = 4, favoriteCount = 2 WHERE id = 10;
UPDATE shop SET avgScore = 4.67, ratingCount = 6, commentCount = 6, favoriteCount = 5 WHERE id = 11;
UPDATE shop SET avgScore = 4.50, ratingCount = 4, commentCount = 4, favoriteCount = 2 WHERE id = 12;
UPDATE shop SET avgScore = 4.88, ratingCount = 8, commentCount = 8, favoriteCount = 6 WHERE id = 13;
UPDATE shop SET avgScore = 4.60, ratingCount = 5, commentCount = 5, favoriteCount = 3 WHERE id = 14;
UPDATE shop SET avgScore = 4.25, ratingCount = 4, commentCount = 4, favoriteCount = 2 WHERE id = 15;