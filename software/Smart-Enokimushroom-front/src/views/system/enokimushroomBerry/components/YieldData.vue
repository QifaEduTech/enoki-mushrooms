<template>
  <div class="yield-data">
    <h3>产量数据</h3>
    <!-- 使用 div 替代 canvas -->
    <div ref="chartContainer" style="width: 100%; height: 400px;"></div>
    <div class="stats">
      <div class="stat-item" :style="{ backgroundColor: '#ff6b6b' }">
        <h4>单株平均产量：</h4>
        <p>{{ avgYield }}kg</p>
      </div>
      <div class="stat-item" :style="{ backgroundColor: '#8250db' }">
        <h4>预计本月产量：</h4>
        <p>{{ estimatedYield }}kg</p>
      </div>
      <div class="stat-item" :style="{ backgroundColor: '#1abc9c' }">
        <h4>成熟株数占比：</h4>
        <p>{{ maturityRate }}%</p>
      </div>
    </div>
  </div>
</template>

<script>
import * as echarts from 'echarts';

export default {
  data () {
    return {
      avgYield: 0.82,
      estimatedYield: 92,
      maturityRate: 62,
      chart: null, // 存储 ECharts 实例
    };
  },
  mounted () {
    this.initChart();
  },
  beforeDestroy () {
    // 在组件销毁前销毁 ECharts 实例，避免内存泄漏
    if (this.chart) {
      this.chart.dispose();
    }
  },
  methods: {
    initChart () {
      // 初始化 ECharts 实例
      this.chart = echarts.init(this.$refs.chartContainer);

      // 配置图表选项
      const option = {
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'shadow',
          },
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          containLabel: true,
        },
        xAxis: [
          {
            type: 'category',
            data: ['3月', '4月', '5月', '6月', '7月'],
            axisTick: {
              alignWithLabel: true,
            },
          },
        ],
        yAxis: [
          {
            type: 'value',
            name: '产量 (kg)',
            min: 0,
            position: 'left',
            axisLine: {
              show: true,
            },
            axisLabel: {
              formatter: '{value}',
            },
          },
        ],
        series: [
          {
            name: '产量预计',
            type: 'bar',
            barWidth: '60%',
            data: [5, 10, 15, 20, 25],
            itemStyle: {
              color: 'rgba(255, 99, 132, 0.2)',
              borderColor: 'rgba(255, 99, 132, 1)',
              borderWidth: 1,
            },
          },
        ],
      };

      // 使用配置项和数据显示图表
      this.chart.setOption(option);

      // 响应窗口大小变化
      window.addEventListener('resize', this.handleResize);
    },
    handleResize () {
      if (this.chart) {
        this.chart.resize();
      }
    },
  },
};
</script>

<style scoped>
.yield-data {
  padding: 20px;
  border-radius: 10px;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.stats {
  display: flex;
  justify-content: space-around;
  margin-top: 20px;
}

.stat-item {
  width: 150px;
  height: 150px;
  border-radius: 50%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  color: white;
  font-size: 18px;
  text-align: center;
}

.stat-item h4 {
  margin: 0;
  font-size: 16px;
}

.stat-item p {
  margin: 5px 0 0;
  font-size: 24px;
  font-weight: bold;
}
</style>