# 金针菇可信溯源二维码演示

源码从 `D:\19-讲课\溯源1.docx` 提取，分为两个文件：

- `server.py`：HTTP 服务，负责数据查询、二维码生成和扫码页展示
- `trace_core.py`：SQLite 业务数据、区块链存证和可信验证

## 运行

双击 `run-demo.bat`，或执行：

```powershell
python server.py
```

启动后：

- 本机页面：`http://127.0.0.1:8000`
- 二维码图片：`trace-qr.png`
- 扫码访问：`http://<局域网IP>:8000/?bag_code=JGf85ba05a`

二维码内容由启动时的局域网 IP 自动生成，手机与电脑处于同一网络时可直接扫码。

## 页面

扫码打开的是 `templates/index.html`，样式参考 `D:\19-讲课\1.html`，数据来自
`/api/status` 接口，展示产品、生产、质量、包装、销售和信用六个标签页。

每次打开页面都会累加该菌包的扫描次数，并显示在产品信息概览中。
