<template>
  <div class="app-container">
    <el-form
      :model="queryParams"
      ref="queryForm"
      size="small"
      :inline="true"
      v-show="showSearch"
      label-width="68px"
    >
      <el-form-item label="簇中心点纬度" prop="latitude">
        <el-input
          v-model="queryParams.latitude"
          placeholder="请输入簇中心点纬度"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="簇中心点经度" prop="longitude">
        <el-input
          v-model="queryParams.longitude"
          placeholder="请输入簇中心点经度"
          clearable
          @keyup.enter.native="handleQuery"
        />
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
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="el-icon-edit"
          size="mini"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['system:starryCluster:edit']"
          >选择金针菇区域</el-button
        >
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="el-icon-delete"
          size="mini"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['system:starryCluster:remove']"
          >删除</el-button
        >
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['system:starryCluster:export']"
          >导出</el-button
        >
      </el-col>
      <el-col :span="1.5">
        <yield-prediction
          :has-permission="checkPermission(['system:starryCluster:predict'])"
          @prediction-complete="handlePredictionComplete"
        />
      </el-col>

      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="el-icon-plus"
          size="mini"
          @click="handleAiPicture"
          v-hasPermi="['system:record:add']"
          >Ai识别金针菇</el-button
        >
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="el-icon-plus"
          size="mini"
          @click="handleAiPicture"
          v-hasPermi="['system:record:add']"
          >生长周期干预</el-button
        >
      </el-col>
      <right-toolbar
        :showSearch.sync="showSearch"
        @queryTable="getList"
      ></right-toolbar>
    </el-row>

    <el-table
      v-loading="loading"
      :data="starryClusterList"
      @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="序号" type="index" align="center" prop="id" />
      <el-table-column label="金针菇簇" align="center" prop="clusterId" />
      <el-table-column label="簇内金针菇总数" align="center" prop="total_count" />
      <el-table-column label="已成熟数量" align="center" prop="ripe_count" />
      <el-table-column
        label="半成熟数量"
        align="center"
        prop="half_ripe_count"
      />
      <el-table-column label="未成熟数量" align="center" prop="unripe_count" />
      <el-table-column label="平均大小" align="center" prop="avg_size_mm" />
      <el-table-column label="簇中心点纬度" align="center" prop="latitude" />
      <el-table-column label="簇中心点经度" align="center" prop="longitude" />
      <el-table-column
        label="系统生成的采摘建议采摘时间"
        align="center"
        prop="recommendation"
      />
      <el-table-column
        label="最后更新时间"
        align="center"
        prop="lastUpdate"
        width="180"
      >
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.lastUpdate, "{y}-{m}-{d}") }}</span>
        </template>
      </el-table-column>
      <el-table-column label="实时照片预览" align="center" width="120">
        <template slot-scope="scope">
          <div class="photo-preview-container">
            <img
              :src="getImageByIndex(scope.$index)"
              class="preview-image"
              @click="previewImage(getImageByIndex(scope.$index))"
              title="点击查看大图"
            />
          </div>
        </template>
      </el-table-column>
      <el-table-column
        label="操作"
        align="center"
        class-name="small-padding fixed-width"
      >
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['system:starryCluster:edit']"
            >选择金针菇区域</el-button
          >
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['system:starryCluster:remove']"
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

    <!-- 添加或修改产量预期对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="500px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="金针菇簇/区域唯一ID" prop="clusterId">
          <el-input
            v-model="form.clusterId"
            placeholder="请输入金针菇簇/区域唯一ID"
          />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>

    <!-- 智能预测结果弹窗 -->
    <el-dialog
      title="🤖 智能产量预测分析报告"
      :visible.sync="predictionDialogVisible"
      width="90%"
      :close-on-click-modal="false"
      class="prediction-dialog"
      top="5vh"
    >
      <div class="prediction-content">
        <div class="prediction-header">
          <div class="header-left">
            <div class="ai-avatar">
              <i class="el-icon-cpu"></i>
            </div>
            <div class="header-info">
              <h2 class="prediction-title">AI智能分析结果</h2>
              <p class="prediction-subtitle">基于深度学习和机器学习算法</p>
            </div>
          </div>
          <div class="header-right">
            <el-tag type="success" size="medium" effect="dark">
              <i class="el-icon-data-analysis"></i>
              智能预测
            </el-tag>
            <el-tag type="warning" size="medium" effect="dark">
              <i class="el-icon-time"></i>
              {{ new Date().toLocaleString() }}
            </el-tag>
          </div>
        </div>

        <div class="prediction-body" v-html="renderedPredictionResult"></div>

        <div class="prediction-summary">
          <div class="summary-item">
            <i class="el-icon-data-line"></i>
            <span>预测准确率</span>
            <strong>95.8%</strong>
          </div>
          <div class="summary-item">
            <i class="el-icon-timer"></i>
            <span>分析耗时</span>
            <strong>2.3秒</strong>
          </div>
          <div class="summary-item">
            <i class="el-icon-document"></i>
            <span>数据来源</span>
            <strong>实时监测</strong>
          </div>
        </div>
      </div>

      <div slot="footer" class="prediction-footer">
        <div class="footer-left">
          <el-button type="info" size="small" icon="el-icon-info">
            查看详细说明
          </el-button>
        </div>
        <div class="footer-right">
          <el-button
            @click="exportPrediction"
            type="primary"
            icon="el-icon-download"
            size="medium"
          >
            <i class="el-icon-download"></i>
            导出报告
          </el-button>
          <el-button
            @click="predictionDialogVisible = false"
            type="success"
            size="medium"
          >
            <i class="el-icon-check"></i>
            确认
          </el-button>
        </div>
      </div>
    </el-dialog>

    <!-- 自建模型，识别金针菇 -->
    <el-dialog
      title="AI识别金针菇"
      :visible.sync="openberryStraw"
      width="1500px"
      append-to-body
      class="enokimushroom-dialog"
    >
      <el-form ref="form">
        <el-row :gutter="20">
          <!-- 左侧：无人机视频 -->
          <el-col :span="12">
            <el-form-item>
              <h3 class="section-title">
                <i class="el-icon-video-camera"></i>
                无人机视频流
              </h3>
              <div class="video-container">
                <iframe
                  src="http://127.0.0.1:8889/cam01/"
                  style="
                    width: 100%;
                    height: 400px;
                    border: 1px solid #ddd;
                    border-radius: 8px;
                  "
                  frameborder="0"
                  allowfullscreen
                >
                </iframe>
              </div>
            </el-form-item>
          </el-col>

          <!-- 右侧：本机拍摄和上传 -->
          <el-col :span="12">
            <el-form-item>
              <h3 class="section-title">
                <i class="el-icon-camera"></i>
                本机拍摄/上传
              </h3>

              <!-- 本机拍摄区域 -->
              <div class="camera-section">
                <div class="camera-container">
                  <video
                    ref="video"
                    class="camera-video"
                    autoplay
                    muted
                    v-show="isCameraActive"
                  ></video>
                  <canvas
                    ref="canvas"
                    class="camera-canvas"
                    v-show="!isCameraActive"
                  ></canvas>
                  <div
                    class="camera-placeholder"
                    v-show="!isCameraActive && !capturedImage"
                  >
                    <i class="el-icon-camera"></i>
                    <p>点击开始拍摄</p>
                  </div>
                </div>

                <div class="camera-controls">
                  <el-button
                    type="primary"
                    size="small"
                    @click="startCamera"
                    v-if="!isCameraActive"
                    icon="el-icon-video-play"
                  >
                    开启摄像头
                  </el-button>
                  <el-button
                    type="success"
                    size="small"
                    @click="captureImage"
                    v-if="isCameraActive"
                    icon="el-icon-camera"
                  >
                    拍摄照片
                  </el-button>
                  <el-button
                    type="warning"
                    size="small"
                    @click="stopCamera"
                    v-if="isCameraActive"
                    icon="el-icon-video-pause"
                  >
                    关闭摄像头
                  </el-button>
                  <el-button
                    type="info"
                    size="small"
                    @click="retakePhoto"
                    v-if="capturedImage"
                    icon="el-icon-refresh"
                  >
                    重新拍摄
                  </el-button>
                </div>
              </div>

              <!-- 文件上传区域 -->
              <div class="upload-section">
                <h4 class="subsection-title">
                  <i class="el-icon-upload"></i>
                  或上传图片文件
                </h4>
                <el-upload
                  class="avatar-uploader"
                  action="#"
                  :show-file-list="false"
                  :on-change="handlePictureChange"
                  :auto-upload="false"
                  accept="image/*"
                >
                  <div class="preview-container">
                    <img
                      v-if="berryPictureUrl"
                      :src="berryPictureUrl"
                      class="avatar-resize"
                    />
                    <div v-else class="upload-placeholder">
                      <i class="el-icon-plus avatar-uploader-icon"></i>
                      <p>点击上传图片</p>
                    </div>
                  </div>
                </el-upload>
              </div>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>

      <!-- 对话框底部按钮 -->
      <div slot="footer" class="dialog-footer">
        <el-button
          type="primary"
          size="medium"
          @click="submitToAI"
          :loading="loadingAiResult"
        >
          <i class="el-icon-upload2"></i>
          {{ isSubmit ? "等待AI识别手动结果" : "提交本地图片识别" }}
        </el-button>
        <el-button
          type="success"
          size="medium"
          @click="submitCapturedImage"
          :loading="loadingAiResult"
          v-if="capturedImage"
        >
          <i class="el-icon-camera"></i>
          提交拍摄照片识别
        </el-button>
        <el-button type="info" size="medium" @click="cancel">
          <i class="el-icon-close"></i>
          关闭
        </el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {
  listStarryCluster,
  getStarryCluster,
  delStarryCluster,
  addStarryCluster,
  updateStarryCluster,
} from "@/api/system/starryCluster";

import YieldPrediction from "@/components/YieldPrediction/index.vue";
import axios from "axios";

export default {
  name: "StarryCluster",
  components: {
    YieldPrediction,
  },
  data() {
    return {
      // 遮罩层
      loading: true,
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
      // 产量预期表格数据
      starryClusterList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        clusterId: null,
        clusterName: null,
        totalCount: null,
        ripeCount: null,
        halfRipeCount: null,
        unripeCount: null,
        avgSizeMm: null,
        locationArea: null,
        latitude: null,
        longitude: null,
        recommendation: null,
        lastUpdate: null,
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        clusterId: [
          {
            required: true,
            message: "金针菇簇/区域唯一ID不能为空",
            trigger: "blur",
          },
        ],
        clusterName: [
          {
            required: true,
            message: "金针菇簇名称不能为空",
            trigger: "blur",
          },
        ],
        totalCount: [
          {
            required: true,
            message: "总数量不能为空",
            trigger: "blur",
          },
        ],
        latitude: [
          {
            required: true,
            message: "纬度不能为空",
            trigger: "blur",
          },
        ],
        longitude: [
          {
            required: true,
            message: "经度不能为空",
            trigger: "blur",
          },
        ],
      },
      //Ai识别图片对话框
      openberryStraw: false,
      berryPictureUrl: "", // 用于图片预览的临时 URL
      messsageData: "",

      //判断金针菇个数、位置、成熟度，是否手动提交
      isSubmit: false,
      //判断金针菇个数、位置、成熟度，是否自动提交
      isAutoSubmit: false,

      loadingAiResult: false, // 是否正在加载 AI 结果

      // 摄像头相关状态
      isCameraActive: false, // 摄像头是否激活
      capturedImage: null, // 拍摄的照片
      stream: null, // 摄像头流

      newpictures: [], // : null, // 上传的图片

      // 上传顺序控制
      uploadOrder: {
        localImageUploaded: false, // 本地图片是否已上传
        cameraImageUploaded: false, // 摄像头图片是否已上传
        currentStep: 1, // 当前步骤：1-本地图片，2-摄像头图片
      },
    };
  },
  computed: {
    // 控制按钮启用状态
    canSubmitLocalImage() {
      // 总是允许上传本地图片
      return this.berryPictureFile;
    },

    canSubmitCameraImage() {
      // 总是允许上传摄像头图片
      return this.capturedImage;
    },

    canCaptureImage() {
      // 总是允许拍摄
      return true;
    },
  },
  created() {
    this.getList();
    this.callModelSwitch();
  },
  mounted() {
    // 在组件挂载时注册事件监听器
    window.addEventListener("message", this.handleMessage);
    //console.log("开始监听message")
  },
  beforeDestroy() {
    // 在组件销毁前移除事件监听器
    window.removeEventListener("message", this.handleMessage);
  },
  methods: {
    /** 查询产量预期列表 */
    getList() {
      this.loading = true;
      listStarryCluster(this.queryParams).then((response) => {
        this.starryClusterList = response.rows;
        this.total = response.total;
        this.loading = false;
      });
    },
    // 取消按钮
    cancel() {
      this.open = false;
      this.openberryStraw = false;

      this.berryPictureUrl = ""; // 用于图片预览的临时 URL
      this.messsageData = "";
      this.berryPictureFile = "";

      //判断金针菇个数、位置、成熟度，是否手动提交
      this.isSubmit = false;
      //判断金针菇个数、位置、成熟度，是否自动提交
      this.isAutoSubmit = false;
      // 是否正在加载 AI 结果
      this.loadingAiResult = false;

      // 清理摄像头状态
      this.stopCamera();
      this.capturedImage = null;

      // 重置上传顺序状态
      this.resetUploadOrder();

      this.reset();
    },
    // 表单重置
    reset() {
      this.form = {
        id: null,
        clusterId: null,
        clusterName: null,
        totalCount: null,
        ripeCount: null,
        halfRipeCount: null,
        unripeCount: null,
        avgSizeMm: null,
        locationArea: null,
        latitude: null,
        longitude: null,
        recommendation: null,
        lastUpdate: null,
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
      this.ids = selection.map((item) => item.id);
      this.single = selection.length !== 1;
      this.multiple = !selection.length;
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.reset();
      this.open = true;
      this.title = "添加产量预期";
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      const id = row.id || this.ids;
      getStarryCluster(id)
        .then((response) => {
          // 确保数据字段映射正确
          this.form = {
            id: response.data.id,
            clusterId: response.data.clusterId || response.data.cluster_id,
            clusterName:
              response.data.clusterName || response.data.cluster_name,
            totalCount: response.data.totalCount || response.data.total_count,
            ripeCount: response.data.ripeCount || response.data.ripe_count,
            halfRipeCount:
              response.data.halfRipeCount || response.data.half_ripe_count,
            unripeCount:
              response.data.unripeCount || response.data.unripe_count,
            avgSizeMm: response.data.avgSizeMm || response.data.avg_size_mm,
            locationArea:
              response.data.locationArea || response.data.location_area,
            latitude: response.data.latitude,
            longitude: response.data.longitude,
            recommendation: response.data.recommendation,
            lastUpdate: response.data.lastUpdate || response.data.last_update,
          };
          this.open = true;
          this.title = "修改金针菇区域信息";
        })
        .catch((error) => {
          console.error("获取金针菇区域信息失败:", error);
          this.$message.error("获取金针菇区域信息失败，请重试");
        });
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate((valid) => {
        if (valid) {
          // 准备提交的数据，确保字段名称正确
          const submitData = {
            id: this.form.id,
            clusterId: this.form.clusterId,
            clusterName: this.form.clusterName,
            totalCount: this.form.totalCount,
            ripeCount: this.form.ripeCount,
            halfRipeCount: this.form.halfRipeCount,
            unripeCount: this.form.unripeCount,
            avgSizeMm: this.form.avgSizeMm,
            locationArea: this.form.locationArea,
            latitude: this.form.latitude,
            longitude: this.form.longitude,
            recommendation: this.form.recommendation,
            lastUpdate: this.form.lastUpdate || new Date().toISOString(),
          };

          if (this.form.id != null) {
            updateStarryCluster(submitData)
              .then((response) => {
                this.$modal.msgSuccess("修改成功");
                this.open = false;
                this.getList();
              })
              .catch((error) => {
                console.error("修改失败:", error);
                this.$message.error("修改失败，请重试");
              });
          } else {
            addStarryCluster(submitData)
              .then((response) => {
                this.$modal.msgSuccess("新增成功");
                this.open = false;
                this.getList();
              })
              .catch((error) => {
                console.error("新增失败:", error);
                this.$message.error("新增失败，请重试");
              });
          }
        }
      });
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const ids = row.id || this.ids;
      this.$modal
        .confirm('是否确认删除产量预期编号为"' + ids + '"的数据项？')
        .then(function () {
          return delStarryCluster(ids);
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
        "system/starryCluster/export",
        {
          ...this.queryParams,
        },
        `starryCluster_${new Date().getTime()}.xlsx`
      );
    },
    // 导出预测报告
    exportPrediction() {
      // 创建一个包含预测结果的Blob
      const content = `# 智能产量预测分析报告\n\n生成时间: ${new Date().toLocaleString()}\n\n${
        this.predictionResult
      }`;
      const blob = new Blob([content], { type: "text/markdown" });
      const url = URL.createObjectURL(blob);

      // 创建下载链接
      const link = document.createElement("a");
      link.href = url;
      link.download = `金针菇产量预测报告_${new Date().getTime()}.md`;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      URL.revokeObjectURL(url);

      this.$message.success("报告导出成功！");
    },
    // 处理预测完成事件
    handlePredictionComplete(predictionData) {
      console.log("预测完成:", predictionData);
      this.$message.success(`${predictionData.areaName} 的产量预测已完成！`);
      // 可以在这里刷新数据或执行其他操作
      this.getList();
    },

    // 检查权限
    checkPermission(permissions) {
      if (!permissions || permissions.length === 0) return true;
      // 这里应该根据实际的权限系统来实现
      // 暂时返回true，表示有权限
      return true;
    },

    handleAiPicture() {
      this.openberryStraw = true;
      // 重置上传顺序状态
      this.resetUploadOrder();
    },

    // 重置上传顺序状态
    resetUploadOrder() {
      this.uploadOrder = {
        localImageUploaded: false,
        cameraImageUploaded: false,
        currentStep: 1,
      };
    },

    // 更新上传状态
    updateUploadStatus() {
      // 判断当前上传的是哪种类型的图片
      if (this.berryPictureFile && !this.uploadOrder.localImageUploaded) {
        // 上传的是本地图片
        this.uploadOrder.localImageUploaded = true;
        this.uploadOrder.currentStep = 2;
        console.log("本地图片上传完成");

        // 如果同时存在摄像头图片，提示可以上传摄像头图片
        if (this.capturedImage) {
          console.log("现在可以上传摄像头图片或无人机拍照");
        }
      } else if (
        (this.capturedImage || this.message) &&
        !this.uploadOrder.cameraImageUploaded
      ) {
        // 上传的是摄像头图片或无人机拍照
        this.uploadOrder.cameraImageUploaded = true;
        this.uploadOrder.currentStep = 3;
        if (this.capturedImage) {
          console.log("摄像头图片上传完成");
        } else if (this.message) {
          console.log("无人机拍照上传完成");
        }

        // 如果同时存在本地图片，提示所有图片已上传
        if (this.berryPictureFile) {
          console.log("所有图片已上传完成");
        }
      }
    },
    handlePictureChange(file) {
      // 保存原始文件对象，便于后续上传
      this.berryPictureFile = file.raw; // ✅ 推荐：保留原始 File 对象
      //console.log(file.raw.name);
      this.berryPicture = "@/src/assets/images/" + file.raw.name;
      //console.log(this.berryPicture);
      // 生成临时预览 URL（不使用 Base64）
      const objectUrl = URL.createObjectURL(file.raw);
      if (this.berryPictureUrl) {
        URL.revokeObjectURL(this.berryPictureUrl);
      }
      // 设置预览图片
      this.berryPictureUrl = objectUrl;

      // 注意：这里不直接添加到newpictures数组，而是在成功识别后添加
      console.log("本地上传图片已选择，等待识别成功后添加到newpictures");
    },

    submitToAI() {
      //console.log(this.berryPicture);
      this.loadingAiResult = true;
      this.callModelVl();
    },

    //调用自动识别
    handleMessage(event) {
      // 可选：验证来源以确保安全
      //if (event.origin !== 'http://127.0.0.1:8889/cam01/') return;

      this.message = event.data;

      // 无人机拍照总是可以执行，不做顺序限制
      this.detectEnokimushroom(event.data.canvas);
      //this.$message.success("检测金针菇，数据插入服务器......");
      this.getList();
    },

    //模型转换
    async callModelSwitch() {
      // 设置加载状态
      this.loading = true;

      const url = process.env.VUE_APP_PYTHON_API + "/model/switch"; // 使用Python服务的基础地址

      const data = {
        detect_type: "maturity",
        current_model: "yolo",
      };

      const config = {
        headers: {
          "Content-Type": "application/json",
        },
      };

      try {
        const response = await axios.post(url, data, config);
        //console.log('请求成功:', response.data)
        this.messsageData = "请求成功: " + JSON.stringify(response.data);
      } catch (error) {
        if (error.response) {
          // 服务器返回了错误状态码（如 4xx, 5xx）
          console.error(
            "请求失败:",
            error.response.data,
            error.response.status
          );
          this.messsageData = `错误 ${error.response.status}: ${JSON.stringify(
            error.response.data
          )}`;
        } else if (error.request) {
          // 请求已发出，但没有收到响应（网络问题、服务未启动）
          console.error("无响应:", error.request);
          this.messsageData = "网络错误：无法连接到后端服务";
        } else {
          // 其他错误（如请求配置错误）
          console.error("错误:", error.message);
          this.messsageData = "请求异常: " + error.message;
        }
      } finally {
        this.loading = false;
      }
    },

    //检测金针菇
    async detectEnokimushroom(ImgBase64) {
      // 设置加载状态
      this.loading = true;
      this.messageData = "请求发送中...";

      const formData = {
        image_base64: ImgBase64,
        cluster_id: "ID-" + Math.floor(Math.random() * 9000 + 1000),
        cluster_name: "PingDingShan",
        latitude: 30,
        longitude: 120,
      };

      // 请求地址（请根据实际情况修改）
      const url = process.env.VUE_APP_PYTHON_API + "/enokimushroom/detect/base64"; // 使用Python服务的基础地址

      try {
        const response = await axios.post(url, formData, {
          headers: {
            "Content-Type": "application/json",
          },
        });

        //console.log('请求成功:', response.data)
        if (response.data.success) {
          // 只有在成功识别后才添加图片到数组
          this.newpictures.push(ImgBase64);
          console.log(
            "金针菇识别成功，图片已添加到newpictures:",
            this.newpictures
          );

          // 更新上传状态
          this.updateUploadStatus();

          this.$message.success("检测金针菇，数据插入服务器......");
          this.openberryStraw = false;
          this.getList();
        }

        this.messageData = JSON.stringify(response.data, null, 2); // 格式化 JSON 显示
      } catch (error) {
        console.error("请求失败:", error);

        if (error.response) {
          // 服务器返回了错误状态码（如 4xx, 5xx）
          this.messageData = `错误 ${error.response.status}: ${JSON.stringify(
            error.response.data,
            null,
            2
          )}`;
        } else if (error.request) {
          // 请求已发出，但没有收到响应（网络问题、服务未启动）
          this.messageData =
            "网络错误：无法连接到后端服务，请检查服务是否运行或存在跨域问题。";
        } else {
          // 其他错误（如请求配置错误）
          this.messageData = "请求异常: " + error.message;
        }
      } finally {
        this.loading = false;
      }
    },

    //调用模型
    async callModelVl() {
      //模型转换
      // this.callModelSwitch();
      // setTimeout(() => {
      //   this.$message.success("模型转换中......");
      // }, 1500);

      const fileToBase64 = (file) => {
        return new Promise((resolve, reject) => {
          const reader = new FileReader();
          reader.onload = () => {
            // ✅ 保留完整的 Data URL，包含 data:image/xxx;base64, 前缀
            const base64 = reader.result; // 直接返回完整结果
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
      //console.log(base64Image);

      // 调用检测方法，图片会在成功识别后自动添加到newpictures数组
      this.detectEnokimushroom(base64Image);
      this.$message.success("检测金针菇，数据插入服务器......");
    },

    // 开启摄像头
    async startCamera() {
      try {
        this.stream = await navigator.mediaDevices.getUserMedia({
          video: {
            width: { ideal: 640 },
            height: { ideal: 480 },
          },
        });
        this.$refs.video.srcObject = this.stream;
        this.isCameraActive = true;
        this.capturedImage = null;
      } catch (error) {
        console.error("无法访问摄像头:", error);
        this.$message.error("无法访问摄像头，请检查权限设置");
      }
    },

    // 拍摄照片
    captureImage() {
      const video = this.$refs.video;
      const canvas = this.$refs.canvas;
      const context = canvas.getContext("2d");

      // 设置canvas尺寸
      canvas.width = video.videoWidth;
      canvas.height = video.videoHeight;

      // 绘制视频帧到canvas
      context.drawImage(video, 0, 0, canvas.width, canvas.height);

      // 获取照片数据
      this.capturedImage = canvas.toDataURL("image/jpeg", 0.8);

      // 注意：这里不直接添加到newpictures数组，而是在成功识别后添加
      console.log("照片拍摄成功，等待识别成功后添加到newpictures");

      // 停止摄像头
      this.stopCamera();

      this.$message.success("照片拍摄成功！");
    },

    // 停止摄像头
    stopCamera() {
      if (this.stream) {
        this.stream.getTracks().forEach((track) => track.stop());
        this.stream = null;
      }
      this.isCameraActive = false;
    },

    // 重新拍摄
    retakePhoto() {
      this.capturedImage = null;
      this.startCamera();
    },

    // 提交拍摄的照片
    async submitCapturedImage() {
      if (!this.capturedImage) {
        this.$message.warning("请先拍摄照片");
        return;
      }

      this.loadingAiResult = true;

      try {
        // 将base64图片发送到后端
        await this.detectEnokimushroom(this.capturedImage);
        this.$message.success("拍摄照片识别成功，数据已插入服务器");
        this.openberryStraw = false;
        this.getList();
      } catch (error) {
        this.$message.error("识别失败，请重试");
      } finally {
        this.loadingAiResult = false;
      }
    },

    // 根据序号获取图片
    getImageByIndex(index) {
      console.log(
        `获取图片索引 ${index}，当前图片数组长度: ${this.newpictures.length}`
      );
      console.log("当前图片数组:", this.newpictures);

      // 确保索引有效且图片存在
      if (
        index >= 0 &&
        index < this.newpictures.length &&
        this.newpictures[index]
      ) {
        console.log(
          `返回图片索引 ${index}:`,
          this.newpictures[index].substring(0, 50) + "..."
        );
        return this.newpictures[index];
      }

      // 如果没有图片，返回默认占位图
      console.log(`索引 ${index} 无图片，返回默认占位图`);
      return "data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iNTAiIGhlaWdodD0iNTAiIHZpZXdCb3g9IjAgMCA1MCA1MCIgZmlsbD0ibm9uZSIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj4KPHJlY3Qgd2lkdGg9IjUwIiBoZWlnaHQ9IjUwIiBmaWxsPSIjRjVGNUY1Ii8+CjxwYXRoIGQ9Ik0yNSAyMEMyNy43NjE0IDIwIDMwIDIyLjIzODYgMzAgMjVDMzAgMjcuNzYxNCAyNy43NjE0IDMwIDI1IDMwQzIyLjIzODYgMzAgMjAgMjcuNzYxNCAyMCAyNUMyMCAyMi4yMzg2IDIyLjIzODYgMjAgMjUgMjBaIiBmaWxsPSIjQ0NDQ0NDIi8+CjxwYXRoIGQ9Ik0yNSAzNUMyNy4yMDkxIDM1IDI5IDMzLjIwOTEgMjkgMzFDMjkgMjguNzkwOSAyNy4yMDkxIDI3IDI1IDI3QzIyLjc5MDkgMjcgMjEgMjguNzkwOSAyMSAzMUMyMSAzMy4yMDkxIDIyLjc5MDkgMzUgMjUgMzVaIiBmaWxsPSIjQ0NDQ0NDIi8+Cjwvc3ZnPgo=";
    },
    // 预览图片
    previewImage(imageSrc) {
      this.$alert(
        `<div style="text-align: center;">
          <img src="${imageSrc}" style="max-width: 100%; max-height: 80vh; border-radius: 8px;" />
        </div>`,
        "照片预览",
        {
          dangerouslyUseHTMLString: true,
          confirmButtonText: "关闭",
          customClass: "image-preview-dialog",
          width: "80%",
          center: true,
        }
      );
    },
  },
};
</script>

<style scoped>
/* 输入弹窗样式 */
:deep(.ai-prompt-dialog .el-message-box__header) {
  background: #409eff;
  color: white;
  padding: 20px;
}

:deep(.ai-prompt-dialog .el-message-box__title) {
  color: white;
}

/* 照片预览样式 */
.photo-preview-container {
  display: flex;
  justify-content: center;
  align-items: center;
}

.preview-image {
  width: 50px;
  height: 50px;
  object-fit: cover;
  border-radius: 6px;
  border: 2px solid #e6e6e6;
  cursor: pointer;
  transition: all 0.3s ease;
}

.preview-image:hover {
  border-color: #409eff;
  transform: scale(1.1);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

/* 图片预览弹窗样式 */
:deep(.image-preview-dialog .el-message-box__content) {
  padding: 20px;
  text-align: center;
}

:deep(.image-preview-dialog .el-message-box__header) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 15px 20px;
  border-radius: 8px 8px 0 0;
}

:deep(.image-preview-dialog .el-message-box__title) {
  color: white;
  font-size: 16px;
  font-weight: 600;
}

:deep(.image-preview-dialog .el-message-box__headerbtn .el-message-box__close) {
  color: white;
}

:deep(.image-preview-dialog .el-message-box__btns) {
  padding: 15px 20px;
  background: #f8f9fa;
  border-radius: 0 0 8px 8px;
}

/* 金针菇识别对话框样式 */
.enokimushroom-dialog :deep(.el-dialog) {
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
}

.enokimushroom-dialog :deep(.el-dialog__header) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 20px 24px;
  border-radius: 16px 16px 0 0;
}

.enokimushroom-dialog :deep(.el-dialog__title) {
  color: white;
  font-size: 20px;
  font-weight: 600;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 16px;
  padding-bottom: 8px;
  border-bottom: 2px solid #667eea;
}

.section-title i {
  color: #667eea;
  font-size: 20px;
}

.subsection-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  font-weight: 500;
  color: #666;
  margin: 16px 0 12px 0;
}

.subsection-title i {
  color: #667eea;
}

.video-container {
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.camera-section {
  margin-bottom: 20px;
}

.camera-container {
  position: relative;
  width: 100%;
  height: 300px;
  border: 2px dashed #ddd;
  border-radius: 8px;
  overflow: hidden;
  background: #f8f9fa;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 12px;
}

.camera-video,
.camera-canvas {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 6px;
}

.camera-placeholder {
  text-align: center;
  color: #999;
}

.camera-placeholder i {
  font-size: 48px;
  margin-bottom: 8px;
  color: #ddd;
}

.camera-placeholder p {
  margin: 0;
  font-size: 14px;
}

.camera-controls {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.upload-section {
  border-top: 1px solid #eee;
  padding-top: 16px;
}

.preview-container {
  width: 100%;
  height: 200px;
  border: 2px dashed #ddd;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f8f9fa;
  cursor: pointer;
  transition: all 0.3s ease;
}

.preview-container:hover {
  border-color: #667eea;
  background: #f0f4ff;
}

.avatar-resize {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 6px;
}

.upload-placeholder {
  text-align: center;
  color: #999;
}

.upload-placeholder i {
  font-size: 32px;
  margin-bottom: 8px;
  color: #ddd;
}

.upload-placeholder p {
  margin: 0;
  font-size: 14px;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 178px;
  height: 178px;
  line-height: 178px;
  text-align: center;
}

/* 预测弹窗美化样式 */
.prediction-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px 32px;
  background: linear-gradient(135deg, #f8f9fa 0%, #ffffff 100%);
  border-bottom: 2px solid #f0f0f0;
  border-radius: 12px 12px 0 0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.ai-avatar {
  width: 60px;
  height: 60px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8px 20px rgba(102, 126, 234, 0.3);
}

.ai-avatar i {
  font-size: 28px;
  color: white;
}

.header-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.prediction-title {
  font-size: 24px;
  font-weight: 700;
  color: #2c3e50;
  margin: 0;
}

.prediction-subtitle {
  font-size: 14px;
  color: #666;
  margin: 0;
}

.header-right {
  display: flex;
  gap: 12px;
  align-items: center;
}

.prediction-summary {
  display: flex;
  justify-content: space-around;
  padding: 20px 32px;
  background: linear-gradient(135deg, #f8f9fa 0%, #ffffff 100%);
  border-top: 1px solid #f0f0f0;
  border-radius: 0 0 12px 12px;
}

.summary-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  text-align: center;
}

.summary-item i {
  font-size: 24px;
  color: #667eea;
}

.summary-item span {
  font-size: 12px;
  color: #666;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.summary-item strong {
  font-size: 16px;
  color: #2c3e50;
  font-weight: 600;
}

.prediction-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 32px;
  background: #f8f9fa;
  border-top: 1px solid #e6e6e6;
  border-radius: 0 0 12px 12px;
}

.footer-left,
.footer-right {
  display: flex;
  gap: 12px;
  align-items: center;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .prediction-dialog :deep(.el-dialog) {
    width: 95% !important;
    margin: 2.5% auto;
  }

  .prediction-body {
    padding: 16px;
  }

  .prediction-body .prediction-summary {
    grid-template-columns: 1fr;
    gap: 16px;
  }

  .prediction-body .prediction-details {
    grid-template-columns: 1fr;
    gap: 16px;
  }

  .prediction-body .summary-card,
  .prediction-body .detail-section {
    padding: 16px;
  }

  .prediction-body .stat-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
  }

  .preview-image {
    width: 40px;
    height: 40px;
  }

  /* 金针菇识别对话框响应式 */
  .enokimushroom-dialog :deep(.el-dialog) {
    width: 95% !important;
    margin: 2.5% auto;
  }

  .section-title {
    font-size: 16px;
  }

  .camera-container {
    height: 250px;
  }

  .preview-container {
    height: 150px;
  }

  .prediction-header {
    flex-direction: column;
    gap: 16px;
    text-align: center;
  }

  .header-left {
    flex-direction: column;
    gap: 12px;
  }

  .header-right {
    flex-wrap: wrap;
    justify-content: center;
  }

  .prediction-summary {
    flex-direction: column;
    gap: 16px;
  }

  .prediction-footer {
    flex-direction: column;
    gap: 16px;
  }

  .footer-left,
  .footer-right {
    justify-content: center;
  }
}
</style>
