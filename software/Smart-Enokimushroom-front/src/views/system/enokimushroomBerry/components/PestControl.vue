<template>
  <div class="pest-control">
    <h3>病虫害情况</h3>
    <div class="predictions">
      <!-- 第一个预测项 -->
      <div class="prediction-item">
        <h4>7天预计成熟</h4>
        <!-- 使用 Font Awesome 的 leaf 图标 -->
        <p><i class="fas fa-leaf icon"></i> {{ prediction7Days }} 株</p>
      </div>
      <!-- 第二个预测项 -->
      <div class="prediction-item">
        <h4>10天预计成熟</h4>
        <!-- 使用 Font Awesome 的 seedling 图标 -->
        <p><i class="fas fa-seedling icon"></i> {{ prediction10Days }} 株</p>
      </div>
      <!-- 第三个预测项 -->
      <div class="prediction-item">
        <h4>14天预计成熟</h4>
        <!-- 使用 Font Awesome 的 tree 图标 -->
        <p><i class="fas fa-tree icon"></i> {{ prediction14Days }} 株</p>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  data () {
    return {
      prediction7Days: 50,
      prediction10Days: 75,
      prediction14Days: 100,
    };
  },
};
</script>

<style scoped>
.pest-control {
  padding: 20px;
  border-radius: 10px;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.predictions {
  /* 修改为垂直排列 */
  display: flex;
  flex-direction: column;
  /* 可选：增加项之间的间距 */
  gap: 15px;
  margin-top: 20px;
}

.prediction-item {
  /* 文本居中 */
  text-align: center;
  /* 可选：添加内边距使内容更舒适 */
  padding: 10px;
  /* 可选：添加轻微背景色或边框以区分每个项 */
  /* background-color: #f9f9f9; */
  /* border: 1px solid #eee; */
  /* border-radius: 5px; */
}

/* 图标样式 */
.icon {
  /* 设置图标的颜色 */
  color: #28a745;
  /* 绿色，代表生长 */
  /* 设置图标的大小 */
  font-size: 1.2em;
  /* 设置图标与文字之间的间距 */
  margin-right: 8px;
  /* 可选：让图标垂直居中 */
  vertical-align: middle;
}
</style>