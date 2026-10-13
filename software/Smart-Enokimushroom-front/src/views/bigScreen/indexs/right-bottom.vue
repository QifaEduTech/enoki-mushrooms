<template>
<div v-if="pageflag" class="records-scroll">
    <vue-seamless-scroll :data="deviceLogList" :class-option="defaultOption">
        <div class="record-row" v-for="(item, i) in deviceLogList" :key="i">
            <div class="record-index">{{ String(i + 1).padStart(2, '0') }}</div>
            <div class="record-grid">
                <div class="record-cell">
                    <span class="cell-label">记录ID</span>
                    <span class="cell-value accent">{{ item.recordId }}</span>
                </div>
                <div class="record-cell">
                    <span class="cell-label">空气温度</span>
                    <span class="cell-value">{{ item.airTemperature }}°C</span>
                </div>
                <div class="record-cell">
                    <span class="cell-label">空气湿度</span>
                    <span class="cell-value">{{ item.airHumidity }}%</span>
                </div>
                <div class="record-cell">
                    <span class="cell-label">土壤温度</span>
                    <span class="cell-value">{{ item.soilTemperature }}°C</span>
                </div>
                <div class="record-cell">
                    <span class="cell-label">土壤湿度</span>
                    <span class="cell-value">{{ item.soilHumidity }}%</span>
                </div>
                <div class="record-cell">
                    <span class="cell-label">光照强度</span>
                    <span class="cell-value">{{ item.lightLux || item.lightIntensity }} Lux</span>
                </div>
                <div class="record-cell">
                    <span class="cell-label">二氧化碳</span>
                    <span class="cell-value">{{ item.airCo2 || item.co2 }} ppm</span>
                </div>
                <div class="record-cell">
                    <span class="cell-label">土壤PH</span>
                    <span class="cell-value">{{ item.soilPh }}</span>
                </div>
            </div>
        </div>
    </vue-seamless-scroll>
</div>
<Reacquire v-else @onclick="refresh" style="line-height:200px;color:#23cdd8;margin-left:210px;">
    重新获取
</Reacquire>
</template>

<script>
import vueSeamlessScroll from "vue-seamless-scroll";

export default {
    name: "RightBottom",
    components: { vueSeamlessScroll },
    props: ["list"],
    data() {
        return {
            deviceLogList: [],
            pageflag: true,
            defaultOption: {
                step: 1.5,
                limitMoveNum: 4,
                hoverStop: true,
                singleHeight: 280,
                openWatch: true,
                waitTime: 3000,
            },
        };
    },
    created() {
        this.updateLogs();
    },
    watch: {
        list: {
            handler() {
                this.updateLogs();
            },
            deep: true,
        },
    },
    methods: {
        updateLogs() {
            if (!this.list || this.list.length === 0) {
                this.pageflag = false;
                this.deviceLogList = [];
                return;
            }
            this.pageflag = true;
            this.deviceLogList = this.list.slice(-50).map((i) => ({
                recordId: i.recordId,
                airTemperature: i.airTemperature,
                airHumidity: i.airHumidity,
                soilTemperature: i.soilTemperature,
                soilHumidity: i.soilHumidity,
                lightLux: i.lightLux || i.lightIntensity,
                airCo2: i.airCo2 || i.co2,
                soilPh: i.soilPh,
            }));
        },
        refresh() {
            this.updateLogs();
        },
    },
};
</script>

<style lang="scss" scoped>
.records-scroll {
    width: 100%;
    height: 100%;
    overflow: hidden;
    font-size: 12px;
}

.record-row {
    display: flex;
    align-items: center;
    padding: 8px 4px;
    border-bottom: 1px solid rgba(0, 114, 255, 0.08);
    transition: background 0.2s;

    &:hover {
        background: rgba(0, 234, 255, 0.04);
    }

    &:last-child {
        border-bottom: none;
    }
}

.record-index {
    width: 32px;
    height: 24px;
    line-height: 24px;
    text-align: center;
    background: rgba(0, 234, 255, 0.1);
    border: 1px solid rgba(0, 234, 255, 0.2);
    border-radius: 4px;
    color: #00eaff;
    font-size: 12px;
    font-weight: 700;
    flex-shrink: 0;
    margin-right: 10px;
}

.record-grid {
    display: flex;
    flex-wrap: wrap;
    flex: 1;
    gap: 2px 0;
}

.record-cell {
    width: 25%;
    display: flex;
    align-items: center;
    gap: 4px;
    padding: 2px 0;

    .cell-label {
        color: rgba(255, 255, 255, 0.4);
        font-size: 11px;
        white-space: nowrap;
    }

    .cell-value {
        color: #e8f0fe;
        font-size: 12px;
        font-weight: 500;
        font-variant-numeric: tabular-nums;

        &.accent {
            color: #00eaff;
            font-weight: 700;
        }
    }
}
</style>
