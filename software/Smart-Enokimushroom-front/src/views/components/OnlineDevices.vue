<template>
  <el-card class="online-devices">
    <div slot="header" class="clearfix">
      <span>今日在线设备</span>
    </div>
    <div>
      <p>{{ activeDevices }}/{{ totalDevices }}台</p>
      <el-progress :percentage="ratio" color="#409eff"></el-progress>
    </div>
  </el-card>
</template>

<script>
import { listManageByCount } from "@/api/system/device";

export default {
  name: 'OnlineDevices',
  data () {
    return {

      totalDevices: null,
      activeDevices: null,
      ratio: 0,
    };
  },
  mounted () {
    this.getDeviceSum();
  },
  methods: {
    getDeviceSum () {
      listManageByCount().then(response => {
        if (response.code === 200) {
          this.totalDevices = response.rows[0].totalDevices;
          this.activeDevices = response.rows[0].activeDevices;
          this.ratio = response.rows[0].activeDevices / response.rows[0].totalDevices * 100;
        } else {
          this.$message.error("读取设备统计数据错误！");
        }
      });
    }
  }
}
</script>

<style scoped>
.online-devices {
  text-align: center;
}
</style>