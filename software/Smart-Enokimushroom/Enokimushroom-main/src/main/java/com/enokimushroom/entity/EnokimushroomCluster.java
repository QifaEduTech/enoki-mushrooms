package com.enokimushroom.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.enokimushroom.common.annotation.Excel;
import com.enokimushroom.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Date;

/**
 * 产量预期对象 enokimushroom_cluster
 * 
 * @author yejainfeng
 * @date 2025-08-18
 */
public class EnokimushroomCluster extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** $column.columnComment */
    private Long id;

    /** 金针菇簇/区域唯一ID */
    @Excel(name = "金针菇簇/区域唯一ID")
    private String clusterId;

    /** 区域/地块名称（例如：温室A北区） */
    @Excel(name = "区域/地块名称", readConverterExp = "例=如：温室A北区")
    private String clusterName;

    /** 簇内金针菇总数 */
    @JsonProperty("total_count")
    @Excel(name = "簇内金针菇总数")
    private Long totalCount;

    /** 已成熟数量 */
    @JsonProperty("ripe_count")
    @Excel(name = "已成熟数量")
    private Long ripeCount;

    /** 半成熟数量 */
    @Excel(name = "半成熟数量")
    @JsonProperty("half_ripe_count")
    private Long halfRipeCount;

    /** 未成熟数量 */
    @Excel(name = "未成熟数量")
    @JsonProperty("unripe_count")
    private Long unripeCount;

    /** 平均大小（毫米） */
    @JsonProperty("avg_size_mm")
    @Excel(name = "平均大小", readConverterExp = "毫=米")
    private Long avgSizeMm;

    /** 区域范围（地块/簇） */
    @Excel(name = "区域范围", readConverterExp = "地=块/簇")
    private String locationArea;

    /** 簇中心点纬度 */
    @Excel(name = "簇中心点纬度")
    private Long latitude;

    /** 簇中心点经度 */
    @Excel(name = "簇中心点经度")
    private Long longitude;

    /** 系统生成的采摘建议采摘时间（由AI或定时任务写入） */
    @Excel(name = "系统生成的采摘建议采摘时间", readConverterExp = "由=AI或定时任务写入")
    private String recommendation;

    /** 最后更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "最后更新时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date lastUpdate;

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
    public void setClusterName(String clusterName) 
    {
        this.clusterName = clusterName;
    }

    public String getClusterName() 
    {
        return clusterName;
    }
    public void setTotalCount(Long totalCount) 
    {
        this.totalCount = totalCount;
    }

    public Long getTotalCount() 
    {
        return totalCount;
    }
    public void setRipeCount(Long ripeCount) 
    {
        this.ripeCount = ripeCount;
    }

    public Long getRipeCount() 
    {
        return ripeCount;
    }
    public void setHalfRipeCount(Long halfRipeCount) 
    {
        this.halfRipeCount = halfRipeCount;
    }

    public Long getHalfRipeCount() 
    {
        return halfRipeCount;
    }
    public void setUnripeCount(Long unripeCount) 
    {
        this.unripeCount = unripeCount;
    }

    public Long getUnripeCount() 
    {
        return unripeCount;
    }
    public void setAvgSizeMm(Long avgSizeMm) 
    {
        this.avgSizeMm = avgSizeMm;
    }

    public Long getAvgSizeMm() 
    {
        return avgSizeMm;
    }
    public void setLocationArea(String locationArea) 
    {
        this.locationArea = locationArea;
    }

    public String getLocationArea() 
    {
        return locationArea;
    }
    public void setLatitude(Long latitude) 
    {
        this.latitude = latitude;
    }

    public Long getLatitude() 
    {
        return latitude;
    }
    public void setLongitude(Long longitude) 
    {
        this.longitude = longitude;
    }

    public Long getLongitude() 
    {
        return longitude;
    }
    public void setRecommendation(String recommendation) 
    {
        this.recommendation = recommendation;
    }

    public String getRecommendation() 
    {
        return recommendation;
    }
    public void setLastUpdate(Date lastUpdate) 
    {
        this.lastUpdate = lastUpdate;
    }

    public Date getLastUpdate() 
    {
        return lastUpdate;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("clusterId", getClusterId())
            .append("clusterName", getClusterName())
            .append("totalCount", getTotalCount())
            .append("ripeCount", getRipeCount())
            .append("halfRipeCount", getHalfRipeCount())
            .append("unripeCount", getUnripeCount())
            .append("avgSizeMm", getAvgSizeMm())
            .append("locationArea", getLocationArea())
            .append("latitude", getLatitude())
            .append("longitude", getLongitude())
            .append("recommendation", getRecommendation())
            .append("lastUpdate", getLastUpdate())
            .toString();
    }
}
