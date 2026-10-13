<template>
<div ref="statsChart" style="width: 100%; height: 100%"></div>
</template>

<script>
export default {
    name: "LeftCenter",
    props: ["list"],
    data() {
        return {
            chart: null,
        };
    },
    mounted() {
        this.chart = this.$echarts.init(this.$refs.statsChart);
        this.draw();
        window.addEventListener("resize", this.handleResize);
    },
    beforeDestroy() {
        window.removeEventListener("resize", this.handleResize);
        if (this.chart) this.chart.dispose();
    },
    watch: {
        list: {
            handler() {
                this.draw();
            },
            deep: true,
        },
    },
    methods: {
        latest() {
            if (!this.list || this.list.length === 0) return {};
            return this.list[this.list.length - 1] || {};
        },
        draw() {
            const cur = this.latest();
            if (!cur || Object.keys(cur).length === 0) return;

            const gaugeStyle = {
                axisLine: {
                    lineStyle: {
                        width: 12,
                        color: [
                            [0.3, "#67e0e3"],
                            [0.7, "#00eaff"],
                            [1, "#ff6b6b"],
                        ],
                    },
                },
                axisTick: { show: false },
                splitLine: { length: 8, lineStyle: { width: 1, color: "rgba(255,255,255,0.2)" } },
                axisLabel: { distance: 16, color: "rgba(255,255,255,0.5)", fontSize: 10 },
                pointer: { width: 4, length: "60%", itemStyle: { color: "#00eaff" } },
                anchor: { show: true, size: 10, itemStyle: { borderColor: "#00eaff", borderWidth: 2, color: "#0a1a3d" } },
                detail: {
                    fontSize: 16,
                    fontWeight: 700,
                    color: "#00eaff",
                    offsetCenter: [0, "65%"],
                    formatter: function(v) { return v + "°C"; },
                },
                title: {
                    show: true,
                    color: "rgba(255,255,255,0.7)",
                    fontSize: 12,
                    offsetCenter: [0, "85%"],
                },
            };

            const gaugeStyle2 = {
                ...gaugeStyle,
                detail: {
                    ...gaugeStyle.detail,
                    formatter: function(v) { return v + "%"; },
                },
            };

            const option = {
                series: [
                    {
                        type: "gauge",
                        radius: "42%",
                        center: ["25%", "50%"],
                        ...gaugeStyle,
                        data: [{ value: cur.soilTemperature || 0, name: "土壤温度" }],
                    },
                    {
                        type: "gauge",
                        radius: "42%",
                        center: ["75%", "50%"],
                        ...gaugeStyle2,
                        data: [{ value: cur.airHumidity || 0, name: "空气湿度" }],
                    },
                ],
            };
            this.chart.setOption(option, true);
        },
        handleResize() {
            this.chart && this.chart.resize();
        },
    },
};
</script>
