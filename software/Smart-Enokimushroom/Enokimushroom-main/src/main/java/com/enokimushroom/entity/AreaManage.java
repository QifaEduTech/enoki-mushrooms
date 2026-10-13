package com.enokimushroom.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.enokimushroom.common.annotation.Excel;
import com.enokimushroom.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Date;

/**
 * 【请填写功能名称】对象 area_manage
 * 
 * @author ruoyi
 * @date 2024-06-08
 */
public class AreaManage extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 场地ID */
    private Long areaId;

    /** 场地名称 */
    @Excel(name = "场地名称")
    private String areaName;

    /** 场地地址 */
    @Excel(name = "场地地址")
    private String areaAddress;

    /** 场地类型 */
    @Excel(name = "场地类型")
    private String areaType;

    /** 场地面积（单位：亩） */
    @Excel(name = "场地面积", readConverterExp = "单=位：亩")
    private Long areaSquare;

    /** 场地状态：正常；停用；待建 */
    @Excel(name = "场地状态：正常；停用；待建")
    private String areaState;

    /** 场地户主 */
    @Excel(name = "场地户主")
    private String areaLeader;

    /** 联系方式 */
    @Excel(name = "联系方式")
    private String areaPhone;

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

    /** 是否删除：0，未删除；1，删除 */
    private Long delFlag;

    public void setAreaId(Long areaId) 
    {
        this.areaId = areaId;
    }

    public Long getAreaId() 
    {
        return areaId;
    }
    public void setAreaName(String areaName) 
    {
        this.areaName = areaName;
    }

    public String getAreaName() 
    {
        return areaName;
    }
    public void setAreaAddress(String areaAddress) 
    {
        this.areaAddress = areaAddress;
    }

    public String getAreaAddress() 
    {
        return areaAddress;
    }
    public void setAreaType(String areaType) 
    {
        this.areaType = areaType;
    }

    public String getAreaType() 
    {
        return areaType;
    }
    public void setAreaSquare(Long areaSquare) 
    {
        this.areaSquare = areaSquare;
    }

    public Long getAreaSquare() 
    {
        return areaSquare;
    }
    public void setAreaState(String areaState) 
    {
        this.areaState = areaState;
    }

    public String getAreaState() 
    {
        return areaState;
    }
    public void setAreaLeader(String areaLeader) 
    {
        this.areaLeader = areaLeader;
    }

    public String getAreaLeader() 
    {
        return areaLeader;
    }
    public void setAreaPhone(String areaPhone) 
    {
        this.areaPhone = areaPhone;
    }

    public String getAreaPhone() 
    {
        return areaPhone;
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
            .append("areaId", getAreaId())
            .append("areaName", getAreaName())
            .append("areaAddress", getAreaAddress())
            .append("areaType", getAreaType())
            .append("areaSquare", getAreaSquare())
            .append("areaState", getAreaState())
            .append("areaLeader", getAreaLeader())
            .append("areaPhone", getAreaPhone())
            .append("createDate", getCreateDate())
            .append("createBy", getCreateBy())
            .append("updateDate", getUpdateDate())
            .append("updateBy", getUpdateBy())
            .append("delFlag", getDelFlag())
            .toString();
    }
}
