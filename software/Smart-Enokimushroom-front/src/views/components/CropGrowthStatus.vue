<template>
  <el-card class="crop-growth-status">
    <div slot="header" class="clearfix">
      <span>作物生长状态</span>
      <el-button style="float: right; padding: 3px 0" type="text">查看详情</el-button>
    </div>
    <div>
      <!-- 生长状态列表 -->
      <el-row :gutter="20" v-for="(crop, index) in crops" :key="index" class="crop-item">
        <el-col :span="4">
          <img :src="crop.image" alt="Crop Image" class="crop-image">
        </el-col>
        <el-col :span="20">
          <div class="crop-name">{{ crop.name }}</div>
          <div class="crop-progress">生长进度 {{ crop.progress }}%</div>
          <el-progress :percentage="crop.progress" :color="customColors"></el-progress>
        </el-col>
      </el-row>
    </div>
  </el-card>
</template>

<script>
export default {
  name: 'CropGrowthStatus',
  data () {
    return {
      crops: [
        { name: '水稻', progress: 85, image: require('@/assets/images/shuidao.jpg') },
        { name: '金针菇', progress: 45, image: require('@/assets/images/straw.jpg') },
        { name: '羊肚菌', progress: 70, image: require('@/assets/images/yangdujun.jpg') },
        { name: '黄秋葵', progress: 60, image: require('@/assets/images/huagnqiukui.jpg') },
        { name: '蓝莓', progress: 60, image: require('@/assets/images/bluestraw.jpg') }
      ],

      customColors: [
        { color: '#f56c6c', percentage: 20 },
        { color: '#e6a23c', percentage: 40 },
        { color: '#5cb87a', percentage: 60 },
        { color: '#1989fa', percentage: 80 },
        { color: '#6f7ad3', percentage: 100 }
      ]
    };
  }
};
</script>

<style scoped>
.crop-growth-status {
  text-align: left;
}

.crop-item {
  margin-bottom: 10px;
}

.crop-image {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
}

.crop-name {
  font-size: 16px;
  margin-bottom: 5px;
}

.crop-progress {
  color: #999;
}
</style>