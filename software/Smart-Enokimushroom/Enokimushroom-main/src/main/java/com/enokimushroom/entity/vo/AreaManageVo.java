package com.enokimushroom.entity.vo;

import java.math.BigDecimal;

public class AreaManageVo {
    private Long totalArea;        // 总面积（亩）
    private Long normalArea;       // 正常状态面积
    private BigDecimal ratio;         // 占比（百分比）

    public Long getTotalArea() {
        return totalArea;
    }

    public void setTotalArea(Long totalArea) {
        this.totalArea = totalArea;
    }

    public Long getNormalArea() {
        return normalArea;
    }

    public void setNormalArea(Long normalArea) {
        this.normalArea = normalArea;
    }

    public BigDecimal getRatio() {
        return ratio;
    }

    public void setRatio(BigDecimal ratio) {
        this.ratio = ratio;
    }

    @Override
    public String toString() {
        return "AreaManageVo{" +
                "totalArea=" + totalArea +
                ", normalArea=" + normalArea +
                ", ratio=" + ratio +
                '}';
    }
}
