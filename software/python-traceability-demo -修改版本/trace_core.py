from __future__ import annotations

import hashlib
import json
import sqlite3
from datetime import datetime, timezone
from pathlib import Path
from typing import Any

from main_system_client import MainSystemUnavailable, fetch_main_system_data


BASE_DIR = Path(__file__).resolve().parent
DB_PATH = BASE_DIR / "trace_state.db"
GENESIS_PREV_HASH = "0" * 64
DEFAULT_BAG_CODE = "PLANT-2"


def utc_now() -> str:
    return datetime.now(timezone.utc).isoformat(timespec="seconds")


def get_connection() -> sqlite3.Connection:
    connection = sqlite3.connect(DB_PATH)
    connection.row_factory = sqlite3.Row
    return connection


def init_database(reset: bool = False) -> None:
    """只初始化扫码、日志和存证状态；业务数据始终来自主系统 MySQL。"""
    if reset and DB_PATH.exists():
        DB_PATH.unlink()

    with get_connection() as conn:
        conn.executescript(
            """
            CREATE TABLE IF NOT EXISTS blocks (
                block_index INTEGER PRIMARY KEY,
                timestamp TEXT NOT NULL,
                trace_code TEXT NOT NULL,
                data_hash TEXT NOT NULL,
                prev_hash TEXT NOT NULL,
                block_hash TEXT NOT NULL
            );

            CREATE TABLE IF NOT EXISTS certifications (
                trace_code TEXT PRIMARY KEY,
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
                trace_code TEXT PRIMARY KEY,
                scan_count INTEGER NOT NULL DEFAULT 0
            );
            """
        )
        conn.execute(
            "INSERT INTO operation_logs(created_at, action, detail) VALUES (?, ?, ?)",
            (utc_now(), "系统启动", "业务数据源：Smart-Enokimushroom MySQL"),
        )


def _write_log(conn: sqlite3.Connection, action: str, detail: str) -> None:
    conn.execute(
        "INSERT INTO operation_logs(created_at, action, detail) VALUES (?, ?, ?)",
        (utc_now(), action, detail),
    )


def record_scan(trace_code: str) -> int:
    with get_connection() as conn:
        conn.execute(
            "INSERT OR IGNORE INTO scan_counts(trace_code, scan_count) VALUES (?, 0)",
            (trace_code,),
        )
        conn.execute(
            "UPDATE scan_counts SET scan_count = scan_count + 1 WHERE trace_code = ?",
            (trace_code,),
        )
        row = conn.execute(
            "SELECT scan_count FROM scan_counts WHERE trace_code = ?", (trace_code,)
        ).fetchone()
    return int(row["scan_count"])


def _date_only(value: Any) -> str | None:
    if value is None:
        return None
    return str(value).split(" ", 1)[0].split("T", 1)[0]


def _environment_summary(record: dict[str, Any] | None) -> str | None:
    if not record:
        return None
    parts = []
    if record.get("air_temperature") is not None:
        parts.append(f"空气温度 {record['air_temperature']}℃")
    if record.get("air_humidity") is not None:
        parts.append(f"空气湿度 {record['air_humidity']}%")
    if record.get("light_lux") is not None:
        parts.append(f"光照 {record['light_lux']}Lux")
    if record.get("air_co2") is not None:
        parts.append(f"CO₂ {record['air_co2']}ppm")
    return "，".join(parts) or None


def _timeline(main: dict[str, Any]) -> list[dict[str, Any]]:
    plant = main["plant"]
    area = main.get("area") or {}
    items: list[dict[str, Any]] = []

    if plant.get("create_date"):
        items.append(
            {
                "name": "种植档案建立",
                "date": _date_only(plant.get("create_date")),
                "start_time": plant.get("create_date"),
                "operator": plant.get("create_by"),
                "area": area.get("area_name"),
                "notes": "；".join(
                    part
                    for part in [
                        f"主系统植物编号：{plant.get('plant_id')}",
                        f"植物类型：{plant.get('plant_type')}" if plant.get("plant_type") else None,
                        f"生命周期：{plant.get('life_cycle')}个月" if plant.get("life_cycle") is not None else None,
                    ]
                    if part
                ),
            }
        )

    for device in main.get("devices", []):
        state = "正常使用" if device.get("device_state") == 1 else "停用"
        items.append(
            {
                "name": "设备接入",
                "date": _date_only(device.get("create_date")),
                "start_time": device.get("create_date"),
                "area": device.get("device_name"),
                "notes": "；".join(
                    part
                    for part in [
                        f"设备编号：{device.get('device_number')}" if device.get("device_number") else None,
                        f"设备类型：{device.get('device_type')}" if device.get("device_type") else None,
                        f"状态：{state}",
                    ]
                    if part
                ),
            }
        )

    for cluster in main.get("clusters", []):
        items.append(
            {
                "name": "产量预测",
                "date": _date_only(cluster.get("last_update")),
                "start_time": cluster.get("last_update"),
                "area": cluster.get("cluster_name") or f"簇 {cluster.get('cluster_id')}",
                "notes": "；".join(
                    part
                    for part in [
                        f"总数 {cluster.get('total_count')}" if cluster.get("total_count") is not None else None,
                        f"成熟 {cluster.get('ripe_count')}" if cluster.get("ripe_count") is not None else None,
                        f"半成熟 {cluster.get('half_ripe_count')}" if cluster.get("half_ripe_count") is not None else None,
                        f"未成熟 {cluster.get('unripe_count')}" if cluster.get("unripe_count") is not None else None,
                        cluster.get("recommendation"),
                    ]
                    if part
                ),
            }
        )

    for pest in main.get("pest_diseases", []):
        confidence = pest.get("confidence")
        confidence_text = f"置信度 {float(confidence) * 100:.0f}%" if confidence is not None else None
        items.append(
            {
                "name": "病虫害检测",
                "date": _date_only(pest.get("detected_time")),
                "start_time": pest.get("detected_time"),
                "area": pest.get("object_type"),
                "notes": "；".join(
                    part
                    for part in [
                        pest.get("pest_disease_class"),
                        f"严重程度：{pest.get('severity_level')}" if pest.get("severity_level") else None,
                        confidence_text,
                        pest.get("recommendation"),
                    ]
                    if part
                ),
            }
        )
    return items


def _assemble(main: dict[str, Any]) -> tuple[dict[str, Any], dict[str, Any]]:
    plant = main["plant"]
    area = main.get("area") or {}
    records = main.get("environment", [])
    latest = records[-1] if records else None
    pests = main.get("pest_diseases", [])
    supplement = main.get("supplement") or {}

    environment = [
        {
            "env_id": row.get("record_id"),
            "collect_time": row.get("create_date"),
            "temp": row.get("air_temperature"),
            "humidity": row.get("air_humidity"),
            "co2": row.get("air_co2"),
            "light_lux": row.get("light_lux"),
            "soil_temperature": row.get("soil_temperature"),
            "soil_humidity": row.get("soil_humidity"),
            "soil_ph": row.get("soil_ph"),
        }
        for row in records
    ]

    snapshot = {
        "bag_code": main["trace_code"],
        "material": {
            "material_id": f"AREA-{area.get('area_id')}" if area.get("area_id") else None,
            "raw_name": plant.get("plant_type"),
            "supplier": area.get("area_name"),
            "produce_date": None,
        },
        "bag": {
            "bag_code": main["trace_code"],
            "material_id": f"AREA-{area.get('area_id')}" if area.get("area_id") else None,
            "strain_name": None,
            "inoculation_date": None,
        },
        "environment": environment,
        "harvest": [],
        "sales": [],
        "devices": main.get("devices", []),
        "clusters": main.get("clusters", []),
        "pest_diseases": pests,
        "source": main.get("source"),
    }

    profile: dict[str, Any] = {
        "product_name": supplement.get("product_name") or plant.get("plant_name"),
        "strain_name": supplement.get("strain_name"),
        "company": area.get("area_name"),
        "origin": area.get("area_address"),
        "producer": area.get("area_leader"),
        "phone": area.get("area_phone"),
        "produce_date": supplement.get("produce_date"),
        "base_area": (
            f"{area.get('area_name')}（{area.get('area_square')}亩）"
            if area.get("area_name") and area.get("area_square") is not None
            else area.get("area_name")
        ),
        "timeline": _timeline(main),
        "environment_monitor": _environment_summary(latest),
        "qr_image": "/static/trace-qr.png",
        "pack_date": supplement.get("pack_date"),
        "pack_qty": supplement.get("pack_qty"),
        "pack_weight": supplement.get("pack_weight"),
        "packaging_image": supplement.get("packaging_image"),
        "sale": {
            "location": supplement.get("sale_location"),
            "customer": supplement.get("sale_customer"),
            "sale_date": supplement.get("sale_date"),
            "quantity": supplement.get("sale_quantity"),
            "recorder": supplement.get("sale_recorder"),
            "sale_note": supplement.get("sale_note"),
        } if supplement.get("sale_customer") else None,
        "credit_level": supplement.get("credit_level"),
        "credit_record": supplement.get("credit_record"),
        "credit_sample": supplement.get("credit_sample"),
        "credit_complaint": supplement.get("credit_complaint"),
        "credit_penalty": supplement.get("credit_penalty"),
        "credit_lawsuit": supplement.get("credit_lawsuit"),
        "credit_tax": supplement.get("credit_tax"),
        "inspector": supplement.get("inspector"),
        "report_no": supplement.get("report_no"),
    }

    if pests:
        pest = pests[0]
        confidence = pest.get("confidence")
        result = pest.get("pest_disease_class") or "发现异常"
        if pest.get("severity_level"):
            result += f"（{pest['severity_level']}）"
        if confidence is not None:
            result += f"，置信度 {float(confidence) * 100:.0f}%"
        profile.update(
            {
                "inspect_date": _date_only(pest.get("detected_time")),
                "report": result,
                "inspection_items": "病虫害智能检测",
            }
        )
    return snapshot, profile


def get_trace_snapshot(trace_code: str) -> dict[str, Any] | None:
    main = fetch_main_system_data(trace_code)
    return _assemble(main)[0] if main else None


def get_trace_profile(trace_code: str) -> dict[str, Any] | None:
    main = fetch_main_system_data(trace_code)
    return _assemble(main)[1] if main else None


def _normalize_for_hash(value: Any) -> Any:
    if isinstance(value, dict):
        return {key: _normalize_for_hash(value[key]) for key in sorted(value)}
    if isinstance(value, list):
        return [_normalize_for_hash(item) for item in value]
    return value


def canonical_snapshot(snapshot: dict[str, Any]) -> str:
    return json.dumps(_normalize_for_hash(snapshot), ensure_ascii=False, separators=(",", ":"))


def snapshot_hash(snapshot: dict[str, Any]) -> str:
    return hashlib.sha256(canonical_snapshot(snapshot).encode("utf-8")).hexdigest()


def calculate_block_hash(index: int, timestamp: str, trace_code: str, data_hash: str, prev_hash: str) -> str:
    raw = f"{index}|{timestamp}|{trace_code}|{data_hash}|{prev_hash}"
    return hashlib.sha256(raw.encode("utf-8")).hexdigest()


def certify_trace(trace_code: str) -> dict[str, Any]:
    snapshot = get_trace_snapshot(trace_code)
    if snapshot is None:
        raise ValueError("主系统未查询到该植物档案")
    data_hash = snapshot_hash(snapshot)
    timestamp = utc_now()
    with get_connection() as conn:
        last = conn.execute("SELECT * FROM blocks ORDER BY block_index DESC LIMIT 1").fetchone()
        index = int(last["block_index"]) + 1 if last else 0
        prev_hash = last["block_hash"] if last else GENESIS_PREV_HASH
        block_hash = calculate_block_hash(index, timestamp, trace_code, data_hash, prev_hash)
        conn.execute(
            "INSERT INTO blocks VALUES (?, ?, ?, ?, ?, ?)",
            (index, timestamp, trace_code, data_hash, prev_hash, block_hash),
        )
        conn.execute(
            """
            INSERT INTO certifications VALUES (?, ?, ?, ?, ?, ?)
            ON CONFLICT(trace_code) DO UPDATE SET
                block_index=excluded.block_index, block_hash=excluded.block_hash,
                data_hash=excluded.data_hash, certified_at=excluded.certified_at,
                snapshot_json=excluded.snapshot_json
            """,
            (trace_code, index, block_hash, data_hash, timestamp, canonical_snapshot(snapshot)),
        )
        _write_log(conn, "可信存证", f"{trace_code} 已按主系统当前数据生成存证")
    return {"trace_code": trace_code, "block_index": index, "block_hash": block_hash, "data_hash": data_hash, "certified_at": timestamp}


def verify_chain_integrity() -> tuple[bool, list[str]]:
    errors: list[str] = []
    blocks = get_blocks()
    previous = GENESIS_PREV_HASH
    for block in blocks:
        expected = calculate_block_hash(block["block_index"], block["timestamp"], block["trace_code"], block["data_hash"], block["prev_hash"])
        if block["prev_hash"] != previous:
            errors.append(f"区块 {block['block_index']} 前序哈希不一致")
        if block["block_hash"] != expected:
            errors.append(f"区块 {block['block_index']} 哈希不一致")
        previous = block["block_hash"]
    return not errors, errors


def verify_trace(
    trace_code: str,
    record_log: bool = True,
    snapshot: dict[str, Any] | None = None,
) -> dict[str, Any]:
    snapshot = snapshot if snapshot is not None else get_trace_snapshot(trace_code)
    if snapshot is None:
        return {"certified": False, "trusted": False, "message": "主系统未查询到该植物档案"}
    current_hash = snapshot_hash(snapshot)
    chain_ok, chain_errors = verify_chain_integrity()
    with get_connection() as conn:
        certification = conn.execute("SELECT * FROM certifications WHERE trace_code = ?", (trace_code,)).fetchone()
        certified = certification is not None
        trusted = bool(certified and chain_ok and certification["data_hash"] == current_hash)
        message = "当前主系统数据与存证一致" if trusted else ("尚未存证" if not certified else "主系统数据已变化或区块链异常")
        if record_log:
            _write_log(conn, "可信验证", f"{trace_code}：{message}")
    return {"certified": certified, "trusted": trusted, "message": message, "current_hash": current_hash, "chain_errors": chain_errors}


def tamper_supplier(trace_code: str = DEFAULT_BAG_CODE) -> dict[str, Any]:
    return {"supported": False, "message": "修改版只读主系统数据，不提供篡改主系统功能", "trace_code": trace_code}


def restore_supplier(trace_code: str = DEFAULT_BAG_CODE) -> dict[str, Any]:
    return {"supported": False, "message": "修改版没有改动主系统数据，无需恢复", "trace_code": trace_code}


def get_blocks() -> list[dict[str, Any]]:
    with get_connection() as conn:
        rows = conn.execute("SELECT * FROM blocks ORDER BY block_index").fetchall()
    return [dict(row) for row in rows]


def get_logs(limit: int = 30) -> list[dict[str, Any]]:
    with get_connection() as conn:
        rows = conn.execute("SELECT * FROM operation_logs ORDER BY log_id DESC LIMIT ?", (limit,)).fetchall()
    return [dict(row) for row in rows]


def get_dashboard_status(trace_code: str = DEFAULT_BAG_CODE) -> dict[str, Any]:
    try:
        main = fetch_main_system_data(trace_code)
        if main is None:
            return {
                "bag_code": trace_code,
                "scan_count": 0,
                "profile": None,
                "snapshot": None,
                "verification": None,
                "source_error": "主系统未查询到该植物档案",
            }
        snapshot, profile = _assemble(main)
        scan_count = record_scan(main["trace_code"])
        verification = verify_trace(main["trace_code"], record_log=False, snapshot=snapshot)
        return {
            "bag_code": main["trace_code"],
            "scan_count": scan_count,
            "profile": profile,
            "snapshot": snapshot,
            "verification": verification,
            "blocks": get_blocks(),
            "logs": get_logs(),
            "source": main.get("source"),
        }
    except MainSystemUnavailable as exc:
        return {
            "bag_code": trace_code,
            "scan_count": 0,
            "profile": None,
            "snapshot": None,
            "verification": None,
            "source_error": str(exc),
        }
