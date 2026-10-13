<template>
  <div class="container">
    <div class="header">
      <div class="header-content">
        <span>农业种植数据可视化系统</span>
        <div class="refresh-controls">
          <button @click="manualRefresh" class="refresh-btn">🔄 手动刷新</button>
          <span class="refresh-status">自动刷新: 每{{ refreshInterval/1000 }}秒</span>
        </div>
      </div>
    </div>
    <div class="content">
      <div class="left">
        <div class="top">
          <leftTop :list="jkxxList" v-if="jkxxList.length !== 0"></leftTop>
        </div>
        <div class="center">
          <div class="first">
            <left-center-first :list="jkxxList" v-if="jkxxList.length !== 0"></left-center-first>
          </div>
          <div class="second">
            <left-center-second :list="jkxxList" v-if="jkxxList.length !== 0"></left-center-second>
          </div>
        </div>
        <div class="bottom">
          <left-bottom :list="jkxxList" v-if="jkxxList.length !== 0"></left-bottom>
        </div>
      </div>
      <div class="middle">
        <div class="center-img">
          <centerTop :list="jkxxList" v-if="jkxxList.length !== 0"></centerTop>
        </div>
        <div class="bottom">
          <center-bottom :list="jkxxList" v-if="jkxxList.length !== 0"></center-bottom>
        </div>
      </div>
      <div class="right">
        <div class="top">
          <rightTop :list="jkxxList" v-if="jkxxList.length !== 0">
          </rightTop>
        </div>
        <div class="center">
          <div class="first">
            <div class="jk"> 监控图像1</div>
            <img src="../../../assets/images/grow.jpg" alt="" class="grow-img" style="width: 100%;height: 90%;">
          </div>
          <div class="second">
            <div class="jk"> 监控图像2</div>
            <img src="../../../assets/images/grow1.jpg" alt="" class="grow-img" style="width: 100%;height: 90%;">
          </div>
        </div>
        <div class="bottom">
          <right-bottom :list="jkxxList" v-if="jkxxList.length !== 0"></right-bottom>
        </div>
      </div>
    </div>
  </div>
</template>
<script>
import leftTop from './components/left-top.vue'
import leftCenterFirst from './components/left-center-first.vue'
import leftCenterSecond from './components/left-center-second.vue'
import leftBottom from './components/left-bottom.vue'
import centerTop from './components/center-top.vue'
import centerBottom from './components/center-bottom.vue'
import rightTop from './components/right-top.vue'
import rightCenter from './components/right-center.vue'
import rightBottom from './components/right-bottom.vue'
import { listRecord } from "@/api/system/record";

export default {
  components: {
    leftTop,
    leftCenterFirst,
    leftCenterSecond,
    leftBottom,
    centerTop,
    centerBottom,
    rightTop,
    rightCenter,
    rightBottom
  },
  data() {
    return {
      queryParams: {
        pageNum: 1,
        pageSize: 100,
      },
      jkxxList: [],
      refreshTimer: null, // 添加定时器变量
      refreshInterval: 2000 // 刷新间隔：2秒
    }
  },
  created() {
    this.getList();
  },
  mounted() {
    // 启动定时刷新
    this.startAutoRefresh();
  },
  beforeDestroy() {
    // 组件销毁前清除定时器
    this.stopAutoRefresh();
  },
  methods: {
    getList() {
      console.log('开始请求数据...');
      listRecord(this.queryParams).then(response => {
        console.log('API响应数据:', response);
        if (response && response.rows) {
          this.jkxxList = response.rows;
          console.log('设置到jkxxList的数据:', this.jkxxList);
          console.log('数据条数:', this.jkxxList.length);
          if (this.jkxxList.length > 0) {
            console.log('第一条数据:', this.jkxxList[0]);
            console.log('第一条数据的pH值:', this.jkxxList[0].soilPh);
          }
        } else {
          console.warn('API响应格式异常:', response);
        }
      }).catch(error => {
        console.error('请求失败:', error);
      });
    },
    
    // 启动自动刷新
    startAutoRefresh() {
      console.log('启动自动刷新，间隔:', this.refreshInterval + 'ms');
      this.refreshTimer = setInterval(() => {
        console.log('定时器触发，刷新数据...');
        this.getList();
      }, this.refreshInterval);
    },
    
    // 停止自动刷新
    stopAutoRefresh() {
      if (this.refreshTimer) {
        console.log('停止自动刷新');
        clearInterval(this.refreshTimer);
        this.refreshTimer = null;
      }
    },
    
    // 手动刷新数据
    manualRefresh() {
      console.log('手动刷新数据');
      this.getList();
    }
  }
}


</script>
<style lang="scss" scoped>
.container {
  width: 100%;
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  height: 100vh;
  z-index: 99999;
  background: rgba(33, 11, 114);

  .header {
    width: 100%;
    height: 50px;
    line-height: 50px;
    color: #ffffff;
    text-align: center;
    font-size: 30px;
    /* 加载背景图 */
    background-image: url("../../../assets/images/top.png");
    /* 背景图垂直、水平均居中 */
    background-position: center center;
    background-repeat: no-repeat;
    background-size: cover;

    .header-content {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 0 10px;

      span {
        font-size: 20px;
        color: #ffffff;
      }

      .refresh-controls {
        display: flex;
        align-items: center;
        gap: 10px;

        .refresh-btn {
          background: rgba(255, 255, 255, 0.2);
          color: white;
          padding: 6px 12px;
          border-radius: 20px;
          border: 1px solid rgba(255, 255, 255, 0.3);
          cursor: pointer;
          font-size: 12px;
          transition: all 0.3s ease;
          backdrop-filter: blur(10px);

          &:hover {
            background: rgba(255, 255, 255, 0.3);
            transform: translateY(-1px);
            box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
          }
        }

        .refresh-status {
          font-size: 12px;
          color: rgba(255, 255, 255, 0.8);
          background: rgba(0, 0, 0, 0.2);
          padding: 4px 8px;
          border-radius: 12px;
          backdrop-filter: blur(5px);
        }
      }
    }
  }

  .content {
    padding: 14px;
    display: flex;
    box-sizing: border-box;
    justify-content: space-between;
    width: 100%;
    height: 100%;

    .left {
      width: 30%;
      height: calc(100% - 50px);
      display: flex;
      flex-direction: column;
      justify-content: space-between;
    }

    .middle {
      width: 39%;
      height: calc(100% - 50px);
      display: flex;
      flex-direction: column;
      justify-content: space-between;

      .center-img {
        height: 66%;
      }
    }

    .right {
      width: 30%;
      height: calc(100% - 50px);
      display: flex;
      flex-direction: column;
      justify-content: space-between;
    }

    .top {
      height: 33%;
      width: 100%;
      background: url("../../../assets/images/panel-r-b.png") no-repeat;
      background-size: 100% 100%;
    }

    .center {
      height: 32%;
      width: 100%;
      display: flex;
      justify-content: space-between;

      .first {
        width: 49.5%;
        height: 100%;
        background: url("../../../assets/images/panel-r-b.png") no-repeat;
        background-size: 100% 100%;
      }

      .second {
        width: 49.5%;
        height: 100%;
        background: url("../../../assets/images/panel-r-b.png") no-repeat;
        background-size: 100% 100%;
      }
    }

    .bottom {
      height: 33%;
      width: 100%;
      background: url("../../../assets/images/panel-r-b.png") no-repeat;
      background-size: 100% 100%;
    }

    .jk {
      text-align: center;
      margin-top: 10px;
      font-size: 16px;
      color: #ffffff;
    }

    .grow-img {
      width: 100%;
      height: auto;
      margin-top: 10px;
    }
  }

}
</style>