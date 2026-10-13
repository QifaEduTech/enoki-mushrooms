from __future__ import annotations

import hashlib
import hmac
import json
import sqlite3
from datetime import datetime, timezone
from pathlib import Path
from typing import Any

BASE_DIR = Path(__file__).resolve().parent
DB_PATH = BASE_DIR / "traceability.db"
GENESIS_PREV_HASH = "0" * 64
ORIGINAL_SUPPLIER = "豫北农业合作社"
TAMPERED_SUPPLIER = "未知供应商"
DEFAULT_BAG_CODE = "JGf85ba05a"


def utc_now() -> str:
    return datetime.now(timezone.utc).isoformat(timespec="seconds")


def get_connection() -> sqlite3.Connection:
    connection = sqlite3.connect(DB_PATH)
    connection.row_factory = sqlite3.Row
    connection.execute("PRAGMA foreign_keys = ON")
    return connection


def init_database(reset: bool = False) -> None:
    """创建演示数据库并写入五张业务表的样例数据。"""
    if reset and DB_PATH.exists():
        DB_PATH.unlink()

    with get_connection() as conn:
        conn.executescript(
            """
            CREATE TABLE IF NOT EXISTS mushroom_material (
                material_id TEXT PRIMARY KEY,
                raw_name TEXT NOT NULL,
                supplier TEXT NOT NULL,
                produce_date TEXT NOT NULL
            );

            CREATE TABLE IF NOT EXISTS mushroom_bag (
                bag_code TEXT PRIMARY KEY,
                material_id TEXT NOT NULL,
                strain_name TEXT NOT NULL,
                inoculation_date TEXT NOT NULL,
                FOREIGN KEY (material_id) REFERENCES mushroom_material(material_id)
            );

            CREATE TABLE IF NOT EXISTS mushroom_env (
                env_id INTEGER PRIMARY KEY AUTOINCREMENT,
                bag_code TEXT NOT NULL,
                collect_time TEXT NOT NULL,
                temp REAL NOT NULL,
                humidity REAL NOT NULL,
                co2 INTEGER NOT NULL,
                FOREIGN KEY (bag_code) REFERENCES mushroom_bag(bag_code)
            );

            CREATE TABLE IF NOT EXISTS mushroom_harvest (
                harvest_id TEXT PRIMARY KEY,
                bag_code TEXT NOT NULL,
                harvest_date TEXT NOT NULL,
                weight REAL NOT NULL,
                inspector TEXT NOT NULL,
                FOREIGN KEY (bag_code) REFERENCES mushroom_bag(bag_code)
            );

            CREATE TABLE IF NOT EXISTS mushroom_sale (
                sale_id TEXT PRIMARY KEY,
                harvest_id TEXT NOT NULL,
                distributor TEXT NOT NULL,
                store_name TEXT NOT NULL,
                sale_date TEXT NOT NULL,
                FOREIGN KEY (harvest_id) REFERENCES mushroom_harvest(harvest_id)
            );

            CREATE TABLE IF NOT EXISTS blocks (
                block_index INTEGER PRIMARY KEY,
                timestamp TEXT NOT NULL,
                bag_code TEXT NOT NULL,
                data_hash TEXT NOT NULL,
                prev_hash TEXT NOT NULL,
                block_hash TEXT NOT NULL
            );

            CREATE TABLE IF NOT EXISTS certifications (
                bag_code TEXT PRIMARY KEY,
                block_index INTEGER NOT NULL,
                block_hash TEXT NOT NULL,
                data_hash TEXT NOT NULL,
                certified_at TEXT NOT NULL,
                snapshot_json TEXT NOT NULL
            );

            CREATE TABLE IF NOT EXISTS operation_logs (
                log_id INTEGER PRIMARY KEY AUTOINCREMENT,
                created_at TEXT NOT NULL,
                action TEXT NOT NULL,
                detail TEXT NOT NULL
            );

            CREATE TABLE IF NOT EXISTS scan_counts (
                bag_code TEXT PRIMARY KEY,
                scan_count INTEGER NOT NULL DEFAULT 0
            );

            CREATE TABLE IF NOT EXISTS trace_profile (
                bag_code TEXT PRIMARY KEY,
                profile_json TEXT NOT NULL
            );
            """
        )

        profile = {
            "product_name": "金针菇",
            "strain_name": "金针菇F21",
            "company": "金针菇种植基地",
            "origin": "吉林省长春市九台区龙嘉街道龙家堡村",
            "producer": "曲仁涛",
            "phone": "18943949110",
            "base_area": "金针菇种植基地（3000亩）",
            "produce_date": "2026-06-18",
            "timeline": [
                {
                    "name": "拌料",
                    "date": "2026-06-01",
                    "start_time": "2026-06-01 08:00",
                    "end_time": "2026-06-01 16:30",
                    "operator": "李强",
                    "area": "配料车间 · 双螺旋拌料机",
                    "notes": "棉籽壳复合培养基按 SOP-01 配比，混合均匀后送入制包车间。",
                },
                {
                    "name": "制包",
                    "date": "2026-06-02",
                    "start_time": "2026-06-02 08:00",
                    "end_time": "2026-06-02 17:00",
                    "operator": "王磊",
                    "area": "制包车间 · 自动装袋机组",
                    "notes": "菌包规格 1.2kg/包，当日完成 1200 包并抽检重量。",
                },
                {
                    "name": "灭菌",
                    "date": "2026-06-02",
                    "start_time": "2026-06-02 18:00",
                    "end_time": "2026-06-03 06:00",
                    "operator": "王磊",
                    "area": "灭菌区 · 高压灭菌柜（121℃）",
                    "notes": "121℃ 高压灭菌，灭菌曲线由设备自动记录并留存。",
                },
                {
                    "name": "接种",
                    "date": "2026-06-03",
                    "start_time": "2026-06-03 08:30",
                    "end_time": "2026-06-03 12:00",
                    "operator": "赵敏",
                    "area": "无菌接种室 · 超净工作台",
                    "notes": "无菌室接种，菌种为金针菇F21，接种后即时封口。",
                },
                {
                    "name": "培养",
                    "date": "2026-06-04",
                    "start_time": "2026-06-04 00:00",
                    "end_time": "2026-06-10 08:00",
                    "operator": "赵敏",
                    "area": "恒温培养室 · 智能温控系统",
                    "notes": "恒温暗光培养，菌丝长满后转入出菇区。",
                },
                {
                    "name": "入库",
                    "date": "2026-06-10",
                    "start_time": "2026-06-10 08:00",
                    "end_time": "2026-06-10 17:00",
                    "operator": "刘洋",
                    "area": "培养库房 · 条码扫码枪",
                    "notes": "培养室菌包按批次入库，并完成生长区分配。",
                },
                {
                    "name": "生长管理",
                    "date": "2026-06-10",
                    "start_time": "2026-06-10 08:00",
                    "end_time": "2026-06-18 06:00",
                    "operator": "刘洋",
                    "area": "出菇区 · 环境监测系统",
                    "notes": "温湿度、光照、CO₂ 实时监测，异常自动告警。",
                },
                {
                    "name": "采收",
                    "date": "2026-06-18",
                    "start_time": "2026-06-18 06:00",
                    "end_time": "2026-06-18 11:00",
                    "operator": "孙丽",
                    "area": "出菇区 · 专用采收工具",
                    "notes": "成熟度抽检合格后采收，采后 2 小时内进入预冷。",
                },
                {
                    "name": "包装",
                    "date": "2026-06-18",
                    "start_time": "2026-06-18 13:00",
                    "end_time": "2026-06-18 17:30",
                    "operator": "周芳",
                    "area": "包装车间 · 自动称重贴标线",
                    "notes": "称重、贴标、装箱，包装信息同步写入追溯系统。",
                },
            ],
            "environment_monitor": "温室A：24.3℃，湿度60%，光照147Lux，CO₂ 858ppm",
            "inspector": "王语",
            "inspect_date": "2026-06-18",
            "report": "已出具合格报告",
            "pack_date": "2026-06-18",
            "pack_qty": "50 箱",
            "pack_weight": "1000 公斤",
            "packaging_image": "/static/packaging.jpg",
            "qr_image": "/static/trace-qr.png",
            "sale": {
                "customer": "北京",
                "location": "北京",
                "sale_date": "2026-06-19",
                "quantity": "1000 公斤",
                "total_weight": "1000 公斤",
                "recorder": "张秀宁",
                "sale_note": "冷链配送，全程温度 2~8℃，随车附合格质检报告。",
            },
            "credit_level": "信用良好",
            "credit_record": "无失信记录",
            "credit_sample": "≥98%",
            "credit_complaint": "无投诉",
            "credit_penalty": "无",
            "credit_lawsuit": "无",
            "credit_tax": "A级",
        }
        conn.execute(
            """
            INSERT INTO trace_profile(bag_code, profile_json) VALUES (?, ?)
            ON CONFLICT(bag_code) DO UPDATE SET profile_json = excluded.profile_json
            """,
            (DEFAULT_BAG_CODE, json.dumps(profile, ensure_ascii=False)),
        )

        existing = conn.execute(
            "SELECT 1 FROM mushroom_bag WHERE bag_code = ?", (DEFAULT_BAG_CODE,)
        ).fetchone()
        if existing:
            return

        conn.execute(
            "INSERT INTO mushroom_material VALUES (?, ?, ?, ?)",
            ("MAT001", "棉籽壳复合培养基", ORIGINAL_SUPPLIER, "2026-06-01"),
        )
        conn.execute(
            "INSERT INTO mushroom_bag VALUES (?, ?, ?, ?)",
            (DEFAULT_BAG_CODE, "MAT001", "金针菇F21", "2026-06-03"),
        )
        conn.executemany(
            """
            INSERT INTO mushroom_env(bag_code, collect_time, temp, humidity, co2)
            VALUES (?, ?, ?, ?, ?)
            """,
            [
                (DEFAULT_BAG_CODE, "2026-06-08 08:00:00", 14.2, 88.5, 1180),
                (DEFAULT_BAG_CODE, "2026-06-08 12:00:00", 14.7, 87.9, 1215),
                (DEFAULT_BAG_CODE, "2026-06-08 16:00:00", 14.4, 89.1, 1192),
            ],
        )
        conn.execute(
            "INSERT INTO mushroom_harvest VALUES (?, ?, ?, ?, ?)",
            ("HAR001", DEFAULT_BAG_CODE, "2026-06-18", 2.36, "张检验员"),
        )
        conn.execute(
            "INSERT INTO mushroom_sale VALUES (?, ?, ?, ?, ?)",
            ("SALE001", "HAR001", "中原冷链配送", "郑州示范门店", "2026-06-19"),
        )
        _write_log(conn, "初始化", "创建五表演示数据，默认菌包编码为 JGf85ba05a")


def _write_log(conn: sqlite3.Connection, action: str, detail: str) -> None:
    conn.execute(
        "INSERT INTO operation_logs(created_at, action, detail) VALUES (?, ?, ?)",
        (utc_now(), action, detail),
    )


def record_scan(bag_code: str) -> int:
    """每次扫码访问页面时累加扫描次数，返回最新计数。"""
    with get_connection() as conn:
        conn.execute(
            "INSERT OR IGNORE INTO scan_counts(bag_code, scan_count) VALUES (?, 0)",
            (bag_code,),
        )
        conn.execute(
            "UPDATE scan_counts SET scan_count = scan_count + 1 WHERE bag_code = ?",
            (bag_code,),
        )
        row = conn.execute(
            "SELECT scan_count FROM scan_counts WHERE bag_code = ?",
            (bag_code,),
        ).fetchone()
    return int(row["scan_count"])


def rows_to_dicts(rows: list[sqlite3.Row]) -> list[dict[str, Any]]:
    return [dict(row) for row in rows]


def get_trace_snapshot(bag_code: str) -> dict[str, Any] | None:
    """按固定规则查询五张表，并组装为结构化数据库快照。"""
    with get_connection() as conn:
        bag = conn.execute(
            """
            SELECT bag_code, material_id, strain_name, inoculation_date
            FROM mushroom_bag
            WHERE bag_code = ?
            """,
            (bag_code,),
        ).fetchone()
        if bag is None:
            return None

        material = conn.execute(
            """
            SELECT m.material_id, m.raw_name, m.supplier, m.produce_date
            FROM mushroom_material m
            JOIN mushroom_bag b ON b.material_id = m.material_id
            WHERE b.bag_code = ?
            """,
            (bag_code,),
        ).fetchone()

        environment = conn.execute(
            """
            SELECT env_id, collect_time, temp, humidity, co2
            FROM mushroom_env
            WHERE bag_code = ?
            ORDER BY collect_time, env_id
            """,
            (bag_code,),
        ).fetchall()

        harvest = conn.execute(
            """
            SELECT harvest_id, harvest_date, weight, inspector
            FROM mushroom_harvest
            WHERE bag_code = ?
            ORDER BY harvest_date, harvest_id
            """,
            (bag_code,),
        ).fetchall()

        sales = conn.execute(
            """
            SELECT s.sale_id, s.harvest_id, s.distributor, s.store_name, s.sale_date
            FROM mushroom_sale s
            JOIN mushroom_harvest h ON h.harvest_id = s.harvest_id
            WHERE h.bag_code = ?
            ORDER BY s.sale_date, s.sale_id
            """,
            (bag_code,),
        ).fetchall()
    return {
        "bag_code": bag_code,
        "material": dict(material) if material else None,
        "bag": dict(bag),
        "environment": rows_to_dicts(environment),
        "harvest": rows_to_dicts(harvest),
        "sales": rows_to_dicts(sales),
    }


def get_trace_profile(bag_code: str) -> dict[str, Any] | None:
    """读取菌包对应的展示档案；没有档案时返回 None。"""
    with get_connection() as conn:
        row = conn.execute(
            "SELECT profile_json FROM trace_profile WHERE bag_code = ?",
            (bag_code,),
        ).fetchone()
    if row is None:
        return None
    return json.loads(row["profile_json"])


def _normalize_for_hash(value: Any) -> Any:
    """递归标准化：固定字典字段顺序，并固定多条记录顺序。"""
    if isinstance(value, dict):
        return {key: _normalize_for_hash(value[key]) for key in sorted(value)}
    if isinstance(value, list):
        normalized_items = [_normalize_for_hash(item) for item in value]
        return sorted(
            normalized_items,
            key=lambda item: json.dumps(
                item,
                ensure_ascii=False,
                sort_keys=True,
                separators=(",", ":"),
                default=str,
            ),
        )
    if isinstance(value, (str, int, float, bool)) or value is None:
        return value
    return str(value)


def canonical_snapshot(snapshot: dict[str, Any]) -> str:
    """生成稳定 JSON：中文保留、字段固定、记录固定、空格移除。"""
    normalized = _normalize_for_hash(snapshot)
    return json.dumps(
        normalized,
        ensure_ascii=False,
        sort_keys=True,
        separators=(",", ":"),
        default=str,
    )


def snapshot_hash(snapshot: dict[str, Any]) -> str:
    """对标准化数据库快照计算 64 位 SHA-256 数据指纹。"""
    canonical_json = canonical_snapshot(snapshot)
    return hashlib.sha256(canonical_json.encode("utf-8")).hexdigest()


def calculate_block_hash(
    index: int,
    timestamp: str,
    bag_code: str,
    data_hash: str,
    prev_hash: str,
) -> str:
    raw = f"{index}|{timestamp}|{bag_code}|{data_hash}|{prev_hash}"
    return hashlib.sha256(raw.encode("utf-8")).hexdigest()


def certify_trace(bag_code: str) -> dict[str, Any]:
    snapshot = get_trace_snapshot(bag_code)
    if snapshot is None:
        raise ValueError("未查询到该菌包")

    canonical_json = canonical_snapshot(snapshot)
    data_hash = snapshot_hash(snapshot)
    timestamp = utc_now()

    with get_connection() as conn:
        last_block = conn.execute(
            "SELECT * FROM blocks ORDER BY block_index DESC LIMIT 1"
        ).fetchone()
        index = 0 if last_block is None else int(last_block["block_index"]) + 1
        prev_hash = GENESIS_PREV_HASH if last_block is None else last_block["block_hash"]
        block_hash = calculate_block_hash(
            index, timestamp, bag_code, data_hash, prev_hash
        )

        conn.execute(
            """
            INSERT INTO blocks(
                block_index, timestamp, bag_code, data_hash, prev_hash, block_hash
            ) VALUES (?, ?, ?, ?, ?, ?)
            """,
            (index, timestamp, bag_code, data_hash, prev_hash, block_hash),
        )
        conn.execute(
            """
            INSERT INTO certifications(
                bag_code, block_index, block_hash, data_hash, certified_at, snapshot_json
            ) VALUES (?, ?, ?, ?, ?, ?)
            ON CONFLICT(bag_code) DO UPDATE SET
                block_index = excluded.block_index,
                block_hash = excluded.block_hash,
                data_hash = excluded.data_hash,
                certified_at = excluded.certified_at,
                snapshot_json = excluded.snapshot_json
            """,
            (bag_code, index, block_hash, data_hash, timestamp, canonical_json),
        )
        _write_log(
            conn,
            "区块链存证",
            f"菌包 {bag_code} 写入第 {index} 个区块，数据指纹 {data_hash[:12]}…",
        )

    return {
        "bag_code": bag_code,
        "block_index": index,
        "block_hash": block_hash,
        "data_hash": data_hash,
        "certified_at": timestamp,
    }


def verify_chain_integrity() -> tuple[bool, list[str]]:
    errors: list[str] = []
    with get_connection() as conn:
        blocks = conn.execute(
            "SELECT * FROM blocks ORDER BY block_index"
        ).fetchall()

    expected_prev = GENESIS_PREV_HASH
    for expected_index, block in enumerate(blocks):
        if block["block_index"] != expected_index:
            errors.append(f"区块编号不连续：期望 {expected_index}")
        if block["prev_hash"] != expected_prev:
            errors.append(f"区块 {block['block_index']} 的前序哈希不匹配")

        recalculated = calculate_block_hash(
            block["block_index"],
            block["timestamp"],
            block["bag_code"],
            block["data_hash"],
            block["prev_hash"],
        )
        if not hmac.compare_digest(recalculated, block["block_hash"]):
            errors.append(f"区块 {block['block_index']} 的整体哈希被修改")
        expected_prev = block["block_hash"]

    return len(errors) == 0, errors


def verify_trace(bag_code: str, record_log: bool = True) -> dict[str, Any]:
    snapshot = get_trace_snapshot(bag_code)
    if snapshot is None:
        raise ValueError("未查询到该菌包")

    current_hash = snapshot_hash(snapshot)
    chain_valid, chain_errors = verify_chain_integrity()

    with get_connection() as conn:
        certified = conn.execute(
            "SELECT * FROM certifications WHERE bag_code = ?", (bag_code,)
        ).fetchone()

        if certified is None:
            result = {
                "bag_code": bag_code,
                "certified": False,
                "chain_valid": chain_valid,
                "chain_errors": chain_errors,
                "certified_hash": None,
                "current_hash": current_hash,
                "data_intact": False,
                "trusted": False,
                "message": "该菌包尚未存证",
            }
            if record_log:
                _write_log(conn, "可信验证", f"菌包 {bag_code} 尚未存证")
            return result
        data_intact = hmac.compare_digest(certified["data_hash"], current_hash)
        trusted = chain_valid and data_intact
        message = "数据与存证时一致" if trusted else "检测到数据变化，请人工核查"
        if record_log:
            _write_log(
                conn,
                "可信验证",
                f"菌包 {bag_code} 验证结果：{'可信' if trusted else '不可信'}",
            )
    return {
        "bag_code": bag_code,
        "certified": True,
        "block_index": certified["block_index"],
        "block_hash": certified["block_hash"],
        "certified_at": certified["certified_at"],
        "chain_valid": chain_valid,
        "chain_errors": chain_errors,
        "certified_hash": certified["data_hash"],
        "current_hash": current_hash,
        "data_intact": data_intact,
        "trusted": trusted,
        "message": message,
    }


def tamper_supplier(bag_code: str = DEFAULT_BAG_CODE) -> dict[str, Any]:
    with get_connection() as conn:
        row = conn.execute(
            "SELECT material_id FROM mushroom_bag WHERE bag_code = ?", (bag_code,)
        ).fetchone()
        if row is None:
            raise ValueError("未查询到该菌包")
        conn.execute(
            "UPDATE mushroom_material SET supplier = ? WHERE material_id = ?",
            (TAMPERED_SUPPLIER, row["material_id"]),
        )
        _write_log(
            conn,
            "模拟篡改",
            f"演示数据库：{bag_code} 的供应商改为“{TAMPERED_SUPPLIER}”",
        )
    return {"bag_code": bag_code, "supplier": TAMPERED_SUPPLIER}


def restore_supplier(bag_code: str = DEFAULT_BAG_CODE) -> dict[str, Any]:
    with get_connection() as conn:
        row = conn.execute(
            "SELECT material_id FROM mushroom_bag WHERE bag_code = ?", (bag_code,)
        ).fetchone()
        if row is None:
            raise ValueError("未查询到该菌包")
        conn.execute(
            "UPDATE mushroom_material SET supplier = ? WHERE material_id = ?",
            (ORIGINAL_SUPPLIER, row["material_id"]),
        )
        _write_log(
            conn,
            "恢复数据",
            f"演示数据库：{bag_code} 的供应商恢复为“{ORIGINAL_SUPPLIER}”",
        )
    return {"bag_code": bag_code, "supplier": ORIGINAL_SUPPLIER}


def get_blocks() -> list[dict[str, Any]]:
    with get_connection() as conn:
        rows = conn.execute(
            "SELECT * FROM blocks ORDER BY block_index DESC"
        ).fetchall()
    return rows_to_dicts(rows)


def get_logs(limit: int = 30) -> list[dict[str, Any]]:
    with get_connection() as conn:
        rows = conn.execute(
            """
            SELECT created_at, action, detail
            FROM operation_logs
            ORDER BY log_id DESC
            LIMIT ?
            """,
            (limit,),
        ).fetchall()
    return rows_to_dicts(rows)


def get_dashboard_status(bag_code: str = DEFAULT_BAG_CODE) -> dict[str, Any]:
    snapshot = get_trace_snapshot(bag_code)
    verification = verify_trace(bag_code, record_log=False) if snapshot else None
    scan_count = record_scan(bag_code) if snapshot else 0
    return {
        "bag_code": bag_code,
        "scan_count": scan_count,
        "profile": get_trace_profile(bag_code),
        "snapshot": snapshot,
        "verification": verification,
        "blocks": get_blocks(),
        "logs": get_logs(),
    }
