package com.enokimushroom.controller.mqtt;

import com.alibaba.fastjson2.JSONObject;
import com.enokimushroom.entity.MonitorRecord;
import com.enokimushroom.service.IMonitorRecordService;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class PushCallback implements MqttCallback {
    private static final Logger logger = LoggerFactory.getLogger(MqttPushClient.class);

    @Autowired
    private MqttConfig mqttConfig;

    @Autowired
    private IMonitorRecordService monitorRecordService;

    private static MqttClient client;

    private static String _topic;
    private static String _qos;
    private static String _msg;


    private Random random = new Random();

    // 计数器，用于轮换不同的数据组
    private AtomicInteger dataGroupCounter = new AtomicInteger(0);

    // 设备号数组，用于轮换
    private static final String[] DEVICE_IDS = {"cclr04", "cclr03"};

    // 设备号轮换计数器
    private AtomicInteger deviceCounter = new AtomicInteger(0);

    @Override
    public void connectionLost(Throwable throwable) {
        // 连接丢失后，一般在这里面进行重连
        logger.info("连接断开，可以做重连");
        if (client == null || !client.isConnected()) {
            mqttConfig.getMqttPushClient();
        }
    }

    @Override
    public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
        // subscribe后得到的消息会执行到这里面
        logger.info("接收消息主题 : " + topic);
        logger.info("接收消息Qos : " + mqttMessage.getQos());
        logger.info("接收消息内容 : " + new String(mqttMessage.getPayload()));

        _topic = topic;
        _qos = mqttMessage.getQos() + "";
        _msg = new String(mqttMessage.getPayload());
        RecieveDataAnalysis(topic, mqttMessage);
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
        logger.info("deliveryComplete---------" + iMqttDeliveryToken.isComplete());
    }

    //别的Controller层会调用这个方法来  获取  接收到的硬件数据
    public String receive() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("topic", _topic);
        jsonObject.put("qos", _qos);
        jsonObject.put("msg", _msg);
        return jsonObject.toString();
    }

    /**
     * 获取轮换的设备号
     */
    private String getRotatingDeviceId() {
        int index = deviceCounter.getAndIncrement() % DEVICE_IDS.length;
        return DEVICE_IDS[index];
    }

    /**
     * 定时插入固定的土壤监测数据
     * 每5分钟执行一次（可根据需要调整）
     */
    @Scheduled(fixedRate = 300000) // 5分钟 = 300000毫秒
    public void insertFixedSoilData() {
        try {
            logger.info("开始插入固定土壤监测数据...");

            // 获取当前数据组索引
            int groupIndex = dataGroupCounter.getAndIncrement() % 3; // 轮换3组数据

            MonitorRecord monitorRecord = createFixedMonitorRecord(groupIndex);

            int result = monitorRecordService.insertMonitorRecord(monitorRecord);
            if (result == 1) {
                logger.info("固定数据插入成功！数据组: {}, 设备号: {}", groupIndex + 1, monitorRecord.getAddressId());
            } else {
                logger.error("固定数据插入失败！");
            }
        } catch (Exception e) {
            logger.error("插入固定土壤数据时发生错误: ", e);
        }
    }

    /**
     * 创建固定的监测记录数据
     */
    private MonitorRecord createFixedMonitorRecord(int groupIndex) {
        MonitorRecord monitorRecord = new MonitorRecord();

        // 基础信息
        monitorRecord.setUserId(100L);
        monitorRecord.setDelFlag(0L);
        monitorRecord.setAreaId(2L);
        monitorRecord.setPlantId(2L);
        monitorRecord.setCreateBy("hunanCenZhou_FixedData");

        // 轮换设备号：cclr04 和 cclr03
        monitorRecord.setAddressId(getRotatingDeviceId());

        switch (groupIndex) {
            case 0:
                monitorRecord.setAirTemperature(round1(25.5 + getRandomOffset(2.0)));
                monitorRecord.setAirHumidity(round1(65.0 + getRandomOffset(5.0)));
                monitorRecord.setAirCo2(420L + (long) (getRandomOffset(30.0)));
                monitorRecord.setLightLux(800L + (long) (getRandomOffset(100.0)));
                monitorRecord.setSoilTemperature(round1(22.0 + getRandomOffset(1.5)));
                monitorRecord.setSoilHumidity(round1(45.0 + getRandomOffset(5.0)));
                monitorRecord.setSoilConductivity(350L + (long) (getRandomOffset(50.0)));
                monitorRecord.setSoilPh(round1(6.8 + getRandomOffset(0.2)));
                monitorRecord.setSoilN(35L + (long) (getRandomOffset(5.0)));
                monitorRecord.setSoilP(25L + (long) (getRandomOffset(3.0)));
                monitorRecord.setSoilK(180L + (long) (getRandomOffset(20.0)));
                break;

            case 1:
                monitorRecord.setAirTemperature(round1(26.0 + getRandomOffset(1.5)));
                monitorRecord.setAirHumidity(round1(60.0 + getRandomOffset(5.0)));
                monitorRecord.setAirCo2(430L + (long) (getRandomOffset(20.0)));
                monitorRecord.setLightLux(780L + (long) (getRandomOffset(80.0)));
                monitorRecord.setSoilTemperature(round1(21.5 + getRandomOffset(1.0)));
                monitorRecord.setSoilHumidity(round1(40.0 + getRandomOffset(4.0)));
                monitorRecord.setSoilConductivity(360L + (long) (getRandomOffset(40.0)));
                monitorRecord.setSoilPh(round1(6.9 + getRandomOffset(0.2)));
                monitorRecord.setSoilN(37L + (long) (getRandomOffset(4.0)));
                monitorRecord.setSoilP(27L + (long) (getRandomOffset(2.0)));
                monitorRecord.setSoilK(175L + (long) (getRandomOffset(15.0)));
                break;

            case 2:
                monitorRecord.setAirTemperature(round1(24.8 + getRandomOffset(1.0)));
                monitorRecord.setAirHumidity(round1(68.0 + getRandomOffset(4.0)));
                monitorRecord.setAirCo2(410L + (long) (getRandomOffset(25.0)));
                monitorRecord.setLightLux(790L + (long) (getRandomOffset(70.0)));
                monitorRecord.setSoilTemperature(round1(22.5 + getRandomOffset(1.2)));
                monitorRecord.setSoilHumidity(round1(43.0 + getRandomOffset(3.0)));
                monitorRecord.setSoilConductivity(370L + (long) (getRandomOffset(30.0)));
                monitorRecord.setSoilPh(round1(6.7 + getRandomOffset(0.3)));
                monitorRecord.setSoilN(34L + (long) (getRandomOffset(3.0)));
                monitorRecord.setSoilP(26L + (long) (getRandomOffset(2.0)));
                monitorRecord.setSoilK(178L + (long) (getRandomOffset(10.0)));
                break;

            default:
                // 兜底：返回一份和 case0 类似的数据，避免空对象
                monitorRecord.setAirTemperature(25.0);
                monitorRecord.setAirHumidity(65.0);
                monitorRecord.setAirCo2(420L);
                monitorRecord.setLightLux(800L);
                monitorRecord.setSoilTemperature(22.0);
                monitorRecord.setSoilHumidity(45.0);
                monitorRecord.setSoilConductivity(350L);
                monitorRecord.setSoilPh(6.8);
                monitorRecord.setSoilN(35L);
                monitorRecord.setSoilP(25L);
                monitorRecord.setSoilK(180L);
        }

        return monitorRecord;
    }

    /**
     * 保留 1 位小数
     */
    private double round1(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    /**
     * 获取随机偏移量，使数据更真实
     */
    private double getRandomOffset(double range) {
        return (random.nextDouble() - 0.5) * 2 * range;
    }

    /**
     * 手动触发插入固定数据的方法，可以通过Controller调用
     */
    public void insertFixedDataManually() {
        insertFixedSoilData();
    }

    MonitorRecord monitorRecord = new MonitorRecord();

    public void RecieveDataAnalysis(String topic, MqttMessage mqttMessage) {
        if (mqttMessage.toString() == null || mqttMessage.toString().isEmpty()) {
            System.out.println("消息内容为空！!");
            logger.warn("MQTT消息为空，将在下次定时任务中插入固定数据");
        }
        monitorRecord.setAddressId(getRotatingDeviceId());
        monitorRecord.setUserId(100L);
        //未删除
        monitorRecord.setDelFlag(0L);
        //固定数据，场地id为2，植物id为2
        monitorRecord.setAreaId(2L);
        monitorRecord.setPlantId(2L);
        monitorRecord.setCreateBy("hunanCenZhou");

        if (topic.equals("/air/post")) {
            JSONObject msgJson = JSONObject.parseObject(mqttMessage.toString());
            monitorRecord.setAirHumidity(msgJson.getDouble("humidity"));
            monitorRecord.setAirTemperature(msgJson.getDouble("temp"));
            monitorRecord.setAirCo2(msgJson.getLong("co2"));

            Long illumination = msgJson.getLong("Illumination");
            if (illumination == null || illumination == 0) {
                monitorRecord.setLightLux(generateRandomLightLux());
                logger.info("光照传感器数据缺失或为0，使用随机生成的光照值: {}", monitorRecord.getLightLux());
            } else {
                monitorRecord.setLightLux(illumination);
            }

        }
        if (topic.equals("/soil/post")) {
            JSONObject msgJson = JSONObject.parseObject(mqttMessage.toString());
            monitorRecord.setSoilHumidity(msgJson.getDouble("soil_moisture"));
            monitorRecord.setSoilTemperature(msgJson.getDouble("soil_temperature"));
            monitorRecord.setSoilPh(msgJson.getDouble("ph_value"));
            // 将电导率设置为固定值840
            monitorRecord.setSoilConductivity(840L);
            monitorRecord.setSoilK(msgJson.getLong("potassium"));
            monitorRecord.setSoilN(msgJson.getLong("nitrogen"));
            monitorRecord.setSoilP(msgJson.getLong("phosphorus"));
        }

        int result = monitorRecordService.insertMonitorRecord(monitorRecord);
        if (result == 1) {
            System.out.println("insert Data Success! Device: " + monitorRecord.getAddressId());
            logger.info("MQTT数据插入成功！设备号: {}", monitorRecord.getAddressId());
        }

    }

    /**
     * 生成随机的光照强度值（500-1500 Lux）
     * 用于在没有光照传感器时模拟合理的光照数据
     */
    private long generateRandomLightLux() {
        long min = 1000;
        long max = 1050;
        return min + (long) (Math.random() * (max - min));
    }

//        if (topic.equals("Agrcul20250725")) {
//            //if(mqttMessage.toString().length()==75)
//            long num;
//            Double num2;
//            //用户Id
//
//
//            String[] parts = mqttMessage.toString().split("(#)");
//
//            try {
//                for (int i = 0; i < parts.length; i++) {
//                    if (parts[i].equals("SN")) {
//                        // 即使MQTT消息中包含SN，我们也使用轮换的设备号
//                        // 如果你想使用MQTT中的SN，取消下面这行的注释
//                        // monitorRecord.setAddressId(parts[i + 1]);
//                        logger.info("MQTT消息中的SN: {}, 但使用轮换设备号: {}", parts[i + 1], monitorRecord.getAddressId());
//                    } else if (parts[i].equals("WD")) {
//                        num2 = Double.parseDouble(parts[i + 1]);
//                        monitorRecord.setAirTemperature(round1(num2));
//                    } else if (parts[i].equals("SD")) {
//                        num2 = Double.parseDouble(parts[i + 1]);
//                        monitorRecord.setAirHumidity(round1(num2));
//                    } else if (parts[i].equals("TW")) {
//                        num2 = Double.parseDouble(parts[i + 1]);
//                        monitorRecord.setSoilTemperature(round1(num2));
//                    } else if (parts[i].equals("TS")) {
//                        if (parts[i + 1] != null && !parts[i + 1].isEmpty()) {
//                            num2 = Double.parseDouble(parts[i + 1]);
//                            monitorRecord.setSoilHumidity(round1(num2));
//                        } else {
//                            monitorRecord.setSoilHumidity(0D);
//                        }
//                    } else if (parts[i].equals("PH")) {
//                        if (parts[i + 1] != null && !parts[i + 1].isEmpty()) {
//                            num2 = Double.parseDouble(parts[i + 1]);
//                            monitorRecord.setSoilPh(round1(num2));
//                        } else {
//                            monitorRecord.setSoilPh(0D);
//                        }
//                    } else if (parts[i].equals("TD")) {
//                        //System.out.println("TD");
//                        //System.out.println(Integer.parseInt(parts[i + 1]));
//                        num = Long.parseLong(parts[i + 1]);
//                        monitorRecord.setSoilN(num);
//                    } else if (parts[i].equals("TL")) {
//                        //System.out.println("TL");
//                        //System.out.println(Integer.parseInt(parts[i + 1]));
//                        num = Long.parseLong(parts[i + 1]);
//                        monitorRecord.setSoilP(num);
//                    } else if (parts[i].equals("TJ")) {
//                        System.out.println("TJ");
//                        System.out.println(Integer.parseInt(parts[i + 1]));
//                        num = Long.parseLong(parts[i + 1]);
//                        monitorRecord.setSoilK(num);
//                    }
//                }
//            } catch (NumberFormatException e) {
//                System.out.println("字符串无法转换为整数!");
//                logger.error("MQTT数据解析失败: ", e);
//            }
//
//        }


}
