<template>
  <el-card class="system-notifications">
    <div slot="header" class="clearfix">
      <span>系统通知与预警</span>
      <el-button style="float: right; padding: 3px 0" type="text" @click="markAllAsRead">标记全部已读</el-button>
    </div>
    <div>
      <!-- 通知列表 -->
      <el-row :gutter="20" v-for="(notification, index) in notifications" :key="index" class="notification-item">
        <el-col :span="2">
          <i :class="notification.icon" :style="{ color: notification.color }"></i>
        </el-col>
        <el-col :span="18">
          <div class="notification-title">{{ notification.title }}</div>
          <div class="notification-time">{{ notification.time }}</div>
        </el-col>
        <el-col :span="4">
          <el-dropdown>
            <span class="el-dropdown-link">
              ...
            </span>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item>查看详情</el-dropdown-item>
              <el-dropdown-item>标记为已读</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </el-col>
      </el-row>
    </div>
  </el-card>
</template>

<script>
export default {
  name: 'SystemNotifications',
  data () {
    return {
      notifications: [
        { title: '西区土壤湿度过低', time: '10分钟前', icon: 'el-icon-warning', color: '#E6A23C' },
        { title: '温室3号设备离线', time: '30分钟前', icon: 'el-icon-error', color: '#F56C6C' },
        { title: '系统更新完成', time: '1小时前', icon: 'el-icon-info', color: '#909399' },
        { title: '南区监测数据异常', time: '2小时前', icon: 'el-icon-warning', color: '#E6A23C' },
        { title: '北区灌溉系统完成维护', time: '3小时前', icon: 'el-icon-success', color: '#67C23A' }
      ]
    };
  },
  methods: {
    markAllAsRead () {
      // 在这里可以添加逻辑来标记所有通知为已读
      console.log('Mark all as read');
    }
  }
};
</script>

<style scoped>
.system-notifications {
  text-align: left;
}

.notification-item {
  margin-bottom: 15px;
  border-bottom: 1px solid #f0f0f0;
  padding-bottom: 10px;
}

.notification-title {
  font-size: 16px;
  margin-bottom: 5px;
}

.notification-time {
  color: #999;
}

.el-dropdown-link {
  cursor: pointer;
  color: #999;
}
</style>