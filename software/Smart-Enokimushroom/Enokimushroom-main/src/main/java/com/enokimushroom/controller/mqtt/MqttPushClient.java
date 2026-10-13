package com.enokimushroom.controller.mqtt;

import com.enokimushroom.common.core.domain.AjaxResult;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.enokimushroom.common.core.domain.AjaxResult.error;
import static com.enokimushroom.common.core.domain.AjaxResult.success;
import com.alibaba.fastjson2.JSONObject;

@Component
public class MqttPushClient {
    private static final Logger logger = LoggerFactory.getLogger(MqttPushClient.class);

    @Autowired
    private PushCallback pushCallback;

    private static MqttClient client;

    private static MqttClient getClient() {
        return client;
    }

    private static void setClient(MqttClient client) {
        MqttPushClient.client = client;
    }

    /**
     * 客户端连接
     *
     * @param host      ip+端口
     * @param clientID  客户端Id
     * @param username  用户名
     * @param password  密码
     * @param timeout   超时时间
     * @param keepalive 保留数
     */
    public void connect(String host, String clientID, String username, String password, int timeout, int keepalive) {
        MqttClient client;
        try {
            client = new MqttClient(host, clientID, new MemoryPersistence());
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);
            options.setUserName(username);
            options.setPassword(password.toCharArray());
            options.setConnectionTimeout(timeout);
            options.setKeepAliveInterval(keepalive);
            MqttPushClient.setClient(client);
            try {
                client.setCallback(pushCallback);
                client.connect(options);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 发布
     *
     * @param qos         连接方式
     * @param retained    是否保留
     * @param topic       主题
     * @param pushMessage 消息体
     */
    public AjaxResult publish(int qos, boolean retained, String topic, String pushMessage) {
        MqttMessage message = new MqttMessage();
        message.setQos(qos);
        message.setRetained(retained);
        message.setPayload(pushMessage.getBytes());
        MqttTopic mTopic = MqttPushClient.getClient().getTopic(topic);
        if (null == mTopic) {
            logger.error("topic not exist");
        }
        MqttDeliveryToken token;
        try {
            token = mTopic.publish(message);
            token.waitForCompletion();
            return success();
        } catch (MqttPersistenceException e) {
            e.printStackTrace();
            return error();
        } catch (MqttException e) {
            e.printStackTrace();
            return error();
        }
    }

    /**
     * 订阅某个主题
     *
     * @param topic 主题
     * @param qos   连接方式
     */
    public void subscribe(String topic, int qos) {
        logger.info("开始订阅主题" + topic);
        try {
            MqttPushClient.getClient().subscribe(topic, qos);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    /**
     * 滴灌控制
     *
     * @param action    动作：start/stop
     * @param duration  持续时间（秒），仅start时有效
     * @param intensity 强度百分比（1-100），仅start时有效
     */
    public AjaxResult controlIrrigation(String action, Integer duration, Integer intensity) {
        JSONObject command = new JSONObject();
        command.put("deviceId", "cclr04");
        command.put("command", "irrigation");
        command.put("action", action);
        command.put("timestamp", new java.util.Date().toString());
        
        if ("start".equals(action)) {
            command.put("duration", duration != null ? duration : 300);
            command.put("intensity", intensity != null ? intensity : 80);
        }
        
        String topic = "enokimushroom_irrigation";
        logger.info("发送滴灌控制指令: {}", command.toJSONString());
        return publish(1, false, topic, command.toJSONString());
    }

    /**
     * 风扇控制
     *
     * @param action 动作：start/stop
     * @param speed  风速等级（1-5），仅start时有效
     */
    public AjaxResult controlFan(String action, Integer speed) {
        JSONObject command = new JSONObject();
        command.put("deviceId", "cclr04");
        command.put("command", "fan");
        command.put("action", action);
        command.put("timestamp", new java.util.Date().toString());
        
        if ("start".equals(action)) {
            command.put("speed", speed != null ? speed : 3);
        }
        
        String topic = "enokimushroom_fan";
        logger.info("发送风扇控制指令: {}", command.toJSONString());
        return publish(1, false, topic, command.toJSONString());
    }

    /**
     * 补光控制
     *
     * @param action 动作：start/stop
     * @param brightness 亮度百分比（1-100），仅start时有效
     */
    public AjaxResult controlLight(String action, Integer brightness) {
        JSONObject command = new JSONObject();
        command.put("deviceId", "cclr04");
        command.put("command", "light");
        command.put("action", action);
        command.put("timestamp", new java.util.Date().toString());
        
        if ("start".equals(action)) {
            command.put("brightness", brightness != null ? brightness : 70);
        }
        
        String topic = "enokimushroom_light";
        logger.info("发送补光控制指令: {}", command.toJSONString());
        return publish(1, false, topic, command.toJSONString());
    }

    /**
     * 自动发布控制指令（程序启动时调用）
     */
    public void autoPublishControlCommands() {
        try {
            // 等待连接建立
            Thread.sleep(2000);
            
            logger.info("开始自动发布控制指令...");
            
            // 1. 启动滴灌系统（默认5分钟，80%强度）
            AjaxResult irrigationResult = controlIrrigation("start", 300, 80);
            if (irrigationResult.isSuccess()) {
                logger.info("滴灌启动指令发送成功");
            } else {
                logger.error("滴灌启动指令发送失败");
            }
            
            // 等待1秒
            Thread.sleep(1000);
            
            // 2. 启动风扇系统（默认风速3级）
            AjaxResult fanResult = controlFan("start", 3);
            if (fanResult.isSuccess()) {
                logger.info("风扇启动指令发送成功");
            } else {
                logger.error("风扇启动指令发送失败");
            }
            
            // 等待1秒
            Thread.sleep(1000);
            
            // 3. 启动补光系统（默认70%亮度）
            AjaxResult lightResult = controlLight("start", 70);
            if (lightResult.isSuccess()) {
                logger.info("补光启动指令发送成功");
            } else {
                logger.error("补光启动指令发送失败");
            }
            
            logger.info("自动发布控制指令完成");
            
        } catch (Exception e) {
            logger.error("自动发布控制指令失败: ", e);
        }
    }
}
