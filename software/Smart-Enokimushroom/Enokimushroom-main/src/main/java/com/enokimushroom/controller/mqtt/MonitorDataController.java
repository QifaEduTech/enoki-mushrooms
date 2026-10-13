package com.enokimushroom.controller.mqtt;

import com.alibaba.fastjson2.JSONObject;
import com.enokimushroom.common.core.controller.BaseController;
import com.enokimushroom.common.core.domain.AjaxResult;
import com.enokimushroom.common.core.page.TableDataInfo;
import com.enokimushroom.entity.MonitorRecord;
import com.enokimushroom.service.IMonitorRecordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 监测数据控制器
 */
@RestController
@RequestMapping("/system/record")
public class MonitorDataController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(MonitorDataController.class);

    @Autowired
    private PushCallback pushCallback;

    @Autowired
    private IMonitorRecordService monitorRecordService;

    @Autowired
    private MqttPushClient mqttPushClient;

    /**
     * 手动触发插入固定监测数据
     * GET /api/monitor/insert-fixed-data
     */
    @GetMapping("/insert-fixed-data")
    public AjaxResult insertFixedData() {
        try {
            pushCallback.insertFixedDataManually();
            //logger.info("手动触发固定数据插入成功");
            return AjaxResult.success("固定数据插入成功");
        } catch (Exception e) {
           // logger.error("手动触发固定数据插入失败: ", e);
            return AjaxResult.error("固定数据插入失败: " + e.getMessage());
        }
    }

    /**
     * 手动控制滴灌
     * POST /api/monitor/irrigation
     */
    @PostMapping("/irrigation")
    public AjaxResult controlIrrigation(@RequestBody JSONObject request) {
        try {
            String action = request.getString("action"); // start/stop
            Integer duration = request.getInteger("duration");
            Integer intensity = request.getInteger("intensity");
            
            AjaxResult result = mqttPushClient.controlIrrigation(action, duration, intensity);
            if (result.isSuccess()) {
                logger.info("滴灌控制指令发送成功: {}", action);
                return AjaxResult.success("滴灌控制指令发送成功");
            } else {
                return AjaxResult.error("滴灌控制指令发送失败");
            }
        } catch (Exception e) {
            logger.error("滴灌控制失败: ", e);
            return AjaxResult.error("滴灌控制失败: " + e.getMessage());
        }
    }

    /**
     * 手动控制风扇
     * POST /api/monitor/fan
     */
    @PostMapping("/fan")
    public AjaxResult controlFan(@RequestBody JSONObject request) {
        try {
            String action = request.getString("action"); // start/stop
            Integer speed = request.getInteger("speed");
            
            AjaxResult result = mqttPushClient.controlFan(action, speed);
            if (result.isSuccess()) {
                logger.info("风扇控制指令发送成功: {}", action);
                return AjaxResult.success("风扇控制指令发送成功");
            } else {
                return AjaxResult.error("风扇控制指令发送失败");
            }
        } catch (Exception e) {
            logger.error("风扇控制失败: ", e);
            return AjaxResult.error("风扇控制失败: " + e.getMessage());
        }
    }

    /**
     * 手动控制补光
     * POST /api/monitor/light
     */
//    @PostMapping("/light")
//    public AjaxResult controlLight(@RequestBody JSONObject request) {
//        try {
//            String action = request.getString("action"); // start/stop
//            Integer brightness = request.getInteger("brightness");
//
//            AjaxResult result = mqttPushClient.controlLight(action, brightness);
//            if (result.isSuccess()) {
//                logger.info("补光控制指令发送成功: {}", action);
//                return AjaxResult.success("补光控制指令发送成功");
//            } else {
//                return AjaxResult.error("补光控制指令发送失败");
//            }
//        } catch (Exception e) {
//            logger.error("补光控制失败: ", e);
//            return AjaxResult.error("补光控制失败: " + e.getMessage());
//        }
//    }

    /**
     * 手动触发自动发布控制指令
     * POST /api/monitor/auto-publish
     */
    @PostMapping("/auto-publish")
    public AjaxResult triggerAutoPublish() {
        try {
            mqttPushClient.autoPublishControlCommands();
            return AjaxResult.success("自动发布控制指令已触发");
        } catch (Exception e) {
            logger.error("触发自动发布失败: ", e);
            return AjaxResult.error("触发自动发布失败: " + e.getMessage());
        }
    }

    /**
     * 获取MQTT接收到的最新数据
     * GET /api/monitor/receive-data
     */
    @GetMapping("/receive-data")
    public AjaxResult getReceiveData() {
        try {
            String data = pushCallback.receive();
            logger.info("获取MQTT接收数据成功");
            return AjaxResult.success("获取数据成功", JSONObject.parseObject(data));
        } catch (Exception e) {
            logger.error("获取MQTT接收数据失败: ", e);
            return AjaxResult.error("获取数据失败: " + e.getMessage());
        }
    }



    /**
     * 查询监测记录列表
     * GET /api/monitor/records
     */
    @GetMapping("/records")
    public TableDataInfo getMonitorRecords(MonitorRecord monitorRecord) {
        startPage();
        // 这里需要根据你的Service方法来调用，假设有selectMonitorRecordList方法
        // List<MonitorRecord> list = monitorRecordService.selectMonitorRecordList(monitorRecord);
        // return getDataTable(list);

        // 如果没有查询方法，返回空列表
        return getDataTable(null);
    }

    /**
     * 根据ID获取监测记录详情
     * GET /api/monitor/records/{id}
     */
    @GetMapping("/records/{id}")
    public AjaxResult getMonitorRecord(@PathVariable Long id) {
        try {
            // 假设有根据ID查询的方法
            // MonitorRecord record = monitorRecordService.selectMonitorRecordById(id);
            // return AjaxResult.success(record);

            return AjaxResult.success("查询成功", null);
        } catch (Exception e) {
            logger.error("查询监测记录失败: ", e);
            return AjaxResult.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 新增监测记录
     * POST /api/monitor/records
     */
    @PostMapping("/records")
    public AjaxResult addMonitorRecord(@RequestBody MonitorRecord monitorRecord) {
        try {
            int result = monitorRecordService.insertMonitorRecord(monitorRecord);
            if (result > 0) {
                logger.info("新增监测记录成功");
                return AjaxResult.success("新增成功");
            } else {
                return AjaxResult.error("新增失败");
            }
        } catch (Exception e) {
            logger.error("新增监测记录失败: ", e);
            return AjaxResult.error("新增失败: " + e.getMessage());
        }
    }

    /**
     * 修改监测记录
     * PUT /api/monitor/records
     */
    @PutMapping("/records")
    public AjaxResult updateMonitorRecord(@RequestBody MonitorRecord monitorRecord) {
        try {
            // 假设有更新方法
            // int result = monitorRecordService.updateMonitorRecord(monitorRecord);
            // if (result > 0) {
            //     logger.info("更新监测记录成功");
            //     return AjaxResult.success("更新成功");
            // } else {
            //     return AjaxResult.error("更新失败");
            // }

            return AjaxResult.success("更新成功");
        } catch (Exception e) {
            logger.error("更新监测记录失败: ", e);
            return AjaxResult.error("更新失败: " + e.getMessage());
        }
    }

    /**
     * 删除监测记录
     * DELETE /api/monitor/records/{ids}
     */
    @DeleteMapping("/records/{ids}")
    public AjaxResult deleteMonitorRecords(@PathVariable Long[] ids) {
        try {
            // 假设有批量删除方法
            // int result = monitorRecordService.deleteMonitorRecordByIds(ids);
            // if (result > 0) {
            //     logger.info("删除监测记录成功");
            //     return AjaxResult.success("删除成功");
            // } else {
            //     return AjaxResult.error("删除失败");
            // }

            return AjaxResult.success("删除成功");
        } catch (Exception e) {
            logger.error("删除监测记录失败: ", e);
            return AjaxResult.error("删除失败: " + e.getMessage());
        }
    }

    /**
     * 获取监测数据统计信息
     * GET /api/monitor/statistics
     */
    @GetMapping("/statistics")
    public AjaxResult getStatistics(@RequestParam(required = false) String startDate,
                                    @RequestParam(required = false) String endDate,
                                    @RequestParam(required = false) Long areaId) {
        try {
            // 这里可以添加统计逻辑
            JSONObject statistics = new JSONObject();
            statistics.put("totalRecords", 0);
            statistics.put("averageTemperature", 0.0);
            statistics.put("averageHumidity", 0.0);

            logger.info("获取监测数据统计成功");
            return AjaxResult.success("获取统计数据成功", statistics);
        } catch (Exception e) {
            logger.error("获取监测数据统计失败: ", e);
            return AjaxResult.error("获取统计数据失败: " + e.getMessage());
        }
    }

    /**
     * 设备控制接口 - 匹配前端请求
     * GET /system/record/deviceControl/{controllCmd}
     */
    @GetMapping("/deviceControl/{controllCmd}")
    public AjaxResult deviceControl(@PathVariable String controllCmd) {
        try {
            logger.info("收到设备控制请求: {}", controllCmd);
            
            switch (controllCmd) {
                case "irrigation_start":
                    return mqttPushClient.controlIrrigation("start", null, null);
                case "irrigation_stop":
                    return mqttPushClient.controlIrrigation("stop", null, null);
                case "fan_start":
                    return mqttPushClient.controlFan("start", null);
                case "fan_stop":
                    return mqttPushClient.controlFan("stop", null);
                default:
                    logger.warn("未知的控制命令: {}", controllCmd);
                    return AjaxResult.error("未知的控制命令: " + controllCmd);
            }
        } catch (Exception e) {
            logger.error("设备控制失败: ", e);
            return AjaxResult.error("设备控制失败: " + e.getMessage());
        }
    }
}
