package com.enokimushroom.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.enokimushroom.common.annotation.Excel;
import com.enokimushroom.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Date;

/**
 * 【请填写功能名称】对象 plant_manage
 * 
 * @author ruoyi
 * @date 2024-06-08
 */
public class PlantManage extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 植物ID */
    private Long plantId;

    /** 植物名称 */
    @Excel(name = "植物名称")
    private String plantName;

    /** 植物类型 */
    @Excel(name = "植物类型")
    private String plantType;

    /** 生命周期（单位：月） */
    //@Excel(name = "生命周期", readConverterExp = "单=位：月")
    @Excel(name = "生命周期,单位：月")
    private Long plantLifecycle;

    /** 所属场地 */
    @Excel(name = "所属场地")
    private Long areaId;

    /** 所属场地 */
    @Excel(name =  "所属场地")
    private String areaName;

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

    /** 是否删除：0，未删除，1，删除 */
    private Long delFlag;

    public void setPlantId(Long plantId) 
    {
        this.plantId = plantId;
    }

    public Long getPlantId() 
    {
        return plantId;
    }
    public void setPlantName(String plantName) 
    {
        this.plantName = plantName;
    }

    public String getPlantName() 
    {
        return plantName;
    }
    public void setPlantType(String plantType) 
    {
        this.plantType = plantType;
    }

    public String getPlantType() 
    {
        return plantType;
    }
    public void setPlantLifecycle(Long plantLifecycle) 
    {
        this.plantLifecycle = plantLifecycle;
    }

    public Long getPlantLifecycle() 
    {
        return plantLifecycle;
    }
    public void setAreaId(Long areaId) 
    {
        this.areaId = areaId;
    }

    public Long getAreaId() 
    {
        return areaId;
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
            .append("plantId", getPlantId())
            .append("plantName", getPlantName())
            .append("plantType", getPlantType())
            .append("plantLifecycle", getPlantLifecycle())
            .append("areaId", getAreaId())
             .append("areaName", getAreaName())
            .append("createDate", getCreateDate())
            .append("createBy", getCreateBy())
            .append("updateDate", getUpdateDate())
            .append("updateBy", getUpdateBy())
            .append("delFlag", getDelFlag())
            .toString();
    }
}
