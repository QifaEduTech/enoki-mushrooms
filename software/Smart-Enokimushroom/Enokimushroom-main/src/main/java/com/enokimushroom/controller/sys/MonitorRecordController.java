package com.enokimushroom.controller.sys;

import com.enokimushroom.common.annotation.Anonymous;
import com.enokimushroom.common.annotation.Log;
import com.enokimushroom.common.core.controller.BaseController;
import com.enokimushroom.common.core.domain.AjaxResult;
import com.enokimushroom.common.core.domain.entity.SysUser;
import com.enokimushroom.common.core.page.TableDataInfo;
import com.enokimushroom.common.enums.BusinessType;
import com.enokimushroom.common.utils.SecurityUtils;
import com.enokimushroom.common.utils.poi.ExcelUtil;
import com.enokimushroom.entity.MonitorRecord;
import com.enokimushroom.service.IMonitorRecordService;
import com.enokimushroom.controller.mqtt.MqttPushClient;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static com.enokimushroom.common.utils.Base64EncodeData.base64Decode;

/**
 * 【监控记录】Controller
 * 
 * @author ruoyi
 * @date 2024-06-08
 */
@RestController
@RequestMapping("/system/record")
public class MonitorRecordController extends BaseController
{
    @Autowired
    private IMonitorRecordService monitorRecordService;

    /**
     * 查询【监控记录】列表
     */
    @PreAuthorize("@ss.hasPermi('system:record:list')")
    @GetMapping("/list")
    public TableDataInfo list(MonitorRecord monitorRecord)
    {
        SysUser user = SecurityUtils.getLoginUser().getUser();
        monitorRecord.setUserId(user.getUserId());
        startPage();
        List<MonitorRecord> list = monitorRecordService.selectMonitorRecordList(monitorRecord);
        return getDataTable(list);
    }

    /**
     * 导出【监控记录】列表
     */
    @PreAuthorize("@ss.hasPermi('system:record:export')")
    @Log(title = "【导出监控记录】", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, MonitorRecord monitorRecord)
    {
        SysUser user = SecurityUtils.getLoginUser().getUser();
        monitorRecord.setUserId(user.getUserId());
        List<MonitorRecord> list = monitorRecordService.selectMonitorRecordList(monitorRecord);
        ExcelUtil<MonitorRecord> util = new ExcelUtil<MonitorRecord>(MonitorRecord.class);
        util.exportExcel(response, list, "【监控记录】数据");
    }

    /**
     * 获取【监控记录】详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:record:query')")
    @GetMapping(value = "/{recordId}")
    public AjaxResult getInfo(@PathVariable("recordId") Long recordId)
    {
        return success(monitorRecordService.selectMonitorRecordByRecordId(recordId));
    }

    /**
     * 获取【监控记录】详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:record:add')")
    @GetMapping(value = "/controLerLP/{controllCmd}")
    public AjaxResult contrllLp(@PathVariable("controllCmd") String controllCmd)
    {
        //System.out.println(controllCmd);
        String  PubilshTopic ="pus20250725";
        MqttPushClient mqttPushClient= new MqttPushClient();
        return mqttPushClient.publish(0, false,PubilshTopic, controllCmd);
    }

    /**
     * 新增【监控记录】
     */
    @PreAuthorize("@ss.hasPermi('system:record:add')")
    @Log(title = "【新增监控记录】", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody MonitorRecord monitorRecord)
    {
        monitorRecord.setDelFlag(0L);
        return toAjax(monitorRecordService.insertMonitorRecord(monitorRecord));
    }

    /**
     * 修改【监控记录】
     */
    @PreAuthorize("@ss.hasPermi('system:record:edit')")
    @Log(title = "【修改监控记录】", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody MonitorRecord monitorRecord)
    {
        return toAjax(monitorRecordService.updateMonitorRecord(monitorRecord));
    }

    /**
     * 删除【监控记录】
     */
    @PreAuthorize("@ss.hasPermi('system:record:remove')")
    @Log(title = "【删除监控记录】", businessType = BusinessType.DELETE)
	@DeleteMapping("/{recordIds}")
    public AjaxResult remove(@PathVariable Long[] recordIds)
    {
        return toAjax(monitorRecordService.deleteMonitorRecordByRecordIds(recordIds));
    }


    /**
     * 新增监控记录/api/receive?LoRaPayload=XXX
     * ZigBee分布式系统。Http Get方法接收数据
     */
    @Anonymous
    //@PreAuthorize("@ss.hasPermi('system:record:add')")
    //@Log(title = "监控记录", businessType = BusinessType.INSERT)
    @GetMapping("/api/receiveLoRa")
    @ResponseBody
    public AjaxResult addLoRaDTU(@RequestParam("payload") String name)
    {
        //System.out.println(name);
        long num;
        Double num2;
        MonitorRecord monitorRecord= new MonitorRecord();
        //用户Id
        monitorRecord.setUserId(100L);
        //未删除
        monitorRecord.setDelFlag(0L);
        //固定数据，场地id为2，植物id为2
        monitorRecord.setAreaId(2L);
        monitorRecord.setPlantId(2L);
        monitorRecord.setCreateBy("changchun");
        //测试数据
        //String name1="U04jY2NscjA0I1NEIys2Ny44I1dEIyszMS43I0NPIys0MjYjTEQjKzEwOTQzI1RTIysyNi41I1RXIysyNy42I1BIIys3LjMwI1REIys1I1RMIysyNiNUSiMrMTk=";
        //SN#cclr02#SD#+62.5#WD#+24.7#CO#+500#LD#+219#TS#+.000#TW#+24.6#DD#+0#PH#+3.00#TD#+5#TL#+26#TJ#+19
        byte[] byteArray = base64Decode(name);

        // 使用UTF-8字符集将字节数组转换为字符串
        String strData = new String(byteArray, StandardCharsets.UTF_8);
        System.out.println(strData);

//        //开始解析数据
//        List<Map<String, Object>> result = new ArrayList<>();
//        String[] blocks = strData.split("@");

        //for (String block : blocks) {
            //System.out.println(block);
            //System.out.println("ok block");
            if (!strData.isEmpty()) {
                //Map<String, Object> data = new HashMap<>();
                String[] pairs = strData.split("#");

                for (int i = 0; i < pairs.length; i += 2) {
                    if (i + 1 < pairs.length) {
                        String key = pairs[i];
                        String value = pairs[i + 1];
                        System.out.print("key:"+key+","+"value:"+value+"    ");
                        // 尝试根据值的格式将其转换为适当的类型
                        // 这里简单处理，将数值转换为Double或Integer
                        try {
                            //if (value.contains(".")) {
                                if (key.equals("TW")) {//土壤温度
                                    num2 = Double.parseDouble(value);
                                    monitorRecord.setSoilTemperature(num2);
                                }
                                else if (key.equals("TS")) {//土壤湿度
                                    num2 = Double.parseDouble(value);
                                    monitorRecord.setSoilHumidity(num2);
                                }
                                else if (key.equals("WD")) {//空气温度
                                    num2 = Double.parseDouble(value);
                                    monitorRecord.setAirTemperature(num2);
                                }
                                else if (key.equals("SD")) {//空气湿度
                                    num2 = Double.parseDouble(value);
                                    monitorRecord.setAirHumidity(num2);
                                }
                                else if (key.equals("PH")) {//土壤PH
                                    num2 = Double.parseDouble(value);
                                    monitorRecord.setSoilPh(num2);
                                }
                            //} else {
                                else if (key.equals("SN")) {
                                    //num = Long.parseLong(value);
                                    monitorRecord.setAddressId(value);
                                }
                                else if (key.equals("TD")) {//土壤氮
                                    num =  Long.parseLong(value);
                                    monitorRecord.setSoilN(num);
                                }
                                else if (key.equals("TL")) {//土壤磷
                                    num =  Long.parseLong(value);
                                    monitorRecord.setSoilP(num);
                                }
                                else if (key.equals("TJ")) {//土壤钾
                                    num =  Long.parseLong(value);
                                    monitorRecord.setSoilK(num);
                                }
                                else if (key.equals("CO")) {//二氧化碳
                                    num =  Long.parseLong(value);
                                    monitorRecord.setAirCo2(num);
                                }

                                if (key.equals("LD")) {//光照强度
                                    num =  Long.parseLong(value);
                                    monitorRecord.setLightLux(num);
                                }
                                if (key.equals("DD")) {//土壤电导率
//                                    num =  Long.parseLong(value);
//                                    monitorRecord.setLightLux(num);
                                }
                            //}
                        } catch (NumberFormatException e) {
                            // 如果不是数字，则直接存储为字符串
                            //data.put(key, value);
                        }
                    }
                }
                //System.out.println("ok!!!!");
                //return toAjax( monitorRecordService.insertMonitorRecord(monitorRecord)) ;
            }
        //}
        return toAjax( monitorRecordService.insertMonitorRecord(monitorRecord)) ;
    }
}
