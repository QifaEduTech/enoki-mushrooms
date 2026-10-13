from __future__ import annotations

import io
import ipaddress
import json
import mimetypes
import os
import socket
import sys
from http import HTTPStatus
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from pathlib import Path
from threading import Timer
from urllib.parse import parse_qs, unquote, urlparse

from main_system_client import list_main_system_batches

from trace_core import (
    DEFAULT_BAG_CODE,
    certify_trace,
    get_blocks,
    get_dashboard_status,
    get_logs,
    get_trace_snapshot,
    init_database,
    restore_supplier,
    tamper_supplier,
    verify_trace,
)

BASE_DIR = Path(__file__).resolve().parent
VENDOR_DIR = BASE_DIR / "vendor"
TEMPLATE_DIR = BASE_DIR / "templates"
STATIC_DIR = BASE_DIR / "static"
PORT_FILE = BASE_DIR / "server-port.txt"
APP_MODE = "main-system-data"


def get_lan_ip() -> str:
    configured = os.getenv("TRACE_HOST", "").strip()
    if configured:
        return configured

    candidates: list[str] = []
    for target in (("8.8.8.8", 80), ("1.1.1.1", 80)):
        try:
            with socket.socket(socket.AF_INET, socket.SOCK_DGRAM) as sock:
                sock.connect(target)
                candidates.append(sock.getsockname()[0])
        except OSError:
            pass

    try:
        candidates.extend(
            item[4][0]
            for item in socket.getaddrinfo(
                socket.gethostname(), None, socket.AF_INET, socket.SOCK_DGRAM
            )
        )
    except OSError:
        pass

    for candidate in candidates:
        try:
            address = ipaddress.ip_address(candidate)
        except ValueError:
            continue
        if not address.is_loopback and not address.is_link_local:
            return candidate
    return "127.0.0.1"


def get_qr_host(lan_ip: str | None = None) -> str:
    """Return the reachable LAN host used by phones and desktop WeChat."""
    configured = os.getenv("TRACE_QR_HOST", "").strip()
    if configured:
        return configured
    return lan_ip or get_lan_ip()


def make_trace_qr(url: str, output: Path) -> Path:
    sys.path.insert(0, str(VENDOR_DIR))
    import qrcode
    from qrcode.constants import ERROR_CORRECT_H

    qr = qrcode.QRCode(
        version=None,
        error_correction=ERROR_CORRECT_H,
        box_size=12,
        border=4,
    )
    qr.add_data(url)
    qr.make(fit=True)
    image = qr.make_image(fill_color="black", back_color="white")
    output.parent.mkdir(parents=True, exist_ok=True)
    temporary = output.with_name(f".{output.name}.tmp")
    image.save(temporary, format="PNG")
    os.replace(temporary, output)
    return output


def refresh_trace_qr(
    port: int = 8000,
    output_paths: list[Path] | tuple[Path, ...] | None = None,
    host: str | None = None,
    bag_code: str = DEFAULT_BAG_CODE,
) -> dict[str, object]:
    lan_host = host or get_lan_ip()
    qr_host = get_qr_host(lan_host)
    qr_url = f"http://{qr_host}:{port}/?bag_code={bag_code}"
    targets = list(output_paths) if output_paths is not None else [
        BASE_DIR / "trace-qr.png",
        STATIC_DIR / "trace-qr.png",
    ]
    for target in targets:
        make_trace_qr(qr_url, Path(target))

    if output_paths is None:
        url_file = BASE_DIR / "trace-qr-url.txt"
        temporary = url_file.with_name(f".{url_file.name}.tmp")
        temporary.write_text(qr_url + "\n", encoding="utf-8")
        os.replace(temporary, url_file)

    return {
        "host": lan_host,
        "qr_host": qr_host,
        "port": port,
        "url": qr_url,
        "files": targets,
    }


def show_qr_image(path: Path) -> None:
    try:
        os.startfile(str(path))
    except OSError as exc:
        print(f"[WARN] 无法自动打开二维码图片：{exc}")


class TraceabilityHandler(BaseHTTPRequestHandler):
    server_version = "TraceabilityDemo/1.0"

    def log_message(self, format: str, *args) -> None:
        # address_string() 会做反向 DNS 查询；部分 Windows 网络会在这里长时间卡住。
        client_ip = self.client_address[0] if self.client_address else "unknown"
        print(f"[HTTP] {client_ip} - {format % args}")

    def _send_json(self, payload: dict | list, status: int = 200) -> None:
        body = json.dumps(payload, ensure_ascii=False).encode("utf-8")
        self.send_response(status)
        self.send_header("Content-Type", "application/json; charset=utf-8")
        self.send_header("Content-Length", str(len(body)))
        self.send_header("Cache-Control", "no-store")
        self.send_header("Access-Control-Allow-Origin", "*")
        self.end_headers()
        self.wfile.write(body)

    def do_OPTIONS(self) -> None:
        self.send_response(HTTPStatus.NO_CONTENT)
        self.send_header("Access-Control-Allow-Origin", "*")
        self.send_header("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
        self.send_header("Access-Control-Allow-Headers", "Authorization, Content-Type")
        self.send_header("Access-Control-Max-Age", "600")
        self.end_headers()

    def _send_file(self, path: Path) -> None:
        if not path.exists() or not path.is_file():
            self._send_json({"success": False, "message": "文件不存在"}, 404)
            return
        body = path.read_bytes()
        content_type = mimetypes.guess_type(path.name)[0] or "application/octet-stream"
        if content_type.startswith("text/"):
            content_type += "; charset=utf-8"
        self.send_response(HTTPStatus.OK)
        self.send_header("Content-Type", content_type)
        self.send_header("Content-Length", str(len(body)))
        self.send_header("Cache-Control", "no-store, no-cache, must-revalidate")
        self.send_header("Access-Control-Allow-Origin", "*")
        self.end_headers()
        self.wfile.write(body)

    def _send_download(self, path: Path, filename: str) -> None:
        if not path.exists() or not path.is_file():
            self._send_json({"success": False, "message": "二维码文件不存在"}, 404)
            return
        body = path.read_bytes()
        self.send_response(HTTPStatus.OK)
        self.send_header("Content-Type", "image/png")
        self.send_header("Content-Length", str(len(body)))
        self.send_header("Content-Disposition", f'attachment; filename="{filename}"')
        self.send_header("Cache-Control", "no-store")
        self.send_header("Access-Control-Allow-Origin", "*")
        self.end_headers()
        self.wfile.write(body)

    def _read_json(self) -> dict:
        length = int(self.headers.get("Content-Length", "0") or 0)
        if length == 0:
            return {}
        raw = self.rfile.read(length)
        return json.loads(raw.decode("utf-8"))

    def _send_png(self, body: bytes) -> None:
        self.send_response(HTTPStatus.OK)
        self.send_header("Content-Type", "image/png")
        self.send_header("Content-Length", str(len(body)))
        self.send_header("Cache-Control", "no-store")
        self.send_header("Access-Control-Allow-Origin", "*")
        self.end_headers()
        self.wfile.write(body)

    def do_GET(self) -> None:
        parsed = urlparse(self.path)
        path = unquote(parsed.path)

        try:
            if path == "/":
                self._send_file(TEMPLATE_DIR / "index.html")
                return
            if path.startswith("/static/"):
                relative = path.removeprefix("/static/")
                target = (STATIC_DIR / relative).resolve()
                if STATIC_DIR.resolve() not in target.parents:
                    self._send_json({"success": False, "message": "非法路径"}, 403)
                    return
                if relative == "trace-qr.png":
                    query = parse_qs(parsed.query)
                    bag_code = query.get("bag_code", [DEFAULT_BAG_CODE])[0]
                    refresh_trace_qr(port=self.server.server_address[1], bag_code=bag_code)
                self._send_file(target)
                return
            if path == "/api/batches":
                self._send_json({"success": True, "batches": list_main_system_batches()})
                return
            if path == "/api/status":
                query = parse_qs(parsed.query)
                bag_code = query.get("bag_code", [DEFAULT_BAG_CODE])[0]
                self._send_json({"success": True, **get_dashboard_status(bag_code)})
                return
            if path == "/api/health":
                port = self.server.server_address[1]
                self._send_json(
                    {
                        "success": True,
                        "app": "Smart-Enokimushroom traceability",
                        "mode": APP_MODE,
                        "instance_dir": str(BASE_DIR),
                        "port": port,
                        "lan_url": f"http://{get_qr_host()}:{port}/?bag_code={DEFAULT_BAG_CODE}",
                    }
                )
                return
            if path == "/api/host":
                port = self.server.server_address[1]
                self._send_json({"success": True, "host": f"http://{get_qr_host()}:{port}"})
                return
            if path == "/api/qr":
                query = parse_qs(parsed.query)
                qr_url = query.get("url", [""])[0]
                if not qr_url.startswith(("http://", "https://")):
                    self._send_json({"success": False, "message": "url 参数必须以 http(s) 开头"}, 400)
                    return
                try:
                    sys.path.insert(0, str(VENDOR_DIR))
                    import qrcode

                    image = qrcode.make(qr_url)
                    buffer = io.BytesIO()
                    image.save(buffer, format="PNG")
                    self._send_png(buffer.getvalue())
                except ImportError:
                    self._send_json({"success": False, "message": "缺少 qrcode 依赖"}, 500)
                return
            if path == "/api/qr/download":
                query = parse_qs(parsed.query)
                bag_code = query.get("bag_code", [DEFAULT_BAG_CODE])[0]
                refresh_trace_qr(port=self.server.server_address[1], bag_code=bag_code)
                safe_code = "".join(char for char in bag_code if char.isalnum() or char in "-_")
                self._send_download(STATIC_DIR / "trace-qr.png", f"traceability-{safe_code or 'batch'}.png")
                return
            if path.startswith("/api/trace/"):
                bag_code = path.removeprefix("/api/trace/")
                snapshot = get_trace_snapshot(bag_code)
                if snapshot is None:
                    self._send_json({"success": False, "message": "未查询到该菌包"}, 404)
                    return
                self._send_json({"success": True, "snapshot": snapshot})
                return
            if path.startswith("/api/verify/"):
                bag_code = path.removeprefix("/api/verify/")
                self._send_json({"success": True, "verification": verify_trace(bag_code)})
                return
            if path == "/api/blocks":
                self._send_json({"success": True, "blocks": get_blocks()})
                return
            if path == "/api/logs":
                self._send_json({"success": True, "logs": get_logs()})
                return

            self._send_json({"success": False, "message": "接口不存在"}, 404)
        except ValueError as exc:
            self._send_json({"success": False, "message": str(exc)}, 404)
        except Exception as exc:
            self._send_json({"success": False, "message": f"服务器错误：{exc}"}, 500)

    def do_POST(self) -> None:
        parsed = urlparse(self.path)
        path = unquote(parsed.path)

        try:
            data = self._read_json()
            bag_code = data.get("bag_code", DEFAULT_BAG_CODE)

            if path == "/api/qr/refresh":
                result = refresh_trace_qr(port=self.server.server_address[1], bag_code=bag_code)
                self._send_json(
                    {
                        "success": True,
                        "qr": {
                            "url": result["url"],
                            "host": result["qr_host"],
                            "port": result["port"],
                            "image": f"/static/trace-qr.png?bag_code={bag_code}",
                        },
                    }
                )
                return

            if path == "/api/certify":
                self._send_json({"success": True, "certification": certify_trace(bag_code)})
                return
            if path == "/api/demo/tamper":
                self._send_json({"success": True, "result": tamper_supplier(bag_code)})
                return
            if path == "/api/demo/restore":
                self._send_json({"success": True, "result": restore_supplier(bag_code)})
                return
            if path == "/api/demo/reset":
                init_database(reset=True)
                self._send_json({"success": True, "message": "演示数据库已重置"})
                return

            self._send_json({"success": False, "message": "接口不存在"}, 404)
        except json.JSONDecodeError:
            self._send_json({"success": False, "message": "JSON 请求格式错误"}, 400)
        except ValueError as exc:
            self._send_json({"success": False, "message": str(exc)}, 404)
        except Exception as exc:
            self._send_json({"success": False, "message": f"服务器错误：{exc}"}, 500)


def requested_port(default: int = 8000) -> int:
    if "--port" in sys.argv:
        index = sys.argv.index("--port")
        if index + 1 >= len(sys.argv):
            raise ValueError("--port 后必须提供端口号")
        return int(sys.argv[index + 1])
    return int(os.getenv("TRACE_PORT", str(default)))


def main(port: int | None = None) -> None:
    init_database()
    host = "0.0.0.0"
    port = port if port is not None else requested_port()
    qr_result = refresh_trace_qr(port=port)
    qr_host = str(qr_result["qr_host"])
    qr_file = BASE_DIR / "trace-qr.png"
    server = ThreadingHTTPServer((host, port), TraceabilityHandler)
    PORT_FILE.write_text(str(port) + "\n", encoding="ascii")
    if os.getenv("TRACE_SHOW_QR") == "1":
        Timer(0.5, lambda: show_qr_image(qr_file)).start()
    print("=" * 64)
    print("金针菇可信溯源演示系统已启动")
    print(f"本机访问：http://127.0.0.1:{port}")
    print(f"扫码访问：http://{qr_host}:{port}")
    print(f"二维码图片：{qr_file}")
    print("按 Ctrl+C 停止服务")
    print("=" * 64)
    try:
        server.serve_forever()
    except KeyboardInterrupt:
        print("\n服务已停止")
    finally:
        server.server_close()


if __name__ == "__main__":
    if "--refresh-qr" in sys.argv:
        result = refresh_trace_qr(port=requested_port())
        print(f"二维码已刷新：{result['url']}")
    else:
        main(port=requested_port())
