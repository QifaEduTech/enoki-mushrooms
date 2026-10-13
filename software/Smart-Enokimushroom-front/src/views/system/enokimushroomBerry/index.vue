<template>
  <div class="app">
    <PlantStats />
    <YieldData />
    <PestControl />
  </div>
</template>

<script>
import PlantStats from './components/PlantStats.vue';
import YieldData from './components/YieldData.vue';
import PestControl from './components/PestControl.vue';

export default {
  components: {
    PlantStats,
    YieldData,
    PestControl,
  },
};
</script>

<style scoped>
.app {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  padding: 20px;
}
</style>