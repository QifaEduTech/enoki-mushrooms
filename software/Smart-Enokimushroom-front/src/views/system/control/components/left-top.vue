<template>
  <div id="leftTop" style="width: 100%; height: 100%" ref="leftTop"></div>
</template>

<script>
import * as echarts from "echarts";

export default {
  name: "leftTop",
  props: ['list'],
  data () {
    return {
      chartInstance: null,
    };
  },
  mounted () {
    //console.log(111, this.list);
    this.chartInstance = echarts.init(this.$refs.leftTop);
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
          text: "温度趋势分析",
          top: 10,
          left: 10,
          textStyle: {
            color: "#ffffff",
            fontSize: 16,
          },
        },
        tooltip: {
          trigger: 'axis'
        },
        grid: {
          left: "5%",
          right: "5%",
          top: "20%",
          bottom: "10%",
          containLabel: true,
        },
        legend: {
          bottom: "0%",
          //   itemWidth: 15,
          //   itemHeight: 15,
          textStyle: {
            color: "#ffffff",
            fontSize: 14,
          },
        },
        xAxis: {
          type: "category",
          axisLabel: {
            color: "#ffffff",
          },
          splitLine: {
            show: false,
          },
          data: this.list.map(item => item.recordId),
        },
        yAxis: {
          type: "value",
          name: "温度",
          nameLocation: 'middle',
          nameTextStyle: {
            color: "#ffffff"
          },
          axisLabel: {
            color: "#ffffff",
          },
          // splitLine: {
          //   show: tre,
          // },
        },
        series: [
          {
            name: "土壤温度",
            data: this.list.map(item => item.soilTemperature),
            type: "line",
          },
          {
            name: "空气温度",
            data: this.list.map(item => item.airTemperature),
            type: "line",
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