<template>
<div class="right-center-wrap">
    <div class="chart-area">
        <dv-active-ring-chart :config="config" style="width: 100%; height: 100%" />
    </div>
    <div class="info-area">
        <div class="info-item">
            <div class="info-icon">☀</div>
            <div class="info-detail">
                <div class="info-label">平均光照强度</div>
                <div class="info-value">{{ avgLight }} <span class="info-unit">Lux</span></div>
                <dv-decoration-3 style="width: 100%; height: 8px; margin-top: 4px" />
            </div>
        </div>
        <div class="info-item">
            <div class="info-icon co2">CO₂</div>
            <div class="info-detail">
                <div class="info-label">平均二氧化碳</div>
                <div class="info-value">{{ avgCo2 }} <span class="info-unit">ppm</span></div>
                <dv-decoration-3 style="width: 100%; height: 8px; margin-top: 4px" />
            </div>
        </div>
    </div>
</div>
</template>

<script>
export default {
    name: "RightCenter",
    props: ["list"],
    data() {
        return {
            config: {},
            avgLight: 0,
            avgCo2: 0,
        };
    },
    created() {
        this.updateConfig();
    },
    watch: {
        list: {
            handler() {
                this.updateConfig();
            },
            deep: true,
        },
    },
    methods: {
        updateConfig() {
            if (!this.list || this.list.length === 0) return;
            const lightArr = this.list.map((i) => i.lightLux || i.lightIntensity || 0);
            const co2Arr = this.list.map((i) => i.airCo2 || i.co2 || 0);
            const avg = (arr) => (arr.length ? Math.round(arr.reduce((a, b) => a + (Number(b) || 0), 0) / arr.length) : 0);
            this.avgLight = avg(lightArr);
            this.avgCo2 = avg(co2Arr);
            this.config = {
                data: [
                    { name: "光照强度(Lux)", value: this.avgLight },
                    { name: "二氧化碳(ppm)", value: this.avgCo2 },
                ],
                color: ["rgba(0, 234, 255, 0.8)", "rgba(255, 219, 92, 0.8)"],
                showValue: true,
                borderWidth: 1,
                borderGap: 4,
            };
        },
    },
};
</script>

<style lang="scss" scoped>
.right-center-wrap {
    display: flex;
    height: 100%;
    align-items: center;
    gap: 8px;
}

.chart-area {
    width: 200px;
    height: 200px;
    flex-shrink: 0;
}

.info-area {
    flex: 1;
    display: flex;
    flex-direction: column;
    justify-content: center;
    gap: 24px;
}

.info-item {
    display: flex;
    align-items: flex-start;
    gap: 12px;

    .info-icon {
        width: 36px;
        height: 36px;
        border-radius: 8px;
        background: rgba(0, 234, 255, 0.1);
        border: 1px solid rgba(0, 234, 255, 0.2);
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 16px;
        color: #00eaff;
        flex-shrink: 0;

        &.co2 {
            font-size: 11px;
            font-weight: 700;
            background: rgba(255, 219, 92, 0.1);
            border-color: rgba(255, 219, 92, 0.2);
            color: #ffdb5c;
        }
    }

    .info-detail {
        flex: 1;
        min-width: 0;

        .info-label {
            font-size: 12px;
            color: rgba(255, 255, 255, 0.5);
            margin-bottom: 4px;
        }

        .info-value {
            font-size: 22px;
            font-weight: 700;
            color: #e8f0fe;
            font-variant-numeric: tabular-nums;

            .info-unit {
                font-size: 12px;
                font-weight: 400;
                color: rgba(255, 255, 255, 0.4);
                margin-left: 4px;
            }
        }
    }
}
</style>
