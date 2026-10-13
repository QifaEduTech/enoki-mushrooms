<template>
  <div class="home">
    <div class="home-content">
      <!-- 功能卡片行，改用 flex 布局支持 5 个等宽卡片 -->
      <div class="features-row">
        <div class="feature-card-wrapper">
          <el-card class="feature-card">
            <div class="feature-icon"><i class="el-icon-setting"></i></div>
            <h3>智能控制中心</h3>
            <p>设备联动、环境调控与策略执行</p>
            <div class="feature-actions">
              <el-button type="primary" size="mini" @click="goTo(['/analysis/smartContro','/system/controll'])">进入控制</el-button>
            </div>
          </el-card>
        </div>

        <div class="feature-card-wrapper">
          <el-card class="feature-card">
            <div class="feature-icon"><i class="el-icon-s-data"></i></div>
            <h3>智能病虫害分析</h3>
            <p>智能监控、病虫害解决与智能建议</p>
            <div class="feature-actions">
              <el-button type="primary" size="mini" @click="goTo(['/analysis/disease'])">进入生长</el-button>
            </div>
          </el-card>
        </div>

        <div class="feature-card-wrapper">
          <el-card class="feature-card">
            <div class="feature-icon"><i class="el-icon-chat-line-round"></i></div>
            <h3>RAG知识库</h3>
            <p>私有知识库管理与智能问答</p>
            <div class="feature-actions">
              <el-button type="primary" size="mini" @click="goTo(['/analysis/Chat'])">进入问答</el-button>
              <el-button size="mini" @click="goTo(['/analysis/professional'])">进入知识库</el-button>
            </div>
          </el-card>
        </div>

        <div class="feature-card-wrapper">
          <el-card class="feature-card">
            <div class="feature-icon"><i class="el-icon-monitor"></i></div>
            <h3>数据中心</h3>
            <p>时序数据、设备数据与分析报表</p>
            <div class="feature-actions">
              <el-button type="primary" size="mini" @click="goTo(['/record/contro'])">进入数据中心</el-button>
            </div>
          </el-card>
        </div>

        <!-- 新增产量预测卡片 -->
        <div class="feature-card-wrapper">
          <el-card class="feature-card">
            <div class="feature-icon"><i class="el-icon-data-line"></i></div>
            <h3>产量预测</h3>
            <p>基于环境数据与生长模型预测产量</p>
            <div class="feature-actions">
              <el-button type="primary" size="mini" @click="goTo(['/analysis/controll'])">进入预测</el-button>
            </div>
          </el-card>
        </div>
      </div>

      <!-- 天气与通知行 -->
      <el-row :gutter="32">
        <el-col :span="16">
          <WeatherForecast />
        </el-col>
        <el-col :span="8">
          <div class="stack">
            <Notifications />
            <PendingTasks />
          </div>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script>
import WeatherForecast from './components/WeatherForecast.vue';
import Notifications from './components/Notifications.vue';
import PendingTasks from './components/PendingTasks.vue';

export default {
  name: "Index",
  components: { WeatherForecast, Notifications, PendingTasks },
  data () {
    return {};
  },
  methods: {
    goTo(paths) {
      const list = Array.isArray(paths) ? paths : [paths];
      for (const p of list) {
        try {
          if (!p) continue; // 跳过空路径
          this.$router.push({ path: p });
          return;
        } catch (e) {
          // ignore and try next
        }
      }
      this.$message.info('目标功能暂不可用');
    }
  }
};
</script>

<style scoped lang="scss">
.home {
  min-height: 100%;
  padding: 40px 24px;
  background: radial-gradient(1200px 600px at 30% 0%, #e6f0ff 0%, #f0f7ff 40%, #ffffff 100%);
}

.home-content {
  max-width: 1400px;
  margin: 0 auto;
}

.home-content :deep(.el-row) + :deep(.el-row) {
  margin-top: 40px;
}

/* 功能卡片行：flex 布局，5 个等宽卡片 */
.features-row {
  display: flex;
  gap: 24px; /* 卡片间距 */
  margin-bottom: 40px;
}

.feature-card-wrapper {
  flex: 1; /* 等分宽度 */
  min-width: 0; /* 防止内容溢出 */
}

.feature-card { 
  text-align: center; 
  padding: 24px 16px 24px; 
  background: linear-gradient(135deg, #ffffff 0%, #f8fbff 100%);
  border: 1px solid rgba(33, 150, 243, 0.15);
  transition: all 0.3s ease;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}
.feature-card h3 { 
  color: #1976d2; 
  margin: 12px 0 8px; 
  font-weight: 600;
}
.feature-card p { 
  color: #64b5f6; 
  margin: 0 0 16px;
  font-size: 14px;
  line-height: 1.5;
}
.feature-card:hover { 
  box-shadow: 0 16px 32px rgba(33, 150, 243, 0.15); 
  transform: translateY(-4px);
  border-color: rgba(33, 150, 243, 0.3);
}
.feature-icon { 
  font-size: 40px; 
  color: #2196f3; 
  margin: 0 auto 16px;
  width: 72px;
  height: 72px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(33, 150, 243, 0.1);
  border-radius: 50%;
}
.feature-actions { 
  margin-top: auto;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.stack { 
  display: flex;
  flex-direction: column;
  gap: 24px;
  height: 100%;
}

.stack > * {
  flex: 1;
  min-height: 200px;
}

::v-deep(.el-card) {
  border-radius: 16px;
  box-shadow: 0 8px 24px rgba(33, 150, 243, 0.08);
  border: 1px solid rgba(33, 150, 243, 0.1);
  transition: all 0.3s ease;
}

::v-deep(.el-card:hover) {
  box-shadow: 0 12px 32px rgba(33, 150, 243, 0.12);
}

::v-deep(.el-button--primary) {
  background-color: #2196f3;
  border-color: #2196f3;
  &:hover {
    background-color: #1e88e5;
    border-color: #1e88e5;
  }
}

::v-deep(.el-button--default) {
  color: #2196f3;
  border-color: #bbdefb;
  &:hover {
    background-color: #e3f2fd;
    border-color: #90caf9;
  }
}

/* 响应式：小屏幕时卡片换行 */
@media (max-width: 768px) {
  .features-row {
    flex-wrap: wrap;
  }
  .feature-card-wrapper {
    flex: 1 1 calc(50% - 12px); /* 一行两个 */
    min-width: 200px;
  }
}
</style>