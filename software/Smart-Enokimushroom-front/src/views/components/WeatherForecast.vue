<template>
  <el-card class="weather-forecast">
    <div slot="header" class="clearfix">
      <span class="title">天气预报与农事建议</span>
      <span class="location">{{ provinceName }} · {{ cityName }}</span>
      <el-tag type="info" style="float: right;">{{ updateTime }}</el-tag>
    </div>
    <div>
      <!-- 实况天气概要 -->
      <div v-if="liveWeather" class="live-summary">
        <el-tag type="success" size="small">当前</el-tag>
        <span class="live-text">
          {{ liveWeather.weather }} {{ liveWeather.temperature }}°C · 湿度 {{ liveWeather.humidity }}% · 风向 {{ liveWeather.winddirection }} · 风力 {{ liveWeather.windpower }}级
        </span>
      </div>
      <div v-else-if="loading" class="loading-state">
        <el-tag type="info" size="small">加载中</el-tag>
        <span class="loading-text">正在获取您的位置和天气信息...</span>
      </div>
      <div v-else-if="error" class="error-state">
        <el-tag type="danger" size="small">错误</el-tag>
        <span class="error-text">{{ error }}</span>
        <el-button type="primary" size="mini" @click="retryFetch">重试</el-button>
      </div>
      
      <!-- 天气卡片 -->
      <el-row :gutter="20">
        <el-col :span="4" v-for="(day, index) in weatherData" :key="index">
          <el-card :body-style="{ padding: '0px' }" class="weather-card">
            <div class="weather-icon">
              <i :class="day.icon"></i>
            </div>
            <div class="weather-info">
              <p class="info-day">{{ day.day }}</p>
              <p class="info-date">{{ day.date }}</p>
              <p class="info-temp">{{ day.high }}°C / {{ day.low }}°C</p>
              <p class="info-cond">{{ day.condition }}</p>
              <p class="info-wind">风力 {{ day.windPower }}级</p>
            </div>
          </el-card>
        </el-col>
      </el-row>
      <!-- 农事建议 -->
      <div class="agriculture-advice">
        <h4>农事管理建议</h4>
        <ul>
          <li v-for="(advice, index) in agricultureAdvice" :key="index" :class="advice.type">
            <i :class="advice.icon"></i>
            {{ advice.text }}
          </li>
        </ul>
      </div>
    </div>
  </el-card>
</template>

<script>
export default {
  name: 'WeatherForecast',
  data () {
    return {
      updateTime: this.getCurrentTime(),
      cityName: '正在定位...',
      provinceName: '正在定位...',
      liveWeather: null,
      weatherData: [],
      agricultureAdvice: [],
      loading: true,
      error: null,
      API_KEY: '5149adb91680882387c3c3afdd800b4d',
      fallbackCity: '郑州市',
      fallbackProvince: '河南省',
      CACHE_DURATION: 60 * 60 * 1000,
      CACHE_KEY: 'WEATHER_CACHE'
    };
  },
  mounted() {
    const cached = this.loadFromCache();
    if (cached) {
      this.applyCacheData(cached);
      this.loading = false;
    } else {
      this.fetchWeatherByIP();
    }
  },
  methods: {
    getCurrentTime() {
      const now = new Date();
      const year = now.getFullYear();
      const month = String(now.getMonth() + 1).padStart(2, '0');
      const day = String(now.getDate()).padStart(2, '0');
      const hour = String(now.getHours()).padStart(2, '0');
      const minute = String(now.getMinutes()).padStart(2, '0');
      return `${year}年${month}月${day}日 ${hour}:${minute}`;
    },
    saveToCache(data) {
      const cacheData = { timestamp: Date.now(), ...data };
      localStorage.setItem(this.CACHE_KEY, JSON.stringify(cacheData));
    },
    loadFromCache() {
      const raw = localStorage.getItem(this.CACHE_KEY);
      if (!raw) return null;
      try {
        const cache = JSON.parse(raw);
        if (Date.now() - cache.timestamp <= this.CACHE_DURATION) {
          return cache;
        } else {
          localStorage.removeItem(this.CACHE_KEY);
          return null;
        }
      } catch (e) {
        return null;
      }
    },
    applyCacheData(cache) {
      this.cityName = cache.cityName;
      this.provinceName = cache.provinceName;
      this.liveWeather = cache.liveWeather;
      this.weatherData = cache.weatherData;
      this.agricultureAdvice = cache.agricultureAdvice;
      this.updateTime = cache.updateTime;
    },
    async fetchWeatherByIP(cityOverride = null) {
      this.loading = true;
      this.error = null;
      this.liveWeather = null;
      this.weatherData = [];
      this.agricultureAdvice = [];

      // 安全处理城市字符串：转为字符串、去除首尾空格、去掉末尾的“市”
      const normalizeCity = (city) => {
        if (city == null) return '';
        const cityStr = String(city).trim();
        return cityStr.endsWith('市') ? cityStr.slice(0, -1) : cityStr;
      };

      try {
        if (cityOverride) {
          // 使用指定的城市（如 fallbackCity）
          const rawCity = String(cityOverride);
          const normalizedCityName = normalizeCity(rawCity);
          this.provinceName = this.fallbackProvince;
          this.cityName = normalizedCityName;
          // 高德天气 API 需要完整的城市名（带“市”后缀）
          const apiCityName = rawCity.endsWith('市') ? rawCity : rawCity + '市';
          const weatherInfo = await this.getLiveWeather(apiCityName);
          this.liveWeather = this.formatLiveWeather(weatherInfo);
          this.generateFutureWeather(weatherInfo);
          this.generateAdvice();
        } else {
          const locationInfo = await this.getCityByIP();
          let userProvince = locationInfo.province || '未知省份';
          let userCity = locationInfo.city || '';
          // 确保为字符串
          userProvince = String(userProvince).trim();
          userCity = String(userCity).trim();
          if (!userCity) {
            console.warn('IP 定位未返回城市，将使用省份名称查询天气');
            userCity = userProvince;
          }
          this.provinceName = userProvince;
          const normalizedCityName = normalizeCity(userCity);
          this.cityName = normalizedCityName;
          // 构造高德 API 需要的城市名（若没有“市”则添加）
          let apiCityName = userCity;
          if (!apiCityName.endsWith('市') && !apiCityName.endsWith('省') && !apiCityName.endsWith('县')) {
            apiCityName = apiCityName + '市';
          }
          const weatherInfo = await this.getLiveWeather(apiCityName);
          this.liveWeather = this.formatLiveWeather(weatherInfo);
          this.generateFutureWeather(weatherInfo);
          this.generateAdvice();
        }
        this.updateTime = this.getCurrentTime();
        this.saveToCache({
          cityName: this.cityName,
          provinceName: this.provinceName,
          liveWeather: this.liveWeather,
          weatherData: this.weatherData,
          agricultureAdvice: this.agricultureAdvice,
          updateTime: this.updateTime
        });
      } catch (err) {
        console.error("获取天气数据失败:", err);
        if (!cityOverride) {
          this.error = `获取天气失败，请点击“重试”`;
        } else {
          this.error = `获取${this.fallbackProvince}${cityOverride}天气失败: ${err.message}`;
        }
      } finally {
        this.loading = false;
        if (!this.error) {
          this.updateTime = this.getCurrentTime();
        }
      }
    },
    async getCityByIP() {
      const url = `https://restapi.amap.com/v3/ip?key=${this.API_KEY}`;
      const response = await fetch(url);
      if (!response.ok) throw new Error(`IP定位API请求失败: ${response.status}`);
      const data = await response.json();
      if (data.status !== '1') throw new Error(`高德IP定位API错误: ${data.info} (代码: ${data.infocode})`);
      return data;
    },
    async getLiveWeather(city) {
      const encodedCity = encodeURIComponent(city);
      const url = `https://restapi.amap.com/v3/weather/weatherInfo?key=${this.API_KEY}&city=${encodedCity}&extensions=base`;
      const response = await fetch(url);
      if (!response.ok) throw new Error(`天气API请求失败: ${response.status}`);
      const data = await response.json();
      if (data.status !== '1') throw new Error(`高德天气API错误: ${data.info} (代码: ${data.infocode})`);
      if (!data.lives || data.lives.length === 0) throw new Error('未获取到天气数据');
      return data.lives[0];
    },
    formatLiveWeather(weatherInfo) {
      return {
        weather: weatherInfo.weather,
        temperature: parseInt(weatherInfo.temperature),
        humidity: parseInt(weatherInfo.humidity),
        winddirection: weatherInfo.winddirection,
        windpower: weatherInfo.windpower
      };
    },
    generateFutureWeather(liveWeather) {
      const today = new Date();
      const weekDays = ['周一', '周二', '周三', '周四', '周五', '周六', '周日'];
      const liveTemp = parseInt(liveWeather.temperature);
      const weatherPatterns = [
        { condition: liveWeather.weather, tempChange: 0 },
        { condition: '晴', tempChange: 2 },
        { condition: '多云', tempChange: -1 },
        { condition: '小雨', tempChange: -3 },
        { condition: '阴', tempChange: -1 },
        { condition: '晴', tempChange: 3 },
        { condition: '多云', tempChange: 1 }
      ];
      const futureData = [];
      for (let i = 0; i < 6; i++) {
        const date = new Date(today);
        date.setDate(today.getDate() + i);
        const dateStr = `${date.getMonth() + 1}.${date.getDate()}`;
        const pattern = weatherPatterns[i % weatherPatterns.length];
        const high = liveTemp + pattern.tempChange + Math.floor(Math.random() * 3);
        const low = high - 8 - Math.floor(Math.random() * 3);
        const windPower = Math.min(5, Math.max(1, Math.floor(Math.random() * 5)));
        futureData.push({
          day: weekDays[date.getDay()],
          date: dateStr,
          icon: this.weatherToIcon(pattern.condition),
          high: high,
          low: low,
          condition: pattern.condition,
          windPower: windPower
        });
      }
      this.weatherData = futureData;
    },
    weatherToIcon(weather) {
      if (!weather) return 'el-icon-cloudy'
      if (/雨/.test(weather)) return 'el-icon-cloudy-and-rain'
      if (/雪/.test(weather)) return 'el-icon-cloudy'
      if (/雷/.test(weather)) return 'el-icon-lightning'
      if (/晴/.test(weather)) return 'el-icon-sunny'
      if (/阴/.test(weather)) return 'el-icon-cloudy'
      if (/多云/.test(weather)) return 'el-icon-partly-cloudy'
      return 'el-icon-cloudy'
    },
    generateAdvice() {
      const advice = [];
      if (!this.weatherData || this.weatherData.length === 0) return;
      const weekWeather = this.weatherData;
      const live = this.liveWeather;
      if (!live) return;
      const hasRainDay = weekWeather.some(d => /雨|雪/.test(d.condition));
      const avgHigh = weekWeather.reduce((sum, d) => sum + d.high, 0) / weekWeather.length;
      const avgLow = weekWeather.reduce((sum, d) => sum + d.low, 0) / weekWeather.length;
      const avgTemp = Math.round((avgHigh + avgLow) / 2);
      if (hasRainDay) advice.push({ type: 'warning', icon: 'el-icon-warning', text: '本周有降水，注意提前疏通排水沟渠，检查大棚防漏。' });
      if (avgTemp >= 15) advice.push({ type: 'warning', icon: 'el-icon-warning', text: '本周气温偏高，大棚注意通风降温，预防金针菇白粉病等病害。' });
      else if (avgTemp <= 5) advice.push({ type: 'warning', icon: 'el-icon-warning', text: '本周气温偏低，加强大棚保温，夜间加盖保温被，防止作物冻害。' });
      else advice.push({ type: 'success', icon: 'el-icon-check', text: '本周气温适宜，适合进行作物追肥和日常田间管理。' });
      if (live.humidity > 75) advice.push({ type: 'warning', icon: 'el-icon-warning', text: `当前湿度较高(${live.humidity}%)，注意大棚通风，预防病虫害滋生。` });
      else if (live.humidity < 40) advice.push({ type: 'warning', icon: 'el-icon-warning', text: `当前湿度较低(${live.humidity}%)，需适当增加土壤或空气湿度。` });
      advice.push({ type: 'info', icon: 'el-icon-info', text: '金针菇管理：注意控制棚内湿度在60-70%，定期检查红蜘蛛和灰霉病。' });
      const maxTempDiff = Math.max(...weekWeather.map(d => d.high - d.low));
      if (maxTempDiff > 12) advice.push({ type: 'warning', icon: 'el-icon-warning', text: '本周昼夜温差大，注意调节大棚通风，防止作物因温差过大受损。' });
      advice.push({ type: 'success', icon: 'el-icon-check', text: '建议在晴天上午进行施肥灌溉，避免夜间湿度过高引发病害。' });
      this.agricultureAdvice = advice;
    },
    retryFetch() {
      localStorage.removeItem(this.CACHE_KEY);
      this.fetchWeatherByIP(this.fallbackCity);
    }
  }
};
</script>

<style scoped>
.weather-forecast {
  text-align: center;
  background: linear-gradient(180deg, #eaf6ff 0%, #f7fbff 100%);
  border: none;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  border-radius: 12px;
  max-width: 1200px;
  margin: 0 auto;
}

.title { 
  font-weight: 600; 
  font-size: 18px; 
  margin-right: 10px; 
  color: #2c3e50;
}

.location { 
  color: #7f8c8d; 
  margin-left: 10px; 
  font-size: 14px;
}

.loading-state, .error-state { 
  display: flex; 
  align-items: center; 
  justify-content: center; 
  margin: 15px 0 25px; 
  padding: 12px;
  background: rgba(243, 156, 18, 0.1);
  border-radius: 8px;
}

.loading-state .loading-text, .error-state .error-text { 
  margin-left: 10px; 
  color: #2c3e50;
  font-size: 15px;
  font-weight: 500;
}

.error-state .error-text {
  color: #e74c3c;
  margin-right: 10px;
}

.live-summary { 
  display: flex; 
  align-items: center; 
  justify-content: center; 
  margin: 15px 0 25px; 
  padding: 12px;
  background: rgba(52, 152, 219, 0.1);
  border-radius: 8px;
}

.live-summary .live-text { 
  margin-left: 10px; 
  color: #2c3e50;
  font-size: 15px;
  font-weight: 500;
}

.weather-card {
  margin-bottom: 20px;
  background: #ffffff;
  border: 1px solid rgba(52, 152, 219, 0.15);
  border-radius: 16px;
  box-shadow: 0 8px 20px rgba(52, 152, 219, 0.08);
  transition: all 0.3s ease;
  cursor: pointer;
  height: 100%;
}

.weather-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 12px 24px rgba(52, 152, 219, 0.15);
}

.weather-icon {
  font-size: 36px;
  color: #3498db;
  padding: 20px 0;
}

.weather-info {
  padding: 0 10px 15px;
  text-align: center;
}

.info-day { 
  font-weight: 600; 
  font-size: 16px;
  color: #2c3e50;
  margin-bottom: 5px;
}

.info-date { 
  color: #95a5a6; 
  font-size: 12px; 
  margin-bottom: 8px;
}

.info-temp { 
  font-size: 18px; 
  color: #e74c3c; 
  margin: 8px 0;
  font-weight: bold;
}

.info-cond, .info-wind { 
  color: #7f8c8d; 
  font-size: 13px; 
  margin: 5px 0;
}

.agriculture-advice {
  margin-top: 30px;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 12px;
  border-left: 4px solid #27ae60;
}

.agriculture-advice h4 {
  margin-bottom: 15px;
  color: #2c3e50;
  font-size: 16px;
  text-align: left;
  padding-bottom: 10px;
  border-bottom: 1px solid #eee;
}

.agriculture-advice ul {
  list-style-type: none;
  padding: 0;
  margin: 0;
}

.agriculture-advice li {
  display: flex;
  align-items: flex-start;
  margin-bottom: 12px;
  padding: 10px 14px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.05);
  text-align: left;
  line-height: 1.5;
  font-size: 14px;
}

.agriculture-advice li:last-child {
  margin-bottom: 0;
}

.agriculture-advice li.success {
  border-left: 3px solid #27ae60;
  color: #27ae60;
}

.agriculture-advice li.warning {
  border-left: 3px solid #f39c12;
  color: #f39c12;
}

.agriculture-advice li.info {
  border-left: 3px solid #3498db;
  color: #3498db;
}

.agriculture-advice li i {
  margin-right: 10px;
  margin-top: 2px;
  font-size: 16px;
  flex-shrink: 0;
}

@media (max-width: 1200px) {
  .el-col {
    width: 20%;
    margin-bottom: 15px;
  }
}

@media (max-width: 992px) {
  .el-col {
    width: 25%;
  }
}

@media (max-width: 768px) {
  .el-col {
    width: 33.33%;
  }
}

@media (max-width: 576px) {
  .el-col {
    width: 50%;
  }
  
  .weather-card {
    margin: 0 auto 15px;
    max-width: 180px;
  }
  
  .live-summary, .loading-state, .error-state {
    flex-direction: column;
    text-align: center;
  }
  
  .live-summary .live-text, .loading-state .loading-text, .error-state .error-text {
    margin-left: 0;
    margin-top: 8px;
    font-size: 14px;
  }
  
  .agriculture-advice li {
    font-size: 13px;
    padding: 8px 10px;
  }
}
</style>