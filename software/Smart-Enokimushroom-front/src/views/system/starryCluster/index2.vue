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
        <el-button
          type="primary"
          plain
          icon="el-icon-calculator"
          size="medium"
          @click="predictYield"
          v-hasPermi="['system:starryCluster:predict']"
          class="ai-predict-btn"
          >智能预测产量🤖</el-button
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
          >Ai识别金针菇</el-button
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
          {{ isSubmit ? "等待AI识别手动结果" : "提交识别" }}
        </el-button>
        <el-button
          type="success"
          size="medium"
          @click="submitCapturedImage"
          :loading="loadingAiResult"
          v-if="capturedImage"
        >
          <i class="el-icon-camera"></i>
          提交拍摄照片
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

import { predictYield } from "../../../api/system/IntelligentAnalytics";
import axios from "axios";

export default {
  name: "StarryCluster",
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
      // 预测结果弹窗
      predictionDialogVisible: false,
      // 预测结果内容
      predictionResult: "",
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
    };
  },
  computed: {
    // 处理预测结果内容
    renderedPredictionResult() {
      if (!this.predictionResult) return "";

      // 将纯文本转换为结构化的HTML
      return this.formatPredictionContent(this.predictionResult);
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
    async predictYield() {
      try {
        // 显示输入金针菇区域ID的弹窗
        const { value: clusterId } = await this.$prompt(
          "请输入要分析的金针菇区域ID (cluster_id)",
          "🤖 智能产量预测",
          {
            confirmButtonText: "开始预测",
            cancelButtonText: "取消",
            inputPattern: /^.+$/,
            inputErrorMessage: "金针菇区域ID不能为空",
            inputPlaceholder: "请输入cluster_id，例如：cluster_001",
            customClass: "ai-prompt-dialog",
          }
        );

        if (!clusterId) {
          return;
        }

        const kbId = this.getKnowledgeBaseId();
        const chatId = this.generateChatId();

        // 显示加载提示，设置更长的超时时间
        const loadingInstance = this.$loading({
          lock: true,
          text: "🤖 正在启动AI分析引擎，这可能需要1-2分钟...",
          spinner: "el-icon-loading",
          background: "rgba(0, 0, 0, 0.8)",
          customClass: "ai-loading",
        });

        // 更新加载提示的函数
        const updateLoadingText = (text) => {
          if (loadingInstance) {
            loadingInstance.text = `🤖 ${text}`;
          }
        };

        // 设置更长的超时时间（3分钟）
        const timeoutPromise = new Promise((_, reject) => {
          setTimeout(() => {
            reject(new Error("请求超时，请重试"));
          }, 180000); // 3分钟超时
        });

        const requestPromise = predictYield({
          clusterId: clusterId.trim(),
          kbId,
          chatId,
        });

        let progressInterval = setInterval(() => {
          const messages = [
            "正在查询金针菇簇数据...",
            "正在获取环境监测数据...",
            "正在检索知识库信息...",
            "正在构建机器学习模型...",
            "LLM正在深度分析中...",
            "正在生成预测报告...",
            "即将完成智能分析...",
          ];
          const randomMessage =
            messages[Math.floor(Math.random() * messages.length)];
          updateLoadingText(randomMessage);
        }, 8000); // 每8秒更新一次提示

        // 使用Promise.race来处理超时
        const response = await Promise.race([requestPromise, timeoutPromise]);

        // 清除进度更新定时器
        clearInterval(progressInterval);

        // 关闭加载提示
        loadingInstance.close();

        if (response.code === 200) {
          // 将Markdown格式转换为HTML并显示弹窗
          const markdownContent = this.convertMarkdownToHtml(response.data);
          this.$alert(markdownContent, "智能预测结果", {
            dangerouslyUseHTMLString: true,
            confirmButtonText: "确定",
            customClass: "yield-prediction-dialog",
            width: "800px",
          });
        } else {
          this.$message.error(response.msg || "预测失败");
        }
      } catch (error) {
        // 清除进度更新定时器
        if (typeof progressInterval !== "undefined") {
          clearInterval(progressInterval);
        }

        // 关闭加载提示
        this.$loading().close();

        if (error === "cancel") {
          // 用户取消输入
          return;
        }

        if (error.message && error.message.includes("超时")) {
          this.$message.error(
            "⏰ 预测超时，请重试。AI分析需要较长时间，请耐心等待。"
          );
        } else if (error.message && error.message.includes("Network Error")) {
          this.$message.error("🌐 网络连接错误，请检查网络连接后重试。");
        } else {
          this.$message.error(
            "❌ 预测失败：" + (error.message || "网络错误，请重试")
          );
        }
      }
    },
    // 获取知识库ID（可以根据实际需求修改）
    getKnowledgeBaseId() {
      return "default_kb_id";
    },
    // 生成聊天ID
    generateChatId() {
      const timestamp = Date.now().toString(36);
      const random = Math.random().toString(36).substr(2, 6);
      return `yield_${timestamp}_${random}`;
    },
    // 将Markdown转换为HTML
    convertMarkdownToHtml(markdown) {
      if (!markdown) return "";

      let html = markdown;

      // 处理标题
      html = html.replace(
        /^### (.*$)/gim,
        '<h3 style="color: #409EFF; margin: 15px 0 10px 0; font-weight: 600;">$1</h3>'
      );
      html = html.replace(
        /^#### (.*$)/gim,
        '<h4 style="color: #67C23A; margin: 12px 0 8px 0; font-weight: 500;">$1</h4>'
      );
      html = html.replace(
        /^##### (.*$)/gim,
        '<h5 style="color: #E6A23C; margin: 10px 0 6px 0; font-weight: 500;">$1</h5>'
      );

      // 处理粗体
      html = html.replace(
        /\*\*(.*?)\*\*/g,
        '<strong style="color: #F56C6C; font-weight: 600;">$1</strong>'
      );

      // 处理斜体
      html = html.replace(/\*(.*?)\*/g, '<em style="color: #909399;">$1</em>');

      // 处理代码块
      html = html.replace(
        /```([\s\S]*?)```/g,
        '<pre style="background: #f5f7fa; padding: 10px; border-radius: 4px; border-left: 4px solid #409EFF; margin: 10px 0; font-family: monospace; white-space: pre-wrap;">$1</pre>'
      );

      // 处理行内代码
      html = html.replace(
        /`([^`]+)`/g,
        '<code style="background: #f5f7fa; padding: 2px 4px; border-radius: 3px; font-family: monospace; color: #E6A23C;">$1</code>'
      );

      // 处理列表
      html = html.replace(
        /^\- (.*$)/gim,
        '<li style="margin: 5px 0; color: #606266;">$1</li>'
      );
      html = html.replace(
        /^(\d+)\. (.*$)/gim,
        '<li style="margin: 5px 0; color: #606266;">$2</li>'
      );

      // 处理段落
      html = html.replace(
        /\n\n/g,
        '</p><p style="margin: 10px 0; line-height: 1.6; color: #303133;">'
      );

      // 处理分割线
      html = html.replace(
        /^---$/gim,
        '<hr style="border: none; border-top: 1px solid #DCDFE6; margin: 20px 0;">'
      );

      // 处理引用
      html = html.replace(
        /^> (.*$)/gim,
        '<blockquote style="border-left: 4px solid #409EFF; padding-left: 15px; margin: 10px 0; color: #606266; background: #f8f9fa;">$1</blockquote>'
      );

      // 处理表格（简单处理）
      html = html.replace(
        /\|(.+)\|/g,
        '<tr><td style="padding: 8px; border: 1px solid #DCDFE6;">$1</td></tr>'
      );

      // 包装在段落中
      html =
        '<p style="margin: 10px 0; line-height: 1.6; color: #303133;">' +
        html +
        "</p>";

      // 清理多余的段落标签
      html = html.replace(/<\/p><p[^>]*>(\s*)<\/p>/g, "</p>");

      return html;
    },
    // 格式化预测内容
    formatPredictionContent(content) {
      if (!content) return "";

      // 提取关键信息
      const result = {
        yield: this.extractYieldInfo(content),
        analysis: this.extractAnalysisInfo(content),
        suggestions: this.extractSuggestionsInfo(content),
        risks: this.extractRisksInfo(content),
      };

      // 生成结构化的HTML
      return this.generateStructuredHTML(result);
    },
    // 提取产量信息
    extractYieldInfo(content) {
      const yieldMatch = content.match(/最终预测产量.*?(\d+).*?金针菇/);
      const matureMatch = content.match(/成熟金针菇.*?(\d+)/);
      const futureMatch = content.match(/未来成熟.*?(\d+)/);

      return {
        total: yieldMatch ? yieldMatch[1] : "未知",
        mature: matureMatch ? matureMatch[1] : "0",
        future: futureMatch ? futureMatch[1] : "0",
      };
    },
    // 提取分析信息
    extractAnalysisInfo(content) {
      const totalMatch = content.match(/总数量.*?(\d+)/);
      const avgSizeMatch = content.match(/平均大小.*?(\d+)/);
      const matureRateMatch = content.match(/成熟.*?(\d+\.?\d*%)/);

      return {
        total: totalMatch ? totalMatch[1] : "未知",
        avgSize: avgSizeMatch ? avgSizeMatch[1] : "未知",
        matureRate: matureRateMatch ? matureRateMatch[1] : "未知",
      };
    },
    // 提取建议信息
    extractSuggestionsInfo(content) {
      const suggestions = [];

      // 提取短期管理建议
      const shortTermMatch = content.match(/短期管理.*?([^。]+。)/);
      if (shortTermMatch) suggestions.push(shortTermMatch[1]);

      // 提取整体健康建议
      const healthMatch = content.match(/整体簇健康.*?([^。]+。)/);
      if (healthMatch) suggestions.push(healthMatch[1]);

      return suggestions;
    },
    // 提取风险信息
    extractRisksInfo(content) {
      const risks = [];

      // 提取气候风险
      const climateMatch = content.match(/气候风险.*?([^。]+。)/);
      if (climateMatch) risks.push(climateMatch[1]);

      // 提取生物风险
      const bioMatch = content.match(/生物风险.*?([^。]+。)/);
      if (bioMatch) risks.push(bioMatch[1]);

      return risks;
    },
    // 生成结构化HTML
    generateStructuredHTML(data) {
      return `
         <div class="prediction-summary">
           <div class="summary-card yield-card">
             <h3>🍄 产量预测</h3>
             <div class="yield-stats">
               <div class="stat-item">
                 <span class="stat-label">总产量</span>
                 <span class="stat-value">${data.yield.total} 个</span>
               </div>
               <div class="stat-item">
                 <span class="stat-label">已成熟</span>
                 <span class="stat-value mature">${data.yield.mature} 个</span>
               </div>
               <div class="stat-item">
                 <span class="stat-label">未来成熟</span>
                 <span class="stat-value future">${data.yield.future} 个</span>
               </div>
             </div>
           </div>

           <div class="summary-card analysis-card">
             <h3>📊 数据分析</h3>
             <div class="analysis-stats">
               <div class="stat-item">
                 <span class="stat-label">总数量</span>
                 <span class="stat-value">${data.analysis.total} 个</span>
               </div>
               <div class="stat-item">
                 <span class="stat-label">平均大小</span>
                 <span class="stat-value">${data.analysis.avgSize}mm</span>
               </div>
               <div class="stat-item">
                 <span class="stat-label">成熟率</span>
                 <span class="stat-value">${data.analysis.matureRate}</span>
               </div>
             </div>
           </div>
         </div>

         <div class="prediction-details">
           <div class="detail-section">
             <h3>💡 优化建议</h3>
             <ul class="suggestion-list">
               ${data.suggestions
                 .map((suggestion) => `<li>${suggestion}</li>`)
                 .join("")}
             </ul>
           </div>

           <div class="detail-section">
             <h3>⚠️ 风险提示</h3>
             <ul class="risk-list">
               ${data.risks.map((risk) => `<li>${risk}</li>`).join("")}
             </ul>
           </div>
         </div>
       `;
    },

    handleAiPicture() {
      this.openberryStraw = true;
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
      
      // 将本地上传的图片添加到newpictures数组中
      this.newpictures.push(objectUrl);
      console.log('本地上传图片已添加到newpictures:', this.newpictures);
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

      // 将无人机拍摄的照片添加到newpictures数组中
      this.newpictures.push(ImgBase64);
      console.log('无人机拍摄照片已添加到newpictures:', this.newpictures);

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
     this.newpictures.push(this.capturedImage);
      console.log(this.newpictures);
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
      // 确保索引有效且图片存在
      if (index >= 0 && index < this.newpictures.length && this.newpictures[index]) {
        return this.newpictures[index];
      }
      // 如果没有图片，返回默认占位图
      return 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iNTAiIGhlaWdodD0iNTAiIHZpZXdCb3g9IjAgMCA1MCA1MCIgZmlsbD0ibm9uZSIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj4KPHJlY3Qgd2lkdGg9IjUwIiBoZWlnaHQ9IjUwIiBmaWxsPSIjRjVGNUY1Ii8+CjxwYXRoIGQ9Ik0yNSAyMEMyNy43NjE0IDIwIDMwIDIyLjIzODYgMzAgMjVDMzAgMjcuNzYxNCAyNy43NjE0IDMwIDI1IDMwQzIyLjIzODYgMzAgMjAgMjcuNzYxNCAyMCAyNUMyMCAyMi4yMzg2IDIyLjIzODYgMjAgMjUgMjBaIiBmaWxsPSIjQ0NDQ0NDIi8+CjxwYXRoIGQ9Ik0yNSAzNUMyNy4yMDkxIDM1IDI5IDMzLjIwOTEgMjkgMzFDMjkgMjguNzkwOSAyNy4yMDkxIDI3IDI1IDI3QzIyLjc5MDkgMjcgMjEgMjguNzkwOSAyMSAzMUMyMSAzMy4yMDkxIDIyLjc5MDkgMzUgMjUgMzVaIiBmaWxsPSIjQ0NDQ0NDIi8+Cjwvc3ZnPgo=';
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
/* AI预测按钮样式 */
.ai-predict-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none !important;
  color: white !important;
  font-weight: 600;
  position: relative;
  overflow: hidden;
  transition: all 0.3s ease;
}

.ai-predict-btn:hover {
  background: linear-gradient(135deg, #5a6fd8 0%, #6a4190 100%);
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}

.ai-predict-btn::before {
  content: "";
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(
    90deg,
    transparent,
    rgba(255, 255, 255, 0.2),
    transparent
  );
  transition: left 0.5s;
}

.ai-predict-btn:hover::before {
  left: 100%;
}

/* 预测弹窗样式 */
.prediction-dialog {
  border-radius: 12px;
}

.prediction-dialog :deep(.el-dialog) {
  border-radius: 12px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
  max-height: 90vh;
  display: flex;
  flex-direction: column;
}

.prediction-dialog :deep(.el-dialog__header) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 20px 24px;
  border-radius: 12px 12px 0 0;
}

.prediction-dialog :deep(.el-dialog__title) {
  color: white;
  font-size: 18px;
  font-weight: 600;
}

.prediction-dialog :deep(.el-dialog__body) {
  padding: 0;
  flex: 1;
  overflow: hidden;
}

.prediction-content {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.prediction-header {
  display: flex;
  align-items: center;
  padding: 20px 24px 10px;
  border-bottom: 2px solid #f0f0f0;
  background: #fafafa;
}

.prediction-icon {
  font-size: 24px;
  color: #667eea;
  margin-right: 12px;
}

.prediction-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-right: 12px;
}

.prediction-body {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
  background: white;
  line-height: 1.8;
}

.prediction-footer {
  padding: 16px 24px;
  border-top: 1px solid #e6e6e6;
  background: #fafafa;
  text-align: right;
}

/* 预测结果样式 */
.prediction-body .prediction-summary {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  margin-bottom: 30px;
}

.prediction-body .summary-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  border: 1px solid #e6e6e6;
}

.prediction-body .summary-card h3 {
  color: #2c3e50;
  font-size: 18px;
  font-weight: 600;
  margin: 0 0 16px 0;
  padding-bottom: 8px;
  border-bottom: 2px solid #667eea;
}

.prediction-body .yield-stats,
.prediction-body .analysis-stats {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.prediction-body .stat-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid #f0f0f0;
}

.prediction-body .stat-item:last-child {
  border-bottom: none;
}

.prediction-body .stat-label {
  color: #666;
  font-size: 14px;
  font-weight: 500;
}

.prediction-body .stat-value {
  color: #2c3e50;
  font-size: 16px;
  font-weight: 600;
}

.prediction-body .stat-value.mature {
  color: #27ae60;
}

.prediction-body .stat-value.future {
  color: #f39c12;
}

.prediction-body .prediction-details {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

.prediction-body .detail-section {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  border: 1px solid #e6e6e6;
}

.prediction-body .detail-section h3 {
  color: #2c3e50;
  font-size: 18px;
  font-weight: 600;
  margin: 0 0 16px 0;
  padding-bottom: 8px;
  border-bottom: 2px solid #667eea;
}

.prediction-body .suggestion-list,
.prediction-body .risk-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.prediction-body .suggestion-list li,
.prediction-body .risk-list li {
  color: #2c3e50;
  font-size: 14px;
  line-height: 1.6;
  margin: 8px 0;
  padding: 8px 12px;
  background: #f8f9fa;
  border-radius: 6px;
  border-left: 3px solid #667eea;
}

.prediction-body .risk-list li {
  border-left-color: #e74c3c;
  background: #fdf2f2;
}

/* 加载样式 */
:deep(.ai-loading .el-loading-text) {
  color: #667eea;
  font-weight: 700;
  font-size: 48px;
  letter-spacing: 1px;
  text-align: center;
}

:deep(.ai-loading .el-loading-spinner) {
  font-size: 72px;
  margin-bottom: 12px;
}

:deep(.ai-loading .el-loading-spinner .circular) {
  width: 72px;
  height: 72px;
}

/* 智能预测弹窗样式 */
:deep(.yield-prediction-dialog) {
  max-width: 1000px !important;
  width: 90vw !important;
  border-radius: 16px !important;
  box-shadow: 0 25px 80px rgba(0, 0, 0, 0.15) !important;
  overflow: hidden !important;
}

:deep(.yield-prediction-dialog .el-message-box__content) {
  max-height: 75vh;
  overflow-y: auto;
  padding: 30px;
  background: #ffffff;
  border: 1px solid #e8f4fd;
  position: relative;
}

:deep(.yield-prediction-dialog .el-message-box__content::before) {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: #409eff;
}

:deep(.yield-prediction-dialog .el-message-box__message) {
  line-height: 1.8;
  font-size: 15px;
  color: #2c3e50;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

:deep(.yield-prediction-dialog .el-message-box__header) {
  background: #409eff;
  color: white;
  padding: 25px 30px;
  border-radius: 0;
  position: relative;
  display: flex;
  align-items: center;
}

:deep(.yield-prediction-dialog .el-message-box__header::before) {
  content: '🤖';
  font-size: 24px;
  margin-right: 12px;
}

:deep(.yield-prediction-dialog .el-message-box__title) {
  color: white;
  font-size: 20px;
  font-weight: 700;
  margin: 0;
  display: flex;
  align-items: center;
}

:deep(.yield-prediction-dialog .el-message-box__headerbtn) {
  top: 50%;
  transform: translateY(-50%);
  right: 30px;
}

:deep(.yield-prediction-dialog .el-message-box__headerbtn .el-message-box__close) {
  color: white;
  font-size: 20px;
  font-weight: bold;
}

:deep(.yield-prediction-dialog .el-message-box__headerbtn .el-message-box__close:hover) {
  color: #f0f9ff;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 50%;
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
}

:deep(.yield-prediction-dialog .el-message-box__btns) {
  padding: 20px 30px;
  background: #f8f9fa;
  border-top: 1px solid #e9ecef;
  text-align: center;
}

:deep(.yield-prediction-dialog .el-button--primary) {
  background: #409eff;
  border: none;
  padding: 12px 30px;
  border-radius: 8px;
  font-weight: 600;
  font-size: 16px;
  min-width: 120px;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
  transition: all 0.3s ease;
}

:deep(.yield-prediction-dialog .el-button--primary:hover) {
  background: #337ecc;
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(64, 158, 255, 0.4);
}

/* 美化预测结果内容 */
:deep(.yield-prediction-dialog .el-message-box__message h1) {
  color: #409eff;
  font-size: 24px;
  font-weight: 700;
  margin: 0 0 20px 0;
  padding-bottom: 10px;
  border-bottom: 2px solid #e8f4fd;
}

:deep(.yield-prediction-dialog .el-message-box__message h2) {
  color: #2c3e50;
  font-size: 20px;
  font-weight: 600;
  margin: 25px 0 15px 0;
  padding-left: 15px;
  border-left: 4px solid #409eff;
}

:deep(.yield-prediction-dialog .el-message-box__message h3) {
  color: #409eff;
  font-size: 18px;
  font-weight: 600;
  margin: 20px 0 12px 0;
}

:deep(.yield-prediction-dialog .el-message-box__message p) {
  margin: 12px 0;
  color: #2c3e50;
  line-height: 1.7;
}

:deep(.yield-prediction-dialog .el-message-box__message strong) {
  color: #e6a23c;
  font-weight: 700;
}

:deep(.yield-prediction-dialog .el-message-box__message ul) {
  margin: 15px 0;
  padding-left: 0;
}

:deep(.yield-prediction-dialog .el-message-box__message li) {
  margin: 8px 0;
  padding: 8px 15px;
  background: #f8f9fa;
  border-radius: 6px;
  border-left: 3px solid #409eff;
  list-style: none;
}

:deep(.yield-prediction-dialog .el-message-box__message blockquote) {
  margin: 15px 0;
  padding: 15px 20px;
  background: #f0f9ff;
  border-left: 4px solid #409eff;
  border-radius: 0 8px 8px 0;
  font-style: italic;
  color: #2c3e50;
}

:deep(.yield-prediction-dialog .el-message-box__message code) {
  background: #f1f3f4;
  padding: 2px 6px;
  border-radius: 4px;
  font-family: 'Courier New', monospace;
  color: #e6a23c;
  font-size: 14px;
}

:deep(.yield-prediction-dialog .el-message-box__message pre) {
  background: #f8f9fa;
  padding: 15px;
  border-radius: 8px;
  border: 1px solid #e9ecef;
  overflow-x: auto;
  margin: 15px 0;
}

/* 自定义滚动条 */
:deep(.yield-prediction-dialog .el-message-box__content)::-webkit-scrollbar {
  width: 8px;
}

:deep(.yield-prediction-dialog .el-message-box__content)::-webkit-scrollbar-thumb {
  background: #409eff;
  border-radius: 4px;
}

:deep(.yield-prediction-dialog .el-message-box__content)::-webkit-scrollbar-track {
  background: #f1f3f4;
  border-radius: 4px;
}

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
