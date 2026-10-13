<template>
  <div class="app-container">
    <el-form
      :model="queryParams"
      ref="queryForm"
      size="small"
      :inline="true"
      v-show="showSearch"
      label-width="108px"
    >
      <!-- <el-form-item label="空气温度" prop="airTemperature">
        <el-input v-model="queryParams.airTemperature" placeholder="请输入空气温度" clearable
          @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="空气湿度" prop="airHumidity">
        <el-input v-model="queryParams.airHumidity" placeholder="请输入空气湿度" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="光照强度" prop="lightLux">
        <el-input v-model="queryParams.lightLux" placeholder="请输入光照强度" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="二氧化碳" prop="airCo2">
        <el-input v-model="queryParams.airCo2" placeholder="请输入二氧化碳" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="土壤温度" prop="soilTemperature">
        <el-input v-model="queryParams.soilTemperature" placeholder="请输入土壤温度" clearable
          @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="土壤湿度" prop="soilHumidity">
        <el-input v-model="queryParams.soilHumidity" placeholder="请输入土壤湿度" clearable
          @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="电导率" prop="soilConductivity">
        <el-input v-model="queryParams.soilConductivity" placeholder="请输入电导率" clearable
          @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="土壤PH值" prop="soilPh">
        <el-input v-model="queryParams.soilPh" placeholder="请输入土壤PH值" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="氮" prop="soilN">
        <el-input v-model="queryParams.soilN" placeholder="请输入氮" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="磷" prop="soilP">
        <el-input v-model="queryParams.soilP" placeholder="请输入磷" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="钾" prop="soilK">
        <el-input v-model="queryParams.soilK" placeholder="请输入钾" clearable @keyup.enter.native="handleQuery" />
      </el-form-item> -->
      <el-form-item label="开始记录时间" prop="startCreateDate">
        <el-date-picker
          clearable
          v-model="queryParams.startCreateDate"
          type="date"
          value-format="yyyy-MM-dd"
          placeholder="请选择快事记录时间"
        >
        </el-date-picker>
      </el-form-item>
      <!-- <el-form-item label="植物ID" prop="plantId">
        <el-input
          v-model="queryParams.plantId"
          placeholder="请输入植物ID"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="场地ID" prop="areaId">
        <el-input
          v-model="queryParams.areaId"
          placeholder="请输入场地ID"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item> -->
      <el-form-item label="结束记录时间" prop="stopCreateDate">
        <el-date-picker
          clearable
          v-model="queryParams.stopCreateDate"
          type="date"
          value-format="yyyy-MM-dd"
          placeholder="请选择结束记录时间"
        >
        </el-date-picker>
      </el-form-item>
      <el-form-item>
        <el-button
          type="primary"
          icon="el-icon-search"
          size="mini"
          @click="handleQuery"
          >搜索</el-button
        >
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery"
          >重置</el-button
        >
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <!-- <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="el-icon-plus"
          size="mini"
          @click="handleAdd"
          v-hasPermi="['system:record:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="el-icon-edit"
          size="mini"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['system:record:edit']"
        >修改</el-button>
      </el-col> -->
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="el-icon-delete"
          size="mini"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['system:record:remove']"
          >删除记录</el-button
        >
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['system:record:export']"
          >导出记录</el-button
        >
      </el-col>
      <!-- <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="el-icon-plus"
          size="mini"
          @click="handleAiGrowthModel"
          handleAiPicture
          v-hasPermi="['system:record:add']"
          >Ai生成生长模型</el-button
        >
      </el-col> -->
      <!-- <el-col :span="1.5">
        <el-button type="danger" plain icon="el-icon-delete" size="mini" @click="handleSpeakMessages">播放语音提醒</el-button>
      </el-col> -->
      <!-- <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="el-icon-plus"
          size="mini"
          @click="handleAiPicture"
          v-hasPermi="['system:record:add']"
          >Ai识别金针菇</el-button
        >
      </el-col> -->

      <!-- 播放控制按钮 -->
      <el-col :span="1.5">
        <!-- <el-button type="primary" size="mini" icon="el-icon-video-play" @click="handlePlay"
          :disabled="isPlaying || isPaused">
          播放
        </el-button> -->

        <el-button
          type="primary"
          size="mini"
          icon="el-icon-video-play"
          @click="handlePlay"
        >
          播放
        </el-button>

        <el-button
          type="warning"
          size="mini"
          icon="el-icon-video-pause"
          @click="handlePause"
          :disabled="!isPlaying"
        >
          暂停
        </el-button>

        <el-button
          type="info"
          size="mini"
          icon="el-icon-video-stop"
          @click="handleStop"
        >
          停止
        </el-button>
      </el-col>

      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="el-icon-plus"
          size="mini"
          @click="handleLowPour"
          v-hasPermi="['system:record:add']"
          >{{ lowPour ? "手动关闭低灌" : "手动开启低灌" }}</el-button
        >
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="el-icon-setting"
          size="mini"
          @click="handleThresholdSetting"
          v-hasPermi="['system:record:add']"
          >设置滴灌阈值</el-button
        >
      </el-col>
      <right-toolbar
        :showSearch.sync="showSearch"
        @queryTable="getList"
      ></right-toolbar>
    </el-row>

    <el-table
      v-loading="loading"
      :data="recordList"
      @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column
        label="序号"
        type="index"
        align="center"
        prop="recordId"
      />
      <el-table-column label="设备编号" align="center" prop="addressId" />
      <el-table-column
        label="空气温度（℃）"
        align="center"
        prop="airTemperature"
        width="75"
      />
      <el-table-column
        label="空气湿度（%）"
        align="center"
        prop="airHumidity"
      />
      <el-table-column label="光照强度（Lux）" align="center" prop="lightLux" />
      <el-table-column label="二氧化碳（ppm）" align="center" prop="airCo2" />
      <el-table-column
        label="土壤温度（℃）"
        align="center"
        prop="soilTemperature"
      />
      <el-table-column
        label="土壤湿度（%）"
        align="center"
        prop="soilHumidity"
      />
      <el-table-column
        label="电导率（us/cm）"
        align="center"
        prop="soilConductivity"
      />
      <el-table-column label="土壤PH值" align="center" prop="soilPh" />
      <el-table-column label="氮（mg/kg）" align="center" prop="soilN" />
      <el-table-column label="磷（mg/kg）" align="center" prop="soilP" />
      <el-table-column label="钾（mg/kg）" align="center" prop="soilK" />
      <el-table-column
        label="记录时间"
        align="center"
        prop="createDate"
        width="180"
      >
        <template slot-scope="scope">
          <span>{{
            parseTime(scope.row.createDate, "{y}-{m}-{d} {h}:{i}:{s}")
          }}</span>
        </template>
      </el-table-column>
      <!-- <el-table-column label="监控植物" align="center" prop="plantId" /> -->
      <el-table-column label="监控植物" align="center" prop="plantName" />
      <!-- <el-table-column label="所属场地" align="center" prop="areaId" /> -->
      <el-table-column label="所属场地" align="center" prop="areaName" />
      <!-- <el-table-column label="更新时间" align="center" prop="updateDate" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.updateDate, '{y}-{m}-{d} {h}:{i}:{s}') }}</span>
        </template>
      </el-table-column> -->
      <el-table-column
        label="操作"
        align="center"
        class-name="small-padding fixed-width"
      >
        <template slot-scope="scope">
          <!-- <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['system:record:edit']"
          >修改</el-button> -->
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['system:record:remove']"
            >删除</el-button
          >
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total > 0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 添加或修改【监控记录】对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="500px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="空气温度" prop="airTemperature">
          <el-input
            v-model="form.airTemperature"
            placeholder="请输入空气温度"
          />
        </el-form-item>
        <el-form-item label="空气湿度" prop="airHumidity">
          <el-input v-model="form.airHumidity" placeholder="请输入空气湿度" />
        </el-form-item>
        <el-form-item label="光照强度" prop="lightLux">
          <el-input v-model="form.lightLux" placeholder="请输入光照强度" />
        </el-form-item>
        <el-form-item label="二氧化碳" prop="airCo2">
          <el-input v-model="form.airCo2" placeholder="请输入二氧化碳" />
        </el-form-item>
        <el-form-item label="土壤温度" prop="soilTemperature">
          <el-input
            v-model="form.soilTemperature"
            placeholder="请输入土壤温度"
          />
        </el-form-item>
        <el-form-item label="土壤湿度" prop="soilHumidity">
          <el-input v-model="form.soilHumidity" placeholder="请输入土壤湿度" />
        </el-form-item>
        <el-form-item label="电导率" prop="soilConductivity">
          <el-input
            v-model="form.soilConductivity"
            placeholder="请输入电导率"
          />
        </el-form-item>
        <el-form-item label="土壤PH值" prop="soilPh">
          <el-input v-model="form.soilPh" placeholder="请输入土壤PH值" />
        </el-form-item>
        <el-form-item label="氮" prop="soilN">
          <el-input v-model="form.soilN" placeholder="请输入氮" />
        </el-form-item>
        <el-form-item label="磷" prop="soilP">
          <el-input v-model="form.soilP" placeholder="请输入磷" />
        </el-form-item>
        <el-form-item label="钾" prop="soilK">
          <el-input v-model="form.soilK" placeholder="请输入钾" />
        </el-form-item>
        <el-form-item label="记录时间" prop="createDate">
          <el-date-picker
            clearable
            v-model="form.createDate"
            type="date"
            value-format="yyyy-MM-dd"
            placeholder="请选择记录时间"
          >
          </el-date-picker>
        </el-form-item>
        <el-form-item label="植物ID" prop="plantId">
          <el-input v-model="form.plantId" placeholder="请输入植物ID" />
        </el-form-item>
        <el-form-item label="场地ID" prop="areaId">
          <el-input v-model="form.areaId" placeholder="请输入场地ID" />
        </el-form-item>
        <el-form-item label="更新时间" prop="updateDate">
          <el-date-picker
            clearable
            v-model="form.updateDate"
            type="date"
            value-format="yyyy-MM-dd"
            placeholder="请选择更新时间"
          >
          </el-date-picker>
        </el-form-item>
        <el-form-item label="是否删除：0，未删除，1，删除" prop="delFlag">
          <el-input
            v-model="form.delFlag"
            placeholder="请输入是否删除：0，未删除，1，删除"
          />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>

    <el-dialog
      title="Ai生成预测生成模型"
      :visible.sync="openView"
      width="1024px"
      append-to-body
    >
      <!-- 左侧：健康档案 -->
      <el-col :span="12">
        <div class="Ai-record">
          <h3>生长环境数据</h3>
          <ul>
            <!-- <li v-for="(line, index) in splitBasic" :key="index">{{ line }}</li> -->
            {{
              splitBasic
            }}
          </ul>
        </div>
      </el-col>

      <!-- 右侧：健康报告 -->
      <el-col :span="12">
        <div class="Ai-report">
          <h3>Ai生成预测模型:</h3>
          <!-- 使用 textarea 显示健康报告内容v-model="responseTextBasic" -->
          <!-- <el-input type="textarea" :rows="20" placeholder="基础报告内容" v-html="responseHtml"></el-input> -->
          <div
            class="custom-textarea"
            v-html="responseTextBasic"
            :style="{ height: fixedHeight }"
          ></div>
        </div>
      </el-col>
      <!-- 对话框底部按钮 -->
      <div slot="footer" class="dialog-footer">
        <el-button
          type="primary"
          size="mini"
          @click="handleAiBasic"
          :loading="loadingAiResult"
        >
          {{ isSubmit ? "等待AI识别结果" : "调用AI，生成预测生长模型" }}
        </el-button>
        <el-button type="success" size="mini" @click="cancel">关闭</el-button>

        <el-button
          type="primary"
          size="mini"
          icon="el-icon-video-play"
          @click="handlePlay"
        >
          播放
        </el-button>

        <el-button
          type="warning"
          size="mini"
          icon="el-icon-video-pause"
          @click="handlePause"
          :disabled="!isPlaying"
        >
          暂停
        </el-button>

        <el-button
          type="info"
          size="mini"
          icon="el-icon-video-stop"
          @click="handleStop"
        >
          停止
        </el-button>
      </div>
    </el-dialog>

    <el-dialog
      title="AI识别金针菇"
      :visible.sync="openberryStraw"
      width="1500px"
      append-to-body
    >
      <el-form ref="form">
        <el-row>
          <el-col :span="24">
            <!-- 建议使用 span=24 让整个表单项占满一行，避免布局错乱 label-width="120px"-->
            <el-form-item>
              <h3>无人机视频</h3>
              <div>
                <!-- 视频容器 -->
                <iframe
                  src="http://127.0.0.1:8889/cam01/"
                  style="
                    width: 100%;
                    height: 480px;
                    border: 1px solid #ddd;
                    border-radius: 4px;
                  "
                  frameborder="0"
                  allowfullscreen
                >
                </iframe>
              </div>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item>
              <h3>上传金针菇图片</h3>
              <div>
                <el-upload
                  class="avatar-uploader"
                  action="#"
                  :show-file-list="false"
                  :on-change="handlePictureChange"
                  :auto-upload="false"
                  accept="image/*"
                >
                  <div class="preview-container">
                    <!-- 使用 URL.createObjectURL 生成临时预览链接 -->
                    <img
                      v-if="berryPictureUrl"
                      :src="berryPictureUrl"
                      class="avatar-resize"
                    />
                    <i v-else class="el-icon-plus avatar-uploader-icon"></i>
                  </div>
                </el-upload>
              </div>
            </el-form-item>
          </el-col>

          <!-- 右侧：AI识别结果 -->
          <el-col :span="24" style="padding-left: 20px">
            <div
              class="custom-textarea"
              :style="{ height: fixedHeight, overflowY: 'auto' }"
            >
              <p v-if="!responseTextPicture" style="color: #999">
                等待AI识别结果...
              </p>
              <div v-else v-html="responseTextPicture"></div>
            </div>
          </el-col>
        </el-row>
      </el-form>
      <!-- 对话框底部按钮 -->
      <div slot="footer" class="dialog-footer">
        <!-- <el-button type="primary" @click="submitToAI" :loading="loadingAiResult">提交识别</el-button> -->
        <el-button
          type="primary"
          size="mini"
          @click="submitToAI"
          :loading="loadingAiResult"
        >
          {{ isSubmit ? "等待AI识别手动结果" : "手动提交识别" }}
        </el-button>
        <el-button type="primary" size="mini" :loading="loadingAiResult">
          {{ isAutoSubmit ? "等待AI识别自动结果" : "自动提交识别" }}
        </el-button>
        <el-button type="success" size="mini" @click="cancel">关闭</el-button>
        <el-button
          type="primary"
          size="mini"
          icon="el-icon-video-play"
          @click="handlePlay"
        >
          播放
        </el-button>

        <el-button
          type="warning"
          size="mini"
          icon="el-icon-video-pause"
          @click="handlePause"
          :disabled="!isPlaying"
        >
          暂停
        </el-button>

        <el-button
          type="info"
          size="mini"
          icon="el-icon-video-stop"
          @click="handleStop"
        >
          停止
        </el-button>
      </div>
    </el-dialog>

    <!-- 设置滴灌阈值对话框 -->
    <el-dialog
      title="💧 设置滴灌阈值"
      :visible.sync="thresholdDialogVisible"
      width="600px"
      append-to-body
      class="threshold-dialog"
    >
      <div class="threshold-content">
        <div class="threshold-header">
          <div class="header-icon">
            <i class="el-icon-setting"></i>
          </div>
          <div class="header-info">
            <h3>智能滴灌控制系统</h3>
            <p>根据土壤湿度自动控制滴灌设备</p>
          </div>
        </div>

        <el-form :model="thresholdForm" :rules="thresholdRules" ref="thresholdForm" label-width="120px">
          <el-form-item label="土壤湿度阈值" prop="soilHumidityThreshold">
            <el-input-number
              v-model="thresholdForm.soilHumidityThreshold"
              :min="10"
              :max="80"
              :step="1"
              controls-position="right"
              style="width: 200px;"
            />
            <span class="unit">%</span>
            <div class="threshold-tip">
              <i class="el-icon-info"></i>
              当土壤湿度低于此值时，系统将自动开启滴灌
            </div>
          </el-form-item>

          <el-form-item label="滴灌持续时间" prop="irrigationDuration">
            <el-input-number
              v-model="thresholdForm.irrigationDuration"
              :min="1"
              :max="60"
              :step="1"
              controls-position="right"
              style="width: 200px;"
            />
            <span class="unit">分钟</span>
            <div class="threshold-tip">
              <i class="el-icon-info"></i>
              每次滴灌的持续时间
            </div>
          </el-form-item>

          <el-form-item label="滴灌间隔" prop="irrigationInterval">
            <el-input-number
              v-model="thresholdForm.irrigationInterval"
              :min="30"
              :max="480"
              :step="30"
              controls-position="right"
              style="width: 200px;"
            />
            <span class="unit">分钟</span>
            <div class="threshold-tip">
              <i class="el-icon-info"></i>
              两次滴灌之间的最小间隔时间
            </div>
          </el-form-item>

          <el-form-item label="自动模式">
            <el-switch
              v-model="thresholdForm.autoMode"
              active-text="开启"
              inactive-text="关闭"
              active-color="#13ce66"
              inactive-color="#ff4949"
            />
            <div class="threshold-tip">
              <i class="el-icon-info"></i>
              开启后系统将根据阈值自动控制滴灌
            </div>
          </el-form-item>
        </el-form>

        <div class="current-status">
          <h4>当前状态</h4>
          <div class="status-grid">
            <div class="status-item">
              <span class="status-label">当前土壤湿度</span>
              <span class="status-value">{{ currentSoilHumidity }}%</span>
            </div>
            <div class="status-item">
              <span class="status-label">滴灌状态</span>
              <span class="status-value" :class="lowPour ? 'active' : 'inactive'">
                {{ lowPour ? '运行中' : '已停止' }}
              </span>
            </div>
            <div class="status-item">
              <span class="status-label">自动模式</span>
              <span class="status-value" :class="thresholdForm.autoMode ? 'active' : 'inactive'">
                {{ thresholdForm.autoMode ? '已开启' : '已关闭' }}
              </span>
            </div>
          </div>
        </div>
      </div>

      <div slot="footer" class="dialog-footer">
        <el-button @click="thresholdDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveThresholdSettings" :loading="savingThreshold">
          <i class="el-icon-check"></i>
          保存设置
        </el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {
  listRecord,
  getRecord,
  delRecord,
  addRecord,
  updateRecord,
  controlLowPour,
} from "@/api/system/record";
import { setTimeout } from "core-js";
import marked from "marked";
import axios from "axios";
import OpenAI from "openai";

export default {
  name: "Record",
  data() {
    return {
      // 遮罩层
      loading: false,
      isAutoLoading: false,
      // 选中数组
      ids: [],
      // 非单个禁用
      single: true,
      // 非多个禁用
      multiple: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 【请填写功能名称】表格数据
      recordList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        airTemperature: null,
        airHumidity: null,
        lightLux: null,
        airCo2: null,
        soilTemperature: null,
        soilHumidity: null,
        soilConductivity: null,
        soilPh: null,
        soilN: null,
        soilP: null,
        soilK: null,
        createDate: null,
        startCreateDate: null,
        stopCreateDate: null,
        plantId: null,
        areaId: null,
        updateDate: null,
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {},
      isPromptBasic:
        "作为植物专家，根据金针菇生长环境数据，直接通过逻辑推理 + 数值分析的方式来建立一个直观、可理解的预测模型",
      Growth_Cycle:
        "金针菇的成长过程大概需要经过六个阶段，分别为萌芽和开始生长期、现蕾期、开花和结果期、旺盛生长期、花芽分化期、休眠期。",
      Growth_condition:
        "温度：根系生长适温15-22℃，茎叶生长适温20-30℃，花芽分化期适温5-15℃，开花结果期温度范围4-40℃。" +
        "土壤：土壤要求疏松肥沃、排水良好、ph呈中性或微酸性。" +
        "光照：金针菇喜光且耐阴性较强，光照较强时，果实色深、品质高。",
      Seed_Treatment_Germination_Stage:
        "种子处理与发芽期（Day 1-15）" +
        "温度：15-20℃（恒温催芽）" +
        "湿度：保持基质湿度70% -80 %（覆盖保鲜膜保湿）" +
        "光照：无需光照（黑暗环境促进发芽）" +
        "种子需提前冷藏（5℃）处理7天打破休眠" +
        "使用泥炭土 + 珍珠岩（3: 1）作为育苗基质",
      Seedling_Emergence_Stage:
        "幼苗出土期（Day 16 - 30）" +
        "温度：18-22℃（夜间不低于15℃）" +
        "湿度：60% -70 %（避免叶面结露）" +
        "光照：逐步增加至12小时/ 天（补光灯辅助）" +
        "肥料：稀释1000倍的氮肥（如尿素）每周1次" +
        "关键特征：    真叶展开2 - 3片, 根系发育关键期（需疏松基质）",
      Vegetative_Growth_Stage:
        "营养生长期（Day 31-90）" +
        "温度：20-25℃（花芽分化需10- 15℃低温诱导）" +
        "湿度：50 % -60 %（降低湿度防病害）" +
        "光照：14 - 16小时 / 天（光强≥20000lux）" +
        "肥料：氮磷钾均衡肥（NPK 20 - 20 - 20）每周1次，补充钙镁肥（防叶尖焦枯）" +
        "管理要点：第60天开始摘除老叶（保留5 - 6片新叶），铺设地膜抑制杂草",
      Budding_Flowering_Stage:
        "现蕾开花期（Day 91-120）" +
        "温度：白天22- 26℃，夜间12 - 15℃（温差促花）" +
        "湿度：40 % -50 %（过高会导致花粉粘连）" +
        "光照：自然光照 + 补光至12小时" +
        "肥料：高磷肥（NPK 10 - 30 - 20）促进花芽,叶面喷施硼肥（0.1 % 硼砂溶液）" +
        "关键节点：Day 100左右出现第一花序,人工授粉（毛笔轻刷花心）",
      Fruit_Development_Stage:
        "果实发育期（Day 121-160）" +
        "温度：18-24℃（＞30℃果实易畸形）" +
        "湿度：50% -60 %（果实膨大期需水较多）" +
        "肥料：高钾肥（NPK 15-10 - 30）每周1次,补充氨基酸叶面肥" +
        "管理要点：疏果（每花序留3- 4个果）,果实悬空防霉变（垫稻草或网兜）",
      Maturity_Harvesting_Stage:
        "成熟采收期（Day 161-180）" +
        "温度：20-25℃（高温加速过熟）" +
        "采收标准：果面80% 转红，糖度≥8 %" +
        "采后处理：预冷至4℃延长保鲜,避免挤压（单层摆放）,环境风险与应对措施",
      //shen
      // apiKey: "sk-622c86d7232749c4a48e818cf8c2f1ef", // ⚠ 注意不要在生产暴露
      // appId: "4a2a067fb1fe4951b28cba9d6a6d3a5b", // 替换为你的实际 App ID
      // tongyiUrl: 'https://dashscope.aliyuncs.com/api/v1/apps/',

      //feng
      //apiKey: "sk-9b536aea7ccd4457b128cd85986a6f87",

      markdownTextBasic: null,
      splitBasic: null,
      openView: false,
      responseTextBasic: null,

      //低灌开启or 低灌关闭
      lowPour: false,
      controlCmd: "",

      //垂直滑块
      lineHeight: 24, // 单行高度（px）
      linesToShow: 20, // 固定显示 20 行

      // 传感器数据对象
      sensorData: {
        airTemperature: null, // 空气温度
        airHumidity: null, // 空气湿度
        lightLux: null, // 光照强度
        airCo2: null, // CO2浓度
        soilTemperature: null, // 土壤温度
        soilHumidity: null, // 土壤湿度
        soilConductivity: null, // 电导率
        soilPh: null, // pH值
        soilN: null, // 氮含量
        soilP: null, // 磷含量
        soilK: null, // 钾含量
      },

      // 阈值配置
      // thresholds: {
      //   airTemperature: { low: 2, high: 32 },
      //   airHumidity: { low: 35, high: 80 },
      //   lightLux: { low: 0, high: 32600000 },
      //   airCo2: { low: 400, high: 800 },
      //   soilTemperature: { low: 15, high: 25 },
      //   soilHumidity: { low: 15, high: 25 },
      //   soilConductivity: { low: 0.5, high: 2 },
      //   soilPh: { low: 5.5, high: 6.5 },
      //   soilN: { low: 20, high: 50 },
      //   soilP: { low: 20, high: 60 },
      //   soilK: { low: 50, high: 100 }
      // },
      thresholds: {
        // 空气温度 (Air Temperature)
        // 金针菇生长最适范围，影响光合作用、开花和果实发育
        airTemperature: { low: 18, high: 25 }, // 单位：℃
        // 💡 说明：白天 20–25℃，夜间 12–15℃ 为佳；低于 5℃ 生长停滞，高于 30℃ 易热害

        // 空气湿度 (Air Humidity)
        // 控制病害（如灰霉病）的关键因素
        airHumidity: { low: 60, high: 75 }, // 单位：%
        // 💡 说明：湿度过高易引发真菌病害，过低则影响蒸腾和营养运输

        // 光照强度 (Light Intensity - Lux)
        // 影响光合作用、花芽分化和糖分积累
        lightLux: { low: 20000, high: 80000 }, // 单位：lux（自然光下）
        // 💡 说明：金针菇需强光，补光时建议使用 PAR（光合有效辐射）指标，理想 PAR 为 200–600 μmol/m²/s
        // 注：80,000 lux ≈ 1200 μmol/m²/s（视光源而定），上限可根据补光系统调整

        // 空气 CO₂ 浓度 (Air CO₂)
        // 提高光合效率，尤其在冬季密闭温室中
        airCo2: { low: 400, high: 1000 }, // 单位：ppm
        // 💡 说明：大气浓度约 400–450 ppm，补光时 CO₂ 可提升至 800–1000 ppm 增产明显

        // 土壤温度 (Soil Temperature)
        // 根系活性和养分吸收的关键
        soilTemperature: { low: 15, high: 20 }, // 单位：℃
        // 💡 说明：根系最适 18–20℃，高于 25℃ 易烂根，低于 10℃ 吸收能力下降

        // 土壤湿度 (Soil Humidity)
        // 指土壤体积含水量（Volumetric Water Content）
        soilHumidity: { low: 30, high: 60 }, // 单位：%
        // 💡 说明：保持湿润但不积水，滴灌管理宜“少量多次”

        // 土壤电导率 (Soil Conductivity, EC)
        // 反映可溶性盐分浓度，影响养分吸收
        soilConductivity: { low: 0.8, high: 1.8 }, // 单位：dS/m（或 mS/cm）
        // 💡 说明：过高易盐害，过低表示养分不足；水培 EC 可控在 1.2–1.8

        // 土壤 pH 值
        // 影响养分有效性（Fe、Mn、Zn 在酸性条件下更易吸收）
        soilPh: { low: 5.8, high: 6.2 }, // 最佳微酸性
        // 💡 说明：pH < 5.5 易铝毒，> 6.8 易缺铁黄化

        // 土壤氮 (Soil Nitrogen, N)
        // 以速效氮（如 NO₃⁻-N）计
        soilN: { low: 80, high: 150 }, // 单位：mg/kg（或 ppm）
        // 💡 说明：苗期需氮较多，开花后控氮防徒长

        // 土壤磷 (Soil Phosphorus, P)
        // 以 Olsen-P 或 Bray-P1 计
        soilP: { low: 30, high: 60 }, // 单位：mg/kg
        // 💡 说明：磷促进根系和花果发育

        // 土壤钾 (Soil Potassium, K)
        // 以交换性钾计
        soilK: { low: 150, high: 250 }, // 单位：mg/kg
        // 💡 说明：钾提升果实品质、糖度和抗逆性
      },

      // 语音播报文本队列
      textToSpeak: [],

      // 是否正在播放
      isPlaying: false,

      // 已播报过的 ID 缓存（用于去重）
      spokenIds: new Set(),

      isPaused: false, // 是否已暂停
      currentIndex: 0, // 当前播放到的索引
      utteranceQueue: [], // 存储 utterance 对象（用于取消）?

      //Ai识别图片对话框
      openberryStraw: false,
      //Ai识别图片
      berryPicture: null,
      berryPictureFile: null, // 用于保存原始文件对象（推荐）

      berryPictureUrl: "", // 用于图片预览的临时 URL

      responseTextPicture: "", // 存储 AI 返回的识别结果
      // fixedHeight: '300px',         // 右侧展示区域高度
      loadingAiResult: false, // 是否正在加载 AI 结果
      image: "@/assets/images/IMG_3729.JPG",
      isPromptPicture: "识别图像中成熟和半成熟的金针菇个数，并标注出位置",
      allItem: "成熟的金针菇红色，半成熟的金针菇一半红一半绿，未成熟的金针菇绿色。",
      mature:
        "成熟的金针菇:" +
        "颜色：成熟的金针菇通常呈现出鲜艳的红色，从浅红到深红不等。果实的颜色均匀，没有绿色或白色的部分。" +
        "质地：果肉饱满且有弹性，轻轻按压会有一些柔软的感觉但不会过于软烂。" +
        "味道：成熟的金针菇具有甜美的味道，有时带有一点点酸味，这是其特有的风味。" +
        "形状：成熟的金针菇通常是心形的，顶部较宽底部尖细。它们的形状规则，表面光滑，边缘圆润。",
      halfMature:
        "半成熟的金针菇:" +
        "颜色：半成熟的金针菇颜色介于绿色与完全成熟的红色之间，通常为橙红色或者粉红色。果实可能部分区域已经变红，而其他部分仍保持绿色或黄色。" +
        "质地：比成熟的金针菇硬一些，缺乏那种饱满的感觉。" +
        "味道：味道较淡，酸味较强，甜度不足。" +
        "形状：尽管半成熟的金针菇也呈心形，但由于尚未完全成熟，它们的形状可能会稍微不够规整，有些个体可能出现扁平化或者不太规则的现象。",
      ablMature:
        "未成熟的金针菇:" +
        "颜色：未成熟的金针菇通常呈现绿色或白色，随着成长过程逐渐转变为黄色或浅粉色。" +
        "质地：非常坚硬，几乎没有弹性。" +
        "味道：未成熟的金针菇几乎没有什么甜味，主要是酸涩的味道。" +
        "形状：未成熟的金针菇虽然也是心形，但由于生长阶段的原因，它们可能显得较小，形状更加不规则，表面也不够光滑。",
      shapeFeatures:
        "金针菇的一般形状特征:" +
        "总体形态：大多数金针菇都具有典型的“心形”轮廓，即顶部宽大，逐渐向下收缩成一个尖端。" +
        "尺寸差异：即使是同一株植物上的金针菇，其大小也可能存在明显差异。一般而言，较大的金针菇往往更接近理想的心形，而较小的金针菇则可能更加扁平或者不规则。" +
        "表面特征：金针菇表面布满了微小的种子（实际是果实的一部分），这些种子分布均匀，并且略微凸起。健康的金针菇表面应光滑无瑕疵，如果有斑点或其他异常，则可能是病害或虫害的迹象。",

      pc: null, // RTCPeerConnection 实例
      //message: '',
      //判断金针菇个数、位置、成熟度，是否手动提交
      isSubmit: false,
      //判断金针菇个数、位置、成熟度，是否自动提交
      isAutoSubmit: false,

      // 滴灌阈值设置相关
      thresholdDialogVisible: false,
      savingThreshold: false,
      thresholdForm: {
        soilHumidityThreshold: 30, // 土壤湿度阈值
        irrigationDuration: 10, // 滴灌持续时间（分钟）
        irrigationInterval: 60, // 滴灌间隔（分钟）
        autoMode: true, // 自动模式
      },
      thresholdRules: {
        soilHumidityThreshold: [
          { required: true, message: '请输入土壤湿度阈值', trigger: 'blur' },
          { type: 'number', min: 10, max: 80, message: '阈值应在10-80%之间', trigger: 'blur' }
        ],
        irrigationDuration: [
          { required: true, message: '请输入滴灌持续时间', trigger: 'blur' },
          { type: 'number', min: 1, max: 60, message: '持续时间应在1-60分钟之间', trigger: 'blur' }
        ],
        irrigationInterval: [
          { required: true, message: '请输入滴灌间隔', trigger: 'blur' },
          { type: 'number', min: 30, max: 480, message: '间隔应在30-480分钟之间', trigger: 'blur' }
        ]
      },
    };
  },
  created() {
    this.getList();
  },
  computed: {
    // 计算最大高度（20行 × 每行高度）
    fixedHeight() {
      return this.lineHeight * this.linesToShow + "px";
    },
    // 获取当前土壤湿度（从最新记录中获取）
    currentSoilHumidity() {
      if (this.recordList && this.recordList.length > 0) {
        const latestRecord = this.recordList[0];
        return latestRecord.soilHumidity || 0;
      }
      return 0;
    },
  },
  mounted() {
    // // 监听页面隐藏事件（如切换标签、最小化）
    // document.addEventListener('visibilitychange', () => {
    //   if (document.visibilityState === 'hidden') {
    //     // 可选择自动暂停或停止
    //     // 这里选择：离开页面就停止
    //     this.handleStop();
    //   }
    // });

    // // 监听浏览器前进/后退等导航事件
    // window.addEventListener('beforeunload', () => {
    //   this.handleStop();
    // });

    // 在组件挂载时注册事件监听器
    window.addEventListener("message", this.handleMessage);
    //console.log("开始监听message")
  },
  beforeDestroy() {
    // 页面离开或组件销毁时，停止语音并清空
    //this.handleStop();
    // 在组件销毁前移除事件监听器
    window.removeEventListener("message", this.handleMessage);
  },
  watch: {
    recordList: {
      handler(newVal, oldVal) {
        if (!Array.isArray(newVal)) return;

        newVal.forEach((record) => {
          for (const key in this.thresholds) {
            const value = record[key];
            const threshold = this.thresholds[key];

            if (value === null || value === undefined || !threshold) continue;

            const id = `${key}-${record.id || record.time}`; // 唯一标识符
            const paramName = this.getLabel(key);

            if (value < threshold.low || value > threshold.high) {
              const message = `${paramName}在${
                record.createDate
              }的值为${value}${this.getUnit(key)}，超出正常范围(${
                threshold.low
              }~${threshold.high}${this.getUnit(key)})`;

              //console.log(message);
              if (!this.spokenIds.has(id)) {
                this.textToSpeak.push(message);
                this.spokenIds.add(id); // 标记已播报
              }
            } else {
              this.spokenIds.delete(id); // 恢复正常后移除标记
            }
          }
        });

        // 触发播放
        if (this.textToSpeak.length > 0 && !this.isPlaying) {
          //this.handleSpeakMessages();
          this.handlePlay();
        }
      },
      deep: true,
    },
  },
  methods: {
    /** 查询【请填写功能名称】列表 */
    getList() {
      this.loading = true;
      listRecord(this.queryParams).then((response) => {
        this.recordList = response.rows;
        //console.log(this.recordList);
        this.total = response.total;
        this.loading = false;
      });
    },
    // 取消按钮
    cancel() {
      this.openView = false;
      this.open = false;
      this.openberryStraw = false;
      this.splitBasic = "";
      this.responseTextBasic = "";
      this.responseTextPicture = "";
      this.berryPicture = "";
      this.berryPictureFile = "";
      this, (this.berryPictureUrl = "");

      this.isSubmit = false;
      this.isAutoSubmit = false;

      this.loading = false;
      this.isAutoLoading = false;

      this.reset();
    },
    // 表单重置
    reset() {
      this.form = {
        recordId: null,
        airTemperature: null,
        airHumidity: null,
        lightLux: null,
        airCo2: null,
        soilTemperature: null,
        soilHumidity: null,
        soilConductivity: null,
        soilPh: null,
        soilN: null,
        soilP: null,
        soilK: null,
        createDate: null,
        startCreateDate: null,
        stopCreateDate: null,
        plantId: null,
        areaId: null,
        createBy: null,
        updateDate: null,
        updateBy: null,
        delFlag: null,
      };
      this.resetForm("form");
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNum = 1;
      this.getList();
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.resetForm("queryForm");
      this.handleQuery();
    },
    // 多选框选中数据
    handleSelectionChange(selection) {
      this.ids = selection.map((item) => item.recordId);
      this.single = selection.length !== 1;
      this.multiple = !selection.length;
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.reset();
      this.open = true;
      this.title = "添加【请填写功能名称】";
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      const recordId = row.recordId || this.ids;
      getRecord(recordId).then((response) => {
        this.form = response.data;
        this.open = true;
        this.title = "修改【请填写功能名称】";
      });
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate((valid) => {
        if (valid) {
          if (this.form.recordId != null) {
            updateRecord(this.form).then((response) => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
          } else {
            addRecord(this.form).then((response) => {
              this.$modal.msgSuccess("新增成功");
              this.open = false;
              this.getList();
            });
          }
        }
      });
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const recordIds = row.recordId || this.ids;
      this.$modal
        .confirm(
          '是否确认删除【请填写功能名称】编号为"' + recordIds + '"的数据项？'
        )
        .then(function () {
          return delRecord(recordIds);
        })
        .then(() => {
          this.getList();
          this.$modal.msgSuccess("删除成功");
        })
        .catch(() => {});
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download(
        "system/record/export",
        {
          ...this.queryParams,
        },
        `record_${new Date().getTime()}.xlsx`
      );
    },
    //数据整理
    getRecordChange() {
      const headers = [
        "序号",
        "设备编号",
        "空气温度(℃)",
        "空气湿度(%)",
        "光照强度(Lux)",
        "二氧化碳(ppm)",
        "土壤温度(℃)",
        "土壤湿度(%)",
        "电导率(uS/cm)",
        "土壤PH值",
        "氮(mg/kg)",
        "磷(mg/kg)",
        "钾(mg/kg)",
        "记录时间",
        "监控植物",
        "所属场地",
      ];
      // 表头字符串
      let tableString = headers.join("\t") + "\n";

      // 遍历记录列表并生成数据行字符串
      this.recordList.forEach((record, index) => {
        const row = [
          index + 1,
          record.addressId,
          record.airTemperature,
          record.airHumidity,
          record.lightLux,
          record.airCo2,
          record.soilTemperature,
          record.soilHumidity,
          record.soilConductivity,
          record.soilPh,
          record.soilN,
          record.soilP,
          record.soilK,
          record.createDate,
          record.plantName,
          record.areaName,
        ];

        tableString += row.join("\t") + "\n";
      });
      //console.log(tableString);
      this.splitBasic = tableString;
    },
    //弹出对话康，准备数据
    handleAiGrowthModel() {
      this.getRecordChange();
      this.openView = true;
      //this.isGeneratingReport = true;
    },
    //AI生成生长模型
    handleAiBasic() {
      // 模拟报告生成
      // this.loading = true;
      //console.log("开始调用大模型！！耐心等待！");
      //this.callDashScopeBasic(this.splitBasic + this.isPromptBasic);
      this.callqwenPlus();
      //console.log(this.splitBasic);
      setTimeout(() => {
        //console.log("调用大模型中，生成基础报告");
        this.loading = false;
      }, 6000);
    },
    //通义千问 模型调用 打字机效果
    typewriterEffectBasic(fullText, speed = 30) {
      this.responseTextBasic = ""; // 初始化响应文本 // 假设你使用 v-html 显示 HTML
      let index = 0;

      return new Promise((resolve) => {
        const interval = setInterval(() => {
          if (index < fullText.length) {
            // this.responseTextBasic = marked.parse(fullText.substring(0, index + 1));
            this.responseTextBasic = marked(fullText.substring(0, index + 1));
            index++;
          } else {
            clearInterval(interval);
            resolve();
          }
        }, speed);
      });
    },

    async callDashScopeBasic(neirong) {
      this.errorLog = "";
      const url = this.tongyiUrl + this.appId + "/completion";
      //console.log(url);
      const data = {
        input: {
          prompt: neirong,
          //prompt: "你好！",
        },
        parameters: {
          rag_options: {
            file_ids: [
              this.Growth_Cycle,
              this.Growth_condition,
              this.Seed_Treatment_Germination_Stage,
              this.Seedling_Emergence_Stage,
              this.Vegetative_Growth_Stage,
              this.Budding_Flowering_Stage,
              this.Fruit_Development_Stage,
              this.Maturity_Harvesting_Stage,
            ], // 替换为指定的文件 ID，多个请用逗号隔开
          },
        },
        debug: {},
      };
      //console.log(data);
      //try {
      console.log("发送请求中。。。。。");
      axios
        .post(url, data, {
          headers: {
            Authorization: `Bearer ${this.apiKey}`,
            "Content-Type": "application/json",
          },
          responseType: "stream", // 以流的方式接收响应
        })
        .then((response) => {
          //console.log(response);
          // 使用 setTimeout 模拟延迟处理（如等待流式数据加载完成）
          if (response.status === 200) {
            // const parsed = JSON.parse(response.data.output.text); // 解析 JSON 字符串
            const parsed = response.data; // 解析 JSON 字符串
            const output1 = parsed.output; // 获取 output 对象
            //正常的结束状态                              表明返回的内容没有被拒绝或过滤掉
            if (
              parsed.output.finish_reason == "stop" &&
              !parsed.output.reject_status
            ) {
              const markdownText = output1?.text || "[无内容]";
              //console.log(markdownText);
              // this.markdownTextBasic = marked.parse(markdownText);
              this.markdownTextBasic = marked(markdownText);
              // 调用打字机效果方法
              this.typewriterEffectBasic(markdownText, 30);
            }
          }
        });
    },

    getLabel(key) {
      const labels = {
        airTemperature: "空气温度",
        airHumidity: "空气湿度",
        lightLux: "光照强度",
        airCo2: "二氧化碳浓度",
        soilTemperature: "土壤温度",
        soilHumidity: "土壤湿度",
        soilConductivity: "电导率",
        soilPh: "土壤pH值",
        soilN: "氮含量",
        soilP: "磷含量",
        soilK: "钾含量",
      };
      return labels[key] || key;
    },

    getUnit(key) {
      const units = {
        airTemperature: "摄氏度",
        airHumidity: "%",
        lightLux: "流明",
        airCo2: "ppm",
        soilTemperature: "摄氏度",
        soilHumidity: "%",
        soilConductivity: "毫西门子每厘米",
        soilPh: "",
        soilN: "毫克每公斤",
        soilP: "毫克每公斤",
        soilK: "毫克每公斤",
      };
      return units[key] || "";
    },
    formatTime(timeStr) {
      const date = new Date(timeStr);
      return date.toLocaleString();
    },

    // 播放语音
    speakText(text) {
      if ("speechSynthesis" in window) {
        const utterance = new SpeechSynthesisUtterance(text);
        utterance.lang = "zh-CN";
        speechSynthesis.speak(utterance);
      } else {
        alert("您的浏览器不支持语音合成");
      }
    },

    // 【播放】按钮：开始或继续播放
    handlePlay() {
      if (this.isPlaying && this.isPaused) {
        // 如果是暂停状态，恢复播放
        speechSynthesis.resume();
        this.isPaused = false;
        return;
      }

      if (this.isPlaying) return; // 正在播放中

      if (!Array.isArray(this.textToSpeak) || this.textToSpeak.length === 0) {
        const warnings = this.collectWarningMessages();
        if (!warnings.length) return;
        this.textToSpeak = warnings.slice();
      }

      this.isPlaying = true;
      this.isPaused = false;
      this.currentIndex = 0;

      this.playNext();
    },

    // 【暂停】按钮
    handlePause() {
      if (this.isPlaying && !this.isPaused) {
        speechSynthesis.pause();
        this.isPaused = true;
      }
    },

    // 【停止】按钮
    handleStop() {
      speechSynthesis.cancel(); // 立即停止所有
      this.isPlaying = false;
      this.isPaused = false;
      this.textToSpeak.splice(0); // 清空队列
      this.utteranceQueue = [];
      this.currentIndex = 0;
    },

    // 播放下一条
    playNext() {
      if (this.currentIndex >= this.textToSpeak.length) {
        this.handleStop();
        // alert('全部内容播放完毕');
        return;
      }

      const text = this.textToSpeak[this.currentIndex];
      const utterance = new SpeechSynthesisUtterance(text);
      utterance.lang = "zh-CN";

      utterance.onend = () => {
        this.currentIndex++;
        this.playNext();
      };

      utterance.onerror = () => {
        this.handleStop();
      };

      speechSynthesis.speak(utterance);
      this.utteranceQueue.push(utterance); // 可选：记录用于管理
    },

    // 采集当前页面的告警信息（不受 spokenIds 限制，用于手动重播）
    collectWarningMessages() {
      const messages = [];
      if (!Array.isArray(this.recordList)) return messages;
      this.recordList.forEach((record) => {
        for (const key in this.thresholds) {
          const value = record[key];
          const threshold = this.thresholds[key];
          if (value === null || value === undefined || !threshold) continue;
          if (value < threshold.low || value > threshold.high) {
            const message = `${this.getLabel(key)}在${record.createDate}的值为${value}${this.getUnit(key)}，超出正常范围(${threshold.low}~${threshold.high}${this.getUnit(key)})`;
            messages.push(message);
          }
        }
      });
      return messages;
    },

    // 手动重播语音提醒：每次点击都重新收集告警并播放
    handleSpeakMessages() {
      if (this.isPlaying) {
        alert("正在播放中，请稍后再试");
        return;
      }

      // 重新收集当前告警文本，并覆盖播放队列
      const warnings = this.collectWarningMessages();
      if (!warnings.length) {
        //alert('没有可播放的内容');
        return;
      }
      // 重置播放状态
      speechSynthesis.cancel();
      this.isPlaying = false;
      this.isPaused = false;
      this.currentIndex = 0;
      this.textToSpeak = warnings.slice();

      // 走统一的播放流程
      this.handlePlay();
    },
    //打开识别图片对话框
    handleAiPicture() {
      this.openberryStraw = true;
    },
    //图片上传监听并预览
    // handlePictureChange (file) {
    //   const reader = new FileReader();
    //   reader.onload = (e) => {
    //     this.berryPicture = e.target.result; // base64 格式
    //   };
    //   reader.readAsDataURL(file.raw);
    // },

    handlePictureChange(file) {
      // 保存原始文件对象，便于后续上传
      this.berryPictureFile = file.raw; // ✅ 推荐：保留原始 File 对象
      //console.log(file.raw.name);
      this.berryPicture = "@/src/assets/images/" + file.raw.name;
      //console.log(this.berryPicture);
      // 生成临时预览 URL（不使用 Base64）
      const objectUrl = URL.createObjectURL(file.raw);
      //console.log(objectUrl);
      // 清除旧的预览 URL 防止内存泄漏（可选优化）
      if (this.berryPictureUrl) {
        URL.revokeObjectURL(this.berryPictureUrl);
      }

      // 设置预览图片
      this.berryPictureUrl = objectUrl;

      // 🔔 提示：file.raw 是原生 File 对象，可直接用于 FormData
      // 示例：
      // const formData = new FormData();
      // formData.append('image', file.raw);
    },

    // 可选：在组件销毁时清理 URL 对象
    // beforeDestroy() {
    //   if (this.berryPictureUrl) {
    //     URL.revokeObjectURL(this.berryPictureUrl);
    //   }
    // },

    //阿里通义百炼视觉模型
    async callQwenVl() {
      this.isSubmit = true;
      this.loading = true;

      const openai = new OpenAI({
        // 若没有配置环境变量，请用阿里云百炼API Key将下行替换为：apiKey: "sk-xxx",
        apiKey: process.env.VUE_APP_DASHSCOPE_API_KEY,
        dangerouslyAllowBrowser: true,
        baseURL: "https://dashscope.aliyuncs.com/compatible-mode/v1",
      });
      // const encodeImage = (imagePath) => {
      //   const imageFile = readFileSync(imagePath);
      //   return imageFile.toString('base64');
      // };
      // ✅ 将 File/Blob 转为 Base64
      const fileToBase64 = (file) => {
        return new Promise((resolve, reject) => {
          const reader = new FileReader();
          reader.onload = () => {
            // 去掉 data:image/xxx;base64, 前缀
            const base64 = reader.result.split(",")[1];
            resolve(base64);
          };
          reader.onerror = reject;
          reader.readAsDataURL(file);
        });
      };

      let base64Image;
      try {
        base64Image = await fileToBase64(this.berryPictureFile);
      } catch (error) {
        this.$message.error("图片转 Base64 失败");
        return;
      }
      // 将xxx/eagle.png替换为你本地图像的绝对路径
      const completion = await openai.chat.completions.create({
        model: "qwen-vl-max", // 此处以qwen-vl-max为例，可按需更换模型名称。模型列表：https://help.aliyun.com/zh/model-studio/models
        messages: [
          {
            role: "system",
            content: [
              {
                type: "text",
                text: "作为金针菇种植专家.",
              },
            ],
          },
          {
            role: "user",
            content: [
              {
                type: "image_url",
                // 需要注意，传入Base64，图像格式（即image/{format}）需要与支持的图片列表中的Content Type保持一致。
                // PNG图像：  data:image/png;base64,${base64Image}
                // JPEG图像： data:image/jpeg;base64,${base64Image}
                // WEBP图像： data:image/webp;base64,${base64Image}
                image_url: { url: `data:image/jpeg;base64,${base64Image}` },
              },
              {
                type: "text",
                text: this.isPromptPicture,
              },
            ],
          },
        ],
      });
      //console.log(completion.choices[0].message.content);
      const str = completion.choices[0].message.content;
      //console.log(str);
      if (str && str.trim() !== "") {
        this.loadingAiResult = false;
        const result = str.replace(/#+/g, ",");
        const id = Date.now(); // 返回当前时间戳（毫秒）
        if (!this.spokenIds.has(id)) {
          this.textToSpeak.push(result);
          this.spokenIds.add(id); // 标记已播报
        }
        // 先触发语音播放（如果条件满足）
        if (this.textToSpeak.length > 0 && !this.isPlaying && !this.isPaused) {
          this.handlePlay();
        }
        // 立即启动打字机效果，不等待语音
        this.typewriterEffectPicture(str, 30);
      }
    },
    async callDashScopePicture(neirong) {
      if (!this.berryPicture) {
        this.$message.warning("请先上传一张图片");
        return;
      }

      // 若没有配置环境变量，可用百炼API Key将下行替换为：apiKey='sk-xxx'。但不建议在生产环境中直接将API Key硬编码到代码中，以减少API Key泄露风险。
      // const apiKey = process.env.DASHSCOPE_API_KEY;
      // const appId = 'YOUR_APP_ID';// 替换为实际的应用 ID

      const appId = "63a611fa23ff44adaac9fcc5947719a9";
      const apiKey = "sk-622c86d7232749c4a48e818cf8c2f1ef";
      //const apiKey = "1ccd652a7c2c40f7b35e2c7b49ca0247_p_efm";

      const url = `https://dashscope.aliyuncs.com/api/v1/apps/${appId}/completion`;
      //const imageUrl = "@/src/assets/images/IMG_3729.JPG";
      const imageUrl = this.berryPicture;

      //const imageUrl = "https://img95.699pic.com/photo/60028/6164.jpg_wh860.jpg";
      const data = {
        input: {
          prompt: neirong,
          image: imageUrl,
          //image: this.berryPicture, // 假设这是你的 Base64 图像数据
        },
        parameters: {
          rag_options: {
            file_ids: [
              this.allItem,
              this.mature,
              this.halfMature,
              this.ablMature,
              this.shapeFeatures,
            ], // 替换为指定的文件 ID，多个请用逗号隔开
          },
        },
        debug: {},
      };

      try {
        const response = await axios.post(url, data, {
          headers: {
            Authorization: `Bearer ${apiKey}`,
            "Content-Type": "application/json",
          },
        });
        //console.log(response);
        if (response.status === 200) {
          const parsed = response.data; // 解析 JSON 字符串
          const output1 = parsed.output; // 获取 output 对象
          if (
            parsed.output.finish_reason == "stop" &&
            !parsed.output.reject_status
          ) {
            const markdownText = output1?.text || "[无内容]";
            //console.log(markdownText);
            // this.markdownTextBasic = marked.parse(markdownText);
            this.markdownTextBasic = marked(markdownText);
            // 调用打字机效果方法
            this.typewriterEffectPicture(markdownText, 30);
          }
          //console.log(`${response.data.output.text}`);
        } else {
          console.log(`request_id=${response.headers["request_id"]}`);
          console.log(`code=${response.status}`);
          console.log(`message=${response.data.message}`);
        }
      } catch (error) {
        console.error(`Error calling DashScope: ${error.message}`);
        if (error.response) {
          console.error(`Response status: ${error.response.status}`);
          console.error(
            `Response data: ${JSON.stringify(error.response.data, null, 2)}`
          );
        }
      }
    },
    //通义千问 模型调用 打字机效果
    typewriterEffectPicture(fullText, speed = 30) {
      this.responseTextPicture = ""; // 初始化响应文本 // 假设你使用 v-html 显示 HTML
      let index = 0;

      return new Promise((resolve) => {
        const interval = setInterval(() => {
          if (index < fullText.length) {
            // this.responseTextBasic = marked.parse(fullText.substring(0, index + 1));
            this.responseTextPicture = marked(fullText.substring(0, index + 1));
            index++;
          } else {
            clearInterval(interval);
            resolve();
          }
        }, speed);
      });
    },
    submitToAI() {
      //console.log(this.berryPicture);
      this.loadingAiResult = true;
      this.responseTextPicture = "";

      this.callQwenVl();
      // 模拟调用后端接口（替换为真实 API 地址）

      // try {
      //   this.callDashScopePicture(this.berryPicture + this.isPromptPicture);
      // } catch (error) {
      //   this.$message.error('AI识别失败，请重试');
      //   this.loadingAiResult = false;
      // }
    },

    //通义语言模型,生成生长模型
    async callqwenPlus() {
      this.isSubmit = true;
      this.loading = true;

      const openai = new OpenAI({
        // 若没有配置环境变量，请用百炼API Key将下行替换为：apiKey: "sk-xxx",
        apiKey: process.env.VUE_APP_DASHSCOPE_API_KEY, // 如何获取API Key：https://help.aliyun.com/zh/model-studio/developer-reference/get-api-key
        dangerouslyAllowBrowser: true,
        baseURL: "https://dashscope.aliyuncs.com/compatible-mode/v1",
      });
      const completion = await openai.chat.completions.create({
        // 模型列表：https://help.aliyun.com/zh/model-studio/getting-started/models
        model: "qwen-plus-latest", // qwen-plus 属于 qwen3 模型，如需开启思考模式，请参见：https://help.aliyun.com/zh/model-studio/deep-thinking
        messages: [
          {
            role: "system",
            content: "作为金针菇植物种植的专家.",
          },
          {
            role: "user",
            content: this.splitBasic + this.isPromptBasic,
          },
        ],
      });
      //console.log(completion.choices[0].message.content);
      const str = completion.choices[0].message.content;
      if (str && str.trim() !== "") {
        this.loadingAiResult = false;
        const result = str.replace(/#+/g, ",");
        const id = Date.now(); // 返回当前时间戳（毫秒）
        if (!this.spokenIds.has(id)) {
          this.textToSpeak.push(result);
          this.spokenIds.add(id); // 标记已播报
        }
        // 先触发语音播放（如果条件满足）
        if (this.textToSpeak.length > 0 && !this.isPlaying && !this.isPaused) {
          this.handlePlay();
        }
        // 立即启动打字机效果，不等待语音
        this.typewriterEffectBasic(str, 30);
      }
    },

    //开启低灌
    handleLowPour() {
      if (!this.lowPour) {
        this.controlCmd = "openLP01"; //打开
      } else {
        this.controlCmd = "closeLP01"; //关闭
      }
      //请求发送，控制低灌打开、关闭
      controlLowPour(this.controlCmd).then((response) => {
        //console.log(response);
        if (response.code == 200) {
          this.lowPour = !this.lowPour;
        }
      });
    },
    //调用自动识别
    handleMessage(event) {
      // 可选：验证来源以确保安全
      //if (event.origin !== 'http://127.0.0.1:8889/cam01/') return;

      this.message = event.data;
      //console.log("Received message:", event.data);
      this.callQwenVlBase64(event.data.canvas);
    },
    //阿里通义百炼视觉模型:图像已经base64编码
    //自动识别
    async callQwenVlBase64(image) {
      this.isAutoSubmit = true;
      this.isAutoLoading = true;
      const openai = new OpenAI({
        // 若没有配置环境变量，请用阿里云百炼API Key将下行替换为：apiKey: "sk-xxx",
        apiKey: process.env.VUE_APP_DASHSCOPE_API_KEY,
        dangerouslyAllowBrowser: true,
        baseURL: "https://dashscope.aliyuncs.com/compatible-mode/v1",
      });
      // 将xxx/eagle.png替换为你本地图像的绝对路径
      const completion = await openai.chat.completions.create({
        model: "qwen-vl-max", // 此处以qwen-vl-max为例，可按需更换模型名称。模型列表：https://help.aliyun.com/zh/model-studio/models
        messages: [
          {
            role: "system",
            content: [
              {
                type: "text",
                text: "作为金针菇种植专家.",
              },
            ],
          },
          {
            role: "user",
            content: [
              {
                type: "image_url",
                // 需要注意，传入Base64，图像格式（即image/{format}）需要与支持的图片列表中的Content Type保持一致。
                // PNG图像：  data:image/png;base64,${base64Image}
                // JPEG图像： data:image/jpeg;base64,${base64Image}
                // WEBP图像： data:image/webp;base64,${base64Image}
                image_url: { url: image },
              },
              {
                type: "text",
                text: this.isPromptPicture,
              },
            ],
          },
        ],
      });
      //console.log(completion.choices[0].message.content);
      const str = completion.choices[0].message.content;
      //console.log(str);
      if (str && str.trim() !== "") {
        this.loadingAiResult = false;
        const result = str.replace(/#+/g, ",");
        const id = Date.now(); // 返回当前时间戳（毫秒）
        if (!this.spokenIds.has(id)) {
          this.textToSpeak.push(result);
          this.spokenIds.add(id); // 标记已播报
        }
        // 先触发语音播放（如果条件满足）
        if (this.textToSpeak.length > 0 && !this.isPlaying && !this.isPaused) {
          this.handlePlay();
        }
        // 立即启动打字机效果，不等待语音
        this.typewriterEffectPicture(str, 30);
      }
    },

    // 打开滴灌阈值设置对话框
    handleThresholdSetting() {
      this.thresholdDialogVisible = true;
    },

    // 保存滴灌阈值设置
    saveThresholdSettings() {
      this.$refs.thresholdForm.validate((valid) => {
        if (valid) {
          this.savingThreshold = true;
          
          // 模拟保存过程
          setTimeout(() => {
            this.savingThreshold = false;
            this.thresholdDialogVisible = false;
            
            // 显示成功消息
            this.$message.success('滴灌阈值设置已保存！');
            
            // 这里可以添加实际的保存逻辑
            console.log('保存的阈值设置:', this.thresholdForm);
            
            // 模拟保存到本地存储
            localStorage.setItem('irrigationThresholds', JSON.stringify(this.thresholdForm));
          }, 1500);
        } else {
          this.$message.error('请检查输入参数');
        }
      });
    },
  },
};
</script>
<style lang="css" scoped>
/* 健康档案样式 */
.Ai-record {
  margin-bottom: 20px;
}

.Ai-record h3 {
  font-size: 18px;
  font-weight: bold;
  margin-bottom: 10px;
}

.Ai-record ul {
  list-style-type: none;
  padding: 0;
}

.Ai-record li {
  background-color: #f5f7fa;
  margin: 5px 0;
  padding: 10px;
  border-radius: 4px;
  font-size: 14px;
}

/* 健康报告样式 */
.Ai-report {
  margin-top: 20px;
}

.Ai-report h3 {
  font-size: 18px;
  font-weight: bold;
  margin-bottom: 10px;
}

.Ai-report p {
  font-size: 14px;
  line-height: 1.6;
  color: #333;
}

.custom-textarea {
  font-family: monospace;
  white-space: pre-wrap;
  /* 保留空格和换行 */
  word-wrap: break-word;
  background-color: #f9f9f9;
  border: 1px solid #ddd;
  padding: 10px;
  line-height: 24px;
  overflow-y: auto;
  /* 超出时显示滚动条 */
  box-sizing: border-box;
}

/* 自定义滚动条样式（仅 Webkit 浏览器有效） */
.custom-textarea::-webkit-scrollbar {
  width: 8px;
  /* 滚动条宽度 */
}

.custom-textarea::-webkit-scrollbar-thumb {
  background-color: #409eff;
  /* Element Plus 的蓝色主题色 */
  border-radius: 4px;
}

.custom-textarea::-webkit-scrollbar-track {
  background-color: #f1f1f1;
}

.preview-container {
  width: 100%;
  /* 图片预览区占据整个容器宽度 */
  display: flex;
  justify-content: center;
  align-items: center;
  overflow: hidden;
}

.avatar-resize {
  max-width: 100%;
  /* 图片最大宽度为容器宽度 */
  height: auto;
  /* 等比缩放高度 */
  display: block;
  border-radius: 6px;
  /* 可选圆角 */
  object-fit: contain;
  /* 保证图片完整显示，不裁剪 */
}

.avatar-uploader .el-upload {
  width: 100%;
  /* 让上传组件占据整个容器宽度 */
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: border-color 0.3s ease;
}

.avatar-uploader .el-upload:hover {
  border-color: #409eff;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 100%;
  height: auto;
  line-height: normal;
  text-align: center;
}

/* 滴灌阈值设置对话框样式 */
.threshold-dialog :deep(.el-dialog) {
  border-radius: 12px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
}

.threshold-dialog :deep(.el-dialog__header) {
  background: #409eff;
  color: white;
  padding: 20px 24px;
  border-radius: 12px 12px 0 0;
}

.threshold-dialog :deep(.el-dialog__title) {
  color: white;
  font-size: 18px;
  font-weight: 600;
}

.threshold-content {
  padding: 20px 0;
}

.threshold-header {
  display: flex;
  align-items: center;
  margin-bottom: 30px;
  padding: 20px;
  background: #f0f9ff;
  border-radius: 12px;
  border-left: 4px solid #409eff;
}

.header-icon {
  width: 60px;
  height: 60px;
  background: #409eff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20px;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

.header-icon i {
  font-size: 28px;
  color: white;
}

.header-info h3 {
  margin: 0 0 8px 0;
  font-size: 20px;
  font-weight: 600;
  color: #2c3e50;
}

.header-info p {
  margin: 0;
  color: #666;
  font-size: 14px;
}

.threshold-tip {
  margin-top: 8px;
  padding: 8px 12px;
  background: #f8f9fa;
  border-radius: 6px;
  font-size: 12px;
  color: #666;
  border-left: 3px solid #409eff;
}

.threshold-tip i {
  color: #409eff;
  margin-right: 4px;
}

.unit {
  margin-left: 8px;
  color: #666;
  font-size: 14px;
}

.current-status {
  margin-top: 30px;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 12px;
  border: 1px solid #e9ecef;
}

.current-status h4 {
  margin: 0 0 16px 0;
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
  display: flex;
  align-items: center;
}

.current-status h4::before {
  content: "📊";
  margin-right: 8px;
}

.status-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
}

.status-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: white;
  border-radius: 8px;
  border: 1px solid #e9ecef;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.status-label {
  font-size: 14px;
  color: #666;
  font-weight: 500;
}

.status-value {
  font-size: 14px;
  font-weight: 600;
  padding: 4px 8px;
  border-radius: 4px;
}

.status-value.active {
  color: #67c23a;
  background: #f0f9ff;
}

.status-value.inactive {
  color: #f56c6c;
  background: #fef0f0;
}

.threshold-dialog :deep(.el-form-item__label) {
  font-weight: 600;
  color: #2c3e50;
}

.threshold-dialog :deep(.el-input-number) {
  width: 200px;
}

.threshold-dialog :deep(.el-switch__label) {
  font-weight: 500;
}

.threshold-dialog :deep(.dialog-footer) {
  padding: 20px 24px;
  background: #f8f9fa;
  border-radius: 0 0 12px 12px;
  text-align: right;
}

.threshold-dialog :deep(.el-button--primary) {
  background: #409eff;
  border: none;
  padding: 10px 20px;
  border-radius: 6px;
  font-weight: 500;
}

.threshold-dialog :deep(.el-button--primary:hover) {
  background: #337ecc;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.4);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .threshold-dialog :deep(.el-dialog) {
    width: 95% !important;
    margin: 2.5% auto;
  }
  
  .threshold-header {
    flex-direction: column;
    text-align: center;
  }
  
  .header-icon {
    margin-right: 0;
    margin-bottom: 16px;
  }
  
  .status-grid {
    grid-template-columns: 1fr;
  }
  
  .threshold-dialog :deep(.el-input-number) {
    width: 100%;
  }
}
</style>
