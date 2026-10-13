// src/utils/eventBus.js
import Vue from 'vue'

// 创建事件总线实例
export const EventBus = new Vue()

// 定义事件名称常量
export const EVENTS = {
  SHOW_LIVE2D_TIP: 'showLive2DTip',
  CLOSE_LIVE2D: 'closeLive2D'
}
