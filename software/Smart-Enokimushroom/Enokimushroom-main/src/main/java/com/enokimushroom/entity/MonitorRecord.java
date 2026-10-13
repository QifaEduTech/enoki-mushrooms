package com.enokimushroom.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.enokimushroom.common.annotation.Excel;
import com.enokimushroom.common.core.domain.BaseEntity;

import java.util.Date;

/**
 * 【请填写功能名称】对象 monitor_record
 * 
 * @author 张申
 * @date 2024-06-08
 */
public class MonitorRecord extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 记录编号 */
    private Long recordId;

    /** 设备地址 */
    private String addressId;

    /** 空气温度（。C） */
    @Excel(name = "空气温度", readConverterExp = "。=C")
    private Double airTemperature;

    /** 空气湿度（%） */
    @Excel(name = "空气湿度", readConverterExp = "%=")
    private Double airHumidity;

    /** 光照强度（Lux） */
    @Excel(name = "光照强度", readConverterExp = "L=ux")
    private Long lightLux;

    /** 二氧化碳（ppm） */
    @Excel(name = "二氧化碳", readConverterExp = "p=pm")
    private Long airCo2;

    /** 土壤温度（。C） */
    @Excel(name = "土壤温度", readConverterExp = "。=C")
    private Double soilTemperature;

    /** 土壤湿度（%） */
    @Excel(name = "土壤湿度", readConverterExp = "%=")
    private Double soilHumidity;

    /** 电导率（us/cm） */
    @Excel(name = "电导率", readConverterExp = "u=s/cm")
    private Long soilConductivity;

    /** 土壤PH值 */
    @Excel(name = "土壤PH值")
    private Double soilPh;

    /** 氮（mg/kg） */
    @Excel(name = "氮", readConverterExp = "m=g/kg")
    private Long soilN;

    /** 磷（mg/kg） */
    @Excel(name = "磷", readConverterExp = "m=g/kg")
    private Long soilP;

    /** 钾（mg/kg） */
    @Excel(name = "钾", readConverterExp = "m=g/kg")
    private Long soilK;

    /** 所属ID */
    //@Excel(name = "所属ID")
    private long userId;

    /** 记录时间 */
    //@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMt+8")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "记录时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date createDate;

    /** 开始创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    //@Excel(name = "开始记录时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date startCreateDate;

    /** 结束创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    //@Excel(name = "结束记录时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date stopCreateDate;

    /** 植物ID */
    //@Excel(name = "植物ID")
    private Long plantId;

    /** 植物名称 */
    @Excel(name = "植物名称")
    private String plantName;

    /** 场地ID */
    //@Excel(name = "场地ID")
    private Long areaId;

    /** 场地名称 */
    @Excel(name = "场地名称")
    private String areaName;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "更新时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date updateDate;

    /** 是否删除：0，未删除，1，删除 */
    private Long delFlag;

    public void setRecordId(Long recordId) 
    {
        this.recordId = recordId;
    }

    public String getAddressId() {
        return addressId;
    }

    public void setAddressId(String addressId) {
        this.addressId = addressId;
    }

    public Long getRecordId()
    {
        return recordId;
    }

    public void setAirHumidity(Double airHumidity)
    {
        this.airHumidity = airHumidity;
    }

    public Double getAirHumidity()
    {
        return airHumidity;
    }
    public void setLightLux(Long lightLux) 
    {
        this.lightLux = lightLux;
    }

    public Long getLightLux() 
    {
        return lightLux;
    }
    public void setAirCo2(Long airCo2) 
    {
        this.airCo2 = airCo2;
    }

    public Long getAirCo2() 
    {
        return airCo2;
    }

    public void setSoilConductivity(Long soilConductivity) 
    {
        this.soilConductivity = soilConductivity;
    }

    public Long getSoilConductivity() 
    {
        return soilConductivity;
    }

    public void setSoilN(Long soilN) 
    {
        this.soilN = soilN;
    }

    public Long getSoilN() 
    {
        return soilN;
    }
    public void setSoilP(Long soilP) 
    {
        this.soilP = soilP;
    }

    public Long getSoilP() 
    {
        return soilP;
    }
    public void setSoilK(Long soilK) 
    {
        this.soilK = soilK;
    }

    public Long getSoilK() 
    {
        return soilK;
    }
    public void setCreateDate(Date createDate) 
    {
        this.createDate = createDate;
    }

    public Date getCreateDate() 
    {
        return createDate;
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

    public Double getAirTemperature() {
        return airTemperature;
    }

    public void setAirTemperature(Double airTemperature) {
        this.airTemperature = airTemperature;
    }

    public Double getSoilTemperature() {
        return soilTemperature;
    }

    public void setSoilTemperature(Double soilTemperature) {
        this.soilTemperature = soilTemperature;
    }

    public Double getSoilHumidity() {
        return soilHumidity;
    }

    public void setSoilHumidity(Double soilHumidity) {
        this.soilHumidity = soilHumidity;
    }

    public Double getSoilPh() {
        return soilPh;
    }

    public void setSoilPh(Double soilPh) {
        this.soilPh = soilPh;
    }

    @Override
    public String toString() {
        return "MonitorRecord{" +
                "recordId=" + recordId +
                ", addressId=" + addressId +
                ", airTemperature=" + airTemperature +
                ", airHumidity=" + airHumidity +
                ", lightLux=" + lightLux +
                ", airCo2=" + airCo2 +
                ", soilTemperature=" + soilTemperature +
                ", soilHumidity=" + soilHumidity +
                ", soilConductivity=" + soilConductivity +
                ", soilPh=" + soilPh +
                ", soilN=" + soilN +
                ", soilP=" + soilP +
                ", soilK=" + soilK +
                ", userId=" + userId +
                ", createDate=" + createDate +
                ", startCreateDate=" + startCreateDate +
                ", stopCreateDate=" + stopCreateDate +
                ", plantId=" + plantId +
                ", plantName='" + plantName + '\'' +
                ", areaId=" + areaId +
                ", areaName='" + areaName + '\'' +
                ", updateDate=" + updateDate +
                ", delFlag=" + delFlag +
                '}';
    }
}
