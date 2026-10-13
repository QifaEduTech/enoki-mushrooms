<template>
  <div id="app">
    <router-view />
    <theme-picker />
    <!-- Live2D 组件 - 使用FixedLive2D -->
    <!-- <FixedLive2D /> -->
  </div>
</template>

<script>
import ThemePicker from "@/components/ThemePicker";
// 修改导入路径，确保指向正确的FixedLive2D组件
import FixedLive2D from "@/components/FixedLive2D.vue";

export default {
  name: "App",
  components: {
    ThemePicker,
    FixedLive2D, // 组件名要匹配
  },
  data () {
    return {
      showLive2D: true,
    };
  },
  metaInfo () {
    return {
      title: this.$store.state.settings.dynamicTitle && this.$store.state.settings.title,
      titleTemplate: title => {
        return title ? `${title} - ${process.env.VUE_APP_TITLE}` : process.env.VUE_APP_TITLE
      }
    }
  },
  methods: {
    toggleLive2D () {
      this.showLive2D = !this.showLive2D;
    },
    // 如果需要重置位置功能，FixedLive2D组件需要添加相应方法
    resetLive2DPosition () {
      if (this.$refs.live2d && this.$refs.live2d.resetPosition) {
        this.$refs.live2d.resetPosition();
      }
    },
  },
};
</script>
<style scoped>
#app .theme-picker {
  display: none;
}

/* 确保Live2D组件不被其他内容遮挡 */
#app .live2d-container {
  z-index: 10000 !important;
}
</style>
