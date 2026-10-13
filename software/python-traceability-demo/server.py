from __future__ import annotations

import io
import json
import mimetypes
import socket
import sys
from http import HTTPStatus
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from pathlib import Path
from urllib.parse import parse_qs, unquote, urlparse

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


def get_lan_ip() -> str:
    try:
        with socket.socket(socket.AF_INET, socket.SOCK_DGRAM) as sock:
            sock.connect(("8.8.8.8", 80))
            return sock.getsockname()[0]
    except OSError:
        return "127.0.0.1"


def make_trace_qr(url: str, output: Path) -> Path:
    sys.path.insert(0, str(VENDOR_DIR))
    import qrcode

    image = qrcode.make(url)
    image.save(output, format="PNG")
    return output


class TraceabilityHandler(BaseHTTPRequestHandler):
    server_version = "TraceabilityDemo/1.0"

    def log_message(self, format: str, *args) -> None:
        print(f"[HTTP] {self.address_string()} - {format % args}")

    def _send_json(self, payload: dict | list, status: int = 200) -> None:
        body = json.dumps(payload, ensure_ascii=False).encode("utf-8")
        self.send_response(status)
        self.send_header("Content-Type", "application/json; charset=utf-8")
        self.send_header("Content-Length", str(len(body)))
        self.send_header("Cache-Control", "no-store")
        self.end_headers()
        self.wfile.write(body)

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
                self._send_file(target)
                return
            if path == "/api/status":
                query = parse_qs(parsed.query)
                bag_code = query.get("bag_code", [DEFAULT_BAG_CODE])[0]
                self._send_json({"success": True, **get_dashboard_status(bag_code)})
                return
            if path == "/api/host":
                port = self.server.server_address[1]
                self._send_json({"success": True, "host": f"http://{get_lan_ip()}:{port}"})
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


def main() -> None:
    init_database()
    host = "0.0.0.0"
    port = 8000
    lan_host = get_lan_ip()
    qr_url = f"http://{lan_host}:{port}/?bag_code={DEFAULT_BAG_CODE}"
    qr_file = BASE_DIR / "trace-qr.png"
    make_trace_qr(qr_url, qr_file)
    server = ThreadingHTTPServer((host, port), TraceabilityHandler)
    print("=" * 64)
    print("金针菇可信溯源演示系统已启动")
    print(f"本机访问：http://127.0.0.1:{port}")
    print(f"局域网访问：http://{lan_host}:{port}")
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
    main()
