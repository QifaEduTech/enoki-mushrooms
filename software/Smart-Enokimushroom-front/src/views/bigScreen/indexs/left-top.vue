<template>
<div id="leftTopChart" style="width: 100%; height: 100%" ref="leftTop"></div>
</template>

<script>
import * as echarts from "echarts";

export default {
    name: "LeftTop",
    props: ["list"],
    data() {
        return {
            chartInstance: null,
        };
    },
    mounted() {
        this.chartInstance = echarts.init(this.$refs.leftTop);
        this.renderChart();
        window.addEventListener("resize", this.handleResize);
    },
    beforeDestroy() {
        window.removeEventListener("resize", this.handleResize);
        if (this.chartInstance) {
            this.chartInstance.dispose();
        }
    },
    watch: {
        list: {
            handler() {
                this.renderChart();
            },
            deep: true,
        },
    },
    methods: {
        renderChart() {
            if (!this.list || this.list.length === 0) return;
            const option = {
                tooltip: {
                    trigger: "axis",
                    backgroundColor: "rgba(6, 30, 65, 0.9)",
                    borderColor: "rgba(0, 234, 255, 0.3)",
                    borderWidth: 1,
                    textStyle: { color: "#e8f0fe", fontSize: 13 },
                    axisPointer: {
                        type: "cross",
                        crossStyle: { color: "rgba(0, 234, 255, 0.3)" },
                        lineStyle: { color: "rgba(0, 234, 255, 0.2)", type: "dashed" },
                    },
                },
                grid: {
                    left: "8%",
                    right: "5%",
                    top: "18%",
                    bottom: "15%",
                    containLabel: true,
                },
                legend: {
                    top: 6,
                    right: 10,
                    textStyle: { color: "rgba(255,255,255,0.7)", fontSize: 12 },
                    itemWidth: 16,
                    itemHeight: 8,
                    itemGap: 20,
                },
                xAxis: {
                    type: "category",
                    axisLabel: {
                        color: "rgba(255,255,255,0.5)",
                        fontSize: 11,
                        interval: 'auto',
                    },
                    axisLine: { lineStyle: { color: "rgba(0, 114, 255, 0.2)" } },
                    axisTick: { show: false },
                    splitLine: { show: false },
                    data: this.list.map((item) => item.recordId),
                },
                yAxis: {
                    type: "value",
                    name: "温度(°C)",
                    nameTextStyle: { color: "rgba(255,255,255,0.6)", fontSize: 11, padding: [0, 0, 0, -20] },
                    axisLabel: { color: "rgba(255,255,255,0.5)", fontSize: 11 },
                    axisLine: { show: false },
                    axisTick: { show: false },
                    splitLine: { lineStyle: { color: "rgba(0, 114, 255, 0.08)", type: "dashed" } },
                },
                series: [
                    {
                        name: "土壤温度",
                        data: this.list.map((item) => item.soilTemperature),
                        type: "line",
                        smooth: true,
                        symbol: "circle",
                        symbolSize: 6,
                        showSymbol: false,
                        lineStyle: { width: 2.5, color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
                            { offset: 0, color: "#0072ff" },
                            { offset: 1, color: "#00eaff" },
                        ])},
                        itemStyle: { color: "#00eaff" },
                        areaStyle: {
                            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                                { offset: 0, color: "rgba(0, 234, 255, 0.25)" },
                                { offset: 1, color: "rgba(0, 234, 255, 0.02)" },
                            ]),
                        },
                    },
                    {
                        name: "空气温度",
                        data: this.list.map((item) => item.airTemperature),
                        type: "line",
                        smooth: true,
                        symbol: "circle",
                        symbolSize: 6,
                        showSymbol: false,
                        lineStyle: { width: 2.5, color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
                            { offset: 0, color: "#ff6b6b" },
                            { offset: 1, color: "#ffa07a" },
                        ])},
                        itemStyle: { color: "#ffa07a" },
                        areaStyle: {
                            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                                { offset: 0, color: "rgba(255, 107, 107, 0.2)" },
                                { offset: 1, color: "rgba(255, 107, 107, 0.02)" },
                            ]),
                        },
                    },
                ],
            };
            this.chartInstance.setOption(option);
        },
        handleResize() {
            this.chartInstance && this.chartInstance.resize();
        },
    },
};
</script>
