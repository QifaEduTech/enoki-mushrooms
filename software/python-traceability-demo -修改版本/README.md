# 金针菇可信溯源二维码演示（主系统数据版）

这是独立的“修改版本”。原目录 `python-traceability-demo` 不参与运行，也不会被修改。

本版本的数据来源已经改为父目录 `Smart-Enokimushroom` 主系统：

- `main_system_client.py`：只读主系统 MySQL，读取种植、场地、设备、环境、产量预测和病虫害数据
- `server.py`：HTTP 服务，负责数据查询、二维码生成和扫码页展示
- `trace_core.py`：把主系统数据转换成溯源页面结构，并使用独立 `trace_state.db` 保存扫码次数和存证状态
- `templates/index.html`：扫码展示页

主系统没有包装、销售、信用数据表，因此这些栏目不会填入虚构内容；没有数据的项目会自动隐藏。

## 运行

双击 `run-demo.bat`，或执行：

```powershell
python server.py
```

运行前先启动父目录中的主系统，确保 MySQL 正常运行。然后启动本版本。

启动后：

- 本机页面：`http://127.0.0.1:8000`
- 二维码图片：`trace-qr.png`
- 扫码访问：`http://<局域网IP>:8000/?bag_code=PLANT-2`

二维码内容由启动时的局域网 IP 自动生成，手机与电脑处于同一网络时可直接扫码。

## 页面

扫码打开的是 `templates/index.html`，数据来自 `/api/status`。追溯码采用
`PLANT-<主系统植物ID>`，例如主系统中的金针菇 `plant_id=2` 对应 `PLANT-2`。

默认自动读取父目录 `runtime/ports.cmd` 中的 MySQL 端口，并使用父目录
`env/mysql/bin/mysql.exe`。如主系统配置已经修改，可在运行前设置：

```bat
set SMART_MYSQL_HOST=127.0.0.1
set SMART_MYSQL_PORT=3306
set SMART_MYSQL_USER=root
set SMART_MYSQL_PASSWORD=你的数据库密码
set SMART_MYSQL_DATABASE=enokimushroom
```

数据库密码只通过环境变量传入，不会返回给浏览器或写入扫码页面。

二维码每次启动都会根据当前局域网 IP 重新生成，并同时覆盖根目录与
`static/trace-qr.png`。即使8000端口已有服务，再次双击 `run-demo.bat` 也会
先通过 `/api/health` 判断是否为当前修改版：如果是当前修改版就刷新二维码；
如果8000属于原 demo、其它程序或无响应的旧进程，修改版会自动从8001开始
选择空闲端口，并把实际端口写入 `server-port.txt`。浏览器访问二维码图片时
还会再次检查当前 IP，并禁用图片缓存。
当前二维码实际地址会写入 `trace-qr-url.txt`，可以直接打开核对。

如果电脑有多个网卡且自动选择的不是手机所在网段，可以在启动前指定：

```bat
set TRACE_HOST=192.168.1.100
run-demo.bat
```

二维码采用高容错等级和更大的像素尺寸生成，适合手机拍摄、缩放和打印。

每次打开页面都会累加该植物追溯码的扫描次数。扫码次数和存证状态保存在
修改版自己的 `trace_state.db` 中；业务信息始终从主系统 MySQL 实时读取。

目录中原有的 `traceability.db` 是复制修改版时带来的旧演示数据库，本版本
不会读取或修改它，保留它只是为了不擅自删除原文件。
