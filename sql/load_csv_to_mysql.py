from __future__ import annotations

import argparse
import csv
from concurrent.futures import ThreadPoolExecutor, as_completed
from pathlib import Path

try:
    import pymysql
except ImportError as exc:  # pragma: no cover
    raise SystemExit("请先安装 pymysql：pip install pymysql") from exc


BASE_DIR = Path(__file__).resolve().parent
DATA_DIR = BASE_DIR / "data"

TABLE_FILES = [
    ("category", "categories.csv"),
    ("user", "users.csv"),
    ("shop", "shops.csv"),
    ("comment", "comments.csv"),
    ("shop_rating", "shop_ratings.csv"),
    ("favorite", "favorites.csv"),
    ("coupon", "coupons.csv"),
    ("coupon_order", "coupon_orders.csv"),
    ("user_coupon", "user_coupons.csv"),
]

TRUNCATE_ORDER = [
    "user_coupon",
    "coupon_order",
    "coupon",
    "favorite",
    "shop_rating",
    "comment",
    "shop",
    "user",
    "category",
]


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="多线程读取 server/sql/data 下的 CSV 并导入 MySQL。")
    parser.add_argument("--host", default="127.0.0.1")
    parser.add_argument("--port", type=int, default=3306)
    parser.add_argument("--user", default="root")
    parser.add_argument("--password", default="123456")
    parser.add_argument("--database", default="dianping_project")
    parser.add_argument("--workers", type=int, default=6, help="每张表的导入线程数")
    parser.add_argument("--chunk-size", type=int, default=1000, help="每批 executemany 的条数")
    parser.add_argument("--truncate", action="store_true", help="导入前先按依赖顺序清空表")
    return parser.parse_args()


def get_conn(args: argparse.Namespace):
    return pymysql.connect(
        host=args.host,
        port=args.port,
        user=args.user,
        password=args.password,
        database=args.database,
        charset="utf8mb4",
        autocommit=False,
        cursorclass=pymysql.cursors.Cursor,
    )


def chunked_rows(path: Path, chunk_size: int):
    with path.open("r", encoding="utf-8-sig", newline="") as f:
        reader = csv.DictReader(f)
        fieldnames = reader.fieldnames or []
        batch = []
        for row in reader:
            normalized = [None if value == "" else value for value in (row[name] for name in fieldnames)]
            batch.append(normalized)
            if len(batch) >= chunk_size:
                yield fieldnames, batch
                batch = []
        if batch:
            yield fieldnames, batch


def insert_chunk(args: argparse.Namespace, table_name: str, fieldnames: list[str], rows: list[list[str | None]]) -> int:
    placeholders = ", ".join(["%s"] * len(fieldnames))
    columns = ", ".join(f"`{name}`" for name in fieldnames)
    sql = f"INSERT INTO `{table_name}` ({columns}) VALUES ({placeholders})"

    conn = get_conn(args)
    try:
        with conn.cursor() as cursor:
            cursor.executemany(sql, rows)
        conn.commit()
        return len(rows)
    except Exception:
        conn.rollback()
        raise
    finally:
        conn.close()


def truncate_tables(args: argparse.Namespace) -> None:
    conn = get_conn(args)
    try:
        with conn.cursor() as cursor:
            for table_name in TRUNCATE_ORDER:
                cursor.execute(f"TRUNCATE TABLE `{table_name}`")
        conn.commit()
    finally:
        conn.close()


def refresh_shop_stats(args: argparse.Namespace) -> None:
    sql = """
    UPDATE shop s
    LEFT JOIN (
        SELECT shopId, ROUND(AVG(score), 2) AS avgScore, COUNT(*) AS ratingCount
        FROM shop_rating
        WHERE isDelete = 0
        GROUP BY shopId
    ) r ON s.id = r.shopId
    LEFT JOIN (
        SELECT shopId, COUNT(*) AS commentCount
        FROM comment
        WHERE isDelete = 0 AND status = 0
        GROUP BY shopId
    ) c ON s.id = c.shopId
    LEFT JOIN (
        SELECT shopId, COUNT(*) AS favoriteCount
        FROM favorite
        WHERE isDelete = 0
        GROUP BY shopId
    ) f ON s.id = f.shopId
    SET s.avgScore = IFNULL(r.avgScore, 0.00),
        s.ratingCount = IFNULL(r.ratingCount, 0),
        s.commentCount = IFNULL(c.commentCount, 0),
        s.favoriteCount = IFNULL(f.favoriteCount, 0),
        s.viewCount = GREATEST(
            s.viewCount,
            100 + IFNULL(r.ratingCount, 0) * 4 + IFNULL(c.commentCount, 0) * 3 + IFNULL(f.favoriteCount, 0) * 5
        ),
        s.updateTime = NOW()
    """
    conn = get_conn(args)
    try:
        with conn.cursor() as cursor:
            cursor.execute(sql)
        conn.commit()
    finally:
        conn.close()


def import_table(args: argparse.Namespace, table_name: str, filename: str) -> None:
    path = DATA_DIR / filename
    if not path.exists():
        raise FileNotFoundError(f"未找到数据文件: {path}")

    futures = []
    inserted = 0
    with ThreadPoolExecutor(max_workers=args.workers) as executor:
        for fieldnames, rows in chunked_rows(path, args.chunk_size):
            futures.append(executor.submit(insert_chunk, args, table_name, fieldnames, rows))
        for future in as_completed(futures):
            inserted += future.result()

    print(f"[完成] {table_name:<12} 导入 {inserted} 行")


def main() -> None:
    args = parse_args()

    if args.truncate:
        print("正在清空旧数据 ...")
        truncate_tables(args)

    for table_name, filename in TABLE_FILES:
        print(f"正在导入 {table_name} <- {filename}")
        import_table(args, table_name, filename)

    print("正在回刷店铺统计字段 ...")
    refresh_shop_stats(args)
    print("全部导入完成。")


if __name__ == "__main__":
    main()
