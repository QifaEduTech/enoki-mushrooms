# 金针菇智慧种植与可信溯源系统（Smart-Enokimushroom）

面向金针菇工厂化种植场景的物联网管理系统：环境传感器数据通过 MQTT 实时上报入库，平台远程控制风扇、滴灌、补光三类执行器，配合 YOLO 病虫害识别与 SHA-256 哈希链溯源，实现"种植监控 — 智能控制 — 可信溯源"闭环。

**当前状态：**整理中。已验证：后端 API、前端页面、MySQL/Redis 启动、MQTT 数据上报与解析入库、控制指令下发（设备端模拟器联调）、YOLO 服务接口、溯源哈希链存证与验证。已知限制：MQTT broker 地址按部署环境需修改配置；真实硬件联调由嵌入式负责人确认。

**负责人／备用负责人：**康雪莹（项目经理）／【待填写】。**软件负责人：**刘依晨（数据）、史静怡（溯源）／**硬件负责人：**田贺（嵌入式）【待确认】。

**飞书项目页：**【待补链接，完整介绍、四人分工和比赛材料统一放那里】。

**比赛版本／对应硬件版本：**待确认。

## 交资料，先点自己的目录

| 目录 | 谁整理 | 应交内容 |
| --- | --- | --- |
| [software](software) | 软件人员 | 后端/前端/溯源完整源码、数据库脚本、依赖与配置说明、便携部署包说明（见 [software/README.md](software/README.md)） |
| [firmware](firmware) | 嵌入式人员 | 设备端程序、开发环境、编译烧录及 MQTT 通信接口（见 [firmware/README.md](firmware/README.md)，部分待确认） |
| [hardware](hardware) | 硬件人员 | 传感器/执行器选型与接线（见 [hardware/README.md](hardware/README.md)，本仓库无 PCB 源文件） |

[员工网页提交步骤](https://github.com/QifaEduTech/qifa-rd-governance/blob/main/competition-guide.md) · [填好的案例](https://github.com/QifaEduTech/lab-competition-iot-example)

有Agent的员工优先使用操作说明中的软件/硬件指令。Agent先读[AGENTS.md](AGENTS.md)，整理后由本人确认版本，再发起合并申请。

## 环境、安装与启动

开发环境：Windows 10/11，JDK 17，Node.js 14+（webpack4 项目需 `NODE_OPTIONS=--openssl-legacy-provider`），MySQL 8，Redis，Maven 3.9+。

从新电脑开始的启动步骤（详细版见 [software/README.md](software/README.md)）：

1. 启动 MySQL（默认 3306）和 Redis（默认 6379）。
2. 初始化数据库：创建 `enokimushroom` 库并导入 `software/db/enokimushroom.sql`。成功标志：导入无报错，`sys_user` 表有数据。
3. 启动后端：`software/Smart-Enokimushroom` 下 `mvn spring-boot:run`（或打包后运行 `enokimushroom-admin.jar`）。成功标志：`http://localhost:8080/` 返回欢迎语。
4. 启动前端：`software/Smart-Enokimushroom-front` 下 `npm install` 后 `npm run dev`。成功标志：浏览器打开前端地址（默认 80，被占用时改用其它端口）显示登录页。
5. 默认账号：`admin / admin123`。

无硬件时：平台内置定时模拟数据任务，前端监控页可直接查看效果；MQTT 联调可用 `software/` 内的 Python 设备模拟器（需本地 MQTT broker）。

MQTT、Ollama、DashScope、DeepSeek 等外部服务地址与密钥配置在 `software/Smart-Enokimushroom/Enokimushroom-admin/src/main/resources/application.yml`，密钥只按获取方式填写，仓库中不含实际密钥值。

## 软件与硬件如何连接

通信方式：MQTT（EMQX/Mosquitto broker，地址配置于 `application.yml` 的 `hostUrl`）。

上报数据（设备 → 平台，JSON）：

| 主题 | 字段 |
| --- | --- |
| `/air/post` | `temp`、`humidity`、`co2`、`Illumination`（可缺失，平台随机兜底） |
| `/soil/post` | `soil_moisture`、`soil_temperature`、`ph_value`、`nitrogen`、`phosphorus`、`potassium`（电导率平台固定 840） |

控制指令（平台 → 设备，JSON，如 `{"deviceId":"cclr04","command":"fan","action":"start","speed":3,"timestamp":...}`）：

| 主题 | 用途 |
| --- | --- |
| `enokimushroom_fan` | 风扇（action: start/stop，speed 1-5） |
| `enokimushroom_irrigation` | 滴灌（duration 秒，intensity 百分比） |
| `enokimushroom_light` | 补光（brightness 百分比） |

失败处理：连接断开后端自动重连（`PushCallback.connectionLost`）；数据为空时跳过入库并在日志告警。字段解析以 `PushCallback.RecieveDataAnalysis()` 源码为准，双方共同确认。

## 已验证情况与常见问题

- 2026-10-13，Windows 10 本机：MySQL/Redis/后端(8080)/前端(81) 全部启动，HTTP 200；MQTT 数据清洗脚本发布 `/air/post` 后数据成功入库并在前端展示（34 条脏数据清洗后 27 条入库）；控制指令 JSON 经设备模拟器接收解析正常；溯源演示（存证→篡改→验证 False→恢复→True）运行通过。证据与脚本见 `software/` 内 demo 与任务卡目录。
- 未验证：真实传感器/执行器硬件联调；LoRA 通道；线上部署。已知问题：前端 dev server 需 Node 14+ 与 openssl-legacy-provider 参数；端口冲突时脚本自动换空闲端口。

## 文件与来源

PPT、逐字稿、视频、安装包和大文件统一从飞书项目页进入（待补链接）；本仓库 `software/` 为自研源码（基于开源 RuoYi 框架改造，遵循其 MIT 授权）；`python-traceability-demo` 为自研演示服务；第三方依赖均通过 Maven/npm 官方源引入，授权见各工程依赖清单。
