<template>
  <el-card class="real-time-data">
    <div slot="header" class="clearfix">
      <span>实时环境数据</span>
    </div>
    <el-row :gutter="20">
      <el-col :span="12">
        <el-card class="chart-card">
          <div slot="header" class="chart-header">
            <i class="el-icon-temperature-rise"></i>
            <span>温度</span>
            <span class="value">{{ airTemper }}°C</span>
          </div>
          <div ref="temperatureChart" class="chart"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="chart-card">
          <div slot="header" class="chart-header">
            <i class="el-icon-water-drop"></i>
            <span>湿度</span>
            <span class="value">{{ airHum }}%</span>
          </div>
          <div ref="humidityChart" class="chart"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="chart-card">
          <div slot="header" class="chart-header">
            <i class="el-icon-sunny"></i>
            <span>光照</span>
            <span class="value">{{ light }} klx</span>
          </div>
          <div ref="lightChart" class="chart"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="chart-card">
          <div slot="header" class="chart-header">
            <i class="el-icon-grape"></i>
            <span>土壤水分</span>
            <span class="value">{{ soilHum }}%</span>
          </div>
          <div ref="soilMoistureChart" class="chart"></div>
        </el-card>
      </el-col>
    </el-row>
  </el-card>
</template>

<script>
import * as echarts from 'echarts';
import { listRecord } from "@/api/system/record";

export default {
  name: 'RealTimeData',
  data () {
    return {
      temperatureData: [],
      humidityData: [],
      lightData: [],
      soilMoistureData: [],
      timeDate: [],
      timeDate2: [],
      recordList: [],
      queryParams: {
        pageNum: 1,
        pageSize: 30,
      },
      airTemper: 0,
      airHum: 0,
      light: 0,
      soilHum: 0,

    };
  },
  mounted () {
    this.getList();

  },
  methods: {
    /** 查询【请填写功能名称】列表 */
    getList () {
      listRecord(this.queryParams).then(response => {
        if (response.code === 200) {
          this.recordList = response.rows;
          //console.log(this.recordList);
          //构建数据
          this.extractSensorData();
          this.formatTimeToHMS();
          this.calculateAverage();
          //画图
          this.initTemperatureChart();
          this.initHumidityChart();
          this.initLightChart();
          this.initSoilMoistureChart();
        } else {
          this.$message.error("读取生长环境数据错误！");
        }

      });
    },
    // 提取数据到独立数组
    extractSensorData () {
      this.temperatureData = this.recordList.map(item => item.airTemperature);

      this.humidityData = this.recordList.map(item => item.airHumidity);

      this.lightData = this.recordList.map(item => item.lightLux);

      this.soilMoistureData = this.recordList.map(item => item.soilHumidity);

      this.timeDate = this.recordList.map(item => item.createDate);

    },
    //时间格式转换
    formatTimeToHMS () {
      this.timeDate2 = this.timeDate.map(timeStr => timeStr.slice(11, 19)); // 取出 HH:mm:ss
    },
    calculateAverage () {
      const list = this.recordList;
      const len = list.length;

      if (len === 0) {
        // 防止空数组导致除以 0
        this.airTemper = 0;
        this.airHum = 0;
        this.light = 0;
        this.soilHum = 0;
        return;
      }

      this.airTemper = Math.round(
        list.reduce((sum, item) => sum + (item.airTemperature || 0), 0) / len
      );

      this.airHum = Math.round(
        list.reduce((sum, item) => sum + (item.airHumidity || 0), 0) / len
      );

      this.light = Math.round(
        list.reduce((sum, item) => sum + (item.lightLux || 0), 0) / len
      );

      this.soilHum = Math.round(
        list.reduce((sum, item) => sum + (item.soilHumidity || 0), 0) / len
      );
    },
    initTemperatureChart () {
      const chart = echarts.init(this.$refs.temperatureChart);
      const option = {
        title: { text: '' },
        tooltip: { trigger: 'axis' },
        xAxis: { data: this.timeDate2 },
        yAxis: {},
        series: [{
          name: '温度',
          type: 'line',
          data: this.temperatureData,
          smooth: true,
          itemStyle: { color: '#FF6384' }
        }]
      };
      chart.setOption(option);
    },
    initHumidityChart () {
      const chart = echarts.init(this.$refs.humidityChart);
      const option = {
        title: { text: '' },
        tooltip: { trigger: 'axis' },
        xAxis: { data: this.timeDate2 },
        yAxis: {},
        series: [{
          name: '湿度',
          type: 'line',
          data: this.humidityData,
          smooth: true,
          itemStyle: { color: '#409EFF' }
        }]
      };
      chart.setOption(option);
    },
    initLightChart () {
      const chart = echarts.init(this.$refs.lightChart);
      const option = {
        title: { text: '' },
        tooltip: { trigger: 'axis' },
        xAxis: { data: this.timeDate2 },
        yAxis: {},
        series: [{
          name: '光照',
          type: 'line',
          data: this.lightData,
          smooth: true,
          itemStyle: { color: '#E6A23C' }
        }]
      };
      chart.setOption(option);
    },
    initSoilMoistureChart () {
      const chart = echarts.init(this.$refs.soilMoistureChart);
      const option = {
        title: { text: '' },
        tooltip: { trigger: 'axis' },
        xAxis: { data: this.timeDate2 },
        yAxis: {},
        series: [{
          name: '土壤水分',
          type: 'line',
          data: this.soilMoistureData,
          smooth: true,
          itemStyle: { color: '#67C23A' }
        }]
      };
      chart.setOption(option);
    }
  }
};
</script>
<style scoped>
.real-time-data {
  text-align: center;
}

.chart-card {
  margin-bottom: 0px;
}

.chart-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.chart-header i {
  font-size: 20px;
  margin-right: 0px;
}

.chart-header .value {
  font-weight: bold;
}

.chart {
  height: 110px;
}
</style>