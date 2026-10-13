package com.enokimushroom.entity.vo;

import java.math.BigDecimal;

public class DeviceManageVo {
    private Long totalDevices;     // 总设备数
    private Long activeDevices;    // 正常使用设备数
    private BigDecimal usageRate;     // 使用率（百分比）

    public Long getTotalDevices() {
        return totalDevices;
    }

    public void setTotalDevices(Long totalDevices) {
        this.totalDevices = totalDevices;
    }

    public Long getActiveDevices() {
        return activeDevices;
    }

    public void setActiveDevices(Long activeDevices) {
        this.activeDevices = activeDevices;
    }

    public BigDecimal getUsageRate() {
        return usageRate;
    }

    public void setUsageRate(BigDecimal usageRate) {
        this.usageRate = usageRate;
    }

    @Override
    public String toString() {
        return "DeviceManageVo{" +
                "totalDevices=" + totalDevices +
                ", activeDevices=" + activeDevices +
                ", usageRate=" + usageRate +
                '}';
    }
}
