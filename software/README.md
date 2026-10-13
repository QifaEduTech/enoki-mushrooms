# 智慧金针菇 Smart-Enokimushroom 部署包

金针菇种植管理系统，包含 Spring Boot 后端、Vue 前端、MQTT 物联网接入、
MySQL 数据库、Redis 缓存和 AI 相关配置。

本目录提供两种部署方式：

1. Windows 便携包，复制即用，目标机不需要安装任何运行环境。
2. 系统环境部署，目标机已安装 MySQL、JDK、Node.js 时使用。


## 一、Windows 便携包（推荐）

### 1.1 便携包是什么

便携包位于 `portable\Smart-Enokimushroom`，也可以直接使用 `release`
目录下最新的 `Smart-Enokimushroom-portable-*.zip`。

便携包已经内置：

- JDK 17：`env\jdk17`
- Node.js：`env\node`
- MySQL 8：`env\mysql`
- Redis 3.2：`env\redis`
- 后端 JAR：`Smart-Enokimushroom\Enokimushroom-admin\target\enokimushroom-admin.jar`
- 前端静态文件：`Smart-Enokimushroom-front\dist`
- 数据库初始化脚本：`db\enokimushroom.sql`

目标主机不需要安装 JDK、Node.js、MySQL、Redis，也不需要修改 PATH
环境变量。把整个目录复制到目标 Windows 主机即可运行。


### 1.2 运行时调用规则

项目启动时按以下规则调用环境：

1. Java

   优先使用包内 `env\jdk17\bin\java.exe`。
   只有便携 JDK 不存在时，才会继续尝试 `java-path.cmd`、`JAVA_HOME`、
   PATH 中的 Java。

2. Node.js

   优先使用包内 `env\node\node.exe`。
   只有便携 Node.js 不存在时，才会使用 PATH 中的 node。

3. MySQL

   便携包从 3306 起自动选择第一个空闲端口，再使用包内 `env\mysql`
   启动 MySQL，root 密码固定为 `root`，不连接目标机系统 MySQL。

4. Redis

   便携包从 6379 起自动选择第一个空闲端口，再使用包内 `env\redis`
   启动 Redis，不连接目标机系统 Redis。

5. 后端和前端

   后端使用包内 `enokimushroom-admin.jar`，前端使用包内
   `Smart-Enokimushroom-front\dist`。
   所有脚本通过 `%~dp0` 定位包内文件，不依赖原始开发机路径。

简单说：启动时不会结束或抢占目标机已有进程，MySQL、Redis、后端
API 和前端会从默认端口起自动选择空闲端口，前后端会自动适配实际端口。
整个过程不需要管理员权限，也不需要修改 PATH 环境变量。


### 1.3 文件清单和目录说明

根目录文件：

| 文件 | 说明 |
|---|---|
| `deploy-portable.bat` | 一键启动入口，数据库不存在时自动初始化并启动服务 |
| `start-portable.bat` | 日常启动入口 |
| `stop-portable.bat` | 停止入口，只停止本包启动的服务 |
| `deploy-windows.bat` | 实际部署逻辑 |
| `start-windows.bat` | 实际启动逻辑 |
| `start-windows-stop.bat` | 实际停止逻辑 |
| `java-path.cmd` | JDK 路径兜底配置，默认使用 `env\jdk17` |
| `README.md` | 本说明文件 |
| `README-PORTABLE.txt` | 便携版完整说明 |
| `README-DEPLOY.txt` | 系统环境部署说明 |
| `端口和账号信息.txt` | 默认端口和账号速查 |

目录说明：

| 目录 | 说明 |
|---|---|
| `env\jdk17` | 便携 JDK 17 |
| `env\node` | 便携 Node.js |
| `env\mysql` | 便携 MySQL 8 |
| `env\redis` | 便携 Redis |
| `Smart-Enokimushroom\` | 后端工程和 JAR |
| `Smart-Enokimushroom\Enokimushroom-admin\target\` | 后端 JAR 所在目录 |
| `Smart-Enokimushroom\Enokimushroom-admin\src\main\resources\` | 后端配置目录 |
| `Smart-Enokimushroom-front\dist\` | 前端编译后的静态文件 |
| `db\` | 数据库初始化 SQL |
| `scripts\` | MySQL/Redis 启停、前端离线服务器等辅助脚本 |
| `runtime\` | 运行数据目录，首次启动自动生成 |
| `logs\` | 运行日志 |
| `release\` | 打包好的 zip 文件 |

`runtime` 目录首次运行后会自动生成：

- `runtime\mysql`：内置 MySQL 配置和日志
- `runtime\mysql-data`：内置 MySQL 数据目录
- `runtime\redis`：内置 Redis 配置和日志
- `runtime\uploadPath`：上传文件目录
- `runtime\ports.cmd`：本次启动使用的端口
- `runtime\run-api.cmd`：本次后端启动命令
- `runtime\run-front.cmd`：本次前端启动命令


### 1.4 首次启动

1. 把整个 `Smart-Enokimushroom` 目录复制到目标 Windows 主机，
   或直接解压 `release` 目录下的便携包 zip。
   建议路径不要包含中文或空格。

2. 双击 `start-portable.bat`，或双击 `deploy-portable.bat`，两者等效。

3. 无需输入任何确认，脚本会自动完成以下操作：

   - 自动选择空闲端口（不结束任何现有进程）
   - 启动内置 MySQL 和 Redis
   - 数据库不存在时自动创建 `enokimushroom` 并导入 `db\enokimushroom.sql`
   - 启动后端 API
   - 启动前端页面

4. 如果数据库已经存在，脚本不会覆盖已有数据，会直接启动服务。

5. 只有需要强制重建数据库表时，才运行 `deploy-windows.bat` 并按提示
   输入数据库名 `enokimushroom` 确认；该操作会覆盖已有数据，生产环境
   请先备份。

### 1.5 日常启动和停止

以后启动：

```bat
start-portable.bat
```

停止：

```bat
stop-portable.bat
```

`stop-portable.bat` 只停止本包启动的服务：

- 内置 MySQL：只有 `runtime\mysql\mysql.pid` 存在且对应进程是
  `env\mysql\bin\mysqld.exe` 时才停止。
- 内置 Redis：只有 `runtime\redis\redis.pid` 存在且对应进程是
  `env\redis\redis-server.exe` 时才停止。
- 后端和前端：按 `runtime\ports.cmd` 中记录的端口停止。

它不会停止目标机原有的系统 MySQL 和系统 Redis；脚本也不会抢占或
结束这些服务使用的端口，而是自动改到其它空闲端口。


### 1.6 访问地址和默认账号

- 前端：`http://localhost/`
- API：`http://localhost:8080/swagger-ui.html`
- Druid：`http://localhost:8080/druid/`
- 默认账号：`admin / admin123`
- 数据库：`enokimushroom`
- MySQL 账号：`root / root`

脚本会从 80、8080 起自动选择空闲端口，再启动包内后端和前端。
启动窗口和 `runtime\ports.cmd` 会显示实际使用的前端端口和 API 端口。


### 1.7 端口配置

默认端口：

- MySQL：3306
- Redis：6379
- 后端 API：8080
- 前端：80

脚本会优先使用包内 MySQL/Redis、JDK 和 Node.js，不结束占用进程。
如果 MySQL、Redis、后端或前端端口已被占用，脚本会从该默认端口向后
自动选择第一个空闲端口，后端 JAR 和前端离线服务器会自动适配。

如需修改端口，请同步修改 `start-windows.bat` 和
`deploy-windows.bat` 顶部的端口：

```bat
set "MYSQL_PORT=3307"
set "REDIS_PORT=6380"
```

也可以先在命令行设置环境变量再运行：

```bat
set MYSQL_PORT=3307
set REDIS_PORT=6380
set MYSQL_PASSWORD=root
set MYSQL_DATABASE=enokimushroom
deploy-portable.bat
```

两个脚本中的 `MYSQL_PORT`、`REDIS_PORT`、`MYSQL_PASSWORD`、
`MYSQL_DATABASE` 必须保持一致。


### 1.8 外部服务配置

MQTT、Ollama、DashScope、DeepSeek 等外部服务的地址和密钥位于：

```text
Smart-Enokimushroom\Enokimushroom-admin\src\main\resources\application.yml
```

部署到新主机或新网络时，请按实际网络环境修改该文件。


### 1.9 数据目录和备份迁移

- MySQL 数据保存在 `runtime\mysql-data`
- 上传文件保存在 `runtime\uploadPath`
- Redis 默认不持久化
- 日志保存在 `logs\`

迁移数据时建议先停止服务，再备份：

- `runtime\mysql-data`
- `runtime\uploadPath`

也可以使用 mysqldump 导出数据库后，在目标机重新执行
`start-portable.bat` 或 `deploy-portable.bat` 自动初始化缺失的数据库。


### 1.10 常见问题

1. 提示无法连接 MySQL

   脚本不要求管理员权限。MySQL 端口被占用时会自动选择空闲端口，
   启动窗口会显示实际端口，账号为 `root / root`。

2. 首次初始化很慢或失败

   检查 `runtime\mysql\mysql.err.log` 和 `logs\` 下的日志。
   初始化内置 MySQL 需要几十秒，请等待脚本完成。

3. 80 或 8080 被占用

   脚本不会结束占用进程，会自动改用其它空闲端口，并在启动窗口
   显示实际使用的前端端口和 API 端口。

4. 页面能打开但登录报错

   确认 Redis 已启动且后端使用了正确的 Redis 端口。端口被占用时
   脚本会自动改用空闲端口，无需结束 6379 上的进程。

5. 杀毒软件拦截

   首次启动内置 MySQL、Node、Java 时可能被安全软件询问，
   请选择允许，或把整个目录加入信任区。

6. 复制到带中文或空格的路径

   建议使用纯英文路径，避免批处理和 Java 参数解析异常。


## 二、系统环境部署

如果目标主机不使用便携包，而是使用系统已安装的环境：

1. 安装并启动 MySQL，默认 `127.0.0.1:3306`，账号 `root`，密码 `root`，
   把 MySQL `bin` 目录加入 PATH。
2. 安装 JDK 17 和 Node.js 14+，把 Node.js 加入 PATH。
3. 编辑 `java-path.cmd`，把 `JAVA_HOME_LOCAL` 改成目标主机的 JDK
   根目录，例如 `set "JAVA_HOME_LOCAL=D:\java17"`。
4. 如需重新初始化数据库，运行 `deploy-windows.bat`，按提示输入
   数据库名 `enokimushroom` 确认。
5. 如果数据库已经存在，直接运行 `start-windows.bat` 启动。
6. Linux 主机可直接运行 `bash deploy-linux.sh` 初始化并启动，
   或 `bash start-linux.sh start` 启动。

常用脚本：

| 脚本 | 作用 |
|---|---|
| `deploy-windows.bat` | 一键初始化数据库并启动服务 |
| `deploy-linux.sh` | Linux 下一键初始化数据库并启动服务 |
| `deploy-portable.bat` | 便携包一键启动入口（自动建库，不覆盖已有数据） |
| `start-windows.bat` | 启动后端和前端 |
| `start-linux.sh` | Linux 下启动/停止/查看状态 |
| `start-portable.bat` | 便携包日常启动入口 |
| `start-windows-stop.bat` | 停止后端和前端 |
| `stop-portable.bat` | 停止便携包启动的服务 |
| `scripts/init-mysql.bat` | 单独初始化数据库 |
| `build-offline-package.bat` | 重新打包后端 JAR 和前端 dist |
