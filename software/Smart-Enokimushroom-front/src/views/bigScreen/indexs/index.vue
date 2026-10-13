<template>
<div class="contents">
    <div class="contetn_left">
        <div class="pagetab">
            <dv-decoration-12 style="width:28px;height:28px;" />
            <div class="item">实时监测数据</div>
        </div>
        <ItemWrap class="contetn_left-top contetn_lr-item" title="设备总览">
            <LeftTop :list="jkxxList" v-if="jkxxList.length !== 0" />
        </ItemWrap>
        <ItemWrap class="contetn_left-center contetn_lr-item" title="环境指标概览">
            <LeftCenter :list="jkxxList" v-if="jkxxList.length !== 0" />
        </ItemWrap>
        <ItemWrap class="contetn_left-bottom contetn_lr-item" title="温度时序">
            <LeftBottom :list="jkxxList" v-if="jkxxList.length !== 0" />
        </ItemWrap>
    </div>
    <div class="contetn_center">
        <CenterMap class="contetn_center_top" />
        <ItemWrap class="contetn_center-bottom" title="服务器信息">
            <CenterBottom />
        </ItemWrap>
    </div>
    <div class="contetn_right">
        <ItemWrap class="contetn_right-top contetn_lr-item" title="湿度时序">
            <RightTop :list="jkxxList" v-if="jkxxList.length !== 0" />
        </ItemWrap>
        <ItemWrap class="contetn_right-center contetn_lr-item" title="光照与CO₂">
            <RightCenter :list="jkxxList" v-if="jkxxList.length !== 0" />
        </ItemWrap>
        <ItemWrap class="contetn_right-bottom contetn_lr-item" title="最新记录">
            <RightBottom :list="jkxxList" v-if="jkxxList.length !== 0" />
        </ItemWrap>
    </div>
</div>
</template>

<script>
import LeftTop from './left-top.vue'
import LeftCenter from "./left-center.vue";
import LeftBottom from "./left-bottom.vue";
import CenterMap from "./center-map.vue";
import CenterBottom from "./center-bottom.vue";
import RightTop from "./right-top.vue";
import RightCenter from "./right-center.vue";
import RightBottom from "./right-bottom.vue";
import { listRecord } from "@/api/system/record";

export default {
    components: {
        LeftTop,
        LeftCenter,
        LeftBottom,
        CenterMap,
        RightTop,
        RightCenter,
        RightBottom,
        CenterBottom,
    },
    data() {
        return {
            queryParams: {
                pageNum: 1,
                pageSize: 100,
            },
            jkxxList: [],
            refreshTimer: null,
            refreshInterval: 2000,
        };
    },
    filters: {
        numsFilter(msg) {
            return msg || 0;
        },
    },
    created() {
        this.getList();
    },
    mounted() {
        this.startAutoRefresh();
    },
    beforeDestroy() {
        this.stopAutoRefresh();
    },
    methods: {
        getList() {
            listRecord(this.queryParams).then(response => {
                if (response && response.rows) {
                    this.jkxxList = response.rows;
                }
            }).catch(() => {});
        },
        startAutoRefresh() {
            this.refreshTimer = setInterval(() => {
                this.getList();
            }, this.refreshInterval);
        },
        stopAutoRefresh() {
            if (this.refreshTimer) {
                clearInterval(this.refreshTimer);
                this.refreshTimer = null;
            }
        },
    },
};
</script>

<style lang="scss" scoped>
.contents {
    display: flex;
    gap: 10px;
    height: calc(100% - 76px);

    .contetn_left,
    .contetn_right {
        width: 500px;
        box-sizing: border-box;
        display: flex;
        flex-direction: column;
        justify-content: space-between;
        gap: 8px;
        position: relative;
    }

    .contetn_center {
        flex: 1;
        display: flex;
        flex-direction: column;
        justify-content: space-between;
        gap: 8px;
    }

    .contetn_lr-item {
        flex: 1;
        min-height: 0;
    }

    .contetn_center_top {
        flex: 1;
        min-height: 0;
    }

    .contetn_center-bottom {
        height: 260px;
        flex-shrink: 0;
    }

    // 左侧标签
    .contetn_left {
        .pagetab {
            position: absolute;
            top: -2px;
            left: 4px;
            display: flex;
            align-items: center;
            z-index: 2;

            .item {
                color: rgba(0, 234, 255, 0.85);
                font-size: 14px;
                font-weight: 500;
                letter-spacing: 1px;
                margin-left: 6px;
            }
        }
    }
}
</style>
