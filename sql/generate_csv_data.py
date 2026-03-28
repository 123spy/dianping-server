from __future__ import annotations

import csv
import json
from collections import defaultdict
from datetime import datetime, timedelta
from decimal import Decimal
from pathlib import Path


BASE_DIR = Path(__file__).resolve().parent
DATA_DIR = BASE_DIR / "data"

BASE_NOW = datetime(2026, 3, 25, 12, 0, 0)

STRESS_MANAGER_COUNT = 40
STRESS_USER_COUNT = 3000
STRESS_SHOP_COUNT = 3000
STRESS_COMMENT_COUNT = 60000
STRESS_RATING_COUNT = 40000
STRESS_FAVORITE_COUNT = 50000
STRESS_ORDER_COUNT = 15000


CATEGORIES = [
    (1, "火锅", 100),
    (2, "烧烤", 90),
    (3, "奶茶", 80),
    (4, "咖啡", 70),
    (5, "快餐", 60),
    (6, "甜品", 50),
    (7, "日料", 85),
    (8, "西餐", 75),
    (9, "川菜", 95),
    (10, "粤菜", 88),
]

CATEGORY_META = {
    1: {"prefix": "火锅", "name": "火锅店", "tags": ["火锅", "聚餐", "热门", "麻辣"]},
    2: {"prefix": "烧烤", "name": "烧烤铺", "tags": ["烧烤", "夜宵", "人气", "烤串"]},
    3: {"prefix": "奶茶", "name": "奶茶店", "tags": ["奶茶", "饮品", "学生党", "网红"]},
    4: {"prefix": "咖啡", "name": "咖啡馆", "tags": ["咖啡", "安静", "下午茶", "办公"]},
    5: {"prefix": "快餐", "name": "快餐店", "tags": ["快餐", "便宜", "午餐", "外卖"]},
    6: {"prefix": "甜品", "name": "甜品屋", "tags": ["甜品", "约会", "下午茶", "拍照"]},
    7: {"prefix": "日料", "name": "日料店", "tags": ["日料", "刺身", "寿司", "高端"]},
    8: {"prefix": "西餐", "name": "西餐厅", "tags": ["西餐", "牛排", "约会", "浪漫"]},
    9: {"prefix": "川菜", "name": "川菜馆", "tags": ["川菜", "麻辣", "聚餐", "地道"]},
    10: {"prefix": "粤菜", "name": "粤菜馆", "tags": ["粤菜", "茶餐厅", "点心", "早茶"]},
}

BASE_USERS = [
    ("张三", "zhangsan", "13800000001", "https://example.com/avatar1.jpg", "喜欢探店和火锅，美食爱好者", "user"),
    ("李四", "lisi", "13800000002", "https://example.com/avatar2.jpg", "奶茶爱好者，甜品控", "user"),
    ("王店长", "manager01", "13800000003", "https://example.com/avatar3.jpg", "负责店铺运营，10年餐饮经验", "manager"),
    ("赵店长", "manager02", "13800000004", "https://example.com/avatar4.jpg", "擅长活动策划和营销", "manager"),
    ("管理员", "admin", "13800000005", "https://example.com/avatar5.jpg", "系统管理员", "admin"),
    ("王小明", "wangxiaoming", "13800000006", "https://example.com/avatar6.jpg", "大学生，喜欢尝试新店", "user"),
    ("陈小红", "chenxiaohong", "13800000007", "https://example.com/avatar7.jpg", "美食博主，经常分享探店经历", "user"),
    ("刘伟", "liuwei", "13800000008", "https://example.com/avatar8.jpg", "上班族，喜欢火锅和烧烤", "user"),
    ("赵丽", "zhaoli", "13800000009", "https://example.com/avatar9.jpg", "下午茶爱好者，喜欢咖啡甜品", "user"),
    ("孙鹏", "sunpeng", "13800000010", "https://example.com/avatar10.jpg", "健身达人，喜欢健康轻食", "user"),
    ("周敏", "zhoumin", "13800000011", "https://example.com/avatar11.jpg", "宝妈，喜欢带孩子吃美食", "user"),
    ("吴刚", "wugang", "13800000012", "https://example.com/avatar12.jpg", "夜宵爱好者，烧烤达人", "user"),
    ("郑洁", "zhengjie", "13800000013", "https://example.com/avatar13.jpg", "甜品控，喜欢拍照打卡", "user"),
    ("林涛", "lintao", "13800000014", "https://example.com/avatar14.jpg", "咖啡爱好者，手冲达人", "user"),
    ("郭静", "guojing", "13800000015", "https://example.com/avatar15.jpg", "美食评论家，口味挑剔", "user"),
]

BASE_SHOPS = [
    (3, "蜀香火锅", "主打川味牛油火锅，适合朋友聚餐", ["火锅", "聚餐", "热门", "麻辣"], 1, "116.397128", "39.916527", "北京市朝阳区建国路88号", "北京"),
    (4, "深夜烧烤摊", "营业到凌晨两点的烧烤小店", ["烧烤", "夜宵", "人气", "烤串"], 2, "116.407128", "39.926527", "北京市海淀区中关村大街100号", "北京"),
    (3, "甜心奶茶", "招牌杨枝甘露和芝士葡萄", ["奶茶", "饮品", "学生党", "网红"], 3, "121.473701", "31.230416", "上海市浦东新区世纪大道200号", "上海"),
    (4, "慢时光咖啡馆", "适合学习办公的安静咖啡店", ["咖啡", "安静", "下午茶", "办公"], 4, "121.483701", "31.240416", "上海市徐汇区漕溪北路66号", "上海"),
    (3, "老街快餐", "平价便捷，出餐快", ["快餐", "便宜", "午餐", "外卖"], 5, "113.264385", "23.129112", "广州市天河区体育西路10号", "广州"),
    (4, "樱花甜品屋", "主打蛋糕和双皮奶", ["甜品", "约会", "下午茶", "拍照"], 6, "113.274385", "23.139112", "广州市越秀区北京路66号", "广州"),
    (3, "和风日料", "新鲜刺身，地道日本料理", ["日料", "刺身", "寿司", "高端"], 7, "116.417128", "39.936527", "北京市朝阳区三里屯路33号", "北京"),
    (4, "西堤牛排", "精选进口牛排，环境优雅", ["西餐", "牛排", "约会", "浪漫"], 8, "121.493701", "31.250416", "上海市黄浦区南京东路300号", "上海"),
    (3, "麻辣诱惑", "正宗川菜，麻辣鲜香", ["川菜", "麻辣", "聚餐", "地道"], 9, "113.284385", "23.149112", "广州市天河区珠江新城10号", "广州"),
    (4, "粤式茶餐厅", "地道粤菜，点心精致", ["粤菜", "茶餐厅", "点心", "早茶"], 10, "113.294385", "23.159112", "广州市越秀区中山五路20号", "广州"),
    (3, "喜茶", "网红奶茶，新品不断", ["奶茶", "网红", "打卡", "果茶"], 3, "116.427128", "39.946527", "北京市朝阳区朝阳大悦城", "北京"),
    (4, "星巴克", "经典咖啡，休闲空间", ["咖啡", "连锁", "商务", "休闲"], 4, "121.503701", "31.260416", "上海市静安区南京西路1000号", "上海"),
    (3, "海底捞火锅", "服务好，食材新鲜", ["火锅", "服务好", "聚餐", "热门"], 1, "113.304385", "23.169112", "广州市天河区体育西路50号", "广州"),
    (4, "奈雪的茶", "欧包+茶饮，品质保证", ["奶茶", "欧包", "网红", "下午茶"], 3, "116.437128", "39.956527", "北京市海淀区五道口购物中心", "北京"),
    (3, "必胜客", "披萨专家，家庭聚餐", ["西餐", "披萨", "家庭", "快餐"], 8, "121.513701", "31.270416", "上海市浦东新区陆家嘴环路", "上海"),
]


def fmt_time(dt: datetime) -> str:
    return dt.strftime("%Y-%m-%d %H:%M:%S")


def write_csv(name: str, rows: list[dict], fieldnames: list[str]) -> None:
    DATA_DIR.mkdir(parents=True, exist_ok=True)
    path = DATA_DIR / name
    with path.open("w", encoding="utf-8-sig", newline="") as f:
        writer = csv.DictWriter(f, fieldnames=fieldnames)
        writer.writeheader()
        writer.writerows(rows)


def generate_categories() -> list[dict]:
    rows = []
    for id_, name, sort in CATEGORIES:
        rows.append({
            "id": id_,
            "name": name,
            "sort": sort,
            "createTime": fmt_time(BASE_NOW),
            "updateTime": fmt_time(BASE_NOW),
            "isDelete": 0,
        })
    return rows


def generate_users() -> list[dict]:
    rows = []
    for idx, (name, account, phone, avatar, profile, role) in enumerate(BASE_USERS, start=1):
        rows.append({
            "id": idx,
            "userName": name,
            "userAccount": account,
            "userPassword": "123456",
            "userPhone": phone,
            "avatar": avatar,
            "userProfile": profile,
            "userRole": role,
            "status": 0,
            "createTime": fmt_time(BASE_NOW),
            "updateTime": fmt_time(BASE_NOW),
            "isDelete": 0,
        })

    next_id = len(rows) + 1
    for i in range(STRESS_MANAGER_COUNT):
        user_id = next_id + i
        rows.append({
            "id": user_id,
            "userName": f"压测店长{user_id:03d}",
            "userAccount": f"stress_manager_{user_id:04d}",
            "userPassword": "123456",
            "userPhone": f"137{user_id:08d}"[-11:],
            "avatar": f"https://example.com/stress/manager-{user_id}.jpg",
            "userProfile": "用于压测场景的店长账号",
            "userRole": "manager",
            "status": 0,
            "createTime": fmt_time(BASE_NOW - timedelta(days=i % 30)),
            "updateTime": fmt_time(BASE_NOW - timedelta(days=i % 7)),
            "isDelete": 0,
        })

    next_id = len(rows) + 1
    for i in range(STRESS_USER_COUNT):
        user_id = next_id + i
        rows.append({
            "id": user_id,
            "userName": f"压测用户{user_id:04d}",
            "userAccount": f"stress_user_{user_id:05d}",
            "userPassword": "123456",
            "userPhone": f"139{user_id:08d}"[-11:],
            "avatar": f"https://example.com/stress/user-{user_id}.jpg",
            "userProfile": f"用于并发压测的普通用户 #{user_id}",
            "userRole": "user",
            "status": 0,
            "createTime": fmt_time(BASE_NOW - timedelta(days=i % 120)),
            "updateTime": fmt_time(BASE_NOW - timedelta(days=i % 15)),
            "isDelete": 0,
        })
    return rows


def generate_shops() -> list[dict]:
    rows = []
    for idx, (manager_id, name, description, tags, category_id, lng, lat, address, city) in enumerate(BASE_SHOPS, start=1):
        rows.append({
            "id": idx,
            "managerId": manager_id,
            "name": name,
            "description": description,
            "tags": json.dumps(tags, ensure_ascii=False),
            "categoryId": category_id,
            "longitude": lng,
            "latitude": lat,
            "address": address,
            "city": city,
            "businessStatus": 1,
            "auditStatus": 1,
            "avgScore": "0.00",
            "ratingCount": 0,
            "commentCount": 0,
            "favoriteCount": 0,
            "viewCount": 50 + idx * 10,
            "createTime": fmt_time(BASE_NOW - timedelta(days=idx % 20)),
            "updateTime": fmt_time(BASE_NOW - timedelta(days=idx % 5)),
            "isDelete": 0,
        })

    next_id = len(rows) + 1
    cities = ["北京", "上海", "广州", "深圳", "成都", "杭州"]
    for i in range(STRESS_SHOP_COUNT):
        shop_id = next_id + i
        category_id = (i % 10) + 1
        meta = CATEGORY_META[category_id]
        city = cities[i % len(cities)]
        rows.append({
            "id": shop_id,
            "managerId": 16 + (i % STRESS_MANAGER_COUNT),
            "name": f"压测{meta['name']}{shop_id:04d}",
            "description": f"{meta['prefix']}场景门店，适合高频并发测试，门店编号 {shop_id}",
            "tags": json.dumps(meta["tags"], ensure_ascii=False),
            "categoryId": category_id,
            "longitude": f"{116.100000 + (i % 500) / 1000:.6f}",
            "latitude": f"{39.600000 + (i % 500) / 1000:.6f}",
            "address": f"{city}市压测大道{shop_id}号",
            "city": city,
            "businessStatus": 1,
            "auditStatus": 1,
            "avgScore": "0.00",
            "ratingCount": 0,
            "commentCount": 0,
            "favoriteCount": 0,
            "viewCount": 100 + (i * 7) % 5000,
            "createTime": fmt_time(BASE_NOW - timedelta(days=i % 90)),
            "updateTime": fmt_time(BASE_NOW - timedelta(days=i % 15)),
            "isDelete": 0,
        })
    return rows


def unique_pairs(total: int, user_ids: list[int], shop_ids: list[int], a: int, b: int) -> list[tuple[int, int]]:
    seen: set[tuple[int, int]] = set()
    pairs: list[tuple[int, int]] = []
    idx = 0
    while len(pairs) < total:
        user_id = user_ids[idx % len(user_ids)]
        shop_id = shop_ids[(idx * a + idx // b) % len(shop_ids)]
        pair = (user_id, shop_id)
        if pair not in seen:
            seen.add(pair)
            pairs.append(pair)
        idx += 1
    return pairs


def generate_comments(shop_ids: list[int], user_ids: list[int]) -> list[dict]:
    templates = [
        "环境不错，上菜速度快，整体体验很好",
        "口味在线，价格也比较合理，值得再来",
        "适合朋友聚会，服务态度让人满意",
        "饮品出品稳定，拍照也很出片",
        "午餐时段人很多，但翻台效率还可以",
        "分量足，性价比不错，适合工作日来吃",
        "甜品颜值高，味道也没有踩雷",
        "整体没有明显短板，是一间比较稳的店",
    ]
    rows = []
    for idx in range(1, STRESS_COMMENT_COUNT + 1):
        user_id = user_ids[(idx - 1) % len(user_ids)]
        shop_id = shop_ids[(idx * 7 + idx // 19) % len(shop_ids)]
        created_at = BASE_NOW - timedelta(days=idx % 180, minutes=idx % 1440)
        rows.append({
            "id": idx,
            "userId": user_id,
            "shopId": shop_id,
            "content": f"{templates[idx % len(templates)]}（压测评论 #{idx}）",
            "likeCount": (idx * 7) % 36,
            "status": 0,
            "createTime": fmt_time(created_at),
            "updateTime": fmt_time(created_at),
            "isDelete": 0,
        })
    return rows


def generate_ratings(shop_ids: list[int], user_ids: list[int]) -> list[dict]:
    score_pattern = [5, 4, 5, 4, 3, 5, 4, 5, 4, 5]
    rows = []
    for idx, (user_id, shop_id) in enumerate(unique_pairs(STRESS_RATING_COUNT, user_ids, shop_ids, 11, 17), start=1):
        created_at = BASE_NOW - timedelta(days=idx % 120, minutes=(idx * 3) % 720)
        rows.append({
            "id": idx,
            "userId": user_id,
            "shopId": shop_id,
            "score": score_pattern[idx % len(score_pattern)],
            "createTime": fmt_time(created_at),
            "updateTime": fmt_time(created_at),
            "isDelete": 0,
        })
    return rows


def generate_favorites(shop_ids: list[int], user_ids: list[int]) -> list[dict]:
    rows = []
    for idx, (user_id, shop_id) in enumerate(unique_pairs(STRESS_FAVORITE_COUNT, user_ids, shop_ids, 13, 23), start=1):
        created_at = BASE_NOW - timedelta(days=idx % 220, minutes=(idx * 5) % 500)
        rows.append({
            "id": idx,
            "userId": user_id,
            "shopId": shop_id,
            "createTime": fmt_time(created_at),
            "updateTime": fmt_time(created_at),
            "isDelete": 0,
        })
    return rows


def generate_coupons(shops: list[dict]) -> list[dict]:
    rows = []
    for idx, shop in enumerate(shops, start=1):
        price = Decimal(40 + (idx % 12) * 10)
        discount = price - Decimal(5 + (idx % 5) * 2)
        available_start = BASE_NOW - timedelta(days=10 + (idx % 30))
        available_end = BASE_NOW + timedelta(days=120 + (idx % 90))
        use_end = available_end + timedelta(days=30)
        rows.append({
            "id": idx,
            "shopId": shop["id"],
            "title": f"{shop['name']}优惠券",
            "description": f"{shop['name']} 的专属优惠券，适合压测订单和领券流程",
            "type": idx % 3,
            "price": f"{price:.2f}",
            "discountPrice": f"{discount:.2f}",
            "stock": 100 + (idx % 200),
            "availableStartTime": fmt_time(available_start),
            "availableEndTime": fmt_time(available_end),
            "useStartTime": fmt_time(available_start),
            "useEndTime": fmt_time(use_end),
            "status": 1,
            "createTime": fmt_time(BASE_NOW - timedelta(days=idx % 90)),
            "updateTime": fmt_time(BASE_NOW - timedelta(days=idx % 15)),
            "isDelete": 0,
        })
    return rows


def generate_orders_and_user_coupons(coupons: list[dict], user_ids: list[int]) -> tuple[list[dict], list[dict]]:
    orders: list[dict] = []
    user_coupons: list[dict] = []
    order_status_pattern = [0, 1, 1, 3, 2, 1, 4, 1, 3, 0]
    pay_types = ["wechat", "alipay"]

    for idx in range(1, STRESS_ORDER_COUNT + 1):
        coupon = coupons[(idx - 1) % len(coupons)]
        status = order_status_pattern[idx % len(order_status_pattern)]
        created_at = BASE_NOW - timedelta(days=idx % 120, minutes=idx % 1440)
        pay_time = created_at + timedelta(minutes=5) if status in (1, 3, 4) else None
        cancel_time = created_at + timedelta(minutes=20) if status == 2 else None
        finish_time = created_at + timedelta(days=1) if status == 3 else None

        orders.append({
            "id": idx,
            "orderNo": f"ORD202603{idx:06d}",
            "userId": user_ids[(idx * 3) % len(user_ids)],
            "shopId": coupon["shopId"],
            "couponId": coupon["id"],
            "totalAmount": coupon["price"],
            "payAmount": coupon["discountPrice"],
            "payType": pay_types[idx % 2],
            "status": status,
            "payTime": fmt_time(pay_time) if pay_time else "",
            "cancelTime": fmt_time(cancel_time) if cancel_time else "",
            "finishTime": fmt_time(finish_time) if finish_time else "",
            "createTime": fmt_time(created_at),
            "updateTime": fmt_time(created_at if status == 0 else (finish_time or cancel_time or pay_time or created_at)),
            "isDelete": 0,
        })

        if status in (1, 3, 4):
            coupon_status = 0 if status == 1 else (1 if status == 3 else 3)
            user_coupons.append({
                "id": len(user_coupons) + 1,
                "userId": orders[-1]["userId"],
                "couponId": coupon["id"],
                "orderId": idx,
                "code": f"CODE202603{idx:06d}",
                "status": coupon_status,
                "obtainTime": fmt_time(pay_time or created_at),
                "useTime": fmt_time(finish_time) if finish_time else "",
                "expireTime": fmt_time(created_at + timedelta(days=180)),
                "createTime": fmt_time(created_at),
                "updateTime": fmt_time(finish_time or pay_time or created_at),
                "isDelete": 0,
            })

    return orders, user_coupons


def refresh_shop_stats(
    shops: list[dict],
    comments: list[dict],
    ratings: list[dict],
    favorites: list[dict],
) -> None:
    rating_stats: dict[int, list[int]] = defaultdict(list)
    for item in ratings:
        rating_stats[item["shopId"]].append(item["score"])

    comment_counts: dict[int, int] = defaultdict(int)
    for item in comments:
        if item["status"] == 0:
            comment_counts[item["shopId"]] += 1

    favorite_counts: dict[int, int] = defaultdict(int)
    for item in favorites:
        favorite_counts[item["shopId"]] += 1

    for shop in shops:
        scores = rating_stats.get(shop["id"], [])
        rating_count = len(scores)
        avg_score = (sum(scores) / rating_count) if rating_count else 0
        comment_count = comment_counts.get(shop["id"], 0)
        favorite_count = favorite_counts.get(shop["id"], 0)
        view_count = max(int(shop["viewCount"]), 100 + rating_count * 4 + comment_count * 3 + favorite_count * 5)

        shop["avgScore"] = f"{avg_score:.2f}"
        shop["ratingCount"] = rating_count
        shop["commentCount"] = comment_count
        shop["favoriteCount"] = favorite_count
        shop["viewCount"] = view_count


def main() -> None:
    categories = generate_categories()
    users = generate_users()
    shops = generate_shops()

    user_ids = [item["id"] for item in users if item["userRole"] == "user"]
    shop_ids = [item["id"] for item in shops]

    comments = generate_comments(shop_ids, user_ids)
    ratings = generate_ratings(shop_ids, user_ids)
    favorites = generate_favorites(shop_ids, user_ids)
    refresh_shop_stats(shops, comments, ratings, favorites)

    coupons = generate_coupons(shops)
    orders, user_coupons = generate_orders_and_user_coupons(coupons, user_ids)

    write_csv("categories.csv", categories, ["id", "name", "sort", "createTime", "updateTime", "isDelete"])
    write_csv(
        "users.csv",
        users,
        ["id", "userName", "userAccount", "userPassword", "userPhone", "avatar", "userProfile", "userRole", "status", "createTime", "updateTime", "isDelete"],
    )
    write_csv(
        "shops.csv",
        shops,
        ["id", "managerId", "name", "description", "tags", "categoryId", "longitude", "latitude", "address", "city", "businessStatus", "auditStatus", "avgScore", "ratingCount", "commentCount", "favoriteCount", "viewCount", "createTime", "updateTime", "isDelete"],
    )
    write_csv(
        "comments.csv",
        comments,
        ["id", "userId", "shopId", "content", "likeCount", "status", "createTime", "updateTime", "isDelete"],
    )
    write_csv(
        "shop_ratings.csv",
        ratings,
        ["id", "userId", "shopId", "score", "createTime", "updateTime", "isDelete"],
    )
    write_csv(
        "favorites.csv",
        favorites,
        ["id", "userId", "shopId", "createTime", "updateTime", "isDelete"],
    )
    write_csv(
        "coupons.csv",
        coupons,
        ["id", "shopId", "title", "description", "type", "price", "discountPrice", "stock", "availableStartTime", "availableEndTime", "useStartTime", "useEndTime", "status", "createTime", "updateTime", "isDelete"],
    )
    write_csv(
        "coupon_orders.csv",
        orders,
        ["id", "orderNo", "userId", "shopId", "couponId", "totalAmount", "payAmount", "payType", "status", "payTime", "cancelTime", "finishTime", "createTime", "updateTime", "isDelete"],
    )
    write_csv(
        "user_coupons.csv",
        user_coupons,
        ["id", "userId", "couponId", "orderId", "code", "status", "obtainTime", "useTime", "expireTime", "createTime", "updateTime", "isDelete"],
    )

    print(f"CSV 数据已生成到: {DATA_DIR}")


if __name__ == "__main__":
    main()
