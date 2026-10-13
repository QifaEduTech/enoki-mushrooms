# 智慧金针菇MQTT物联网系统使用手册

## 目录
1. [系统概述](#系统概述)
2. [系统架构](#系统架构)
3. [MQTT配置](#mqtt配置)
4. [主题设计](#主题设计)
5. [消息格式](#消息格式)
6. [API接口](#api接口)
7. [使用示例](#使用示例)
8. [故障排除](#故障排除)
9. [开发指南](#开发指南)

## 系统概述

### 系统简介
智慧金针菇MQTT物联网系统是一个基于MQTT协议的农业物联网控制系统，主要用于金针菇种植环境的智能监控和设备控制。系统能够实时接收传感器数据，并根据预设规则或手动指令控制农业设备。

### 主要功能
- **环境监测**：实时接收温度、湿度、土壤等传感器数据
- **设备控制**：远程控制滴灌、风扇、补光等农业设备
- **数据存储**：自动存储监测数据到数据库
- **自动控制**：程序启动时自动发送设备启动指令
- **手动控制**：提供REST API接口进行手动设备控制

## 系统架构

### 整体架构图
```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   前端Vue应用   │    │   Spring Boot   │    │   MQTT Broker   │
│                 │◄──►│   后端服务      │◄──►│   (服务器)      │
│                 │    │                 │    │                 │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                                │                       │
                                │                       │
                                ▼                       ▼
                       ┌─────────────────┐    ┌─────────────────┐
                       │   数据库        │    │   农业设备      │
                       │   (MySQL)       │    │   (传感器+执行器)│
                       └─────────────────┘    └─────────────────┘
```

### 核心组件
1. **MqttConfig**：MQTT连接配置管理
2. **MqttPushClient**：MQTT客户端，负责连接、发布、订阅
3. **PushCallback**：MQTT消息回调处理，数据解析和存储
4. **MonitorDataController**：REST API控制器，提供手动控制接口

## MQTT配置

### 配置文件位置
`Enokimushroom-admin/src/main/resources/application.yml`

### 配置参数
```yaml
spring:
  mqtt:
    username: "your_username"           # MQTT用户名
    password: "your_password"           # MQTT密码
    hostUrl: "tcp://broker:1883"       # MQTT服务器地址
    clientId: "enokimushroom_client"       # 客户端ID
    defaultTopic: "Agrcul20250725"     # 订阅主题
    timeout: 60                         # 连接超时时间(秒)
    keepalive: 60                       # 心跳间隔(秒)
    enabled: true                       # 是否启用MQTT功能
```

### 连接参数说明
- **hostUrl**：MQTT服务器地址，格式：`tcp://IP:端口` 或 `ssl://IP:端口`
- **clientId**：客户端唯一标识，建议使用有意义的名称
- **defaultTopic**：默认订阅主题，支持逗号分隔的多个主题
- **timeout**：连接超时时间，建议设置为60秒
- **keepalive**：心跳间隔，建议设置为60秒

## 主题设计

### 主题层次结构
```
Agrcul20250725                    ← 主主题（传感器数据）
├── sensor/                       ← 传感器数据子主题
│   ├── environment              ← 环境数据
│   └── soil                    ← 土壤数据
├── control/                     ← 控制指令子主题
│   ├── irrigation              ← 滴灌控制
│   ├── fan                     ← 风扇控制
│   └── light                   ← 补光控制
└── status/                      ← 设备状态子主题
    ├── irrigation              ← 滴灌状态
    ├── fan                     ← 风扇状态
    └── light                   ← 补光状态

enokimushroom_irrigation            ← 滴灌控制主题
enokimushroom_fan                   ← 风扇控制主题
enokimushroom_light                 ← 补光控制主题
```

### 主题说明
- **Agrcul20250725**：农业传感器数据接收主题
- **enokimushroom_irrigation**：滴灌设备控制主题
- **enokimushroom_fan**：风扇设备控制主题
- **enokimushroom_light**：补光设备控制主题

## 消息格式

### 传感器数据格式（接收）
**主题**：`Agrcul20250725`

**消息格式**：
```
SN#设备号#WD#温度#SD#湿度#TW#土壤温度#TS#土壤湿度#PH#pH值#TD#氮#TL#磷#TJ#钾
```

**示例数据**：
```
SN#cclr04#WD#25.5#SD#65.0#TW#22.0#TS#45.0#PH#6.8#TD#35#TL#25#TJ#180
```

**字段说明**：
| 字段 | 含义 | 单位 | 说明 |
|------|------|------|------|
| SN | 设备序列号 | - | 设备标识 |
| WD | 空气温度 | ℃ | 环境温度 |
| SD | 空气湿度 | % | 环境湿度 |
| TW | 土壤温度 | ℃ | 土壤温度 |
| TS | 土壤湿度 | % | 土壤湿度 |
| PH | 土壤pH值 | - | 酸碱度 |
| TD | 土壤氮含量 | mg/kg | 氮肥含量 |
| TL | 土壤磷含量 | mg/kg | 磷肥含量 |
| TJ | 土壤钾含量 | mg/kg | 钾肥含量 |

### 控制指令格式（发送）

#### 滴灌控制
**主题**：`enokimushroom_irrigation`

**消息格式**：
```json
{
  "deviceId": "cclr04",
  "command": "irrigation",
  "action": "start",
  "duration": 300,
  "intensity": 80,
  "timestamp": "2024-01-15T10:30:00"
}
```

**参数说明**：
- `deviceId`：设备ID，固定为"cclr04"
- `command`：命令类型，固定为"irrigation"
- `action`：动作，"start"或"stop"
- `duration`：持续时间（秒），仅start时有效
- `intensity`：强度百分比（1-100），仅start时有效
- `timestamp`：时间戳

#### 风扇控制
**主题**：`enokimushroom_fan`

**消息格式**：
```json
{
  "deviceId": "cclr04",
  "command": "fan",
  "action": "start",
  "speed": 3,
  "timestamp": "2024-01-15T10:30:00"
}
```

**参数说明**：
- `deviceId`：设备ID，固定为"cclr04"
- `command`：命令类型，固定为"fan"
- `action`：动作，"start"或"stop"
- `speed`：风速等级（1-5），仅start时有效
- `timestamp`：时间戳

#### 补光控制
**主题**：`enokimushroom_light`

**消息格式**：
```json
{
  "deviceId": "cclr04",
  "command": "light",
  "action": "start",
  "brightness": 70,
  "timestamp": "2024-01-15T10:30:00"
}
```

**参数说明**：
- `deviceId`：设备ID，固定为"cclr04"
- `command`：命令类型，固定为"light"
- `action`：动作，"start"或"stop"
- `brightness`：亮度百分比（1-100），仅start时有效
- `timestamp`：时间戳

## API接口

### 基础信息
- **基础路径**：`/api/monitor`
- **请求方式**：POST/GET
- **数据格式**：JSON
- **认证方式**：无（可根据需要添加）

### 接口列表

#### 1. 滴灌控制
**接口地址**：`POST /api/monitor/irrigation`

**请求参数**：
```json
{
  "action": "start",
  "duration": 300,
  "intensity": 80
}
```

**响应示例**：
```json
{
  "code": 200,
  "msg": "滴灌控制指令发送成功",
  "data": null
}
```

#### 2. 风扇控制
**接口地址**：`POST /api/monitor/fan`

**请求参数**：
```json
{
  "action": "start",
  "speed": 3
}
```

**响应示例**：
```json
{
  "code": 200,
  "msg": "风扇控制指令发送成功",
  "data": null
}
```

#### 3. 补光控制
**接口地址**：`POST /api/monitor/light`

**请求参数**：
```json
{
  "action": "start",
  "brightness": 70
}
```

**响应示例**：
```json
{
  "code": 200,
  "msg": "补光控制指令发送成功",
  "data": null
}
```

#### 4. 手动触发自动发布
**接口地址**：`POST /api/monitor/auto-publish`

**请求参数**：无

**响应示例**：
```json
{
  "code": 200,
  "msg": "自动发布控制指令已触发",
  "data": null
}
```

#### 5. 获取MQTT接收数据
**接口地址**：`GET /api/monitor/receive-data`

**请求参数**：无

**响应示例**：
```json
{
  "code": 200,
  "msg": "获取数据成功",
  "data": {
    "topic": "Agrcul20250725",
    "qos": "0",
    "msg": "SN#cclr04#WD#25.5#SD#65.0#TW#22.0#TS#45.0#PH#6.8#TD#35#TL#25#TJ#180"
  }
}
```

#### 6. 手动插入固定数据
**接口地址**：`GET /api/monitor/insert-fixed-data`

**请求参数**：无

**响应示例**：
```json
{
  "code": 200,
  "msg": "固定数据插入成功",
  "data": null
}
```

## 使用示例

### 前端Vue调用示例

#### 开启滴灌
```javascript
import axios from 'axios'

async function startIrrigation() {
  try {
    const response = await axios.post('/api/monitor/irrigation', {
      action: 'start',
      duration: 600,    // 10分钟
      intensity: 90     // 90%强度
    })
    
    if (response.data.code === 200) {
      console.log('滴灌开启成功')
    } else {
      console.error('滴灌开启失败')
    }
  } catch (error) {
    console.error('请求失败:', error)
  }
}
```

#### 停止风扇
```javascript
async function stopFan() {
  try {
    const response = await axios.post('/api/monitor/fan', {
      action: 'stop'
    })
    
    if (response.data.code === 200) {
      console.log('风扇停止成功')
    } else {
      console.error('风扇停止失败')
    }
  } catch (error) {
    console.error('请求失败:', error)
  }
}
```

#### 开启补光
```javascript
async function startLight() {
  try {
    const response = await axios.post('/api/monitor/light', {
      action: 'start',
      brightness: 85    // 85%亮度
    })
    
    if (response.data.code === 200) {
      console.log('补光开启成功')
    } else {
      console.error('补光开启失败')
    }
  } catch (error) {
    console.error('请求失败:', error)
  }
}
```

### 定时任务示例

#### 定时开启滴灌
```javascript
// 每天上午8点开启滴灌
setInterval(() => {
  const now = new Date()
  if (now.getHours() === 8 && now.getMinutes() === 0) {
    startIrrigation()
  }
}, 60000) // 每分钟检查一次
```

#### 根据温度自动控制风扇
```javascript
// 当温度超过28度时自动开启风扇
async function autoControlFan(temperature) {
  if (temperature > 28) {
    await startFan()
  } else if (temperature < 25) {
    await stopFan()
  }
}
```

## 故障排除

### 常见问题及解决方案

#### 1. MQTT连接失败
**问题描述**：程序启动时MQTT连接失败

**可能原因**：
- MQTT服务器地址错误
- 用户名密码错误
- 网络连接问题
- MQTT服务器未启动

**解决方案**：
1. 检查`application.yml`中的配置参数
2. 确认MQTT服务器状态
3. 检查网络连接
4. 查看日志文件中的错误信息

#### 2. 消息发送失败
**问题描述**：控制指令发送失败

**可能原因**：
- MQTT连接断开
- 主题不存在
- 权限不足

**解决方案**：
1. 检查MQTT连接状态
2. 确认主题名称正确
3. 检查用户权限
4. 重新连接MQTT服务器

#### 3. 数据接收异常
**问题描述**：传感器数据接收异常

**可能原因**：
- 主题订阅失败
- 数据格式错误
- 回调处理异常

**解决方案**：
1. 检查主题订阅状态
2. 验证数据格式
3. 查看日志文件
4. 重启应用程序

### 日志查看
系统日志文件位置：`logs/`目录

关键日志信息：
- MQTT连接状态
- 消息发送/接收记录
- 错误异常信息
- 设备控制指令

## 开发指南

### 添加新的控制设备

#### 1. 在MqttPushClient中添加控制方法
```java
public AjaxResult controlNewDevice(String action, Integer parameter) {
    JSONObject command = new JSONObject();
    command.put("deviceId", "cclr04");
    command.put("command", "newDevice");
    command.put("action", action);
    command.put("parameter", parameter);
    command.put("timestamp", new java.util.Date().toString());
    
    String topic = "enokimushroom_newDevice";
    logger.info("发送新设备控制指令: {}", command.toJSONString());
    return publish(1, false, topic, command.toJSONString());
}
```

#### 2. 在MonitorDataController中添加API接口
```java
@PostMapping("/newDevice")
public AjaxResult controlNewDevice(@RequestBody JSONObject request) {
    try {
        String action = request.getString("action");
        Integer parameter = request.getInteger("parameter");
        
        AjaxResult result = mqttPushClient.controlNewDevice(action, parameter);
        if (result.isSuccess()) {
            return AjaxResult.success("新设备控制指令发送成功");
        } else {
            return AjaxResult.error("新设备控制指令发送失败");
        }
    } catch (Exception e) {
        logger.error("新设备控制失败: ", e);
        return AjaxResult.error("新设备控制失败: " + e.getMessage());
    }
}
```

### 自定义数据解析

#### 1. 修改PushCallback中的数据解析逻辑
```java
public void RecieveDataAnalysis(String topic, MqttMessage mqttMessage) {
    if (topic.equals("your_new_topic")) {
        // 解析新主题的数据
        String data = new String(mqttMessage.getPayload());
        // 处理数据...
    }
}
```

#### 2. 添加新的数据字段
```java
// 在MonitorRecord实体类中添加新字段
private Double newParameter;

// 在数据解析中设置值
monitorRecord.setNewParameter(parseValue);
```

### 扩展功能建议

1. **数据可视化**：添加图表展示传感器数据趋势
2. **报警系统**：当数据超出阈值时发送报警通知
3. **历史数据查询**：提供历史数据查询和导出功能
4. **设备状态监控**：实时监控设备运行状态
5. **智能控制**：基于环境数据自动控制设备

## 总结

本手册详细介绍了智慧金针菇MQTT物联网系统的使用方法，包括：

- 系统架构和核心组件
- MQTT配置和主题设计
- 消息格式和API接口
- 使用示例和故障排除
- 开发指南和扩展建议

通过本手册，用户可以：
1. 快速了解系统架构和功能
2. 正确配置MQTT连接参数
3. 使用API接口控制农业设备
4. 解决常见问题和故障
5. 根据需求扩展系统功能

如有疑问或需要技术支持，请联系开发团队。
