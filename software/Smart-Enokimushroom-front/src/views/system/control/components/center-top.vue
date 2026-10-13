<template>
  <div class="center-top">
    <!-- 调试信息 -->
    <div v-if="dataLoaded" style="position: absolute; top: 5px; right: 5px; background: rgba(0,0,0,0.7); color: white; padding: 5px; border-radius: 3px; font-size: 10px;">
      数据已加载 | pH: {{ newObj.soilPh }} | 温度: {{ newObj.airTemperature }}
    </div>
    
    <div class="header">
      <div class="wendu-box">
        <div class="wendu-name">空气温度</div>
        <div class="wendu-value">{{ newObj.airTemperature }}</div>
      </div>
      <div class="wendu-box">
        <div class="wendu-name">二氧化碳含量</div>
        <div class="wendu-value">{{ newObj.airCo2 }}</div>
      </div>
      <div class="wendu-box">
        <div class="wendu-name">空气光照</div>
        <div class="wendu-value">{{ newObj.lightLux }}</div>
      </div>
    </div>

    <div class="trph">
      <i class="el-icon-s-data" style="color: #1989fa; font-size: 40px;" />
      <div style="color: #ffffff;margin-left: 5px;">
        <div>土壤PH值</div>
        <div>{{ newObj.soilPh }}</div>
      </div>
    </div>
    <div class="trthl">
      <i class="el-icon-s-data" style="color: #1989fa; font-size: 40px;" />
      <div style="color: #ffffff;margin-left: 5px;">
        <div>土壤氮含量</div>
        <div>{{ newObj.soilN }}mg/kg</div>
      </div>
    </div>
    <div class="trlhl">
      <i class="el-icon-s-data" style="color: #1989fa; font-size: 40px;" />
      <div style="color: #ffffff;margin-left: 5px;">
        <div>土壤磷含量</div>
        <div>{{ newObj.soilP }}mg/kg</div>
      </div>
    </div>
    <div class="trjhl">
      <i class="el-icon-s-data" style="color: #1989fa; font-size: 40px;" />
      <div style="color: #ffffff;margin-left: 5px;">
        <div>土壤钾含量</div>
        <div>{{ newObj.soilK }}mg/kg</div>
      </div>
    </div>
    <div class="trddl">
      <i class="el-icon-s-data" style="color: #1989fa; font-size: 40px;" />
      <div style="color: #ffffff;margin-left: 5px;">
        <div>土壤导电率</div>
        <div>{{ newObj.soilConductivity }}</div>
      </div>
    </div>
    <div class="eyhthl">
      <i class="el-icon-s-data" style="color: #1989fa; font-size: 40px;" />
      <div style="color: #ffffff;margin-left: 5px;">
        <div>土壤温度</div>
        <div>{{ newObj.soilTemperature }}</div>
      </div>
    </div>
  </div>
</template>
<script>
export default {
  props: ['list'],
  data() {
    return {
      newObj: {},
      dataLoaded: false
    };
  },
  computed: {
    // 计算属性：获取最新数据
    latestData() {
      if (this.list && this.list.length > 0) {
        return this.list[0]; // 第一条就是最新数据
      }
      return {};
    }
  },
  watch: {
    list: {
      handler(newList) {
        console.log('center-top组件接收到数据列表:', newList);
        if (newList && newList.length > 0) {
          // 由于后端已经按时间倒序排列，第一条就是最新数据
          this.newObj = newList[0];
          this.dataLoaded = true;
          console.log('使用的最新数据对象:', this.newObj);
          console.log('最新pH值:', this.newObj.soilPh);
          console.log('最新温度:', this.newObj.airTemperature);
        } else {
          console.log('数据列表为空');
          this.dataLoaded = false;
        }
      },
      immediate: true,
      deep: true
    }
  },
  mounted() {
    console.log('center-top组件挂载完成，等待数据...');
  }
};
</script>
<style lang="scss" scoped>
.center-top {
  height: 100%;
  width: 100%;
  background: url("../../../../assets/images/enokimushroom.jpg") no-repeat;
  background-size: 100% 100%;
  position: relative;

  .header {
    width: 60%;
    margin: 0 auto;
    padding-top: 10px;
    text-align: center;
    display: flex;
    justify-content: space-between;
  }
}

.wendu-box {
  height: 70px;
  width: 120px;
  background: url("../../../../assets/images/panel-r-b.png") no-repeat;
  background-size: 100% 100%;
  text-align: center;
  color: #ffffff;
  font-size: 18px;

  .wendu-name {
    line-height: 40px;
  }

  .wendu-value {
    color: orange;
  }
}

.trph {
  display: flex;
  position: absolute;
  font-size: 12px;
  left: 5%;
  bottom: 12%;
}

.trthl {
  display: flex;
  position: absolute;
  font-size: 12px;
  left: 5%;
  bottom: 2%;
}

.trlhl {
  display: flex;
  position: absolute;
  font-size: 12px;
  left: 30%;
  bottom: 2%;
}

.trjhl {
  display: flex;
  position: absolute;
  font-size: 12px;
  left: 55%;
  bottom: 2%;
}

.trddl {
  display: flex;
  position: absolute;
  font-size: 12px;
  left: 80%;
  bottom: 2%;
}

.eyhthl {
  display: flex;
  position: absolute;
  font-size: 12px;
  left: 80%;
  bottom: 12%;
}
</style>