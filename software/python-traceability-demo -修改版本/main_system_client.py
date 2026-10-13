from __future__ import annotations

import json
import os
import re
import shutil
import subprocess
from pathlib import Path
from typing import Any


BASE_DIR = Path(__file__).resolve().parent
PROJECT_ROOT = BASE_DIR.parent

# Keep the demo portable: these locations are all relative to the demo's
# parent project, so the whole project may be moved without reconfiguration.
_DRUID_CONFIGS = (
    PROJECT_ROOT / "Smart-Enokimushroom" / "Enokimushroom-admin" / "src" / "main" / "resources" / "application-druid.yml",
    PROJECT_ROOT / "Smart-Enokimushroom" / "Enokimushroom-admin" / "target" / "classes" / "application-druid.yml",
    PROJECT_ROOT / "portable" / "Smart-Enokimushroom" / "Smart-Enokimushroom" / "Enokimushroom-admin" / "src" / "main" / "resources" / "application-druid.yml",
)


class MainSystemUnavailable(RuntimeError):
    """主系统数据库不可访问。"""


def _project_setting(name: str) -> str | None:
    pattern = re.compile(rf'(?:if\s+not\s+defined\s+{re.escape(name)}\s+)?set\s+"{re.escape(name)}=([^"\r\n]+)"', re.IGNORECASE)
    for path in (PROJECT_ROOT / "runtime" / "ports.cmd", PROJECT_ROOT / "start-windows.bat"):
        if not path.exists():
            continue
        match = pattern.search(path.read_text(encoding="utf-8", errors="ignore"))
        if match:
            return match.group(1).strip()
    return None


def _druid_master_setting(name: str) -> str | None:
    """Read the active master datasource setting without requiring PyYAML."""
    pattern = re.compile(rf"^\s*{re.escape(name)}\s*:\s*(.*?)\s*(?:#.*)?$", re.IGNORECASE | re.MULTILINE)
    for path in _DRUID_CONFIGS:
        if not path.exists():
            continue
        content = path.read_text(encoding="utf-8", errors="ignore")
        master = re.search(r"^\s*master\s*:\s*$([\s\S]*?)(?=^\s*slave\s*:|\Z)", content, re.IGNORECASE | re.MULTILINE)
        if not master:
            continue
        match = pattern.search(master.group(1))
        if match:
            value = match.group(1).strip().strip("\"'")
            if value:
                return value
    return None


def _runtime_port() -> int:
    configured = os.getenv("SMART_MYSQL_PORT")
    if configured:
        return int(configured)

    project_port = _project_setting("MYSQL_PORT")
    if project_port:
        return int(project_port)

    url = _druid_master_setting("url")
    if url:
        match = re.search(r":(\d+)(?:/|\?)", url)
        if match:
            return int(match.group(1))
    return 3306


def _mysql_executable() -> str:
    configured = os.getenv("SMART_MYSQL_EXE")
    if configured:
        return configured

    for bundled in (
        PROJECT_ROOT / "env" / "mysql" / "bin" / "mysql.exe",
        PROJECT_ROOT / "portable" / "Smart-Enokimushroom" / "env" / "mysql" / "bin" / "mysql.exe",
    ):
        if bundled.exists():
            return str(bundled)

    system_mysql = shutil.which("mysql")
    if system_mysql:
        return system_mysql
    raise MainSystemUnavailable("未找到 MySQL 客户端，请检查主系统 env/mysql 目录")


def _query_json_lines(sql: str) -> list[dict[str, Any]]:
    command = [
        _mysql_executable(),
        "--batch",
        "--raw",
        "--skip-column-names",
        "--default-character-set=utf8mb4",
        "--connect-timeout=4",
        "--host",
        os.getenv("SMART_MYSQL_HOST", "127.0.0.1"),
        "--port",
        str(_runtime_port()),
        "--user",
        os.getenv("SMART_MYSQL_USER", "root"),
        os.getenv("SMART_MYSQL_DATABASE", "enokimushroom"),
        "--execute",
        sql,
    ]
    password = (
        os.getenv("SMART_MYSQL_PASSWORD")
        or _project_setting("MYSQL_PASSWORD")
        or _druid_master_setting("password")
    )
    if password is None:
        raise MainSystemUnavailable("未配置主系统 MySQL 密码，请设置 SMART_MYSQL_PASSWORD")
    environment = os.environ.copy()
    environment["MYSQL_PWD"] = password
    try:
        result = subprocess.run(
            command,
            capture_output=True,
            text=True,
            encoding="utf-8",
            errors="replace",
            env=environment,
            timeout=8,
            check=False,
            creationflags=getattr(subprocess, "CREATE_NO_WINDOW", 0),
        )
    except (OSError, subprocess.TimeoutExpired) as exc:
        raise MainSystemUnavailable("连接主系统 MySQL 失败") from exc

    if result.returncode != 0:
        message = (result.stderr or "").strip().splitlines()
        detail = message[-1] if message else "MySQL 未运行或连接配置不正确"
        raise MainSystemUnavailable(f"主系统数据库不可用：{detail}")

    rows: list[dict[str, Any]] = []
    for line in result.stdout.splitlines():
        line = line.strip()
        if line and line.lower() != "null":
            rows.append(json.loads(line))
    return rows


def _one(sql: str) -> dict[str, Any] | None:
    rows = _query_json_lines(sql)
    return rows[0] if rows else None


def list_main_system_batches() -> list[dict[str, Any]]:
    """Return every active planting record as a selectable traceability batch."""
    return _query_json_lines(
        "SELECT JSON_OBJECT("
        "'trace_code', CONCAT('PLANT-', p.plant_id), "
        "'plant_id', p.plant_id, "
        "'product_name', COALESCE(NULLIF(s.product_name, ''), p.plant_name), "
        "'plant_name', p.plant_name, 'plant_type', p.plant_type, 'area_id', p.area_id, "
        "'create_date', DATE_FORMAT(p.create_date, '%Y-%m-%d'), "
        "'produce_date', DATE_FORMAT(s.produce_date, '%Y-%m-%d'), "
        "'pack_date', DATE_FORMAT(s.pack_date, '%Y-%m-%d'), "
        "'sale_date', DATE_FORMAT(s.sale_date, '%Y-%m-%d')) "
        "FROM plant_manage p "
        "LEFT JOIN trace_supplement s ON s.trace_code=CONCAT('PLANT-', p.plant_id) "
        "WHERE COALESCE(p.del_flag, '0')='0' ORDER BY p.plant_id DESC"
    )


def parse_plant_id(trace_code: str) -> int:
    match = re.fullmatch(r"(?:PLANT-)?(\d+)", trace_code.strip(), re.IGNORECASE)
    if not match:
        raise ValueError("追溯码格式应为 PLANT-数字，例如 PLANT-2")
    return int(match.group(1))


def fetch_main_system_data(trace_code: str) -> dict[str, Any] | None:
    plant_id = parse_plant_id(trace_code)

    plant = _one(
        "SELECT JSON_OBJECT("
        "'plant_id', plant_id, 'plant_name', plant_name, 'plant_type', plant_type, "
        "'life_cycle', plant_lifeCycle, 'area_id', area_id, 'create_date', create_date, "
        "'create_by', create_by, 'update_date', update_date, 'update_by', update_by) "
        f"FROM plant_manage WHERE plant_id={plant_id} AND COALESCE(del_flag, 0)=0 LIMIT 1"
    )
    if plant is None:
        return None

    area_id = int(plant.get("area_id") or 0)
    area = _one(
        "SELECT JSON_OBJECT("
        "'area_id', area_id, 'area_name', area_name, 'area_address', area_address, "
        "'area_type', area_type, 'area_square', area_square, 'area_state', area_state, "
        "'area_leader', area_leader, 'area_phone', area_phone, 'create_date', create_date, "
        "'create_by', create_by) "
        f"FROM area_manage WHERE area_id={area_id} AND COALESCE(del_flag, 0)=0 LIMIT 1"
    ) if area_id else None

    environment = _query_json_lines(
        "SELECT JSON_OBJECT("
        "'record_id', record_id, 'address_id', address_id, 'air_temperature', air_temperature, "
        "'air_humidity', air_humidity, 'light_lux', light_lux, 'air_co2', air_co2, "
        "'soil_temperature', soil_temperature, 'soil_humidity', soil_humidity, "
        "'soil_conductivity', soil_conductivity, 'soil_ph', soil_PH, "
        "'soil_n', soil_N, 'soil_p', soil_P, 'soil_k', soil_K, 'create_date', create_date) "
        "FROM (SELECT * FROM monitor_record "
        f"WHERE plant_id={plant_id} AND COALESCE(del_flag, 0)=0 "
        "ORDER BY create_date DESC, record_id DESC LIMIT 20) latest "
        "ORDER BY create_date ASC, record_id ASC"
    )

    devices = _query_json_lines(
        "SELECT JSON_OBJECT("
        "'device_id', device_id, 'device_number', device_number, 'device_name', device_name, "
        "'device_type', device_type, 'device_state', device_state, 'create_date', create_date) "
        f"FROM device_manage WHERE plant_id={plant_id} AND COALESCE(del_flag, 0)=0 "
        "ORDER BY device_id"
    )

    clusters = _query_json_lines(
        "SELECT JSON_OBJECT("
        "'id', id, 'cluster_id', cluster_id, 'cluster_name', cluster_name, "
        "'total_count', total_count, 'ripe_count', ripe_count, 'half_ripe_count', half_ripe_count, "
        "'unripe_count', unripe_count, 'avg_size_mm', avg_size_mm, "
        "'latitude', latitude, 'longitude', longitude, 'recommendation', recommendation, "
        "'last_update', last_update) "
        f"FROM enokimushroom_cluster WHERE cluster_id='{plant_id}' ORDER BY last_update DESC, id DESC"
    )

    pest_diseases = _query_json_lines(
        "SELECT JSON_OBJECT("
        "'id', id, 'cluster_id', cluster_id, 'object_type', object_type, "
        "'confidence', confidence, 'pest_disease_class', pest_disease_class, "
        "'severity_level', severity_level, 'recommendation', recommendation, "
        "'detected_time', detected_time) "
        f"FROM enokimushroom_pest_disease WHERE cluster_id='{plant_id}' "
        "ORDER BY detected_time DESC, id DESC"
    )

    supplement = _one(
        "SELECT JSON_OBJECT("
        "'trace_code', trace_code, 'product_name', product_name, 'strain_name', strain_name, "
        "'produce_date', DATE_FORMAT(produce_date, '%Y-%m-%d'), "
        "'pack_date', DATE_FORMAT(pack_date, '%Y-%m-%d'), "
        "'pack_qty', pack_qty, 'pack_weight', pack_weight, 'packaging_image', packaging_image, "
        "'report_no', report_no, 'inspector', inspector, "
        "'sale_customer', sale_customer, 'sale_location', sale_location, "
        "'sale_date', DATE_FORMAT(sale_date, '%Y-%m-%d'), "
        "'sale_quantity', sale_quantity, 'sale_recorder', sale_recorder, 'sale_note', sale_note, "
        "'credit_level', credit_level, 'credit_record', credit_record, "
        "'credit_sample', credit_sample, 'credit_complaint', credit_complaint, "
        "'credit_penalty', credit_penalty, 'credit_lawsuit', credit_lawsuit, "
        "'credit_tax', credit_tax) "
        f"FROM trace_supplement WHERE trace_code='PLANT-{plant_id}' LIMIT 1"
    )

    return {
        "trace_code": f"PLANT-{plant_id}",
        "plant": plant,
        "area": area,
        "environment": environment,
        "devices": devices,
        "clusters": clusters,
        "pest_diseases": pest_diseases,
        "supplement": supplement,
        "source": {
            "system": "Smart-Enokimushroom",
            "database": os.getenv("SMART_MYSQL_DATABASE", "enokimushroom"),
            "host": os.getenv("SMART_MYSQL_HOST", "127.0.0.1"),
            "port": _runtime_port(),
        },
    }
