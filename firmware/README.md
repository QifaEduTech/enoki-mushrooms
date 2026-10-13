# 嵌入式资料

在这里放主控的最终程序。板卡设计放hardware，电脑端AI和后台放software。

请填：

- 主控型号及板卡版本：【待嵌入式负责人确认】
- 最终程序入口和版本：【待确认。设备端联调暂用 software/ 内 Python 设备模拟器（MQTT 客户端）代替真机】
- 开发工具、SDK与版本：【待确认】
- 编译、烧录、复位及启动步骤：【待确认】
- 串口/网络参数、数据上报及控制指令：设备端通过网络接入 MQTT broker（EMQX/Mosquitto，地址由平台 `application.yml` 的 `hostUrl` 统一配置，需与设备端一致）。
  - 上报（JSON）：`/air/post` 发 `{"temp":25.1,"humidity":65,"co2":420,"Illumination":800}`；`/soil/post` 发 `{"soil_moisture":45,"soil_temperature":22,"ph_value":6.8,"nitrogen":35,"phosphorus":25,"potassium":180}`
  - 订阅控制（JSON）：`enokimushroom_fan` / `enokimushroom_irrigation` / `enokimushroom_light`，指令形如 `{"deviceId":"cclr04","command":"fan","action":"start","speed":3,"timestamp":...}`
  - Python 模拟器源码：`software/` 下 MQTT 设备模拟器脚本，双向联调已验证
- 实际烧录和硬件运行结果：【待真机联调后补充，2026-10-13 已完成模拟器级双向联调】
- 未确认的软件接口和其他缺项：主控型号、烧录流程、LoRA 通道实现待嵌入式负责人（田贺）确认后补齐。

不懂AI处理过程可以不写，只说明设备端的输入输出。不涉及嵌入式则写“不涉及”。[网页提交步骤](https://github.com/QifaEduTech/qifa-rd-governance/blob/main/competition-guide.md)。
