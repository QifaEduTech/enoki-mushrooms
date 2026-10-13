<template>
  <el-card class="site-area">
    <div slot="header" class="clearfix">
      <span>场地总面积</span>
    </div>
    <div>
      <p>{{ totalArea }}亩</p>
      <el-progress :percentage="ratio" color="#67c23a"></el-progress>
    </div>
  </el-card>
</template>

<script>
import { listManageByCount } from "@/api/system/area";
export default {
  name: 'SiteArea',
  data () {
    return {
      // 遮罩层
      totalArea: null,
      ratio: 0,
    };
  },
  mounted () {
    this.getAreaUser();
  },
  methods: {
    getAreaUser () {
      listManageByCount().then(response => {
        if (response.code === 200) {
          this.totalArea = response.rows[0].totalArea;
          this.ratio = response.rows[0].normalArea / response.rows[0].totalArea * 100;
        } else {
          this.$message.error("读取场地统计数据错误！");
        }
      });
    }
  }
}
</script>

<style scoped>
.site-area {
  text-align: center;
}
</style>