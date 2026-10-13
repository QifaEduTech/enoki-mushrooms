<template>
  <div class="plant-growth-model">
    <div class="container">
      <div class="header">
        <h1>🌱 植物生长周期模型管理</h1>
        <p>上传植物生长周期数据，智能监控和控制植物生长环境</p>
      </div>

      <!-- 模型上传区域 -->
      <div v-if="!currentModel" class="upload-section">
        <div class="upload-card">
          <div class="upload-icon">📁</div>
          <h2>上传生长周期模型</h2>
          <p>请上传包含植物生长周期信息的JSON文件</p>

          <div
            class="upload-area"
            @click="triggerFileUpload"
            @drop="handleFileDrop"
            @dragover.prevent
          >
            <div class="upload-content">
              <div class="upload-icon-large">📤</div>
              <p>点击选择文件或拖拽文件到此处</p>
              <p class="upload-hint">支持JSON格式文件</p>
            </div>
            <input
              ref="fileInput"
              type="file"
              accept=".json"
              @change="handleFileSelect"
              style="display: none"
            />
          </div>

          <div class="upload-actions">
            <button @click="loadDefaultModel" class="btn btn-secondary">
              🍄 加载金针菇模型示例
            </button>
            <button @click="downloadSampleModel" class="btn btn-outline">
              📥 下载示例模板
            </button>
          </div>
        </div>
      </div>

      <!-- 模型展示和控制区域 -->
      <div v-else class="model-display">
        <!-- 植物信息头部 -->
        <div class="plant-header">
          <div class="plant-info">
            <h2>{{ currentModel.plantName }} 生长周期</h2>
            <p>
              {{ currentModel.description }} ({{ currentModel.growthDuration }})
            </p>
          </div>
          <div class="plant-actions">
            <button @click="resetModel" class="btn btn-outline">
              🔄 重新上传
            </button>
            <button @click="downloadCurrentModel" class="btn btn-secondary">
              💾 导出模型
            </button>
          </div>
        </div>

        <!-- 切换按钮 -->
        <div class="toggle-button" @click="toggleView">
          <span v-if="!isChartView">📊 图表模式</span>
          <span v-else>📅 时间线模式</span>
        </div>

        <!-- 时间线视图 -->
        <div v-show="!isChartView" class="timeline-container">
          <div class="central-cylinder"></div>

          <div class="timeline-items">
            <div
              class="timeline-item"
              v-for="(stage, index) in currentModel.stages"
              :key="index"
              :class="{
                'left-side': index % 2 === 0,
                'right-side': index % 2 === 1,
              }"
            >
              <div class="timeline-content" @click="handleStageClick(stage)">
                <div class="stage-title">
                  <div
                    class="stage-icon"
                    :style="{ backgroundColor: stage.color }"
                  >
                    {{ stage.icon }}
                  </div>
                  {{ stage.name }}
                </div>

                <div class="stage-duration">
                  {{ stage.duration }}
                </div>

                <div class="stage-details">
                  {{ stage.description }}
                </div>

                <div class="conditions" v-if="stage.conditions">
                  <div
                    class="condition-item"
                    v-for="(condition, key) in stage.conditions"
                    :key="key"
                  >
                    <span class="condition-label">{{ key }}:</span>
                    {{ condition }}
                  </div>
                </div>

                <div
                  class="management-section"
                  v-if="stage.fertilizer || stage.medicine"
                >
                  <div class="fertilizer-management" v-if="stage.fertilizer">
                    <h4>🌱 用肥管理</h4>
                    <p>{{ stage.fertilizer }}</p>
                  </div>
                  <div class="medicine-management" v-if="stage.medicine">
                    <h4>💊 用药管理</h4>
                    <p>{{ stage.medicine }}</p>
                  </div>
                </div>

                <div class="image-preview" v-if="stage.image">
                  <img :src="getImageUrl(stage.image)" :alt="stage.name" />
                </div>

                <div class="soil-parameters">
                  <div class="parameter-item">
                    <span class="parameter-label">氮(N):</span>
                    <span class="parameter-value">{{ stage.npk.n }}</span>
                  </div>
                  <div class="parameter-item">
                    <span class="parameter-label">磷(P):</span>
                    <span class="parameter-value">{{ stage.npk.p }}</span>
                  </div>
                  <div class="parameter-item">
                    <span class="parameter-label">钾(K):</span>
                    <span class="parameter-value">{{ stage.npk.k }}</span>
                  </div>
                  <div class="parameter-item">
                    <span class="parameter-label">pH值:</span>
                    <span class="parameter-value">{{ stage.npk.ph }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 折线图视图 -->
        <div v-show="isChartView" class="chart-container">
          <!-- NPK和pH值图表 -->
          <div class="chart-section">
            <h3 class="chart-title">
              {{ currentModel.plantName }}生长周期 NPK 和 pH 值变化趋势
            </h3>
            <div class="chart-wrapper">
              <canvas ref="chartCanvas" width="800" height="400"></canvas>
            </div>
            <div class="chart-legend">
              <div class="legend-item">
                <span
                  class="legend-color"
                  style="background-color: #ff6b6b"
                ></span>
                <span>氮(N)</span>
              </div>
              <div class="legend-item">
                <span
                  class="legend-color"
                  style="background-color: #4ecdc4"
                ></span>
                <span>磷(P)</span>
              </div>
              <div class="legend-item">
                <span
                  class="legend-color"
                  style="background-color: #45b7d1"
                ></span>
                <span>钾(K)</span>
              </div>
              <div class="legend-item">
                <span
                  class="legend-color"
                  style="background-color: #96ceb4"
                ></span>
                <span>pH值</span>
              </div>
            </div>
          </div>

          <!-- 环境参数图表 -->
          <div class="chart-section">
            <h3 class="chart-title">环境参数变化趋势</h3>
            <div class="chart-wrapper">
              <canvas ref="envChartCanvas" width="800" height="400"></canvas>
            </div>
            <div class="chart-legend">
              <div class="legend-item">
                <span
                  class="legend-color"
                  style="background-color: #ff6b6b"
                ></span>
                <span>空气温度 (°C)</span>
              </div>
              <div class="legend-item">
                <span
                  class="legend-color"
                  style="background-color: #4ecdc4"
                ></span>
                <span>空气湿度 (%)</span>
              </div>
              <div class="legend-item">
                <span
                  class="legend-color"
                  style="background-color: #45b7d1"
                ></span>
                <span>光照强度 (Lux)</span>
              </div>
              <div class="legend-item">
                <span
                  class="legend-color"
                  style="background-color: #96ceb4"
                ></span>
                <span>二氧化碳 (ppm)</span>
              </div>
              <div class="legend-item">
                <span
                  class="legend-color"
                  style="background-color: #ffeaa7"
                ></span>
                <span>土壤温度 (°C)</span>
              </div>
              <div class="legend-item">
                <span
                  class="legend-color"
                  style="background-color: #a8a8a8"
                ></span>
                <span>土壤湿度 (%)</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
// 静态导入图片
import seedling1 from "@/assets/images/growth/seedling1.jpg";
import anthesis2 from "@/assets/images/growth/anthesis2.png";
import fruiting3 from "@/assets/images/growth/fruiting3.png";
import exuberant4 from "@/assets/images/growth/exuberant4.png";
import grow5 from "@/assets/images/growth/grow5.png";
import ripe6 from "@/assets/images/growth/ripe6.png";

export default {
  name: "PlantGrowthModel",
  data() {
    return {
      isChartView: false,
      currentModel: null,
      fileInput: null,
      // 图片映射表
      imageMap: {
        "growth/seedling1.jpg": seedling1,
        "growth/anthesis2.png": anthesis2,
        "growth/fruiting3.png": fruiting3,
        "growth/exuberant4.png": exuberant4,
        "growth/grow5.png": grow5,
        "growth/ripe6.png": ripe6,
      },
    };
  },
  mounted() {
    this.initAnimations();
    this.fileInput = this.$refs.fileInput;
  },
  methods: {
    toggleView() {
      this.isChartView = !this.isChartView;
      if (this.isChartView) {
        this.$nextTick(() => {
          this.drawChart();
          this.drawEnvChart();
        });
      }
    },
    handleStageClick(stage) {
      this.$emit("stage-click", stage);
      console.log("点击了阶段:", stage.name);
    },
    triggerFileUpload() {
      this.fileInput.click();
    },
    handleFileSelect(event) {
      const file = event.target.files[0];
      if (file) {
        const reader = new FileReader();
        reader.onload = (e) => {
          try {
            this.currentModel = JSON.parse(e.target.result);
            this.isChartView = false; // 默认显示时间线
            this.$nextTick(() => {
              this.initAnimations();
            });
          } catch (error) {
            alert("上传的文件格式不正确，请确保是JSON文件。");
            console.error("Error parsing JSON:", error);
          }
        };
        reader.readAsText(file);
      }
    },
    handleFileDrop(event) {
      event.preventDefault();
      const file = event.dataTransfer.files[0];
      if (file) {
        const reader = new FileReader();
        reader.onload = (e) => {
          try {
            this.currentModel = JSON.parse(e.target.result);
            this.isChartView = false; // 默认显示时间线
            this.$nextTick(() => {
              this.initAnimations();
            });
          } catch (error) {
            alert("上传的文件格式不正确，请确保是JSON文件。");
            console.error("Error parsing JSON:", error);
          }
        };
        reader.readAsText(file);
      }
    },
    loadDefaultModel() {
      // 从JSON文件加载金针菇模型
      fetch("/src/assets/data/enokimushroom-growth-model.json")
        .then((response) => response.json())
        .then((data) => {
          this.currentModel = data;
          this.isChartView = false; // 默认显示时间线
          this.$nextTick(() => {
            this.initAnimations();
          });
        })
        .catch((error) => {
          console.error("加载金针菇模型失败:", error);
          alert("加载金针菇模型失败，请检查文件路径");
        });
    },
    downloadSampleModel() {
      // 下载JSON模板文件
      fetch("/src/assets/data/enokimushroom-growth-model.json")
        .then((response) => response.json())
        .then((data) => {
          const jsonString = JSON.stringify(data, null, 2);
          const blob = new Blob([jsonString], { type: "application/json" });
          const url = URL.createObjectURL(blob);
          const a = document.createElement("a");
          a.href = url;
          a.download = "plant_growth_model_template.json";
          document.body.appendChild(a);
          a.click();
          document.body.removeChild(a);
          URL.revokeObjectURL(url);
        })
        .catch((error) => {
          console.error("下载模板失败:", error);
          alert("下载模板失败，请检查文件路径");
        });
    },
    resetModel() {
      this.currentModel = null;
      this.isChartView = false;
      this.fileInput.value = ""; // 清空文件输入框
    },
    downloadCurrentModel() {
      if (!this.currentModel) {
        alert("请先上传或加载一个模型。");
        return;
      }
      const jsonString = JSON.stringify(this.currentModel, null, 2);
      const blob = new Blob([jsonString], { type: "application/json" });
      const url = URL.createObjectURL(blob);
      const a = document.createElement("a");
      a.href = url;
      a.download = `${this.currentModel.plantName}_growth_model.json`;
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
      URL.revokeObjectURL(url);
    },
    drawChart() {
      const canvas = this.$refs.chartCanvas;
      const ctx = canvas.getContext("2d");

      // 清空画布
      ctx.clearRect(0, 0, canvas.width, canvas.height);

      // 设置画布尺寸
      const width = canvas.width;
      const height = canvas.height;
      const padding = 60;
      const chartWidth = width - 2 * padding;
      const chartHeight = height - 2 * padding;

      // 数据准备
      const stages = this.currentModel.stages.map((stage) => stage.name);
      const npkData = this.currentModel.stages.map((stage) => ({
        n: this.parseRange(stage.npk.n),
        p: this.parseRange(stage.npk.p),
        k: this.parseRange(stage.npk.k),
        ph: this.parseRange(stage.npk.ph),
      }));

      // 计算Y轴范围
      const allValues = npkData.flatMap((item) => [
        item.n,
        item.p,
        item.k,
        item.ph * 10,
      ]); // pH值乘以10以便在同一图表显示
      const minY = Math.min(...allValues);
      const maxY = Math.max(...allValues);
      const yRange = maxY - minY;

      // 绘制坐标轴
      ctx.strokeStyle = "#333";
      ctx.lineWidth = 2;

      // X轴
      ctx.beginPath();
      ctx.moveTo(padding, height - padding);
      ctx.lineTo(width - padding, height - padding);
      ctx.stroke();

      // Y轴
      ctx.beginPath();
      ctx.moveTo(padding, padding);
      ctx.lineTo(padding, height - padding);
      ctx.stroke();

      // 绘制网格线
      ctx.strokeStyle = "#ddd";
      ctx.lineWidth = 1;

      // Y轴网格线
      const ySteps = 5;
      for (let i = 0; i <= ySteps; i++) {
        const y = padding + (i / ySteps) * chartHeight;
        ctx.beginPath();
        ctx.moveTo(padding, y);
        ctx.lineTo(width - padding, y);
        ctx.stroke();

        // Y轴标签
        const value = maxY - (i / ySteps) * yRange;
        ctx.fillStyle = "#333";
        ctx.font = "12px Arial";
        ctx.textAlign = "right";
        ctx.fillText(value.toFixed(1), padding - 10, y + 4);
      }

      // X轴标签
      ctx.fillStyle = "#333";
      ctx.font = "12px Arial";
      ctx.textAlign = "center";
      stages.forEach((stage, index) => {
        const x = padding + (index / (stages.length - 1)) * chartWidth;
        ctx.fillText(stage, x, height - padding + 20);
      });

      // 绘制折线
      const colors = ["#ff6b6b", "#4ecdc4", "#45b7d1", "#96ceb4"];
      const dataKeys = ["n", "p", "k", "ph"];

      dataKeys.forEach((key, keyIndex) => {
        const color = colors[keyIndex];
        ctx.strokeStyle = color;
        ctx.lineWidth = 3;
        ctx.fillStyle = color;

        // 先绘制折线
        ctx.beginPath();
        npkData.forEach((item, index) => {
          const x = padding + (index / (stages.length - 1)) * chartWidth;
          const value = key === "ph" ? item[key] * 10 : item[key];
          const y = padding + ((maxY - value) / yRange) * chartHeight;

          if (index === 0) {
            ctx.moveTo(x, y);
          } else {
            ctx.lineTo(x, y);
          }
        });
        ctx.stroke();

        // 再绘制数据点
        npkData.forEach((item, index) => {
          const x = padding + (index / (stages.length - 1)) * chartWidth;
          const value = key === "ph" ? item[key] * 10 : item[key];
          const y = padding + ((maxY - value) / yRange) * chartHeight;

          ctx.beginPath();
          ctx.arc(x, y, 5, 0, 2 * Math.PI);
          ctx.fill();
        });
      });

      // 绘制标题
      ctx.fillStyle = "#333";
      ctx.font = "bold 18px Arial";
      ctx.textAlign = "center";
      ctx.fillText("金针菇生长周期 NPK 和 pH 值变化趋势", width / 2, 30);
    },

    // 绘制环境参数图表
    drawEnvChart() {
      const canvas = this.$refs.envChartCanvas;
      const ctx = canvas.getContext("2d");

      // 清空画布
      ctx.clearRect(0, 0, canvas.width, canvas.height);

      // 设置画布尺寸
      const width = canvas.width;
      const height = canvas.height;
      const padding = 60;
      const chartWidth = width - 2 * padding;
      const chartHeight = height - 2 * padding;

      // 数据准备
      const stages = this.currentModel.stages.map((stage) => stage.name);
      const envData = this.currentModel.stages.map((stage) => ({
        airTemperature: this.parseRange(stage.environment.airTemperature),
        airHumidity: this.parseRange(stage.environment.airHumidity),
        lightLux: this.parseRange(stage.environment.lightLux) / 1000, // 转换为千Lux显示
        airCo2: this.parseRange(stage.environment.airCo2) / 100, // 转换为百ppm显示
        soilTemperature: this.parseRange(stage.environment.soilTemperature),
        soilHumidity: this.parseRange(stage.environment.soilHumidity),
      }));

      // 计算Y轴范围
      const allValues = envData.flatMap((item) => [
        item.airTemperature,
        item.airHumidity,
        item.lightLux,
        item.airCo2,
        item.soilTemperature,
        item.soilHumidity,
      ]);
      const minY = Math.min(...allValues);
      const maxY = Math.max(...allValues);
      const yRange = maxY - minY;

      // 绘制坐标轴
      ctx.strokeStyle = "#333";
      ctx.lineWidth = 2;

      // X轴
      ctx.beginPath();
      ctx.moveTo(padding, height - padding);
      ctx.lineTo(width - padding, height - padding);
      ctx.stroke();

      // Y轴
      ctx.beginPath();
      ctx.moveTo(padding, padding);
      ctx.lineTo(padding, height - padding);
      ctx.stroke();

      // 绘制网格线
      ctx.strokeStyle = "#ddd";
      ctx.lineWidth = 1;

      // Y轴网格线
      const ySteps = 5;
      for (let i = 0; i <= ySteps; i++) {
        const y = padding + (i / ySteps) * chartHeight;
        ctx.beginPath();
        ctx.moveTo(padding, y);
        ctx.lineTo(width - padding, y);
        ctx.stroke();

        // Y轴标签
        const value = maxY - (i / ySteps) * yRange;
        ctx.fillStyle = "#333";
        ctx.font = "12px Arial";
        ctx.textAlign = "right";
        ctx.fillText(value.toFixed(1), padding - 10, y + 4);
      }

      // X轴标签
      ctx.fillStyle = "#333";
      ctx.font = "12px Arial";
      ctx.textAlign = "center";
      stages.forEach((stage, index) => {
        const x = padding + (index / (stages.length - 1)) * chartWidth;
        ctx.fillText(stage, x, height - padding + 20);
      });

      // 绘制折线
      const colors = [
        "#ff6b6b",
        "#4ecdc4",
        "#45b7d1",
        "#96ceb4",
        "#ffeaa7",
        "#a8a8a8",
      ];
      const dataKeys = [
        "airTemperature",
        "airHumidity",
        "lightLux",
        "airCo2",
        "soilTemperature",
        "soilHumidity",
      ];

      dataKeys.forEach((key, keyIndex) => {
        const color = colors[keyIndex];
        ctx.strokeStyle = color;
        ctx.lineWidth = 3;
        ctx.fillStyle = color;

        // 先绘制折线
        ctx.beginPath();
        envData.forEach((item, index) => {
          const x = padding + (index / (stages.length - 1)) * chartWidth;
          const value = item[key];
          const y = padding + ((maxY - value) / yRange) * chartHeight;

          if (index === 0) {
            ctx.moveTo(x, y);
          } else {
            ctx.lineTo(x, y);
          }
        });
        ctx.stroke();

        // 再绘制数据点
        envData.forEach((item, index) => {
          const x = padding + (index / (stages.length - 1)) * chartWidth;
          const value = item[key];
          const y = padding + ((maxY - value) / yRange) * chartHeight;

          ctx.beginPath();
          ctx.arc(x, y, 5, 0, 2 * Math.PI);
          ctx.fill();
        });
      });

      // 绘制标题
      ctx.fillStyle = "#333";
      ctx.font = "bold 18px Arial";
      ctx.textAlign = "center";
      ctx.fillText("环境参数变化趋势", width / 2, 30);
    },
    parseRange(rangeStr) {
      // 解析范围字符串，返回中值
      const match = rangeStr.match(/(\d+(?:\.\d+)?)/g);
      if (match && match.length >= 2) {
        return (parseFloat(match[0]) + parseFloat(match[1])) / 2;
      }
      return parseFloat(match[0]) || 0;
    },
    initAnimations() {
      const observer = new IntersectionObserver(
        (entries) => {
          entries.forEach((entry) => {
            if (entry.isIntersecting) {
              entry.target.classList.add("animate-in");
            }
          });
        },
        {
          threshold: 0.1,
          rootMargin: "0px 0px -50px 0px",
        }
      );

      this.$nextTick(() => {
        document.querySelectorAll(".timeline-item").forEach((item) => {
          observer.observe(item);
        });
      });
    },
    getImageUrl(imagePath) {
      // 处理图片路径，使用图片映射表
      if (!imagePath) {
        return "";
      }

      if (imagePath.startsWith("/")) {
        return imagePath; // 如果是绝对路径，则直接返回
      }

      // 使用图片映射表获取正确的图片URL
      if (this.imageMap[imagePath]) {
        return this.imageMap[imagePath];
      }

      // 如果映射表中没有，尝试使用静态路径
      console.warn(`图片未找到: ${imagePath}`);
      return `/assets/images/${imagePath}`;
    },
  },
};
</script>

<style scoped>
.plant-growth-model {
  font-family: "Microsoft YaHei", -apple-system, BlinkMacSystemFont, sans-serif;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  min-height: 100vh;
  overflow-x: hidden;
  position: relative;
}

/* 切换按钮样式 */
.toggle-button {
  position: absolute;
  top: 20px;
  right: 20px;
  background: rgba(255, 255, 255, 0.9);
  color: #333;
  padding: 12px 20px;
  border-radius: 25px;
  cursor: pointer;
  font-weight: 600;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.2);
  transition: all 0.3s ease;
  z-index: 1000;
  backdrop-filter: blur(10px);
}

.toggle-button:hover {
  background: rgba(255, 255, 255, 1);
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.3);
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 40px 20px;
  position: relative;
}

.header {
  text-align: center;
  margin-bottom: 60px;
  color: white;
  position: relative;
  padding-top: 20px;
}

.header h1 {
  font-size: 2.5rem;
  margin-bottom: 10px;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.3);
  font-weight: 600;
}

.header p {
  font-size: 1.2rem;
  opacity: 0.9;
  margin: 0;
}

/* 图表容器样式 */
.chart-container {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 20px;
  padding: 30px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.15);
  backdrop-filter: blur(10px);
}

.chart-section {
  margin-bottom: 40px;
}

.chart-section:last-child {
  margin-bottom: 0;
}

.chart-title {
  text-align: center;
  color: #333;
  font-size: 1.5rem;
  font-weight: 600;
  margin-bottom: 20px;
  padding: 15px;
  background: linear-gradient(
    135deg,
    rgba(103, 126, 234, 0.1),
    rgba(118, 75, 162, 0.1)
  );
  border-radius: 12px;
  border-left: 4px solid #667eea;
}

.chart-wrapper {
  display: flex;
  justify-content: center;
  margin-bottom: 20px;
}

.chart-legend {
  display: flex;
  justify-content: center;
  gap: 30px;
  flex-wrap: wrap;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 500;
  color: #333;
}

.legend-color {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  border: 2px solid #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
}

.timeline-container {
  position: relative;
  display: flex;
  justify-content: center;
  min-height: 1000px;
}

/* 中央圆柱体 */
.central-cylinder {
  position: absolute;
  width: 20px;
  height: 100%;
  background: linear-gradient(
    180deg,
    #ff6b6b 0%,
    #4ecdc4 16.67%,
    #45b7d1 33.33%,
    #96ceb4 50%,
    #ffeaa7 66.67%,
    #a8a8a8 83.33%,
    #a8a8a8 100%
  );
  border-radius: 10px;
  box-shadow: 0 0 20px rgba(255, 255, 255, 0.3),
    inset 0 0 0 2px rgba(255, 255, 255, 0.1);
  z-index: 1;
}

.central-cylinder::before {
  content: "";
  position: absolute;
  top: -15px;
  left: 50%;
  transform: translateX(-50%);
  width: 50px;
  height: 30px;
  background: radial-gradient(
    ellipse at center,
    rgba(255, 255, 255, 0.8) 0%,
    transparent 70%
  );
  border-radius: 50%;
}

.central-cylinder::after {
  content: "";
  position: absolute;
  bottom: -15px;
  left: 50%;
  transform: translateX(-50%);
  width: 50px;
  height: 30px;
  background: radial-gradient(
    ellipse at center,
    rgba(0, 0, 0, 0.3) 0%,
    transparent 70%
  );
  border-radius: 50%;
}

/* 时间线项目 */
.timeline-items {
  position: relative;
  width: 100%;
  z-index: 2;
}

.timeline-item {
  position: relative;
  margin-bottom: 100px;
  opacity: 0;
  transform: translateY(30px);
  transition: all 0.8s cubic-bezier(0.25, 0.46, 0.45, 0.94);
}

.timeline-item.animate-in {
  opacity: 1;
  transform: translateY(0);
}

.timeline-item.right-side {
  text-align: right;
  padding-right: 60px;
}

.timeline-item.left-side {
  text-align: left;
  padding-left: 60px;
}

.timeline-content {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 20px;
  padding: 30px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.15),
    0 0 0 1px rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  position: relative;
  transition: all 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94);
  cursor: pointer;
  animation: float 6s ease-in-out infinite;
}

.timeline-item.left-side .timeline-content {
  animation-delay: -3s;
}

.timeline-content:hover {
  transform: translateY(-8px);
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.25),
    0 0 0 1px rgba(255, 255, 255, 0.2);
}

/* 连接线 */
.timeline-item::before {
  content: "";
  position: absolute;
  top: 50%;
  width: 40px;
  height: 2px;
  background: linear-gradient(
    90deg,
    transparent,
    rgba(255, 255, 255, 0.8),
    transparent
  );
  z-index: 3;
}

.timeline-item.right-side::before {
  right: 20px;
}

.timeline-item.left-side::before {
  left: 20px;
}

/* 节点圆圈 */
.timeline-item::after {
  content: "";
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  width: 16px;
  height: 16px;
  background: #fff;
  border-radius: 50%;
  box-shadow: 0 0 0 4px rgba(255, 255, 255, 0.3), 0 2px 8px rgba(0, 0, 0, 0.2);
  z-index: 4;
  transition: all 0.3s ease;
}

.timeline-item.right-side::after {
  right: 12px;
}

.timeline-item.left-side::after {
  left: 12px;
}

.timeline-item:hover::after {
  transform: translateY(-50%) scale(1.2);
  box-shadow: 0 0 0 4px rgba(255, 255, 255, 0.5), 0 4px 12px rgba(0, 0, 0, 0.3);
}

.stage-title {
  font-size: 1.5rem;
  font-weight: bold;
  color: #333;
  margin-bottom: 15px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.stage-icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.3rem;
  box-shadow: 0 3px 10px rgba(0, 0, 0, 0.2);
  transition: transform 0.3s ease;
}

.stage-icon:hover {
  transform: scale(1.1) rotate(5deg);
}

.stage-duration {
  background: linear-gradient(45deg, #667eea, #764ba2);
  color: white;
  padding: 8px 16px;
  border-radius: 18px;
  font-size: 0.95rem;
  font-weight: 500;
  display: inline-block;
  margin-bottom: 15px;
  box-shadow: 0 3px 10px rgba(102, 126, 234, 0.3);
}

.stage-details {
  color: #555;
  line-height: 1.7;
  margin-bottom: 20px;
  font-size: 1rem;
}

/* 条件参数区域 */
.conditions {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));
  gap: 10px;
  margin: 20px 0;
  padding: 15px;
  background: rgba(103, 126, 234, 0.05);
  border-radius: 12px;
  border-left: 4px solid #667eea;
}

.condition-item {
  background: linear-gradient(
    135deg,
    rgba(103, 126, 234, 0.1),
    rgba(118, 75, 162, 0.1)
  );
  padding: 10px 12px;
  border-radius: 12px;
  font-size: 0.9rem;
  text-align: center;
  transition: all 0.3s ease;
  border: 1px solid rgba(103, 126, 234, 0.2);
}

.condition-item:hover {
  background: linear-gradient(
    135deg,
    rgba(103, 126, 234, 0.2),
    rgba(118, 75, 162, 0.2)
  );
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(103, 126, 234, 0.2);
}

.condition-label {
  font-weight: 600;
  color: #667eea;
}

/* 管理区域 */
.management-section {
  margin: 20px 0;
  padding: 15px;
  background: rgba(103, 126, 234, 0.05);
  border-radius: 12px;
  border-left: 4px solid #667eea;
}

.fertilizer-management,
.medicine-management {
  margin-bottom: 15px;
}

.fertilizer-management:last-child,
.medicine-management:last-child {
  margin-bottom: 0;
}

.fertilizer-management h4,
.medicine-management h4 {
  color: #667eea;
  font-size: 1rem;
  margin-bottom: 8px;
  font-weight: 600;
}

.fertilizer-management p,
.medicine-management p {
  color: #666;
  line-height: 1.6;
  margin: 0;
  font-size: 0.9rem;
}

/* 图片预览区域 */
.image-preview {
  width: 200px;
  height: 150px;
  border-radius: 15px;
  overflow: hidden;
  margin: 20px 0;
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
  transition: all 0.3s ease;
}

.timeline-item.right-side .image-preview {
  margin-left: auto;
}

.image-preview:hover {
  transform: scale(1.05);
  box-shadow: 0 12px 35px rgba(0, 0, 0, 0.25);
}

.image-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.image-preview:hover img {
  transform: scale(1.1);
}

/* 土壤参数区域 */
.soil-parameters {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  margin-top: 20px;
  padding: 15px;
  background: linear-gradient(
    135deg,
    rgba(255, 193, 7, 0.1),
    rgba(255, 152, 0, 0.1)
  );
  border-radius: 12px;
  border: 1px solid rgba(255, 193, 7, 0.3);
}

.parameter-item {
  text-align: center;
  padding: 10px;
  background: rgba(255, 255, 255, 0.8);
  border-radius: 8px;
  transition: all 0.3s ease;
}

.parameter-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(255, 193, 7, 0.3);
}

.parameter-label {
  display: block;
  font-weight: 600;
  color: #f57c00;
  font-size: 0.85rem;
  margin-bottom: 5px;
}

.parameter-value {
  display: block;
  color: #333;
  font-size: 0.9rem;
  font-weight: 500;
}

/* 上传区域样式 */
.upload-section {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 60vh;
}

.upload-card {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 30px;
  padding: 50px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2);
  backdrop-filter: blur(20px);
  text-align: center;
  max-width: 600px;
  width: 100%;
}

.upload-icon {
  font-size: 4rem;
  margin-bottom: 20px;
}

.upload-card h2 {
  color: #333;
  font-size: 2rem;
  margin-bottom: 15px;
  font-weight: 600;
}

.upload-card p {
  color: #666;
  font-size: 1.1rem;
  margin-bottom: 30px;
}

.upload-area {
  border: 3px dashed #667eea;
  border-radius: 20px;
  padding: 40px;
  margin-bottom: 30px;
  cursor: pointer;
  transition: all 0.3s ease;
  background: rgba(103, 126, 234, 0.05);
}

.upload-area:hover {
  border-color: #5a67d8;
  background: rgba(103, 126, 234, 0.1);
  transform: translateY(-2px);
}

.upload-content {
  text-align: center;
}

.upload-icon-large {
  font-size: 3rem;
  margin-bottom: 15px;
  color: #667eea;
}

.upload-hint {
  color: #999;
  font-size: 0.9rem;
  margin-top: 10px;
}

.upload-actions {
  display: flex;
  gap: 15px;
  justify-content: center;
  flex-wrap: wrap;
}

.btn {
  padding: 12px 24px;
  border: none;
  border-radius: 25px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  text-decoration: none;
  display: inline-block;
}

.btn-secondary {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
}

.btn-secondary:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(103, 126, 234, 0.4);
}

.btn-outline {
  background: transparent;
  color: #667eea;
  border: 2px solid #667eea;
}

.btn-outline:hover {
  background: #667eea;
  color: white;
  transform: translateY(-2px);
}

/* 模型展示区域 */
.model-display {
  animation: fadeIn 0.5s ease-in-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.plant-header {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 20px;
  padding: 30px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.15);
  backdrop-filter: blur(10px);
  margin-bottom: 30px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 20px;
}

.plant-info h2 {
  color: #333;
  margin: 0 0 10px 0;
  font-size: 2rem;
  font-weight: 600;
}

.plant-info p {
  color: #666;
  margin: 0;
  font-size: 1.1rem;
}

.plant-actions {
  display: flex;
  gap: 15px;
  flex-wrap: wrap;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .toggle-button {
    top: 10px;
    left: 10px;
    padding: 10px 16px;
    font-size: 14px;
  }

  .container {
    padding: 20px 10px;
  }

  .upload-card {
    padding: 30px 20px;
  }

  .upload-area {
    padding: 30px 20px;
  }

  .plant-header {
    flex-direction: column;
    text-align: center;
  }

  .timeline-item {
    margin-bottom: 80px;
  }

  .timeline-item.right-side {
    text-align: left;
    padding-right: 0;
    padding-left: 50px;
  }

  .timeline-item.left-side {
    padding-left: 50px;
  }

  .timeline-item::before {
    left: 20px;
    right: auto;
  }

  .timeline-item::after {
    left: 12px;
    right: auto;
  }

  .timeline-item.right-side .image-preview {
    margin-left: 0;
  }

  .header h1 {
    font-size: 2rem;
  }

  .central-cylinder {
    left: 10px;
  }

  .stage-title {
    font-size: 1.3rem;
  }

  .conditions {
    grid-template-columns: repeat(2, 1fr);
  }

  .soil-parameters {
    grid-template-columns: repeat(2, 1fr);
  }

  .timeline-content {
    padding: 25px;
  }

  .image-preview {
    width: 180px;
    height: 135px;
  }

  .chart-legend {
    gap: 20px;
  }
}

@media (max-width: 480px) {
  .toggle-button {
    padding: 8px 14px;
    font-size: 12px;
  }

  .header h1 {
    font-size: 1.8rem;
  }

  .header p {
    font-size: 1rem;
  }

  .timeline-content {
    padding: 20px;
  }

  .conditions {
    grid-template-columns: 1fr;
  }

  .soil-parameters {
    grid-template-columns: 1fr;
  }

  .image-preview {
    width: 160px;
    height: 120px;
  }

  .management-section {
    padding: 12px;
  }

  .chart-legend {
    gap: 15px;
    flex-direction: column;
    align-items: center;
  }
}

/* 动画关键帧 */
@keyframes float {
  0%,
  100% {
    transform: translateY(0px);
  }
  50% {
    transform: translateY(-6px);
  }
}

/* 滚动条美化 */
::-webkit-scrollbar {
  width: 8px;
}

::-webkit-scrollbar-track {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 4px;
}

::-webkit-scrollbar-thumb {
  background: linear-gradient(135deg, #667eea, #764ba2);
  border-radius: 4px;
}

::-webkit-scrollbar-thumb:hover {
  background: linear-gradient(135deg, #5a67d8, #6b46c1);
}
</style>
