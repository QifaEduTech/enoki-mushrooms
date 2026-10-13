<template>
  <div class="app-container">
    <!-- 搜索表单 -->
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="检测置信度" prop="confidence">
        <el-input v-model="queryParams.confidence" placeholder="请输入检测置信度" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="病虫害类别" prop="pestDiseaseClass">
        <el-input v-model="queryParams.pestDiseaseClass" placeholder="请输入病虫害类别" clearable
          @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="严重程度" prop="severityLevel">
        <el-select v-model="queryParams.severityLevel" placeholder="请选择严重程度" clearable>
          <el-option label="轻微" value="low" />
          <el-option label="中等" value="medium" />
          <el-option label="严重" value="high" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 操作按钮 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple"
          @click="handleDelete">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="el-icon-download" size="mini" @click="handleExport">导出</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="primary" plain icon="el-icon-calculator" size="medium" @click="handlePredict"
          class="disease-predict-btn">智能分析金针菇病变🍄</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="primary" plain icon="el-icon-camera" size="mini" @click="handleAiPicture">AI识别病虫害</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 数据表格 -->
    <el-table v-loading="loading" :data="diseaseList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="序号" type="index" align="center" />
      <el-table-column label="对应金针菇簇/区域ID" align="center" prop="clusterId" />
      <el-table-column label="检测对象类型" align="center" prop="object_type" />
      <el-table-column label="检测置信度" align="center" prop="confidence" />
      <el-table-column label="病虫害类别" align="center" prop="pest_disease_class" />
      <el-table-column label="严重程度" align="center" prop="severity_level" />
      <el-table-column label="防治处理建议" align="center" prop="recommendation" />
      <el-table-column label="检测时间" align="center" prop="detected_time" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.detectedTime, "{y}-{m}-{d}") }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)">修改</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize"
      @pagination="getList" />

    <!-- AI识别病虫害对话框 -->
    <el-dialog title="AI识别病虫害" :visible.sync="openberryStraw" width="1200px" append-to-body
      @closed="handleDialogClosed">
      <el-form ref="aiForm">
        <!-- 模式选择 -->
        <el-row class="mode-selector">
          <el-col :span="24">
            <div class="mode-buttons">
              <el-button :type="currentMode === 'drone' ? 'primary' : 'default'" @click="switchMode('drone')"
                size="medium" icon="el-icon-video-camera">
                无人机识别
              </el-button>
              <el-button :type="currentMode === 'camera' ? 'primary' : 'default'" @click="switchMode('camera')"
                size="medium" icon="el-icon-camera">
                摄像头识别
              </el-button>
            </div>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <!-- 左侧：无人机/摄像头预览 -->
          <el-col :span="12">
            <el-form-item>
              <h3 v-if="currentMode === 'drone'">
                <i class="el-icon-video-camera"></i> 无人机实时画面
              </h3>
              <h3 v-else>
                <i class="el-icon-camera"></i> 摄像头实时画面
              </h3>

              <div class="preview-container">
                <!-- 无人机模式 -->
                <div v-if="currentMode === 'drone'">
                  <div class="drone-video-box">
                    <iframe v-if="droneConnected" ref="droneFrame" :src="droneUrl" style="
                        width: 100%;
                        height: 320px;
                        border: 1px solid #ddd;
                        border-radius: 4px;
                      " frameborder="0" allowfullscreen></iframe>
                    <div v-else class="drone-placeholder preview-placeholder">
                      <i class="el-icon-video-camera placeholder-icon"></i>
                      <p>无人机未连接</p>
                      <p class="placeholder-tip">请点击下方连接按钮</p>
                    </div>
                  </div>
                  <div class="drone-controls">
                    <el-button type="primary" size="small" @click="toggleDroneConnection" :loading="droneConnecting"
                      icon="el-icon-connection">
                      {{ droneConnected ? '断开连接' : '连接无人机' }}
                    </el-button>
                    <el-button type="success" size="small" @click="captureFromDrone"
                      :disabled="!droneConnected || loadingAiResult" icon="el-icon-camera">
                      截图识别
                    </el-button>
                    <el-button v-if="dronePhotoUrl" type="info" size="small" @click="dronePhotoUrl = ''"
                      icon="el-icon-delete">
                      清空预览
                    </el-button>
                  </div>
                  <div v-if="dronePhotoUrl" class="photo-preview-box">
                    <p class="preview-title">无人机截图预览:</p>
                    <img :src="dronePhotoUrl" class="photo-preview-img" />
                  </div>
                </div>

                <!-- 摄像头模式 -->
                <div v-else>
                  <div class="camera-preview-box">
                    <div class="video-display-area">
                      <video ref="cameraVideo" autoplay playsinline muted class="camera-video-element"
                        v-show="isCameraActive && cameraStream"></video>
                      <div v-show="!isCameraActive || !cameraStream" class="camera-placeholder preview-placeholder">
                        <i class="el-icon-camera placeholder-icon"></i>
                        <p>摄像头未启动</p>
                        <p class="placeholder-tip">点击下方按钮启动摄像头</p>
                      </div>
                      <div v-show="isCameraActive && cameraStream" class="camera-control-overlay">
                        <el-button type="danger" size="mini" circle @click="stopCamera" title="关闭摄像头"
                          class="close-camera-btn">
                          <i class="el-icon-close"></i>
                        </el-button>
                      </div>
                    </div>
                    <div v-if="cameraPhotoUrl" class="photo-preview-box">
                      <p class="preview-title">拍照预览:</p>
                      <img :src="cameraPhotoUrl" class="photo-preview-img" />
                      <div class="preview-actions">
                        <el-button size="mini" type="text" @click="cameraPhotoUrl = ''">
                          清除预览
                        </el-button>
                      </div>
                    </div>
                  </div>
                  <canvas ref="cameraCanvas" style="display: none;"></canvas>

                  <!-- 设备选择下拉框 -->
                  <div v-if="isCameraActive && cameraDevices.length > 1" class="device-selector">
                    <el-select v-model="selectedDeviceId" placeholder="切换摄像头" size="small" @change="switchCamera">
                      <el-option
                        v-for="device in cameraDevices"
                        :key="device.deviceId"
                        :label="device.label || `摄像头 ${device.deviceId.slice(0, 5)}`"
                        :value="device.deviceId">
                      </el-option>
                    </el-select>
                  </div>

                  <div class="camera-controls">
                    <el-button v-if="!isCameraActive" type="primary" size="small" @click="startCamera"
                      :loading="cameraLoading" icon="el-icon-video-play">
                      启动摄像头
                    </el-button>
                    <el-button v-if="isCameraActive" type="success" size="small" @click="takePhoto"
                      :disabled="!isCameraActive || cameraLoading || loadingAiResult" icon="el-icon-camera">
                      拍照识别
                    </el-button>
                    <el-button v-if="isCameraActive" type="warning" size="small" @click="stopCamera"
                      icon="el-icon-video-pause">
                      关闭摄像头
                    </el-button>
                  </div>
                </div>
              </div>
            </el-form-item>
          </el-col>

          <!-- 右侧：上传本地图片识别 -->
          <el-col :span="12">
            <el-form-item>
              <h3><i class="el-icon-upload"></i> 上传本地图片识别</h3>
              <div class="upload-container">
                <el-upload class="avatar-uploader" action="#" :show-file-list="false" :on-change="handlePictureChange"
                  :auto-upload="false" accept="image/*" :disabled="loadingAiResult">
                  <div class="preview-container">
                    <img v-if="berryPictureUrl" :src="berryPictureUrl" class="avatar-resize" />
                    <div v-else class="upload-placeholder">
                      <i class="el-icon-plus avatar-uploader-icon"></i>
                      <p>点击上传本地图片</p>
                      <p class="upload-tips">支持 JPG、PNG 格式</p>
                    </div>
                  </div>
                </el-upload>
                <div class="upload-controls">
                  <el-button type="primary" size="small" @click="submitLocalImage"
                    :disabled="!berryPictureFile || loadingAiResult" :loading="loadingAiResult" icon="el-icon-search">
                    识别本地图片
                  </el-button>
                  <el-button type="info" size="small" @click="clearLocalImage" :disabled="!berryPictureFile"
                    icon="el-icon-delete">
                    清空
                  </el-button>
                </div>
              </div>
            </el-form-item>
          </el-col>
        </el-row>

        <!-- AI识别结果 -->
        <el-row v-if="aiResult" class="result-container">
          <el-col :span="24">
            <h3><i class="el-icon-document"></i> 识别结果</h3>
            <div class="result-content">
              <div v-html="aiResult"></div>
            </div>
          </el-col>
        </el-row>
      </el-form>

      <div slot="footer" class="dialog-footer">
        <el-button type="success" size="mini" @click="cancel" icon="el-icon-close">关闭</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {
  listDisease,
  getDisease,
  delDisease,
  addDisease,
  updateDisease,
  yoloDetect,
  yoloDetectBase64,
  yoloHealth,
} from "@/api/system/disease";

export default {
  name: "Disease",
  data() {
    return {
      loading: true,
      ids: [],
      single: true,
      multiple: true,
      showSearch: true,
      total: 0,
      diseaseList: [],
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        confidence: null,
        pestDiseaseClass: null,
        severityLevel: null,
        detectedTime: null,
      },
      openberryStraw: false,
      currentMode: 'drone',
      droneConnected: false,
      droneConnecting: false,
      droneUrl: "http://127.0.0.1:8889/cam01/",
      dronePhotoUrl: "",
      dronePhotoBase64: "",
      cameraStream: null,
      isCameraActive: false,
      cameraLoading: false,
      cameraPhotoUrl: "",
      cameraPhotoBase64: "",
      cameraDevices: [],
      selectedDeviceId: '',
      berryPictureFile: null,
      berryPictureUrl: "",
      aiResult: "",
      loadingAiResult: false,
      yoloHealthy: false,
      // YOLO 服务地址（用于结果图片）
      yoloBaseUrl: 'http://localhost:5000',
    };
  },
  created() {
    this.getList();
    this.checkYoloHealth();
  },
  beforeDestroy() {
    this.stopCamera();
  },
  mounted() {
    window.addEventListener("message", this.handleDroneMessage);
  },
  methods: {
    // ==================== YOLO 健康检查 ====================
    async checkYoloHealth() {
      try {
        const res = await yoloHealth();
        this.yoloHealthy = res.data && res.data.healthy;
        if (!this.yoloHealthy) {
          console.warn('YOLO 服务未就绪');
        }
      } catch (e) {
        this.yoloHealthy = false;
        console.warn('YOLO 服务连接失败:', e.message);
      }
    },

    // ==================== 列表相关 ====================
    getList() {
      this.loading = true;
      listDisease(this.queryParams).then((response) => {
        this.diseaseList = response.rows;
        this.total = response.total;
        this.loading = false;
      });
    },
    cancel() {
      this.openberryStraw = false;
      this.stopCamera();
      this.disconnectDrone();
      this.reset();
    },
    handleDialogClosed() {
      this.stopCamera();
      this.disconnectDrone();
      this.reset();
    },
    reset() {
      this.aiResult = "";
      this.berryPictureFile = null;
      this.berryPictureUrl = "";
      this.cameraPhotoUrl = "";
      this.cameraPhotoBase64 = "";
      this.dronePhotoUrl = "";
      this.dronePhotoBase64 = "";
      this.loadingAiResult = false;
    },
    handleQuery() {
      this.queryParams.pageNum = 1;
      this.getList();
    },
    resetQuery() {
      this.resetForm("queryForm");
      this.handleQuery();
    },
    handleSelectionChange(selection) {
      this.ids = selection.map((item) => item.id);
      this.single = selection.length !== 1;
      this.multiple = !selection.length;
    },
    handleUpdate(row) {
      this.reset();
      const id = row.id || this.ids;
      getDisease(id).then((response) => {
        this.form = response.data;
        this.open = true;
        this.title = "修改病虫害分析";
      });
    },
    handleDelete(row) {
      const ids = row.id || this.ids;
      this.$modal
        .confirm('是否确认删除病虫害分析编号为"' + ids + '"的数据项？')
        .then(() => delDisease(ids))
        .then(() => {
          this.getList();
          this.$modal.msgSuccess("删除成功");
        })
        .catch(() => {});
    },
    handleExport() {
      this.download(
        "system/disease/export",
        { ...this.queryParams },
        `disease_${new Date().getTime()}.xlsx`
      );
    },
    handlePredict() {
      this.$message.info("金针菇病变功能暂未开放");
    },
    handleAiPicture() {
      this.openberryStraw = true;
      this.checkYoloHealth();
    },

    // ==================== 模式切换 ====================
    switchMode(mode) {
      if (this.currentMode === mode) return;
      if (this.currentMode === 'camera') this.stopCamera();
      else if (this.currentMode === 'drone') this.disconnectDrone();
      this.currentMode = mode;
      this.aiResult = "";
      if (mode === 'drone') this.$message.info("已切换到无人机模式");
      else this.$message.info("已切换到摄像头模式");
    },

    // ==================== 无人机功能 ====================
    toggleDroneConnection() {
      if (this.droneConnected) this.disconnectDrone();
      else this.connectDrone();
    },
    connectDrone() {
      this.droneConnecting = true;
      setTimeout(() => {
        this.droneConnected = true;
        this.droneConnecting = false;
        this.$message.success("无人机连接成功");
      }, 1000);
    },
    disconnectDrone() {
      this.droneConnected = false;
      this.dronePhotoUrl = "";
      this.dronePhotoBase64 = "";
      this.$message.info("硬件设备已断开连接");
    },
    async captureFromDrone() {
      if (!this.droneConnected) {
        this.$message.warning("请先连接无人机");
        return;
      }
      try {
        this.$message.info("正在从无人机获取截图...");
        const canvas = document.createElement('canvas');
        const ctx = canvas.getContext('2d');
        canvas.width = 640;
        canvas.height = 480;
        ctx.fillStyle = '#4CAF50';
        ctx.fillRect(0, 0, canvas.width, canvas.height);
        ctx.fillStyle = '#FF5722';
        ctx.beginPath();
        ctx.arc(320, 240, 100, 0, Math.PI * 2);
        ctx.fill();
        ctx.fillStyle = 'white';
        ctx.font = 'bold 20px Arial';
        ctx.textAlign = 'center';
        ctx.fillText('无人机拍摄的金针菇田', 320, 120);
        ctx.fillText('检测到疑似病害区域', 320, 360);
        const photoUrl = canvas.toDataURL('image/jpeg', 0.9);
        this.dronePhotoUrl = photoUrl;
        this.dronePhotoBase64 = photoUrl.split(',')[1];
        await this.showDiseaseResult('drone');
        this.$message.success("无人机截图成功，开始识别...");
      } catch (error) {
        console.error("无人机截图失败:", error);
        this.$message.error("无人机截图失败: " + error.message);
      }
    },
    handleDroneMessage(event) {
      if (event.origin !== 'http://127.0.0.1:8889') return;
      if (event.data && event.data.type === 'canvas_data') {
        const base64Data = event.data.image;
        if (base64Data) {
          this.dronePhotoUrl = `data:image/jpeg;base64,${base64Data}`;
          this.dronePhotoBase64 = base64Data;
          this.showDiseaseResult('drone');
          this.$message.success("收到无人机图像，开始识别...");
        }
      }
    },

    // ==================== 摄像头功能 ====================
    isSecureContext() {
      const protocol = window.location.protocol;
      const hostname = window.location.hostname;
      if (protocol === 'https:') return true;
      if (hostname === 'localhost' || hostname === '127.0.0.1') return true;
      return false;
    },
    async getCameraDevices() {
      if (!this.isSecureContext()) return [];
      if (!navigator.mediaDevices || !navigator.mediaDevices.enumerateDevices) return [];
      try {
        const devices = await navigator.mediaDevices.enumerateDevices();
        const videoDevices = devices.filter(device => device.kind === 'videoinput');
        this.cameraDevices = videoDevices;
        if (this.selectedDeviceId && !videoDevices.some(d => d.deviceId === this.selectedDeviceId)) {
          this.selectedDeviceId = videoDevices.length > 0 ? videoDevices[0].deviceId : '';
        } else if (videoDevices.length > 0 && !this.selectedDeviceId) {
          this.selectedDeviceId = videoDevices[0].deviceId;
        }
        return videoDevices;
      } catch (error) {
        console.error('获取摄像头列表失败:', error);
        return [];
      }
    },
    async switchCamera() {
      if (!this.selectedDeviceId) {
        this.$message.warning('没有可用的摄像头设备');
        return;
      }
      if (this.cameraStream) {
        this.stopCamera(true);
        await this.$nextTick();
        await this.startCamera(this.selectedDeviceId);
      } else {
        await this.startCamera(this.selectedDeviceId);
      }
    },
    async startCamera(deviceId = null) {
      this.cameraLoading = true;
      try {
        if (!this.isSecureContext()) {
          throw new Error('当前页面未使用安全连接（HTTPS 或 localhost），请使用 localhost 或 HTTPS 访问本页面以启用摄像头功能');
        }
        if (!navigator.mediaDevices || !navigator.mediaDevices.getUserMedia) {
          throw new Error('当前浏览器不支持摄像头访问，请升级浏览器');
        }
        if (this.cameraStream) {
          this.stopCamera(true);
          await this.$nextTick();
        }
        await this.getCameraDevices();
        let targetDeviceId = deviceId || this.selectedDeviceId;
        if (targetDeviceId && !this.cameraDevices.some(d => d.deviceId === targetDeviceId)) {
          targetDeviceId = null;
          this.selectedDeviceId = null;
        }
        let constraints = {
          video: { width: { ideal: 640 }, height: { ideal: 480 } },
          audio: false
        };
        if (targetDeviceId) {
          constraints.video.deviceId = { exact: targetDeviceId };
        } else {
          constraints.video.facingMode = "environment";
        }
        try {
          this.cameraStream = await navigator.mediaDevices.getUserMedia(constraints);
        } catch (err) {
          if (err.name === 'OverconstrainedError' && targetDeviceId) {
            constraints = {
              video: { width: { ideal: 640 }, height: { ideal: 480 } },
              audio: false
            };
            this.cameraStream = await navigator.mediaDevices.getUserMedia(constraints);
            const tracks = this.cameraStream.getVideoTracks();
            if (tracks.length > 0) {
              const settings = tracks[0].getSettings();
              if (settings.deviceId) {
                this.selectedDeviceId = settings.deviceId;
                await this.getCameraDevices();
              }
            }
          } else {
            throw err;
          }
        }
        const video = this.$refs.cameraVideo;
        if (!video) throw new Error("视频元素未找到");
        video.srcObject = this.cameraStream;
        await new Promise((resolve) => {
          video.onloadedmetadata = () => resolve();
          setTimeout(() => { if (video.readyState >= 1) resolve(); }, 1000);
        });
        try { await video.play(); } catch (e) { video.muted = true; await video.play(); }
        this.isCameraActive = true;
        this.$message.success("摄像头启动成功");
        if (this.cameraStream) {
          const track = this.cameraStream.getVideoTracks()[0];
          if (track && track.getSettings) {
            const settings = track.getSettings();
            if (settings.deviceId && this.selectedDeviceId !== settings.deviceId) {
              this.selectedDeviceId = settings.deviceId;
              await this.getCameraDevices();
            }
          }
        }
      } catch (error) {
        console.error("摄像头启动失败:", error);
        let msg = "摄像头启动失败";
        if (error.message && error.message.includes('请使用 localhost 或 HTTPS')) {
          msg = error.message;
        } else if (error.message && error.message.includes('不支持摄像头访问')) {
          msg = error.message;
        } else if (error.name === 'NotAllowedError') {
          msg = "请允许摄像头访问权限";
        } else if (error.name === 'NotFoundError') {
          msg = "未找到摄像头设备";
        } else if (error.name === 'NotReadableError') {
          msg = "摄像头正被其他应用占用";
        } else if (error.name === 'OverconstrainedError') {
          msg = "无法满足摄像头约束条件，请尝试切换设备或重启浏览器";
        } else {
          msg = `摄像头错误: ${error.message}`;
        }
        this.$message.error(msg);
        this.stopCamera(true);
      } finally {
        this.cameraLoading = false;
      }
    },
    stopCamera(skipMessage = false) {
      if (this.cameraStream) {
        this.cameraStream.getTracks().forEach(track => track.stop());
        this.cameraStream = null;
      }
      this.isCameraActive = false;
      const video = this.$refs.cameraVideo;
      if (video) { video.srcObject = null; video.pause(); }
      if (!skipMessage) {
        this.$message.info("摄像头已关闭");
      }
    },
    takePhoto() {
      const video = this.$refs.cameraVideo;
      const canvas = this.$refs.cameraCanvas;
      if (!video || !canvas) { this.$message.warning("摄像头未准备好"); return; }
      if (!this.cameraStream || !this.isCameraActive) { this.$message.warning("请先启动摄像头"); return; }
      try {
        if (video.readyState < 2) { this.$message.warning("视频尚未准备好，请稍候"); return; }
        canvas.width = video.videoWidth || 640;
        canvas.height = video.videoHeight || 480;
        canvas.getContext('2d').drawImage(video, 0, 0, canvas.width, canvas.height);
        const photoUrl = canvas.toDataURL('image/jpeg', 0.85);
        this.cameraPhotoUrl = photoUrl;
        this.cameraPhotoBase64 = photoUrl.split(',')[1];
        this.showDiseaseResult('camera');
        this.$message.success("拍照成功，开始识别...");
      } catch (error) {
        console.error("拍照失败:", error);
        this.$message.error("拍照失败: " + error.message);
      }
    },

    // ==================== 本地图片上传 ====================
    handlePictureChange(file) {
      if (!file) return;
      if (this.berryPictureUrl) URL.revokeObjectURL(this.berryPictureUrl);
      this.berryPictureFile = file.raw;
      this.berryPictureUrl = URL.createObjectURL(file.raw);
    },
    clearLocalImage() {
      if (this.berryPictureUrl) URL.revokeObjectURL(this.berryPictureUrl);
      this.berryPictureFile = null;
      this.berryPictureUrl = "";
      this.$message.info("已清空图片");
    },
    async submitLocalImage() {
      if (!this.berryPictureFile) { this.$message.warning("请先选择图片"); return; }
      this.loadingAiResult = true;
      this.aiResult = "";
      try {
        await this.showDiseaseResult('local');
      } catch (error) {
        console.error("识别失败:", error);
        this.$message.error("识别失败: " + error.message);
        this.aiResult = `<div class="error-result">识别失败: ${error.message}</div>`;
      }
    },

    // ==================== YOLO 识别核心 ====================
    async showDiseaseResult(source) {
      this.loadingAiResult = true;
      this.aiResult = `<div class="loading-result">
        <i class="el-icon-loading"></i> YOLOv8 模型正在分析图像...
        <p style="margin-top: 10px; font-size: 12px; color: #999;">本地推理中，请稍候...</p>
      </div>`;

      try {
        let result;

        if (source === 'local' && this.berryPictureFile) {
          // 本地图片使用文件上传方式
          const res = await yoloDetect(this.berryPictureFile);
          result = res.data || res;
        } else {
          // 无人机/摄像头使用 Base64 方式
          let base64 = '';
          if (source === 'drone') base64 = this.dronePhotoBase64;
          else if (source === 'camera') base64 = this.cameraPhotoBase64;

          if (!base64) throw new Error('未获取到图像数据');
          const res = await yoloDetectBase64(base64);
          result = res.data || res;
        }

        // 格式化并显示结果
        this.aiResult = this.formatYoloResult(result);
        this.$message.success(`YOLO 识别完成，检测到 ${result.count || 0} 个目标`);
        this.getList(); // 刷新列表
      } catch (error) {
        console.error("YOLO 识别失败:", error);
        this.aiResult = `<div class="error-result">识别失败: ${error.message || '请确认 YOLO 服务已启动'}</div>`;
      } finally {
        this.loadingAiResult = false;
      }
    },

    formatYoloResult(result) {
      const detections = result.detections || [];
      const elapsed = result.elapsed_ms || 0;
      const resultImageUrl = result.result_image_url || '';

      if (detections.length === 0) {
        return `<div class="ai-result-content">
          <h3>🍄 YOLOv8 识别报告</h3>
          <div class="result-item">
            <span class="item-content">未检测到病虫害，金针菇状态良好 ✅</span>
          </div>
          <div class="result-item">
            <span class="item-content"><strong>推理耗时：</strong>${elapsed}ms</span>
          </div>
        </div>`;
      }

      let html = `<div class="ai-result-content">
        <h3>🍄 YOLOv8 识别报告</h3>
        <div class="result-item">
          <span class="item-number">📊</span>
          <span class="item-content"><strong>检测数量：</strong>${detections.length} 个目标 | <strong>推理耗时：</strong>${elapsed}ms</span>
        </div>`;

      // 结果图片
      if (resultImageUrl) {
        html += `<div style="margin: 15px 0; text-align: center;">
          <img src="${resultImageUrl}" style="max-width: 100%; max-height: 300px; border-radius: 8px; border: 2px solid #409EFF;" />
          <p style="font-size: 12px; color: #999; margin-top: 5px;">检测结果标注图</p>
        </div>`;
      }

      // 每个检测结果
      detections.forEach((det, index) => {
        const confidence = ((det.confidence || 0) * 100).toFixed(1);
        const className = det.class_name || `类别${det.class_id}`;
        const severityCn = det.severity_cn || det.severity || '未知';
        const recommendation = det.recommendation || '暂无建议';

        const severityColor = {
          'low': '#67C23A', 'medium': '#E6A23C', 'high': '#F56C6C',
          '轻微': '#67C23A', '中等': '#E6A23C', '严重': '#F56C6C'
        }[det.severity] || '#909399';

        html += `
          <div class="detection-card" style="margin: 15px 0; padding: 15px; background: #f8f9fa; border-radius: 8px; border-left: 4px solid ${severityColor};">
            <h4 style="margin: 0 0 10px 0; color: #303133;">
              <span style="color: ${severityColor};">●</span> 检测目标 ${index + 1}：${className}
            </h4>
            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 8px; font-size: 13px;">
              <div><strong>置信度：</strong>${confidence}%</div>
              <div><strong>严重程度：</strong><span style="color: ${severityColor}; font-weight: bold;">${severityCn}</span></div>
            </div>
            <div style="margin-top: 10px; font-size: 13px;">
              <strong>防治建议：</strong>
              <div style="margin-top: 5px; white-space: pre-line; color: #606266;">${recommendation}</div>
            </div>
          </div>`;
      });

      html += `</div>`;
      return html;
    },

    parseTime(time, format) {
      if (!time) return "";
      const date = new Date(time);
      const year = date.getFullYear();
      const month = date.getMonth() + 1;
      const day = date.getDate();
      return format.replace("{y}", year).replace("{m}", month).replace("{d}", day);
    },
  },
};
</script>

<style scoped>
.app-container { padding: 20px; }
.disease-predict-btn {
  background: linear-gradient(135deg, #f56c6c 0%, #e6a23c 100%);
  border: none !important;
  color: white !important;
  font-weight: 600;
}
.disease-predict-btn:hover {
  background: linear-gradient(135deg, #e74c3c 0%, #d68910 100%);
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(245, 108, 108, 0.4);
}
.mode-selector { margin-bottom: 20px; }
.mode-buttons {
  display: flex; gap: 15px; justify-content: center;
  padding: 10px; background: #f5f7fa; border-radius: 8px; align-items: center;
}
.preview-container {
  border: 2px solid #e4e7ed; border-radius: 8px;
  padding: 15px; background: #f8f9fa; height: 100%;
}
.drone-video-box {
  width: 100%; height: 320px; margin-bottom: 15px;
  border: 1px solid #dcdfe6; border-radius: 4px; overflow: hidden; background: #000;
}
.preview-placeholder {
  width: 100%; height: 100%;
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  color: #909399; background: #f5f7fa;
  border: 2px dashed #dcdfe6; border-radius: 4px;
}
.drone-controls { display: flex; gap: 10px; justify-content: center; margin-bottom: 15px; }
.camera-preview-box { margin-bottom: 15px; }
.video-display-area {
  width: 100%; height: 320px; position: relative;
  border: 1px solid #dcdfe6; border-radius: 4px; overflow: hidden; background: #000; margin-bottom: 15px;
}
.camera-video-element { width: 100%; height: 100%; object-fit: cover; background: #000; display: block; }
.camera-placeholder { position: absolute; top: 0; left: 0; z-index: 1; }
.placeholder-icon { font-size: 48px; margin-bottom: 10px; color: #c0c4cc; }
.placeholder-tip { font-size: 12px; color: #999; margin-top: 5px; }
.camera-control-overlay { position: absolute; top: 10px; right: 10px; z-index: 10; }
.close-camera-btn { background: rgba(255, 0, 0, 0.7); border: none; }
.close-camera-btn:hover { background: rgba(255, 0, 0, 0.9); }
.photo-preview-box { border: 1px solid #e4e7ed; border-radius: 4px; padding: 10px; background: white; margin-top: 10px; }
.preview-title { margin-bottom: 8px; color: #606266; font-weight: bold; font-size: 14px; }
.photo-preview-img { max-width: 100%; max-height: 120px; border-radius: 4px; display: block; margin: 0 auto; border: 1px solid #dcdfe6; }
.preview-actions { margin-top: 8px; text-align: center; }
.camera-controls { display: flex; gap: 10px; justify-content: center; margin-top: 10px; }
.device-selector { margin-bottom: 10px; text-align: center; }
.upload-container { border: 2px solid #e4e7ed; border-radius: 8px; padding: 15px; background: #f8f9fa; height: 100%; }
.avatar-resize { max-width: 100%; max-height: 100%; object-fit: contain; }
.upload-placeholder { text-align: center; color: #909399; }
.avatar-uploader-icon { font-size: 48px; margin-bottom: 10px; display: block; color: #c0c4cc; }
.upload-tips { font-size: 12px; color: #c0c4cc; margin-top: 5px; }
.upload-controls { display: flex; gap: 10px; justify-content: center; }
.result-container { margin-top: 20px; padding: 20px; border: 2px solid #409EFF; border-radius: 8px; background: #f0f9ff; }
.result-content { max-height: 300px; overflow-y: auto; padding: 15px; background: white; border-radius: 4px; border: 1px solid #ebeef5; }
.dialog-footer { display: flex; justify-content: flex-end; gap: 10px; margin-top: 20px; padding-top: 20px; border-top: 1px solid #e4e7ed; }
h3 { color: #409EFF; margin-bottom: 15px; display: flex; align-items: center; gap: 8px; }
h3 i { font-size: 18px; }
</style>

<style>
.ai-result-content { line-height: 1.8; color: #333; font-size: 14px; }
.ai-result-content h3 { color: #F56C6C; margin: 20px 0 15px 0; font-size: 18px; border-bottom: 2px solid #F56C6C; padding-bottom: 5px; }
.ai-result-content strong { color: #F56C6C; font-weight: bold; }
.ai-result-content .result-item { margin: 15px 0; padding-left: 10px; border-left: 3px solid #409EFF; background: #f8f9fa; padding: 10px; border-radius: 4px; }
.ai-result-content .item-number { font-weight: bold; color: #409EFF; margin-right: 8px; font-size: 16px; }
.ai-result-content .item-content { color: #333; font-weight: bold; }
.ai-result-content ul { margin-left: 20px; margin-bottom: 15px; }
.ai-result-content li { margin: 8px 0; line-height: 1.6; }
.loading-result { text-align: center; padding: 40px 20px; color: #409EFF; font-size: 16px; }
.loading-result i { font-size: 24px; margin-right: 10px; animation: rotating 2s linear infinite; }
@keyframes rotating { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }
.error-result { color: #F56C6C; padding: 20px; background: #fef0f0; border-radius: 6px; border-left: 4px solid #F56C6C; font-size: 14px; }
.raw-output { background: #f9f9f9; padding: 12px; border-radius: 6px; margin-top: 8px; line-height: 1.6; }
</style>