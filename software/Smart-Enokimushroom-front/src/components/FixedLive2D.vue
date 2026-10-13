<template>
  <div
    ref="live2dWidget"
    class="live2d-widget"
    :style="containerStyle"
    v-show="!closed"
    @click="handleClick"
  >
    <!-- Live2D 提示文字 -->
    <div v-if="tipVisible" class="live2d-tip">{{ tipMessage }}</div>
  </div>
</template>

<script>
import { EventBus, EVENTS } from "@/utils/eventBus";

export default {
  name: "FixedLive2D",
  props: {
    width: {
      type: Number,
      default: 150,
    },
    height: {
      type: Number,
      default: 300,
    },
    rightOffset: {
      type: Number,
      default: 20,
    },
    bottomOffset: {
      type: Number,
      default: 20,
    },
  },
  data() {
    return {
      widgetLoaded: false,
      widgetError: null,
      live2dInstance: null,
      tipVisible: false,
      tipMessage: "",
      tipTimeout: null,
      closed: false,
    };
  },
  computed: {
    containerStyle() {
      return {
        position: "fixed",
        right: `${this.rightOffset}px`,
        bottom: `${this.bottomOffset}px`,
        width: `${this.width}px`,
        height: `${this.height}px`,
        zIndex: 9999,
        userSelect: "none",
        pointerEvents: "auto",
      };
    },
  },
  mounted() {
    this.loadLive2DWidget()
      .then(() => {
        this.widgetLoaded = true;
        this.$nextTick(() => {
          this.initLive2DWidget();
          this.showLive2DTip("你好，我是你的智慧农业小助手！");
        });
      })
      .catch((error) => {
        this.widgetError = error;
        console.error("加载 Live2D widget 失败:", error);
      });

    // 监听事件总线
    this.setupEventListeners();
  },
  beforeUnmount() {
    // 清理事件监听器
    this.removeEventListeners();

    if (this.live2dInstance) {
      try {
        if (window.L2Dwidget && window.L2Dwidget.destroy) {
          window.L2Dwidget.destroy();
        }
      } catch (e) {
        console.warn("清理Live2D实例时出错:", e);
      }
    }
  },
  methods: {
    // 设置事件监听器
    setupEventListeners() {
      EventBus.$on(EVENTS.SHOW_LIVE2D_TIP, this.showLive2DTip);
      EventBus.$on(EVENTS.CLOSE_LIVE2D, this.closeLive2D);
    },

    // 移除事件监听器
    removeEventListeners() {
      EventBus.$off(EVENTS.SHOW_LIVE2D_TIP, this.showLive2DTip);
      EventBus.$off(EVENTS.CLOSE_LIVE2D, this.closeLive2D);
    },

    loadLive2DWidget() {
      return new Promise((resolve, reject) => {
        if (typeof window.L2Dwidget !== "undefined") {
          resolve();
          return;
        }

        const script = document.createElement("script");
        script.src =
          "https://cdn.jsdelivr.net/npm/live2d-widget@3.1.4/lib/L2Dwidget.min.js";
        script.async = true;

        script.onload = () => {
          //console.log("Live2D widget 加载成功");
          setTimeout(resolve, 100);
        };

        script.onerror = () => {
          reject(new Error("Live2D widget 加载失败"));
        };

        document.head.appendChild(script);
      });
    },

    initLive2DWidget() {
      if (!window.L2Dwidget) {
        console.error("L2Dwidget 未加载");
        return;
      }

      try {
        // this.live2dInstance = window.L2Dwidget.init({
        //   pluginRootPath: "https://cdn.jsdelivr.net/npm/live2d-widget@3.1.4/",
        //   pluginJsPath: "lib/",
        //   pluginModelPath: "assets/",
        //   tagMode: true,
        //   model: {
        //     jsonPath:
        //       // "https://cdn.jsdelivr.net/npm/live2d-widget-model-koharu@1.0.5/assets/koharu.model.json",
        //       "https://cdn.jsdelivr.net/npm/live2d-widget-model-izumi@1.0.5/assets/izumi.model.json",
        //   },
        this.live2dInstance = window.L2Dwidget.init({
          // 使用相对路径指向 public 目录下的 live2dw 文件夹
          pluginRootPath: "/live2dw/",
          pluginJsPath: "live2d-widget-model-chitose/", // 注意这里的路径是相对于 pluginRootPath 的
          pluginModelPath: "assets/", // 同样，这是相对于 pluginRootPath 的
          tagMode: true,
          model: {
            jsonPath:
              "/live2dw/live2d-widget-model-koharu/assets/koharu.model.json", // 指向本地的 Izumi 模型文件
          },
          dialog: {
            enable: false,
          },
          display: {
            superSample: 2,
            width: this.width,
            height: this.height,
            position: "right",
            hOffset: 0,
            vOffset: 0,
          },
          mobile: {
            show: true,
          },
          react: {
            opacity: 0.9,
          },
        });

        window.L2Dwidget.on("*", (eventName) => {
          //console.log("Live2D事件:", eventName);
        });
      } catch (error) {
        console.error("初始化Live2D失败:", error);
      }
    },

    showLive2DTip(message = "", duration = 6000) {
      this.tipMessage = message;
      this.tipVisible = true;
      clearTimeout(this.tipTimeout);
      this.tipTimeout = setTimeout(() => {
        this.tipVisible = false;
      }, duration);
    },

    handleClick() {
      this.showLive2DTip("你好");
    },

    closeLive2D() {
      this.closed = true;

      if (window.L2Dwidget && window.L2Dwidget.destroy) {
        try {
          window.L2Dwidget.destroy();
          //console.log("Live2D 已关闭");
        } catch (e) {
          console.warn("关闭 Live2D 时出错:", e);
        }
      }

      this.showLive2DTip = () => {};
    },
  },
};
</script>

<style scoped>
.live2d-widget {
  width: 100%;
  height: 100%;
  position: relative;
  pointer-events: auto;
}

.live2d-tip {
  position: absolute;
  top: -60px;
  left: 50%;
  transform: translateX(-50%);
  background: #68e2c4;
  color: white;
  padding: 12px 16px;
  border-radius: 8px;
  font-size: 14px;
  white-space: normal;
  word-wrap: break-word;
  word-break: break-all;
  max-width: 200px;
  min-width: 120px;
  z-index: 10000;
  pointer-events: none;
  animation: fadeInOut 6s ease-in-out forwards;
  line-height: 1.4;
  text-align: center;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.15);
}

/* 添加小三角形箭头 */
.live2d-tip::after {
  content: "";
  position: absolute;
  top: 100%;
  left: 50%;
  transform: translateX(-50%);
  border: 6px solid transparent;
  border-top-color: #68e2c4;
}

@keyframes fadeInOut {
  0% {
    opacity: 0;
    transform: translateX(-50%) translateY(10px) scale(0.9);
  }

  15%,
  85% {
    opacity: 1;
    transform: translateX(-50%) translateY(0) scale(1);
  }

  100% {
    opacity: 0;
    transform: translateX(-50%) translateY(-10px) scale(0.9);
  }
}
</style>
