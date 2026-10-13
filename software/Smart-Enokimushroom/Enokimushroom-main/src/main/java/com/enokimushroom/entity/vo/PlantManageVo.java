package com.enokimushroom.entity.vo;

import java.math.BigDecimal;

public class PlantManageVo {
    private String plantName;     // 植物名称
    private Long count;        // 数量
    private BigDecimal ratio;     // 占比（百分比，如 40.00）

    public String getPlantName() {
        return plantName;
    }

    public void setPlantName(String plantName) {
        this.plantName = plantName;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    public BigDecimal getRatio() {
        return ratio;
    }

    public void setRatio(BigDecimal ratio) {
        this.ratio = ratio;
    }

    @Override
    public String toString() {
        return "PlantManageVo{" +
                "plantName='" + plantName + '\'' +
                ", count=" + count +
                ", ratio=" + ratio +
                '}';
    }
}
