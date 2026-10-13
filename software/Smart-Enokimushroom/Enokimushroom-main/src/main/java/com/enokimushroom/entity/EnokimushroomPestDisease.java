package com.enokimushroom.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.enokimushroom.common.annotation.Excel;
import com.enokimushroom.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 病虫害分析对象 enokimushroom_pest_disease
 * 
 * @author yejianfeng
 * @date 2025-08-18
 */
public class EnokimushroomPestDisease extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** $column.columnComment */
    private Long id;

    /** 对应金针菇簇/区域ID */
    @Excel(name = "对应金针菇簇/区域ID")
    private String clusterId;

    /** 检测对象类型：leaf（叶子）/ fruit（果实）/ stem（茎） */
    @JsonProperty("object_type")
    @Excel(name = "检测对象类型：leaf", readConverterExp = "叶=子")
    private String objectType;

    /** 边界框最小X坐标（目标检测框左上角X） */
    @JsonProperty("bbox_x_min")
    @Excel(name = "边界框最小X坐标", readConverterExp = "目=标检测框左上角X")
    private BigDecimal bboxXMin;

    /** 边界框最小Y坐标（目标检测框左上角Y） */
    @JsonProperty("bbox_y_min")
    @Excel(name = "边界框最小Y坐标", readConverterExp = "目=标检测框左上角Y")
    private BigDecimal bboxYMin;

    /** 边界框最大X坐标（目标检测框右下角X） */
    @JsonProperty("bbox_x_max")
    @Excel(name = "边界框最大X坐标", readConverterExp = "目=标检测框右下角X")
    private BigDecimal bboxXMax;

    /** 边界框最大Y坐标（目标检测框右下角Y） */
    @JsonProperty("bbox_y_max")
    @Excel(name = "边界框最大Y坐标", readConverterExp = "目=标检测框右下角Y")
    private BigDecimal bboxYMax;

    /** 检测置信度（0.0 ~ 1.0） */
    @JsonProperty("confidence")
    @Excel(name = "检测置信度", readConverterExp = "0=.0,~=,1=.0")
    private BigDecimal confidence;

    /** 识别出的病虫害类别（如：白粉病、红蜘蛛等） */
    @JsonProperty("pest_disease_class")
    @Excel(name = "识别出的病虫害类别", readConverterExp = "如=：白粉病、红蜘蛛等")
    private String pestDiseaseClass;

    /** 严重程度：low / medium / high */
    @JsonProperty("severity_level")
    @Excel(name = "严重程度：low / medium / high")
    private String severityLevel;

    /** 系统生成的防治或处理建议（由AI或定时任务写入） */
    @Excel(name = "系统生成的防治或处理建议", readConverterExp = "由=AI或定时任务写入")
    private String recommendation;

    /** 病虫害检测时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "病虫害检测时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date detectedTime;

    public void setId(Long id) 
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
    }
    public void setClusterId(String clusterId) 
    {
        this.clusterId = clusterId;
    }

    public String getClusterId() 
    {
        return clusterId;
    }
    public void setObjectType(String objectType) 
    {
        this.objectType = objectType;
    }

    public String getObjectType() 
    {
        return objectType;
    }
    public void setBboxXMin(BigDecimal bboxXMin) 
    {
        this.bboxXMin = bboxXMin;
    }

    public BigDecimal getBboxXMin() 
    {
        return bboxXMin;
    }
    public void setBboxYMin(BigDecimal bboxYMin) 
    {
        this.bboxYMin = bboxYMin;
    }

    public BigDecimal getBboxYMin() 
    {
        return bboxYMin;
    }
    public void setBboxXMax(BigDecimal bboxXMax) 
    {
        this.bboxXMax = bboxXMax;
    }

    public BigDecimal getBboxXMax() 
    {
        return bboxXMax;
    }
    public void setBboxYMax(BigDecimal bboxYMax) 
    {
        this.bboxYMax = bboxYMax;
    }

    public BigDecimal getBboxYMax() 
    {
        return bboxYMax;
    }
    public void setConfidence(BigDecimal confidence) 
    {
        this.confidence = confidence;
    }

    public BigDecimal getConfidence() 
    {
        return confidence;
    }
    public void setPestDiseaseClass(String pestDiseaseClass) 
    {
        this.pestDiseaseClass = pestDiseaseClass;
    }

    public String getPestDiseaseClass() 
    {
        return pestDiseaseClass;
    }
    public void setSeverityLevel(String severityLevel) 
    {
        this.severityLevel = severityLevel;
    }

    public String getSeverityLevel() 
    {
        return severityLevel;
    }
    public void setRecommendation(String recommendation) 
    {
        this.recommendation = recommendation;
    }

    public String getRecommendation() 
    {
        return recommendation;
    }
    public void setDetectedTime(Date detectedTime) 
    {
        this.detectedTime = detectedTime;
    }

    public Date getDetectedTime() 
    {
        return detectedTime;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("clusterId", getClusterId())
            .append("objectType", getObjectType())
            .append("bboxXMin", getBboxXMin())
            .append("bboxYMin", getBboxYMin())
            .append("bboxXMax", getBboxXMax())
            .append("bboxYMax", getBboxYMax())
            .append("confidence", getConfidence())
            .append("pestDiseaseClass", getPestDiseaseClass())
            .append("severityLevel", getSeverityLevel())
            .append("recommendation", getRecommendation())
            .append("detectedTime", getDetectedTime())
            .toString();
    }
}
