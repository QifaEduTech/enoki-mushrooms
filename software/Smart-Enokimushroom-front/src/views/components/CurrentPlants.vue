<template>
  <el-card class="current-plants">
    <div slot="header" class="clearfix">
      <span>当前在种植物{{ categary }} 种</span>
    </div>
    <el-row :gutter="20" class="plant-content" type="flex" align="top">
      <!-- 左侧：ECharts 图表 -->
      <el-col :span="14" class="chart-container">
        <div ref="chart" class="echart"></div>
      </el-col>

      <!-- 右侧：颜色标注（分两列） -->
      <el-col :span="10" class="legend-container">
        <el-row :gutter="10" class="legend-grid">
          <el-col :span="12" v-for="(item, index) in plantData" :key="index" class="legend-item">
            <span class="color-block" :style="{ backgroundColor: item.color }"></span>
            <span class="plant-name">{{ item.plantName }}</span>
          </el-col>
        </el-row>
      </el-col>
    </el-row>
  </el-card>
</template>

<script>
import * as echarts from 'echarts';
import { listManageByCount } from "@/api/system/plant";

export default {
  name: 'CurrentPlants',
  data () {
    return {
      plantData: [
        // { name: '金针菇', count: 10, color: '#FF6384' },
        // { name: '羊肚菌', count: 20, color: '#FFA07A' },
        // { name: '番茄', count: 15, color: '#FFD700' },
        // { name: '菠菜', count: 25, color: '#ADFF2F' },
        // { name: '黄秋葵', count: 10, color: '#00BFFF' },
        // { name: '水稻', count: 20, color: '#87CEFA' }
      ],
      categary: "",
    };
  },
  mounted () {
    this.getPlantByCount();
    // this.$nextTick(() => {
    //   this.initChart();
    // });
  },
  methods: {
    /** 查询植物列表 */
    getPlantByCount () {
      listManageByCount().then(response => {
        if (response.code === 200) {
          this.plantData = response.rows;
          this.categary = this.plantData.length;

          this.addColorToPlantData();
          this.initChart();
          // console.log(this.plantData);
        } else {
          this.$message.error("读取植物统计数据错误！");
        }
      });
    },
    // 假设在 methods 或 mounted 中执行
    addColorToPlantData () {
      const colorPool = ['#FF6384', '#FFA07A', '#FFD700', '#ADFF2F', '#00BFFF', '#87CEFA', '#FF69B4', '#20B2AA'];
      this.plantData = this.plantData.map((item, index) => {
        return {
          ...item, // 保留原有所有字段（name, count 等）
          color: colorPool[index % colorPool.length] // 循环使用颜色
        };
      });
    },
    initChart () {
      const chartDom = this.$refs.chart;
      const myChart = echarts.init(chartDom);
      const option = {
        tooltip: {
          trigger: 'item',
          formatter: '{a} <br/>{b}: {c} ({d}%)'
        },
        series: [
          {
            name: '作物分布',
            type: 'pie',
            radius: ['50%', '70%'], // 环形图效果，可选
            center: ['30%', '30%'],
            avoidLabelOverlap: false,
            label: {
              show: false
            },
            emphasis: {
              label: {
                show: true,
                fontSize: '14',
                fontWeight: 'bold'
              }
            },
            data: this.plantData.map(item => ({
              value: item.count,
              name: item.plantName,
              itemStyle: { color: item.color }
            }))
          }
        ]
      };
      myChart.setOption(option);

      // 响应窗口大小变化
      window.addEventListener('resize', () => myChart.resize());
      this.$once('hook:beforeDestroy', () => {
        window.removeEventListener('resize', () => myChart.resize());
        myChart.dispose();
      });
    }
  }
};
</script>

<style scoped>
.current-plants {
  text-align: left;
}

.plant-content {
  display: flex;
  align-items: flex-start;
  /* 顶部对齐 */
}

.chart-container {
  display: flex;
  justify-content: center;
  align-items: center;
  transform: translateY(15%);
}

.echart {
  width: 300px;
  height: 75px;
  /* margin: 0 auto; */
}

.legend-container {
  padding: 0 0;
}

.legend-grid {
  display: flex;
  flex-wrap: wrap;
}

.legend-item {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
  font-size: 13px;
  /* 缩小文字 */
  white-space: nowrap;
}

.color-block {
  display: inline-block;
  width: 10px;
  height: 10px;
  border-radius: 50%;
  margin-right: 6px;
}

.plant-name {
  font-size: 13px;
  color: #666;
}
</style>