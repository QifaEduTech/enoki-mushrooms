package com.enokimushroom.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.enokimushroom.common.annotation.Excel;
import com.enokimushroom.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Date;

/**
 * 【请填写功能名称】对象 device_manage
 * 
 * @author ruoyi
 * @date 2024-06-08
 */
public class DeviceManage extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 设备id */
    private Long deviceId;

    /** 设备编号 */
    @Excel(name = "设备编号")
    private String deviceNumber;

    /** 设备类型 */
    @Excel(name = "设备类型")
    private String deviceType;

    /** 监控植物 */
   // @Excel(name = "监控植物")
    private Long plantId;

    /** 植物名称 */
    @Excel(name = "植物名称")
    private String plantName;

    /** 所属场地 */
    //@Excel(name = "所属场地")
    private Long areaId;

    /** 场地名称 */
    @Excel(name = "场地名称")
    private String areaName;

    /** 设备状态：1，正常使用；0，停用 */
    @Excel(name = "设备状态：1，正常使用；0，停用")
    private Long deviceState;

    /** 设备名称 */
    @Excel(name = "设备名称")
    private String deviceName;

    /** 所属ID */
    //@Excel(name = "所属ID")
    private long userId;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "创建时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date createDate;

    /** 开始创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    //@Excel(name = "开始创建时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date startCreateDate;

    /** 结束创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    //@Excel(name = "结束创建时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date stopCreateDate;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "更新时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date updateDate;

    /** 是否删除：0，未删除，1删除 */
    private Long delFlag;

    public void setDeviceId(Long deviceId) 
    {
        this.deviceId = deviceId;
    }

    public Long getDeviceId() 
    {
        return deviceId;
    }
    public void setDeviceNumber(String deviceNumber) 
    {
        this.deviceNumber = deviceNumber;
    }

    public String getDeviceNumber() 
    {
        return deviceNumber;
    }
    public void setDeviceType(String deviceType) 
    {
        this.deviceType = deviceType;
    }

    public String getDeviceType() 
    {
        return deviceType;
    }
    public void setPlantId(Long plantId) 
    {
        this.plantId = plantId;
    }

    public Long getPlantId() 
    {
        return plantId;
    }
    public void setAreaId(Long areaId) 
    {
        this.areaId = areaId;
    }

    public Long getAreaId() 
    {
        return areaId;
    }
    public void setDeviceState(Long deviceState) 
    {
        this.deviceState = deviceState;
    }

    public Long getDeviceState() 
    {
        return deviceState;
    }
    public void setDeviceName(String deviceName) 
    {
        this.deviceName = deviceName;
    }

    public String getDeviceName() 
    {
        return deviceName;
    }
    public void setCreateDate(Date createDate) 
    {
        this.createDate = createDate;
    }

    public Date getCreateDate() 
    {
        return createDate;
    }
    public void setUpdateDate(Date updateDate) 
    {
        this.updateDate = updateDate;
    }

    public Date getUpdateDate() 
    {
        return updateDate;
    }
    public void setDelFlag(Long delFlag) 
    {
        this.delFlag = delFlag;
    }

    public Long getDelFlag() 
    {
        return delFlag;
    }

    public String getPlantName() {
        return plantName;
    }

    public void setPlantName(String plantName) {
        this.plantName = plantName;
    }

    public String getAreaName() {
        return areaName;
    }

    public void setAreaName(String areaName) {
        this.areaName = areaName;
    }

    public Date getStartCreateDate() {
        return startCreateDate;
    }

    public void setStartCreateDate(Date startCreateDate) {
        this.startCreateDate = startCreateDate;
    }

    public Date getStopCreateDate() {
        return stopCreateDate;
    }

    public void setStopCreateDate(Date stopCreateDate) {
        this.stopCreateDate = stopCreateDate;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("deviceId", getDeviceId())
            .append("deviceNumber", getDeviceNumber())
            .append("deviceType", getDeviceType())
            .append("plantId", getPlantId())
            .append("areaId", getAreaId())
            .append("deviceState", getDeviceState())
            .append("deviceName", getDeviceName())
            .append("createDate", getCreateDate())
            .append("createBy", getCreateBy())
            .append("updateDate", getUpdateDate())
            .append("updateBy", getUpdateBy())
            .append("delFlag", getDelFlag())
            .toString();
    }
}
