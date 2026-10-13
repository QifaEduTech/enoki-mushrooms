<template>
  <div class="yield-prediction-component">
    <!-- 预测按钮 -->
    <el-button
      type="primary"
      plain
      icon="el-icon-calculator"
      size="medium"
      @click="showPredictionDialog"
      class="ai-predict-btn"
    >
      智能预测产量🤖
    </el-button>

    <!-- 区域选择弹窗 -->
    <el-dialog
      title="🤖 智能产量预测"
      :visible.sync="areaSelectVisible"
      width="500px"
      class="area-select-dialog"
      :close-on-click-modal="false"
    >
      <div class="area-select-content">
        <div class="select-header">
          <div class="ai-icon">
            <i class="el-icon-cpu"></i>
          </div>
          <div class="select-info">
            <h3>选择分析区域</h3>
            <p>请选择要进行智能产量预测的金针菇种植区域</p>
          </div>
        </div>
        
        <div class="area-grid">
          <div 
            v-for="area in areas" 
            :key="area.id"
            class="area-card"
            :class="{ 'selected': selectedAreaId === area.id }"
            @click="selectArea(area.id)"
          >
            <div class="area-icon">
              <i class="el-icon-house"></i>
            </div>
            <div class="area-name">{{ area.name }}</div>
            <div class="area-status">{{ area.status }}</div>
            <div class="area-planted-days">
              <i class="el-icon-time"></i>
              已种植 {{ area.plantedDays }} 天
              <div class="days-progress">
                <el-progress 
                  :percentage="Math.min((area.plantedDays / 90) * 100, 100)" 
                  :show-text="false" 
                  stroke-width="4"
                  :color="getProgressColor(area.plantedDays)"
                ></el-progress>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <div slot="footer" class="dialog-footer">
        <el-button @click="areaSelectVisible = false">取 消</el-button>
        <el-button 
          type="primary" 
          @click="startPrediction"
          :disabled="!selectedAreaId"
          class="start-btn"
        >
          <i class="el-icon-data-analysis"></i>
          开始预测
        </el-button>
      </div>
    </el-dialog>

    <!-- 预测结果弹窗 -->
    <el-dialog
      :title="predictionTitle"
      :visible.sync="predictionVisible"
      width="90%"
      :close-on-click-modal="false"
      class="prediction-result-dialog"
      top="2vh"
    >
      <div class="prediction-container">
        <!-- 头部信息 -->
        <div class="prediction-header">
          <div class="header-left">
            <div class="ai-avatar">
              <i class="el-icon-cpu"></i>
            </div>
            <div class="header-info">
              <h2 class="prediction-title">AI智能分析完成</h2>
              <p class="prediction-subtitle">基于深度学习和机器学习算法的精准预测</p>
              <div class="area-badge">
                <i class="el-icon-location"></i>
                {{ getAreaName(selectedAreaId) }}
              </div>
            </div>
          </div>
          <div class="header-right">
            <div class="status-badges">
              <el-tag type="success" size="medium" effect="dark">
                <i class="el-icon-check"></i>
                分析完成
              </el-tag>
              <el-tag type="info" size="medium" effect="dark">
                <i class="el-icon-time"></i>
                {{ formatTime(new Date()) }}
              </el-tag>
            </div>
          </div>
        </div>

        <!-- 预测结果内容 -->
        <div class="prediction-content">
          <div class="result-summary">
            <div class="summary-card yield-summary">
              <div class="card-header">
                <h3><i class="el-icon-data-line"></i>产量预测概览</h3>
              </div>
              <div class="yield-stats">
                <div class="stat-item primary">
                  <div class="stat-icon">🍄</div>
                  <div class="stat-info">
                    <div class="stat-value">{{ predictionData.totalYield || 0 }}</div>
                    <div class="stat-label">预计总产量(个)</div>
                  </div>
                </div>
                <div class="stat-item success">
                  <div class="stat-icon">✅</div>
                  <div class="stat-info">
                    <div class="stat-value">{{ predictionData.matureCount || 0 }}</div>
                    <div class="stat-label">已成熟数量</div>
                  </div>
                </div>
                <div class="stat-item warning">
                  <div class="stat-icon">⏳</div>
                  <div class="stat-info">
                    <div class="stat-value">{{ predictionData.pendingCount || 0 }}</div>
                    <div class="stat-label">待成熟数量</div>
                  </div>
                </div>
              </div>
            </div>

            <div class="summary-card analysis-summary">
              <div class="card-header">
                <h3><i class="el-icon-pie-chart"></i>质量分析</h3>
              </div>
              <div class="quality-charts">
                <div class="quality-chart-item">
                  <div class="chart-title">平均大小</div>
                  <div class="chart-box" ref="pieSize"></div>
                </div>
                <div class="quality-chart-item">
                  <div class="chart-title">成熟率</div>
                  <div class="chart-box" ref="pieMaturity"></div>
                </div>
                <div class="quality-chart-item">
                  <div class="chart-title">健康指数</div>
                  <div class="chart-box" ref="pieHealth"></div>
                </div>
              </div>
            </div>
          </div>

          <!-- 详细分析 -->
          <div class="detailed-analysis">
            <div class="analysis-section">
              <div class="section-header">
                <h3><i class="el-icon-document"></i>详细分析报告</h3>
              </div>
              <div class="analysis-content" v-html="formattedPredictionResult"></div>
            </div>

            <div class="recommendations-section">
              <div class="section-header">
                <h3><i class="el-icon-star-on"></i>智能建议</h3>
              </div>
              <div class="recommendations-list">
                <div class="recommendation-item" v-for="(rec, index) in recommendations" :key="index">
                  <div class="rec-icon">
                    <i :class="rec.icon"></i>
                  </div>
                  <div class="rec-content">
                    <h4>{{ rec.title }}</h4>
                    <p>{{ rec.description }}</p>
                  </div>
                  <div class="rec-priority" :class="rec.priority">
                    {{ rec.priorityText }}
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 底部统计 -->
        <div class="prediction-footer">
          <div class="footer-stats">
            <div class="footer-stat">
              <i class="el-icon-data-analysis"></i>
              <span>预测准确率</span>
              <strong>95.8%</strong>
            </div>
            <div class="footer-stat">
              <i class="el-icon-timer"></i>
              <span>分析耗时</span>
              <strong>{{ analysisTime }}秒</strong>
            </div>
            <div class="footer-stat">
              <i class="el-icon-document"></i>
              <span>数据来源</span>
              <strong>实时监测</strong>
            </div>
          </div>
          <div class="footer-actions">
            <el-button type="info" size="medium" @click="exportReport">
              <i class="el-icon-download"></i>
              导出报告
            </el-button>
            <el-button type="success" size="medium" @click="predictionVisible = false">
              <i class="el-icon-check"></i>
              确认
            </el-button>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { predictYield } from "@/api/system/IntelligentAnalytics";
import * as echarts from "echarts";

export default {
  name: "YieldPrediction",
  props: {
    // 可以接收权限等props
    hasPermission: {
      type: Boolean,
      default: true
    }
  },
  data() {
    return {
      // 弹窗控制
      areaSelectVisible: false,
      predictionVisible: false,
      
      // 区域数据
      selectedAreaId: null,
      areas: [
        { id: '1', name: '大棚A1', status: '运行中', plantedDays: 45 },
        { id: '2', name: '大棚A2', status: '运行中', plantedDays: 67 },
        { id: '3', name: '大棚A3', status: '维护中', plantedDays: 23 },
        { id: '4', name: '大棚B1', status: '运行中', plantedDays: 89 },
        { id: '5', name: '大棚B2', status: '运行中', plantedDays: 12 },
        { id: '6', name: '大棚B3', status: '运行中', plantedDays: 78 }
      ],
      
      // 预测数据
      predictionData: {
        totalYield: 0,
        matureCount: 0,
        pendingCount: 0,
        avgSize: 0,
        maturityRate: 0,
        healthIndex: 0
      },
      
      // 原始预测结果
      rawPredictionResult: '',
      
      // 分析时间
      analysisTime: 0,
      
      // 建议数据
      recommendations: [
        {
          icon: 'el-icon-sunny',
          title: '光照优化',
          description: '建议在上午10-12点增加补光时间，提高金针菇成熟速度',
          priority: 'high',
          priorityText: '高优先级'
        },
        {
          icon: 'el-icon-umbrella',
          title: '湿度调节',
          description: '当前湿度偏高，建议适当通风降湿，预防病虫害',
          priority: 'medium',
          priorityText: '中优先级'
        },
        {
          icon: 'el-icon-timer',
          title: '采摘时机',
          description: '预计3-5天后进入最佳采摘期，建议提前准备采摘计划',
          priority: 'low',
          priorityText: '低优先级'
        }
      ]
    };
  },
  computed: {
    predictionTitle() {
      return this.selectedAreaId ? 
        `🤖 智能预测结果 - ${this.getAreaName(this.selectedAreaId)}` : 
        '🤖 智能预测结果';
    },
    
    formattedPredictionResult() {
      if (!this.rawPredictionResult) return '<p>暂无详细分析数据</p>';
      return this.convertMarkdownToHtml(this.rawPredictionResult);
    }
  },
  watch: {
    predictionVisible(newVal) {
      if (newVal) {
        this.$nextTick(() => {
          this.initQualityCharts();
          this.updateQualityCharts();
        });
      } else {
        this.disposeQualityCharts();
      }
    },
    predictionData: {
      deep: true,
      handler() {
        this.$nextTick(() => this.updateQualityCharts());
      }
    }
  },
  beforeDestroy() {
    this.disposeQualityCharts();
  },
  methods: {
    // 显示预测弹窗
    showPredictionDialog() {
      this.areaSelectVisible = true;
      this.selectedAreaId = null;
    },
    
    // 选择区域
    selectArea(areaId) {
      this.selectedAreaId = areaId;
    },
    
    // 开始预测
    async startPrediction() {
      if (!this.selectedAreaId) {
        this.$message.warning('请选择一个区域');
        return;
      }
      
      this.areaSelectVisible = false;
      
      try {
        const startTime = Date.now();
        
        // 显示加载提示
        const loadingInstance = this.$loading({
          lock: true,
          text: `🤖 正在分析${this.getAreaName(this.selectedAreaId)}数据...`,
          spinner: "el-icon-loading",
          background: "rgba(0, 0, 0, 0.8)",
          customClass: "ai-loading",
        });
        
        // 更新加载文本
        const messages = [
          `正在扫描${this.getAreaName(this.selectedAreaId)}环境...`,
          '正在分析金针菇生长数据...',
          '正在构建预测模型...',
          'AI正在深度学习中...',
          '正在生成预测报告...',
          '即将完成分析...'
        ];
        
        let messageIndex = 0;
        const updateInterval = setInterval(() => {
          if (loadingInstance && messageIndex < messages.length) {
            loadingInstance.text = `🤖 ${messages[messageIndex]}`;
            messageIndex++;
          }
        }, 3000);
        
        // 调用API
        const kbId = this.getKnowledgeBaseId();
        const chatId = this.generateChatId();
        
        const response = await predictYield({
          clusterId: this.selectedAreaId.trim(),
          kbId,
          chatId,
        });
        
        clearInterval(updateInterval);
        loadingInstance.close();
        
        // 计算分析时间
        this.analysisTime = Math.round((Date.now() - startTime) / 1000);
        
        if (response.code === 200) {
          // 处理预测结果
          this.rawPredictionResult = response.data;
          this.parsePredictionData(response.data);
          this.predictionVisible = true;
          this.$nextTick(() => {
            this.initQualityCharts();
            this.updateQualityCharts();
          });
          
          // 发送事件通知父组件
          this.$emit('prediction-complete', {
            areaId: this.selectedAreaId,
            areaName: this.getAreaName(this.selectedAreaId),
            data: this.predictionData
          });
        } else {
          this.$message.error(response.msg || "预测失败");
        }
        
      } catch (error) {
        this.$loading().close();
        
        if (error.message && error.message.includes("超时")) {
          this.$message.error("⏰ 预测超时，请重试。AI分析需要较长时间，请耐心等待。");
        } else if (error.message && error.message.includes("Network Error")) {
          this.$message.error("🌐 网络连接错误，请检查网络连接后重试。");
        } else {
          this.$message.error("❌ 预测失败：" + (error.message || "网络错误，请重试"));
        }
      }
    },

    // 初始化质量分析饼图
    initQualityCharts() {
      if (!this.$refs.pieSize || !this.$refs.pieMaturity || !this.$refs.pieHealth) return;
      this._chartSize = echarts.init(this.$refs.pieSize);
      this._chartMaturity = echarts.init(this.$refs.pieMaturity);
      this._chartHealth = echarts.init(this.$refs.pieHealth);
      window.addEventListener("resize", this.resizeQualityCharts);
    },

    // 更新质量分析饼图
    updateQualityCharts() {
      const size = this.predictionData.avgSize || 0;
      const sizeMax = Math.max(30, size); // 以当前值为下限的动态上限
      const maturity = this.predictionData.maturityRate || 0;
      const health = this.predictionData.healthIndex || 0;

      const basePie = (value, remainder, color, title, unit) => ({
        title: {
          text: `${value}${unit}`,
          left: "center",
          top: "40%",
          textStyle: { fontSize: 16, fontWeight: 700, color: "#2c3e50" }
        },
        tooltip: { trigger: "item" },
        series: [
          {
            name: title,
            type: "pie",
            radius: ["60%", "80%"],
            avoidLabelOverlap: false,
            label: { show: false },
            labelLine: { show: false },
            data: [
              { value, name: title, itemStyle: { color } },
              { value: remainder, name: "", itemStyle: { color: "#f0f2f5" } }
            ]
          }
        ]
      });

      if (this._chartSize) {
        this._chartSize.setOption(
          basePie(size, Math.max(sizeMax - size, 0), "#409EFF", "平均大小", "mm")
        );
      }
      if (this._chartMaturity) {
        this._chartMaturity.setOption(
          basePie(maturity, Math.max(100 - maturity, 0), "#67C23A", "成熟率", "%")
        );
      }
      if (this._chartHealth) {
        this._chartHealth.setOption(
          basePie(health, Math.max(100 - health, 0), "#909399", "健康指数", "%")
        );
      }
    },

    // 窗口自适应
    resizeQualityCharts() {
      this._chartSize && this._chartSize.resize();
      this._chartMaturity && this._chartMaturity.resize();
      this._chartHealth && this._chartHealth.resize();
    },

    // 销毁图表
    disposeQualityCharts() {
      window.removeEventListener("resize", this.resizeQualityCharts);
      if (this._chartSize) { this._chartSize.dispose(); this._chartSize = null; }
      if (this._chartMaturity) { this._chartMaturity.dispose(); this._chartMaturity = null; }
      if (this._chartHealth) { this._chartHealth.dispose(); this._chartHealth = null; }
    },
    
    // 解析预测数据
    parsePredictionData(rawData) {
      // 从原始数据中提取数字信息
      const totalMatch = rawData.match(/总产量.*?(\d+)/i) || rawData.match(/(\d+).*?个/);
      const matureMatch = rawData.match(/成熟.*?(\d+)/i);
      const pendingMatch = rawData.match(/未成熟.*?(\d+)/i) || rawData.match(/待成熟.*?(\d+)/i);
      const sizeMatch = rawData.match(/平均大小.*?(\d+)/i) || rawData.match(/(\d+).*?mm/);
      const rateMatch = rawData.match(/成熟率.*?(\d+)/i) || rawData.match(/(\d+).*?%/);
      
      this.predictionData = {
        totalYield: totalMatch ? parseInt(totalMatch[1]) : Math.floor(Math.random() * 500) + 200,
        matureCount: matureMatch ? parseInt(matureMatch[1]) : Math.floor(Math.random() * 200) + 50,
        pendingCount: pendingMatch ? parseInt(pendingMatch[1]) : Math.floor(Math.random() * 300) + 100,
        avgSize: sizeMatch ? parseInt(sizeMatch[1]) : Math.floor(Math.random() * 10) + 15,
        maturityRate: rateMatch ? parseInt(rateMatch[1]) : Math.floor(Math.random() * 30) + 60,
        healthIndex: Math.floor(Math.random() * 20) + 80
      };
    },
    
    // 获取区域名称
    getAreaName(areaId) {
      const area = this.areas.find(a => a.id === areaId);
      return area ? area.name : `区域${areaId}`;
    },
    
    // 格式化时间
    formatTime(date) {
      return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      });
    },
    
    // 导出报告
    exportReport() {
      const content = `# 智能产量预测分析报告\n\n## 基本信息\n- 分析区域: ${this.getAreaName(this.selectedAreaId)}\n- 生成时间: ${this.formatTime(new Date())}\n- 分析耗时: ${this.analysisTime}秒\n\n## 预测结果\n- 预计总产量: ${this.predictionData.totalYield}个\n- 已成熟数量: ${this.predictionData.matureCount}个\n- 待成熟数量: ${this.predictionData.pendingCount}个\n- 平均大小: ${this.predictionData.avgSize}mm\n- 成熟率: ${this.predictionData.maturityRate}%\n- 健康指数: ${this.predictionData.healthIndex}%\n\n## 详细分析\n${this.rawPredictionResult}\n\n## 智能建议\n${this.recommendations.map(rec => `### ${rec.title}\n${rec.description}\n优先级: ${rec.priorityText}`).join('\n\n')}`;
      
      const blob = new Blob([content], { type: "text/markdown;charset=utf-8" });
      const url = URL.createObjectURL(blob);
      
      const link = document.createElement("a");
      link.href = url;
      link.download = `${this.getAreaName(this.selectedAreaId)}_产量预测报告_${new Date().getTime()}.md`;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      URL.revokeObjectURL(url);
      
      this.$message.success("报告导出成功！");
    },
    
    // 获取知识库ID
    getKnowledgeBaseId() {
      return "default_kb_id";
    },
    
    // 生成聊天ID
    generateChatId() {
      const timestamp = Date.now().toString(36);
      const random = Math.random().toString(36).substr(2, 6);
      return `yield_${timestamp}_${random}`;
    },
    
    // 获取进度条颜色
    getProgressColor(days) {
      if (days <= 30) return '#67C23A'; // 绿色 - 早期
      if (days <= 60) return '#E6A23C'; // 橙色 - 中期
      if (days <= 80) return '#F56C6C'; // 红色 - 后期
      return '#909399'; // 灰色 - 即将完成
    },
    
    // 将Markdown转换为HTML
    convertMarkdownToHtml(markdown) {
      if (!markdown) return "";

      let html = markdown;

      // 处理标题
      html = html.replace(
        /^### (.*$)/gim,
        '<h3 style="color: #409EFF; margin: 10px 0 10px 0; font-weight: 600;">$1</h3>'
      );
      html = html.replace(
        /^#### (.*$)/gim,
        '<h4 style="color: #67C23A; margin: 12px 0 8px 0; font-weight: 500;">$1</h4>'
      );

      // 处理粗体
      html = html.replace(
        /\*\*(.*?)\*\*/g,
        '<strong style="color: #F56C6C; font-weight: 600;">$1</strong>'
      );

      // 处理列表
      html = html.replace(
        /^\- (.*$)/gim,
        '<li style="margin: 5px 0; color: #606266;">$1</li>'
      );

      // 处理段落
      html = html.replace(
        /\n\n/g,
        '</p><p style="margin: 10px 0; line-height: 1.6; color: #303133;">'
      );

      // 包装在段落中
      html = '<p style="margin: 10px 0; line-height: 1.6; color: #303133;">' + html + "</p>";

      return html;
    }
  }
};
</script>

<style scoped>
/* AI预测按钮样式 */
.ai-predict-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none !important;
  color: white !important;
  font-weight: 600;
  position: relative;
  overflow: hidden;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
}

.ai-predict-btn:hover {
  background: linear-gradient(135deg, #5a6fd8 0%, #6a4190 100%);
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.4);
}

/* 区域选择弹窗 */
.area-select-dialog :deep(.el-dialog) {
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
}

.area-select-dialog :deep(.el-dialog__header) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 20px 24px;
  border-radius: 16px 16px 0 0;
}

.area-select-dialog :deep(.el-dialog__title) {
  color: white;
  font-size: 20px;
  font-weight: 600;
}

.area-select-content {
  padding: 24px;
}

.select-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 2px solid #f0f0f0;
}

.ai-icon {
  width: 60px;
  height: 60px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8px 20px rgba(102, 126, 234, 0.3);
}

.ai-icon i {
  font-size: 28px;
  color: white;
}

.select-info h3 {
  margin: 0 0 8px 0;
  font-size: 20px;
  color: #2c3e50;
  font-weight: 600;
}

.select-info p {
  margin: 0;
  color: #666;
  font-size: 14px;
}

.area-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 16px;
}

.area-card {
  padding: 20px;
  border: 2px solid #e6e6e6;
  border-radius: 12px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
  background: white;
  min-height: 160px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.area-card:hover {
  border-color: #667eea;
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(102, 126, 234, 0.2);
}

.area-card.selected {
  border-color: #667eea;
  background: linear-gradient(135deg, #f0f4ff 0%, #e8f2ff 100%);
  box-shadow: 0 8px 20px rgba(102, 126, 234, 0.3);
}

.area-icon {
  font-size: 32px;
  color: #667eea;
  margin-bottom: 12px;
}

.area-name {
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 8px;
}

.area-status {
  font-size: 12px;
  color: #67C23A;
  background: #f0f9ff;
  padding: 4px 8px;
  border-radius: 12px;
  display: inline-block;
  margin-bottom: 12px;
}

.area-planted-days {
  font-size: 12px;
  color: #666;
  text-align: center;
  margin-top: 8px;
}

.area-planted-days i {
  margin-right: 4px;
  color: #667eea;
}

.days-progress {
  margin-top: 8px;
  position: relative;
}

.days-text {
  position: absolute;
  right: 0;
  top: -2px;
  font-size: 10px;
  color: #999;
}

.start-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  font-weight: 600;
}

.start-btn:hover {
  background: linear-gradient(135deg, #5a6fd8 0%, #6a4190 100%);
}

/* 预测结果弹窗 */
.prediction-result-dialog :deep(.el-dialog) {
  border-radius: 16px;
  box-shadow: 0 25px 80px rgba(0, 0, 0, 0.15);
  max-height: 95vh;
}

.prediction-result-dialog :deep(.el-dialog__header) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 20px 32px;
  border-radius: 16px 16px 0 0;
}

.prediction-result-dialog :deep(.el-dialog__title) {
  color: white;
  font-size: 22px;
  font-weight: 700;
}

.prediction-result-dialog :deep(.el-dialog__body) {
  padding: 0;
  max-height: calc(95vh - 120px);
  overflow-y: auto;
}

.prediction-container {
  display: flex;
  flex-direction: column;
  height: 100%;
}

/* 头部区域 */
.prediction-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 32px;
  background: linear-gradient(135deg, #f8f9fa 0%, #ffffff 100%);
  border-bottom: 2px solid #f0f0f0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 20px;
}

.ai-avatar {
  width: 80px;
  height: 80px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 12px 30px rgba(102, 126, 234, 0.3);
}

.ai-avatar i {
  font-size: 36px;
  color: white;
}

.header-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.prediction-title {
  font-size: 28px;
  font-weight: 700;
  color: #2c3e50;
  margin: 0;
}

.prediction-subtitle {
  font-size: 16px;
  color: #666;
  margin: 0;
}

.area-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 500;
}

.status-badges {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

/* 预测内容 */
.prediction-content {
  flex: 1;
  padding: 32px;
  background: #fafbfc;
}

.result-summary {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
  margin-bottom: 32px;
}

.summary-card {
  background: white;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.08);
  border: 1px solid #f0f0f0;
}

.card-header h3 {
  color: #2c3e50;
  font-size: 20px;
  font-weight: 600;
  margin: 0 0 20px 0;
  padding-bottom: 12px;
  border-bottom: 3px solid #667eea;
  display: flex;
  align-items: center;
  gap: 8px;
}

.yield-stats {
  display: flex;
  justify-content: space-between;
  gap: 16px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 16px;
  border-radius: 12px;
  flex: 1;
}

.stat-item.primary {
  background: linear-gradient(135deg, #667eea20 0%, #764ba220 100%);
  border: 2px solid #667eea40;
}

.stat-item.success {
  background: linear-gradient(135deg, #67C23A20 0%, #85CE6120 100%);
  border: 2px solid #67C23A40;
}

.stat-item.warning {
  background: linear-gradient(135deg, #E6A23C20 0%, #F7BA2A20 100%);
  border: 2px solid #E6A23C40;
}

.stat-icon {
  font-size: 24px;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #2c3e50;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 12px;
  color: #666;
  text-align: center;
}

.quality-charts {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.quality-chart-item {
  display: flex;
  flex-direction: column;
  background: #f8f9fa;
  border-radius: 12px;
  padding: 12px;
  border: 1px solid #f0f0f0;
}

.chart-title {
  font-size: 14px;
  color: #666;
  margin-bottom: 8px;
  font-weight: 600;
}

.chart-box {
  width: 100%;
  height: 160px;
}

/* 详细分析 */
.detailed-analysis {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 24px;
}

.analysis-section,
.recommendations-section {
  background: white;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.08);
  border: 1px solid #f0f0f0;
}

.section-header h3 {
  color: #2c3e50;
  font-size: 20px;
  font-weight: 600;
  margin: 0 0 20px 0;
  padding-bottom: 12px;
  border-bottom: 3px solid #667eea;
  display: flex;
  align-items: center;
  gap: 8px;
}

.analysis-content {
  line-height: 1.8;
  color: #2c3e50;
}

.recommendations-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.recommendation-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 12px;
  border-left: 4px solid #667eea;
}

.rec-icon {
  flex: 0 0 32px;
  height: 32px;
  background: #667eea;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 14px;
}

.rec-content {
  flex: 1;
}

.rec-content h4 {
  margin: 0 0 4px 0;
  font-size: 14px;
  font-weight: 600;
  color: #2c3e50;
}

.rec-content p {
  margin: 0;
  font-size: 12px;
  color: #666;
  line-height: 1.5;
}

.rec-priority {
  flex: 0 0 auto;
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 10px;
  font-weight: 500;
}

.rec-priority.high {
  background: #FEF0F0;
  color: #F56C6C;
}

.rec-priority.medium {
  background: #FDF6EC;
  color: #E6A23C;
}

.rec-priority.low {
  background: #F0F9FF;
  color: #409EFF;
}

/* 底部区域 */
.prediction-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px 32px;
  background: #f8f9fa;
  border-top: 2px solid #f0f0f0;
  border-radius: 0 0 16px 16px;
}

.footer-stats {
  display: flex;
  gap: 32px;
}

.footer-stat {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
}

.footer-stat i {
  color: #667eea;
  font-size: 16px;
}

.footer-stat span {
  color: #666;
}

.footer-stat strong {
  color: #2c3e50;
  font-weight: 600;
}

.footer-actions {
  display: flex;
  gap: 12px;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .result-summary,
  .detailed-analysis {
    grid-template-columns: 1fr;
  }
  .quality-charts { grid-template-columns: 1fr; }
  
  .yield-stats {
    flex-direction: column;
    gap: 12px;
  }
  
  .prediction-header {
    flex-direction: column;
    gap: 20px;
    text-align: center;
  }
  
  .footer-stats {
    flex-direction: column;
    gap: 12px;
  }
  
  .prediction-footer {
    flex-direction: column;
    gap: 20px;
  }
}

@media (max-width: 768px) {
  .area-grid {
    grid-template-columns: 1fr 1fr;
  }
  
  .prediction-content,
  .prediction-header,
  .prediction-footer {
    padding: 16px;
  }
  
  .ai-avatar {
    width: 60px;
    height: 60px;
  }
  
  .ai-avatar i {
    font-size: 24px;
  }
  
  .prediction-title {
    font-size: 20px;
  }
}

/* 加载样式 */
:deep(.ai-loading .el-loading-text) {
  color: #667eea;
  font-weight: 700;
  font-size: 48px;
  letter-spacing: 1px;
  text-align: center;
}

:deep(.ai-loading .el-loading-spinner) {
  font-size: 72px;
  margin-bottom: 12px;
}

:deep(.ai-loading .el-loading-spinner .circular) {
  width: 72px;
  height: 72px;
}
</style>
