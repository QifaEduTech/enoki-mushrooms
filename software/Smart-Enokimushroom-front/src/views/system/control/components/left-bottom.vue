<template>
  <div id="leftBottom" style="width: 100%; height: 100%" ref="leftBottom"></div>
</template>

<script>
import * as echarts from "echarts";

export default {
  name: "leftBottom",
  props: ['list'],
  data () {
    return {
      chartInstance: null,
      sevenList: '',
      newList: ''
    };
  },
  mounted () {
    if (this.list.length > 7) {
      this.sevenList = JSON.parse(JSON.stringify(this.list)).splice(-7)
    } else {
      this.sevenList = JSON.parse(JSON.stringify(this.list))
    }
    this.chartInstance = echarts.init(this.$refs.leftBottom);
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
          text: "7日温度分析-土壤与空气",
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
          data: this.sevenList.map(item => item.recordId),
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
            data: this.sevenList.map(item => item.soilTemperature),
            type: "line",
            smooth: true
          },
          // {
          //   name: "土壤",
          //   data: [5, 3, 5, 7, 3, 2, 4],
          //   type: "line",
          //   smooth:true
          // },
          // {
          //   name: "空气温度-最高",
          //   data: [5,  4, 5, 7, 3, 2, 4],
          //   type: "line",
          //   smooth:true
          // }, 
          {
            name: "空气温度",
            data: this.sevenList.map(item => item.airTemperature),
            type: "line",
            smooth: true
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