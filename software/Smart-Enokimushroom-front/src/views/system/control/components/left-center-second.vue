<template>
  <div id="leftCenterFirst" style="width: 100%; height: 100%" ref="leftCenterFirst"></div>
</template>

<script>
import * as echarts from "echarts";

export default {
  name: "leftCenterFirst",
  props: ['list'],
  data () {
    return {
      chartInstance: null,
      newEst: ''
    };
  },
  mounted () {
    this.newEst = this.list[this.list.length - 1]
    this.chartInstance = echarts.init(this.$refs.leftCenterFirst);
    this.renderChart();
    // 监听窗口大小变化
    window.addEventListener("resize", this.handleResize);
  },
  beforeDestroy () {
    // 移除窗口大小变化监听器
    window.removeEventListener("resize", this.handleResize);
    // 销毁echarts实例
    if (this.chartInstance) {
      this.chartInstance.dispose();
    }
  },
  methods: {
    renderChart () {
      const option = {
        title: {
          text: "土壤温度",
          top: 10,
          left: 10,
          textStyle: {
            color: "#ffffff",
            fontSize: 16,
          },
        },
        series: [
          {
            type: "gauge",
            progress: {
              show: true,
              width: 10,
            },
            axisLine: {
              lineStyle: {
                width: 10,
              },
            },
            axisTick: {
              show: false,
            },
            splitLine: {
              length: 6,
              lineStyle: {
                width: 2,
                color: "#999",
              },
            },
            axisLabel: {
              distance: 10,
              color: "#999",
              fontSize: 14,
            },
            anchor: {
              show: false,
            },
            title: {
              show: false,
            },
            detail: {
              valueAnimation: true,
              formatter: '{value}%',
              color: '#ffffff',
              fontSize: 24,
              offsetCenter: [0, "70%"],
            },
            data: [
              {
                value: this.newEst.soilTemperature,
              },
            ],
          },
        ],
      };
      this.chartInstance.setOption(option);
    },
    handleResize () {
      // 监听到窗口大小变化时重新设置图表大小
      this.chartInstance.resize();
    },
  },
};
</script>

<style>
.chart-container {
  width: 100%;
  height: 400px;
  /* 初始高度，可以根据需要调整 */
}
</style>