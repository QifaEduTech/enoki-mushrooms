<template>
  <el-card class="notifications">
    <div slot="header" class="clearfix">
      <span>今日通知与预警 5条</span>
    </div>
    <div>
      <!-- <p>5条</p> -->
      <el-alert title="已处理：1条" type="info" show-icon></el-alert>
      <el-alert title="4条未处理" type="warning" show-icon></el-alert>
    </div>
  </el-card>
</template>

<script>
export default {
  name: 'Notifications'
}
</script>

<style scoped>
.notifications {
  text-align: center;
}
</style>