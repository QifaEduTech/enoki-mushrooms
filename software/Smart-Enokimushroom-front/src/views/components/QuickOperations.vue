<template>
  <el-card class="quick-operations">
    <div slot="header" class="clearfix">
      <span class="title">快捷操作</span>
    </div>
    <div>
      <el-row :gutter="20">
        <el-col :span="4" v-for="(operation, index) in operations" :key="index" class="operation-item">
          <el-button class="op-btn" @click="handleOperation(operation)">
            <i :class="operation.icon" :style="{ color: operation.iconColor }"></i>
            <br>{{ operation.label }}
          </el-button>
        </el-col>
      </el-row>
    </div>
  </el-card>
</template>

<script>
export default {
  name: 'QuickOperations',
  data () {
    return {
      operations: [
        { label: '灌溉控制', icon: 'el-icon-s-grid', action: 'irrigationControl', iconColor: '#0f2c2c' },
        { label: '视频监控', icon: 'el-icon-video-camera', action: 'videoSurveillance', iconColor: '#0f2c2c' },
        { label: '数据报表', icon: 'el-icon-document', action: 'dataReport', iconColor: '#0f2c2c' },
        { label: '作物管理', icon: 'el-icon-s-management', action: 'cropManagement', iconColor: '#0f2c2c' },
        { label: '设备管理', icon: 'el-icon-setting', action: 'deviceManagement', iconColor: '#0f2c2c' },
        { label: '更多功能', icon: 'el-icon-more', action: 'moreFeatures', iconColor: '#0f2c2c' }
      ]
    };
  },
  methods: {
    handleOperation (operation) {
      console.log(`Handling ${operation.action}`);
      // 在这里可以添加逻辑来处理不同的快捷操作
    }
  }
};
</script>

<style scoped>
.quick-operations {
  text-align: center;
  background: linear-gradient(180deg, #D2F3F1 0%, #D2F3F1 100%);
  border: none;
}

.operation-item {
  margin-bottom: 12px;
}

.op-btn {
  width: 100%;
  padding: 12px 20px;
  /* 调整内边距使得文字和图标有足够空间 */
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  border: 1px solid #D2F3F1;
  border-radius: 10px;
  font-size: 14px;
  color: #0f2c2c;
  background: linear-gradient(180deg, #F2FCFA 0%, #D2F3F1 100%);
  box-shadow: 0 2px 8px rgba(15, 44, 44, 0.06);
  transition: transform .2s ease, box-shadow .2s ease;
}

/* 设置图标的大小和间距 */
.op-btn i {
  font-size: 24px;
  margin-bottom: 8px;
}

/* 当鼠标悬停时稍微改变透明度，提供视觉反馈 */
.op-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(15, 44, 44, 0.10);
}

.title { font-weight: 600; font-size: 16px; color: #0f2c2c; }
</style>