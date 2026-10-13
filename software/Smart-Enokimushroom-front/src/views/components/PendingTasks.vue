<template>
  <el-card class="pending-tasks">
    <div slot="header" class="clearfix">
      <span>待处理任务</span>
      <el-button style="float: right; padding: 3px 0" type="text">查看全部</el-button>
    </div>
    <div>
      <!-- 任务列表 -->
      <el-row :gutter="20" v-for="(task, index) in tasks" :key="index" class="task-item">
        <el-col :span="24">
          <div class="task-title">{{ task.title }}</div>
          <div class="task-time">{{ task.time }}</div>
          <el-tag :type="task.priority === '高优先' ? 'danger' : task.priority === '中优先' ? 'warning' : ''" size="small"
            style="float: right;">
            {{ task.priority }}
          </el-tag>
        </el-col>
      </el-row>
    </div>
  </el-card>
</template>

<script>
export default {
  name: 'PendingTasks',
  data () {
    return {
      tasks: [
        { title: '南区水稻田灌溉', time: '今日 14:00', priority: '高优先' },
        { title: '北区种植园施肥', time: '今日 16:30', priority: '中优先' },
        { title: '温室蔬菜病虫害防治', time: '明日 09:00', priority: '高优先' },
        { title: '设备维护检查', time: '明日 13:30', priority: '中优先' },
        { title: '西区玉米田收割', time: '后天 08:00', priority: '高优先' }
      ]
    };
  }
};
</script>

<style scoped>
.pending-tasks {
  text-align: left;
}

.task-item {
  margin-bottom: 0px;
  border-bottom: 1px solid #f0f0f0;
  padding-bottom: 0px;
}

.task-title {
  font-size: 16px;
  margin-bottom: 5px;
}

.task-time {
  color: #999;
}
</style>