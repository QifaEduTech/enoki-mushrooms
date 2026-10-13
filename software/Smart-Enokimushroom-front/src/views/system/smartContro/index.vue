<template>
  <div class="enokimushroom-smart-control">
    <div class="container">
      <div class="header">
        <h1>🍄 金针菇智能控制系统</h1>
        <p>实时监控金针菇生长状态，智能调节环境参数</p>
        <!-- 大棚区域选择 -->
        <div class="area-selector">
          <label>选择大棚区域:</label>
          <select v-model="selectedArea" @change="onAreaChange" class="area-select">
            <option value="">请选择大棚区域</option>
            <option v-for="area in areas" :key="area.id" :value="area.id">
              {{ area.name }} ({{ area.growthStage }})
            </option>
          </select>
          <div v-if="selectedArea" class="area-info">
            <span class="area-name">{{ getCurrentAreaName() }}</span>
            <span class="growth-stage">{{ getCurrentGrowthStage() }}</span>
            <span class="planted-days">已种植 {{ getCurrentPlantedDays() }} 天</span>
          </div>
        </div>
        
        <div class="refresh-controls">
          <button @click="manualRefresh" class="refresh-btn">
            🔄 手动刷新
          </button>
          <span class="refresh-status"
            >自动刷新: 每{{ refreshInterval / 1000 }}秒</span
          >
        </div>
      </div>

      <!-- 实时状态概览 -->
      <div class="section air-parameters">
        <h2 class="section-title">🌤️ 空气环境参数</h2>
        <div class="status-grid air-grid">
          <div class="status-card air-card" :class="{ 'warning': isParameterAbnormal('temperature') }">
            <div class="status-icon">🌡️</div>
            <div class="status-info">
              <h3>空气温度</h3>
              <p class="status-value">{{ realTimeData.airTemperature }}°C</p>
              <p class="status-status" :class="getStatusClass('temperature')">
                {{ getStatusText("temperature") }}
              </p>
              <div v-if="selectedArea && growthRequirements" class="requirement-info">
                <span class="requirement-label">需求范围:</span>
                <span class="requirement-value">{{ growthRequirements.temperature.min }}°C - {{ growthRequirements.temperature.max }}°C</span>
              </div>
            </div>
          </div>
          <div class="status-card air-card" :class="{ 'warning': isParameterAbnormal('humidity') }">
            <div class="status-icon">💧</div>
            <div class="status-info">
              <h3>空气湿度</h3>
              <p class="status-value">{{ realTimeData.airHumidity }}%</p>
              <p class="status-status" :class="getStatusClass('humidity')">
                {{ getStatusText("humidity") }}
              </p>
              <div v-if="selectedArea && growthRequirements" class="requirement-info">
                <span class="requirement-label">需求范围:</span>
                <span class="requirement-value">{{ growthRequirements.humidity.min }}% - {{ growthRequirements.humidity.max }}%</span>
              </div>
            </div>
          </div>
          <div class="status-card air-card" :class="{ 'warning': isParameterAbnormal('light') }">
            <div class="status-icon">☀️</div>
            <div class="status-info">
              <h3>光照强度</h3>
              <p class="status-value">{{ realTimeData.lightLux }} Lux</p>
              <p class="status-status" :class="getStatusClass('light')">
                {{ getStatusText("light") }}
              </p>
              <div v-if="selectedArea && growthRequirements" class="requirement-info">
                <span class="requirement-label">需求范围:</span>
                <span class="requirement-value">{{ growthRequirements.light.min }} - {{ growthRequirements.light.max }} Lux</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 土壤参数 -->
      <div class="section soil-parameters">
        <h2 class="section-title">🌱 土壤环境参数</h2>
        <div class="status-grid soil-grid">
          <div class="status-card soil-card" :class="{ 'warning': isParameterAbnormal('soil') }">
            <div class="status-icon">🌱</div>
            <div class="status-info">
              <h3>土壤湿度</h3>
              <p class="status-value">{{ realTimeData.soilHumidity }}%</p>
              <p class="status-status" :class="getStatusClass('soil')">
                {{ getStatusText("soil") }}
              </p>
              <div v-if="selectedArea && growthRequirements" class="requirement-info">
                <span class="requirement-label">需求范围:</span>
                <span class="requirement-value">{{ growthRequirements.soil.min }}% - {{ growthRequirements.soil.max }}%</span>
              </div>
            </div>
          </div>
          <div class="status-card soil-card" :class="{ 'warning': isNutritionAbnormal('soilTemp') }">
            <div class="status-icon">🌡️</div>
            <div class="status-info">
              <h3>土壤温度</h3>
              <p class="status-value">{{ realTimeData.soilTemperature }}°C</p>
              <p class="status-status" :class="getStatusClass('soilTemp')">
                {{ getStatusText("soilTemp") }}
              </p>
              <div v-if="selectedArea && growthRequirements" class="requirement-info">
                <span class="requirement-label">需求范围:</span>
                <span class="requirement-value">{{ growthRequirements.soilTemp.min }}°C - {{ growthRequirements.soilTemp.max }}°C</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 土壤营养详情 -->
      <div class="section nutrition-details">
        <h2 class="section-title">🧪 土壤营养详情</h2>
        <div class="nutrition-grid">
          <div class="nutrition-item" :class="{ 'warning': isNutritionAbnormal('pH') }">
            <div class="nutrition-icon">🧪</div>
            <div class="nutrition-info">
              <h4>土壤pH值</h4>
              <p class="nutrition-value">{{ realTimeData.soilPh }}</p>
              <div v-if="selectedArea && growthRequirements" class="requirement-info">
                <span class="requirement-label">需求:</span>
                <span class="requirement-value">{{ growthRequirements.pH.min }} - {{ growthRequirements.pH.max }}</span>
              </div>
            </div>
          </div>
          <div class="nutrition-item" :class="{ 'warning': isNutritionAbnormal('conductivity') }">
            <div class="nutrition-icon">🔋</div>
            <div class="nutrition-info">
              <h4>电导率</h4>
              <p class="nutrition-value">
                {{ realTimeData.soilConductivity }} mS/cm
              </p>
              <div v-if="selectedArea && growthRequirements" class="requirement-info">
                <span class="requirement-label">需求:</span>
                <span class="requirement-value">{{ growthRequirements.conductivity.min }} - {{ growthRequirements.conductivity.max }} mS/cm</span>
              </div>
            </div>
          </div>
          <div class="nutrition-item" :class="{ 'warning': isNutritionAbnormal('N') }">
            <div class="nutrition-icon">N</div>
            <div class="nutrition-info">
              <h4>氮含量</h4>
              <p class="nutrition-value">{{ realTimeData.soilN }} mg/kg</p>
              <div v-if="selectedArea && growthRequirements" class="requirement-info">
                <span class="requirement-label">需求:</span>
                <span class="requirement-value">{{ growthRequirements.N.min }} - {{ growthRequirements.N.max }} mg/kg</span>
              </div>
            </div>
          </div>
          <div class="nutrition-item" :class="{ 'warning': isNutritionAbnormal('P') }">
            <div class="nutrition-icon">P</div>
            <div class="nutrition-info">
              <h4>磷含量</h4>
              <p class="nutrition-value">{{ realTimeData.soilP }} mg/kg</p>
              <div v-if="selectedArea && growthRequirements" class="requirement-info">
                <span class="requirement-label">需求:</span>
                <span class="requirement-value">{{ growthRequirements.P.min }} - {{ growthRequirements.P.max }} mg/kg</span>
              </div>
            </div>
          </div>
          <div class="nutrition-item" :class="{ 'warning': isNutritionAbnormal('K') }">
            <div class="nutrition-icon">K</div>
            <div class="nutrition-info">
              <h4>钾含量</h4>
              <p class="nutrition-value">{{ realTimeData.soilK }} mg/kg</p>
              <div v-if="selectedArea && growthRequirements" class="requirement-info">
                <span class="requirement-label">需求:</span>
                <span class="requirement-value">{{ growthRequirements.K.min }} - {{ growthRequirements.K.max }} mg/kg</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 智能控制面板 -->
      <div class="section control-panel">
        <h2 class="section-title">🎛️ 智能控制面板</h2>
        <div class="control-content">
          <!-- 风扇控制 -->
          <div class="control-section">
            <h3>🌪️ 风扇控制</h3>
            <div class="control-buttons">
              <button
                @click="controlFan('start')"
                :disabled="fanStatus === 'running'"
                class="control-btn start-btn"
              >
                启动风扇
              </button>
              <button
                @click="controlFan('stop')"
                :disabled="fanStatus === 'stopped'"
                class="control-btn stop-btn"
              >
                停止风扇
              </button>
              <div class="speed-control">
                <label>风速等级:</label>
                <select v-model="fanSpeed" :disabled="fanStatus === 'stopped'">
                  <option value="1">1级 (低速)</option>
                  <option value="2">2级 (中低速)</option>
                  <option value="3">3级 (中速)</option>
                  <option value="4">4级 (中高速)</option>
                  <option value="5">5级 (高速)</option>
                </select>
              </div>
            </div>
            <div class="status-indicator">
              <span class="status-dot" :class="fanStatus"></span>
              {{ getFanStatusText() }}
            </div>
          </div>

          <!-- 滴灌控制 -->
          <div class="control-section">
            <h3>💧 滴灌控制</h3>
            <div class="control-buttons">
              <button
                @click="controlIrrigation('start')"
                :disabled="irrigationStatus === 'running'"
                class="control-btn start-btn"
              >
                启动滴灌
              </button>
              <button
                @click="controlIrrigation('stop')"
                :disabled="irrigationStatus === 'stopped'"
                class="control-btn stop-btn"
              >
                停止滴灌
              </button>
              <div class="irrigation-settings">
                <div class="setting-item">
                  <label>持续时间 (秒):</label>
                  <input
                    type="number"
                    v-model="irrigationDuration"
                    min="60"
                    max="3600"
                    :disabled="irrigationStatus === 'running'"
                  />
                </div>
                <div class="setting-item">
                  <label>强度 (%):</label>
                  <input
                    type="number"
                    v-model="irrigationIntensity"
                    min="10"
                    max="100"
                    :disabled="irrigationStatus === 'running'"
                  />
                </div>
              </div>
            </div>
            <div class="status-indicator">
              <span class="status-dot" :class="irrigationStatus"></span>
              {{ getIrrigationStatusText() }}
            </div>
          </div>

          <!-- 自动模式切换 -->
          <div class="control-section auto-mode-section">
            <h3>🤖 自动模式</h3>
            <div class="auto-mode-toggle">
              <label class="switch">
                <input
                  type="checkbox"
                  v-model="autoMode"
                  @change="toggleAutoMode"
                />
                <span class="slider"></span>
              </label>
              <span class="mode-text">{{
                autoMode ? "自动模式已开启" : "手动模式"
              }}</span>
            </div>
            <p class="mode-description">
              {{
                autoMode
                  ? "系统将根据环境参数自动调节风扇和滴灌"
                  : "需要手动控制设备"
              }}
            </p>
          </div>
        </div>
      </div>

      <!-- 设备状态日志 -->
      <div class="section log-section">
        <h2 class="section-title">📝 操作日志</h2>
        <div class="log-container">
          <div
            v-for="(log, index) in operationLogs"
            :key="index"
            class="log-item"
            :class="log.type"
          >
            <span class="log-time">{{ log.timestamp }}</span>
            <span class="log-device">{{ log.device }}</span>
            <span class="log-action">{{ log.action }}</span>
            <span class="log-status">{{ log.status }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { listRecord, controlLowPour } from "@/api/system/record";

export default {
  name: "EnokimushroomSmartControl",
  data() {
    return {
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 100,
      },

      // 实时数据
      realTimeData: {
        airTemperature: 0,
        airHumidity: 0,
        lightLux: 0,
        soilHumidity: 0,
        soilTemperature: 0,
        airCo2: 0,
        soilPh: 0,
        soilConductivity: 0,
        soilN: 0,
        soilP: 0,
        soilK: 0,
      },

      // 设备状态
      fanStatus: "stopped",
      irrigationStatus: "stopped",
      autoMode: false,

      // 控制参数
      fanSpeed: 3,
      irrigationDuration: 300,
      irrigationIntensity: 80,

      // 操作日志
      operationLogs: [],

      // 数据刷新
      refreshTimer: null,
      refreshInterval: 5000, // 5秒刷新一次

      // 数据加载状态
      dataLoaded: false,
      
      // 大棚区域选择
      selectedArea: '',
      
      // 大棚区域数据
      areas: [
        { 
          id: '1', 
          name: '大棚A1', 
          growthStage: '幼苗期', 
          plantedDays: 15,
          requirements: {
            temperature: { min: 20, max: 25 },
            humidity: { min: 70, max: 85 },
            light: { min: 8000, max: 12000 },
            soil: { min: 70, max: 85 },
            pH: { min: 5.5, max: 6.5 },
            conductivity: { min: 1.0, max: 2.0 },
            N: { min: 120, max: 180 },
            P: { min: 80, max: 120 },
            K: { min: 150, max: 200 },
            soilTemp: { min: 18, max: 24 }
          }
        },
        { 
          id: '2', 
          name: '大棚A2', 
          growthStage: '生长期', 
          plantedDays: 35,
          requirements: {
            temperature: { min: 22, max: 28 },
            humidity: { min: 65, max: 80 },
            light: { min: 10000, max: 15000 },
            soil: { min: 65, max: 80 },
            pH: { min: 5.8, max: 6.8 },
            conductivity: { min: 1.5, max: 2.5 },
            N: { min: 150, max: 200 },
            P: { min: 100, max: 150 },
            K: { min: 180, max: 250 },
            soilTemp: { min: 20, max: 26 }
          }
        },
        { 
          id: '3', 
          name: '大棚A3', 
          growthStage: '开花期', 
          plantedDays: 55,
          requirements: {
            temperature: { min: 18, max: 24 },
            humidity: { min: 60, max: 75 },
            light: { min: 12000, max: 18000 },
            soil: { min: 60, max: 75 },
            pH: { min: 6.0, max: 7.0 },
            conductivity: { min: 2.0, max: 3.0 },
            N: { min: 100, max: 150 },
            P: { min: 120, max: 180 },
            K: { min: 200, max: 300 },
            soilTemp: { min: 18, max: 22 }
          }
        },
        { 
          id: '4', 
          name: '大棚B1', 
          growthStage: '结果期', 
          plantedDays: 75,
          requirements: {
            temperature: { min: 20, max: 26 },
            humidity: { min: 55, max: 70 },
            light: { min: 10000, max: 16000 },
            soil: { min: 55, max: 70 },
            pH: { min: 6.2, max: 7.2 },
            conductivity: { min: 2.5, max: 3.5 },
            N: { min: 80, max: 120 },
            P: { min: 150, max: 200 },
            K: { min: 250, max: 350 },
            soilTemp: { min: 19, max: 25 }
          }
        }
      ],
      
      // 当前生长需求
      growthRequirements: null,
    };
  },

  created() {
    this.getList();
  },

  mounted() {
    this.startAutoRefresh();
    this.loadOperationLogs();
  },

  beforeDestroy() {
    this.cleanup();
  },

  methods: {
    // 获取真实数据
    getList() {
      console.log("开始请求金针菇生长环境数据...");
      listRecord(this.queryParams)
        .then((response) => {
          console.log("API响应数据:", response);
          if (response && response.rows && response.rows.length > 0) {
            // 获取最新的数据
            const latestData = response.rows[0];
            console.log("最新数据:", latestData);

            // 更新实时数据
            this.realTimeData = {
              airTemperature: latestData.airTemperature || 0,
              airHumidity: latestData.airHumidity || 0,
              lightLux: latestData.lightLux || 0,
              soilHumidity: latestData.soilHumidity || 0,
              soilTemperature: latestData.soilTemperature || 0,
              airCo2: latestData.airCo2 || 0,
              soilPh: latestData.soilPh || 0,
              soilConductivity: latestData.soilConductivity || 0,
              soilN: latestData.soilN || 0,
              soilP: latestData.soilP || 0,
              soilK: latestData.soilK || 0,
            };

            this.dataLoaded = true;
            console.log("实时数据已更新:", this.realTimeData);

            // 自动模式下的智能控制
            if (this.autoMode) {
              this.autoControl();
            }
          } else {
            console.warn("API响应格式异常或无数据:", response);
          }
        })
        .catch((error) => {
          console.error("请求失败:", error);
          this.addLog("system", "数据获取", "失败", "error");
        });
    },

    // 启动自动刷新
    startAutoRefresh() {
      console.log("启动自动刷新，间隔:", this.refreshInterval + "ms");
      this.refreshTimer = setInterval(() => {
        console.log("定时器触发，刷新数据...");
        this.getList();
      }, this.refreshInterval);
    },

    // 停止自动刷新
    stopAutoRefresh() {
      if (this.refreshTimer) {
        console.log("停止自动刷新");
        clearInterval(this.refreshTimer);
        this.refreshTimer = null;
      }
    },

    // 手动刷新数据
    manualRefresh() {
      console.log("手动刷新数据");
      this.getList();
    },

    // 风扇控制
    async controlFan(action) {
      try {
        const command = {
          deviceId: "cclr04",
          command: "fan",
          action: action,
          speed: this.fanSpeed,
          timestamp: new Date().toISOString(),
        };

        console.log("发送风扇控制指令:", command);

        // 风扇控制API调用
        if (action === "start") {
          await controlLowPour("fan_start"); // 风扇启动
          this.fanStatus = "running";
          this.addLog("fan", "启动", "成功", "success");
        } else {
          await controlLowPour("fan_stop"); // 风扇停止
          this.fanStatus = "stopped";
          this.addLog("fan", "停止", "成功", "success");
        }
      } catch (error) {
        console.error("风扇控制失败:", error);
        this.addLog(
          "fan",
          action === "start" ? "启动" : "停止",
          "失败",
          "error"
        );
      }
    },

    // 滴灌控制
    async controlIrrigation(action) {
      try {
        const command = {
          deviceId: "cclr04",
          command: "irrigation",
          action: action,
          duration: this.irrigationDuration,
          intensity: this.irrigationIntensity,
          timestamp: new Date().toISOString(),
        };

        console.log("发送滴灌控制指令:", command);

        // 滴灌控制API调用
        if (action === "start") {
          await controlLowPour("irrigation_start"); // 滴灌启动
          this.irrigationStatus = "running";
          this.addLog("irrigation", "启动", "成功", "success");

          // 设置自动停止定时器
          setTimeout(() => {
            if (this.irrigationStatus === "running") {
              this.controlIrrigation("stop");
            }
          }, this.irrigationDuration * 1000);
        } else {
          await controlLowPour("irrigation_stop"); // 滴灌停止
          this.irrigationStatus = "stopped";
          this.addLog("irrigation", "停止", "成功", "success");
        }
      } catch (error) {
        console.error("滴灌控制失败:", error);
        this.addLog(
          "irrigation",
          action === "start" ? "启动" : "停止",
          "失败",
          "error"
        );
      }
    },

    // 切换自动模式
    toggleAutoMode() {
      if (this.autoMode) {
        this.addLog("system", "自动模式", "已开启", "info");
      } else {
        this.addLog("system", "自动模式", "已关闭", "info");
      }
    },

    // 自动控制逻辑
    autoControl() {
      // 温度过高时自动开启风扇
      if (
        this.realTimeData.airTemperature > 26 &&
        this.fanStatus === "stopped"
      ) {
        this.controlFan("start");
        this.addLog("auto", "温度过高", "自动开启风扇", "warning");
      }

      // 温度适宜时自动关闭风扇
      if (
        this.realTimeData.airTemperature < 22 &&
        this.fanStatus === "running"
      ) {
        this.controlFan("stop");
        this.addLog("auto", "温度适宜", "自动关闭风扇", "info");
      }

      // 土壤湿度过低时自动开启滴灌
      if (
        this.realTimeData.soilHumidity < 65 &&
        this.irrigationStatus === "stopped"
      ) {
        this.controlIrrigation("start");
        this.addLog("auto", "土壤湿度过低", "自动开启滴灌", "warning");
      }

      // 土壤湿度适宜时自动关闭滴灌
      if (
        this.realTimeData.soilHumidity > 80 &&
        this.irrigationStatus === "running"
      ) {
        this.controlIrrigation("stop");
        this.addLog("auto", "土壤湿度适宜", "自动关闭滴灌", "info");
      }
    },

    // 添加操作日志
    addLog(device, action, status, type) {
      const log = {
        timestamp: new Date().toLocaleString(),
        device: this.getDeviceName(device),
        action: action,
        status: status,
        type: type,
      };

      this.operationLogs.unshift(log);

      // 限制日志数量
      if (this.operationLogs.length > 50) {
        this.operationLogs = this.operationLogs.slice(0, 50);
      }
    },

    // 获取设备名称
    getDeviceName(device) {
      const deviceNames = {
        fan: "风扇",
        irrigation: "滴灌系统",
        auto: "自动控制",
        system: "系统",
      };
      return deviceNames[device] || device;
    },

    // 加载操作日志
    loadOperationLogs() {
      // 模拟加载历史日志
      const mockLogs = [
        {
          timestamp: "2026-04-15 10:00:00",
          device: "系统",
          action: "系统启动",
          status: "成功",
          type: "info",
        },
        {
          timestamp: "2026-04-15 09:36:44",
          device: "滴灌系统",
          action: "启动",
          status: "成功",
          type: "success",
        },
        {
          timestamp: "2026-04-15 09:03:28",
          device: "风扇",
          action: "启动",
          status: "成功",
          type: "success",
        },
      ];
      this.operationLogs = mockLogs;
    },

    // 获取状态样式类
    getStatusClass(type) {
      const value = this.realTimeData[this.getStatusKey(type)];
      const thresholds = this.getThresholds(type);

      if (value < thresholds.min) return "low";
      if (value > thresholds.max) return "high";
      return "normal";
    },

    // 获取状态文本
    getStatusText(type) {
      const value = this.realTimeData[this.getStatusKey(type)];
      const thresholds = this.getThresholds(type);

      if (value < thresholds.min) return "偏低";
      if (value > thresholds.max) return "偏高";
      return "正常";
    },

    // 获取状态键名
    getStatusKey(type) {
      const keyMap = {
        temperature: "airTemperature",
        humidity: "airHumidity",
        light: "lightLux",
        soil: "soilHumidity",
        soilTemp: "soilTemperature",
      };
      return keyMap[type];
    },

    // 获取阈值
    getThresholds(type) {
      const thresholds = {
        temperature: { min: 18, max: 26 },
        humidity: { min: 60, max: 80 },
        light: { min: 8000, max: 15000 },
        soil: { min: 65, max: 80 },
        soilTemp: { min: 18, max: 26 },
      };
      return thresholds[type];
    },

    // 获取风扇状态文本
    getFanStatusText() {
      return this.fanStatus === "running" ? "运行中" : "已停止";
    },

    // 获取滴灌状态文本
    getIrrigationStatusText() {
      return this.irrigationStatus === "running" ? "运行中" : "已停止";
    },

    // 清理资源
    cleanup() {
      this.stopAutoRefresh();
    },
    
    // 大棚区域变化处理
    onAreaChange() {
      if (this.selectedArea) {
        const area = this.areas.find(a => a.id === this.selectedArea);
        if (area) {
          this.growthRequirements = area.requirements;
          this.addLog('system', '切换大棚区域', `${area.name} - ${area.growthStage}`, 'info');
        }
      } else {
        this.growthRequirements = null;
      }
    },
    
    // 获取当前区域名称
    getCurrentAreaName() {
      const area = this.areas.find(a => a.id === this.selectedArea);
      return area ? area.name : '';
    },
    
    // 获取当前生长阶段
    getCurrentGrowthStage() {
      const area = this.areas.find(a => a.id === this.selectedArea);
      return area ? area.growthStage : '';
    },
    
    // 获取当前种植天数
    getCurrentPlantedDays() {
      const area = this.areas.find(a => a.id === this.selectedArea);
      return area ? area.plantedDays : 0;
    },
    
    // 判断环境参数是否异常
    isParameterAbnormal(type) {
      if (!this.selectedArea || !this.growthRequirements) return false;
      
      const value = this.realTimeData[this.getStatusKey(type)];
      const requirements = this.growthRequirements[type];
      
      if (!requirements || value === undefined) return false;
      
      return value < requirements.min || value > requirements.max;
    },
    
    // 判断营养参数是否异常
    isNutritionAbnormal(type) {
      if (!this.selectedArea || !this.growthRequirements) return false;
      
      let value;
      let requirements;
      
      switch(type) {
        case 'pH':
          value = this.realTimeData.soilPh;
          requirements = this.growthRequirements.pH;
          break;
        case 'conductivity':
          value = this.realTimeData.soilConductivity;
          requirements = this.growthRequirements.conductivity;
          break;
        case 'N':
          value = this.realTimeData.soilN;
          requirements = this.growthRequirements.N;
          break;
        case 'P':
          value = this.realTimeData.soilP;
          requirements = this.growthRequirements.P;
          break;
        case 'K':
          value = this.realTimeData.soilK;
          requirements = this.growthRequirements.K;
          break;
        case 'soilTemp':
          value = this.realTimeData.soilTemperature;
          requirements = this.growthRequirements.soilTemp;
          break;
        default:
          return false;
      }
      
      if (!requirements || value === undefined) return false;
      
      return value < requirements.min || value > requirements.max;
    },
  },
};
</script>

<style scoped>
.enokimushroom-smart-control {
  font-family: "Microsoft YaHei", -apple-system, BlinkMacSystemFont, sans-serif;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  min-height: 100vh;
  overflow-x: hidden;
  position: relative;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 40px 20px;
  position: relative;
}

.header {
  text-align: center;
  margin-bottom: 40px;
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
  margin: 0 0 20px 0;
}

/* 大棚区域选择器 */
.area-selector {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 15px;
  padding: 20px;
  margin-bottom: 20px;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.area-selector label {
  display: block;
  color: white;
  font-weight: 600;
  margin-bottom: 10px;
  font-size: 1rem;
}

.area-select {
  width: 100%;
  max-width: 300px;
  padding: 12px 16px;
  border: none;
  border-radius: 10px;
  font-size: 1rem;
  background: rgba(255, 255, 255, 0.9);
  color: #333;
  cursor: pointer;
  transition: all 0.3s ease;
}

.area-select:focus {
  outline: none;
  box-shadow: 0 0 0 3px rgba(255, 255, 255, 0.3);
}

.area-info {
  display: flex;
  gap: 15px;
  margin-top: 15px;
  flex-wrap: wrap;
  justify-content: center;
}

.area-name,
.growth-stage,
.planted-days {
  background: rgba(255, 255, 255, 0.2);
  color: white;
  padding: 8px 12px;
  border-radius: 20px;
  font-size: 0.9rem;
  font-weight: 500;
  backdrop-filter: blur(5px);
}

.refresh-controls {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 15px;
  margin-top: 20px;
}

.refresh-btn {
  background: rgba(255, 255, 255, 0.2);
  color: white;
  padding: 8px 16px;
  border-radius: 20px;
  border: 1px solid rgba(255, 255, 255, 0.3);
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s ease;
  backdrop-filter: blur(10px);
}

.refresh-btn:hover {
  background: rgba(255, 255, 255, 0.3);
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
}

.refresh-status {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.8);
  background: rgba(0, 0, 0, 0.2);
  padding: 6px 12px;
  border-radius: 12px;
  backdrop-filter: blur(5px);
}

/* 通用节样式 */
.section {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 20px;
  padding: 30px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.15);
  backdrop-filter: blur(10px);
  margin-bottom: 40px;
}

.section-title {
  text-align: center;
  color: #333;
  margin-bottom: 25px;
  font-size: 1.8rem;
  font-weight: 600;
  padding-bottom: 15px;
  border-bottom: 2px solid rgba(103, 126, 234, 0.2);
}

/* 空气参数区域 */
.air-parameters {
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.95), rgba(255, 248, 220, 0.95));
  border-left: 5px solid #ffa726;
}

.air-parameters .section-title {
  color: #d84315;
  border-bottom-color: #ffa726;
}

.status-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 20px;
}

.status-card {
  border-radius: 15px;
  padding: 20px;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(10px);
  display: flex;
  align-items: center;
  gap: 15px;
  transition: all 0.3s ease;
}

.status-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}

.status-card.warning {
  background: rgba(255, 193, 7, 0.15);
  border: 2px solid #ffc107;
  animation: warning-pulse 2s infinite;
}

@keyframes warning-pulse {
  0%, 100% { 
    box-shadow: 0 5px 15px rgba(255, 193, 7, 0.3);
  }
  50% { 
    box-shadow: 0 5px 15px rgba(255, 193, 7, 0.6);
  }
}

/* 空气卡片特色 */
.air-card {
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.9), rgba(240, 248, 255, 0.9));
  border: 1px solid rgba(173, 216, 230, 0.3);
}

.air-card .status-icon {
  background: linear-gradient(135deg, #4fc3f7, #29b6f6);
}

/* 土壤参数区域 */
.soil-parameters {
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.95), rgba(245, 245, 245, 0.95));
  border-left: 5px solid #8d6e63;
}

.soil-parameters .section-title {
  color: #5d4037;
  border-bottom-color: #8d6e63;
}

/* 土壤卡片特色 */
.soil-card {
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.9), rgba(245, 245, 245, 0.9));
  border: 1px solid rgba(141, 110, 99, 0.3);
}

.soil-card .status-icon {
  background: linear-gradient(135deg, #8d6e63, #6d4c41);
}

/* 营养详情区域 */
.nutrition-details {
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.95), rgba(248, 248, 255, 0.95));
  border-left: 5px solid #ab47bc;
}

.nutrition-details .section-title {
  color: #7b1fa2;
  border-bottom-color: #ab47bc;
}

.nutrition-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 15px;
}

.nutrition-item {
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.9), rgba(248, 248, 255, 0.9));
  border-radius: 12px;
  padding: 18px;
  text-align: center;
  border: 1px solid rgba(171, 71, 188, 0.2);
  transition: all 0.3s ease;
}

.nutrition-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(171, 71, 188, 0.1);
}

.nutrition-item.warning {
  background: linear-gradient(135deg, rgba(255, 193, 7, 0.2), rgba(255, 224, 178, 0.2));
  border: 2px solid #ffc107;
  animation: nutrition-warning-pulse 2s infinite;
}

@keyframes nutrition-warning-pulse {
  0%, 100% { 
    box-shadow: 0 5px 15px rgba(255, 193, 7, 0.2);
  }
  50% { 
    box-shadow: 0 5px 15px rgba(255, 193, 7, 0.4);
  }
}

.nutrition-icon {
  font-size: 1.8rem;
  margin-bottom: 8px;
  width: 45px;
  height: 45px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: linear-gradient(135deg, #ab47bc, #8e24aa);
  color: white;
  margin: 0 auto 10px;
}

.nutrition-info h4 {
  margin: 0 0 8px 0;
  color: #7b1fa2;
  font-size: 0.9rem;
  font-weight: 600;
}

.nutrition-value {
  margin: 0 0 8px 0;
  font-size: 1.1rem;
  font-weight: bold;
  color: #7b1fa2;
}

/* 控制面板区域 */
.control-panel {
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.95), rgba(240, 248, 240, 0.95));
  border-left: 5px solid #4caf50;
}

.control-panel .section-title {
  color: #2e7d32;
  border-bottom-color: #4caf50;
}

.control-content {
  display: flex;
  flex-direction: column;
  gap: 25px;
}

.control-section {
  padding: 20px;
  background: rgba(76, 175, 80, 0.05);
  border-radius: 15px;
  border-left: 3px solid #4caf50;
}

.control-section h3 {
  color: #2e7d32;
  margin-bottom: 15px;
  font-size: 1.2rem;
  display: flex;
  align-items: center;
  gap: 10px;
}

.control-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
  align-items: center;
  margin-bottom: 15px;
}

.control-btn {
  padding: 10px 20px;
  border: none;
  border-radius: 20px;
  font-size: 0.9rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  min-width: 100px;
}

.start-btn {
  background: linear-gradient(135deg, #4caf50, #2e7d32);
  color: white;
}

.start-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(76, 175, 80, 0.4);
}

.stop-btn {
  background: linear-gradient(135deg, #f44336, #c62828);
  color: white;
}

.stop-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(244, 67, 54, 0.4);
}

.control-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

.speed-control,
.irrigation-settings {
  display: flex;
  gap: 15px;
  align-items: center;
  flex-wrap: wrap;
}

.setting-item {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.setting-item label {
  font-size: 0.8rem;
  color: #666;
  font-weight: 500;
}

.setting-item input,
.speed-control select {
  padding: 6px 10px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 0.8rem;
  min-width: 80px;
}

.status-indicator {
  display: flex;
  align-items: center;
  gap: 10px;
  font-weight: 500;
  color: #2e7d32;
  font-size: 0.9rem;
}

.status-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  display: inline-block;
}

.status-dot.running {
  background: #4caf50;
  animation: pulse 2s infinite;
}

.status-dot.stopped {
  background: #f44336;
}

@keyframes pulse {
  0% {
    opacity: 1;
    transform: scale(1);
  }
  50% {
    opacity: 0.7;
    transform: scale(1.2);
  }
  100% {
    opacity: 1;
    transform: scale(1);
  }
}

/* 自动模式切换 */
.auto-mode-section {
  border-left: 3px solid #ff9800;
  background: rgba(255, 152, 0, 0.05);
}

.auto-mode-section h3 {
  color: #ef6c00;
}

.auto-mode-toggle {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 10px;
}

.switch {
  position: relative;
  display: inline-block;
  width: 50px;
  height: 24px;
}

.switch input {
  opacity: 0;
  width: 0;
  height: 0;
}

.slider {
  position: absolute;
  cursor: pointer;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: #ccc;
  transition: 0.4s;
  border-radius: 24px;
}

.slider:before {
  position: absolute;
  content: "";
  height: 18px;
  width: 18px;
  left: 3px;
  bottom: 3px;
  background-color: white;
  transition: 0.4s;
  border-radius: 50%;
}

input:checked + .slider {
  background-color: #ff9800;
}

input:checked + .slider:before {
  transform: translateX(26px);
}

.mode-text {
  font-weight: 600;
  color: #ef6c00;
  font-size: 0.9rem;
}

.mode-description {
  color: #666;
  margin: 0;
  font-size: 0.85rem;
  line-height: 1.4;
}

/* 日志区域 */
.log-section {
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.95), rgba(248, 249, 250, 0.95));
  border-left: 5px solid #607d8b;
}

.log-section .section-title {
  color: #37474f;
  border-bottom-color: #607d8b;
}

.log-container {
  max-height: 300px;
  overflow-y: auto;
  border: 1px solid rgba(96, 125, 139, 0.1);
  border-radius: 8px;
  padding: 10px;
}

.log-item {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr 1fr;
  gap: 10px;
  padding: 10px 12px;
  margin-bottom: 6px;
  border-radius: 6px;
  font-size: 0.85rem;
  transition: all 0.2s ease;
  background: rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(96, 125, 139, 0.1);
}

.log-item:hover {
  transform: translateX(3px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.log-item.success {
  background: rgba(40, 167, 69, 0.1);
  border-left: 3px solid #28a745;
}

.log-item.error {
  background: rgba(220, 53, 69, 0.1);
  border-left: 3px solid #dc3545;
}

.log-item.warning {
  background: rgba(255, 193, 7, 0.1);
  border-left: 3px solid #ffc107;
}

.log-item.info {
  background: rgba(23, 162, 184, 0.1);
  border-left: 3px solid #17a2b8;
}

.log-time {
  color: #666;
  font-weight: 500;
  font-size: 0.8rem;
}

.log-device {
  color: #333;
  font-weight: 600;
}

.log-action {
  color: #607d8b;
  font-weight: 500;
}

.log-status {
  color: #333;
  font-weight: 500;
}

.requirement-info {
  margin-top: 6px;
  padding: 4px 8px;
  background: rgba(0, 0, 0, 0.05);
  border-radius: 6px;
  font-size: 0.75rem;
}

.requirement-label {
  color: #666;
  font-weight: 500;
  margin-right: 4px;
}

.requirement-value {
  color: #333;
  font-weight: 600;
}

.status-icon {
  font-size: 1.8rem;
  width: 50px;
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  color: white;
  flex-shrink: 0;
}

.status-info h3 {
  margin: 0 0 6px 0;
  color: #333;
  font-size: 1rem;
  font-weight: 600;
}

.status-value {
  font-size: 1.5rem;
  font-weight: bold;
  color: #667eea;
  margin: 0 0 4px 0;
}

.status-status {
  margin: 0 0 6px 0;
  font-size: 0.85rem;
  font-weight: 500;
  padding: 3px 8px;
  border-radius: 8px;
  display: inline-block;
}

.status-status.normal {
  background: #d4edda;
  color: #155724;
}

.status-status.low {
  background: #fff3cd;
  color: #856404;
}

.status-status.high {
  background: #f8d7da;
  color: #721c24;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .container {
    padding: 20px 10px;
  }
  
  .header h1 {
    font-size: 1.8rem;
  }
  
  .area-selector {
    padding: 15px;
  }
  
  .area-select {
    max-width: 100%;
  }
  
  .area-info {
    flex-direction: column;
    align-items: center;
    gap: 8px;
  }
  
  .section {
    padding: 20px;
  }
  
  .status-grid {
    grid-template-columns: 1fr;
  }
  
  .nutrition-grid {
    grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));
  }
  
  .control-buttons {
    flex-direction: column;
    align-items: stretch;
  }
  
  .speed-control,
  .irrigation-settings {
    flex-direction: column;
    align-items: stretch;
    gap: 8px;
  }
  
  .log-item {
    grid-template-columns: 1fr;
    gap: 3px;
  }
  
  .log-time,
  .log-device,
  .log-action,
  .log-status {
    text-align: center;
  }
  
  .section-title {
    font-size: 1.5rem;
  }
  
  .control-section {
    padding: 15px;
  }
}

@media (max-width: 480px) {
  .header h1 {
    font-size: 1.6rem;
  }

  .header p {
    font-size: 0.9rem;
  }

  .section-title {
    font-size: 1.3rem;
  }
  
  .nutrition-grid {
    grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
  }
  
  .nutrition-item {
    padding: 12px;
  }
  
  .status-card {
    padding: 15px;
    gap: 10px;
  }
  
  .status-icon {
    width: 40px;
    height: 40px;
    font-size: 1.4rem;
  }
  
  .status-value {
    font-size: 1.3rem;
  }
}

/* 滚动条美化 */
.log-container::-webkit-scrollbar {
  width: 6px;
}

.log-container::-webkit-scrollbar-track {
  background: rgba(96, 125, 139, 0.1);
  border-radius: 3px;
}

.log-container::-webkit-scrollbar-thumb {
  background: linear-gradient(135deg, #607d8b, #546e7a);
  border-radius: 3px;
}

.log-container::-webkit-scrollbar-thumb:hover {
  background: linear-gradient(135deg, #546e7a, #455a64);
}
</style>