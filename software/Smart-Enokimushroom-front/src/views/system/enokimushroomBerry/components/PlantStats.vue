<template>
  <div class="plant-stats">
    <img src="@/assets/images/enokimushroom.jpg" alt="Enokimushroom" />
    <h3>总种植株数：{{ totalPlants }}</h3>
    <p>平均预计成熟时间：{{ averageMaturity }}天</p>
    <div class="progress-bar">
      <span :style="{ width: progress + '%' }"></span>
    </div>
    <p>{{ grownDays }}天 / {{ remainingDays }}天</p>
  </div>
</template>

<script>
export default {
  data () {
    return {
      totalPlants: 156,
      averageMaturity: 28,
      progress: 66,
      grownDays: 18,
      remainingDays: 10,
    };
  },
};
</script>

<style scoped>
.plant-stats {
  padding: 20px;
  border-radius: 10px;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.progress-bar {
  height: 10px;
  background-color: #f0f0f0;
  border-radius: 5px;
  margin: 10px 0;
}

.progress-bar span {
  display: block;
  height: 100%;
  background-color: #ff6b6b;
  border-radius: 5px;
}
</style>