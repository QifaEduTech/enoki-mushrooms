<template>
  <div class="app-container">
    <el-form ref="form" label-width="120px">
      <el-row>
        <!-- 左侧：上传图片 -->
        <el-col :span="12">
          <el-form-item label="上传金针菇图片">
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
                  <img v-if="berryPictureUrl" :src="berryPictureUrl" class="avatar-resize" />
                  <div v-else class="upload-placeholder">
                    <i class="el-icon-plus avatar-uploader-icon"></i>
                    <p>点击上传图片</p>
                  </div>
                </div>
              </el-upload>
            </div>
          </el-form-item>
        </el-col>

        <!-- 右侧：AI识别结果 -->
        <el-col :span="12" style="padding-left: 20px;">
          <div class="custom-textarea" :style="{ height: fixedHeight, overflowY: 'auto' }">
            <p v-if="!responseTextPicture && !loadingAiResult" style="color: #999;">等待AI识别结果...</p>
            <p v-else-if="loadingAiResult" style="color: #409EFF;">
              <i class="el-icon-loading"></i> AI正在识别中，请稍候...
            </p>
            <div v-else v-html="responseTextPicture"></div>
          </div>
        </el-col>
      </el-row>
    </el-form>
    
    <!-- 底部按钮 -->
    <div class="dialog-footer">
      <el-button 
        type="primary" 
        size="mini" 
        @click="submitToAI" 
        :loading="loadingAiResult"
        :disabled="!berryPictureFile"
      >
        {{ loadingAiResult ? '识别中...' : '提交识别' }}
      </el-button>
      <el-button type="success" size="mini" @click="cancel">关闭</el-button>
      <el-button type="primary" size="mini" icon="el-icon-video-play" @click="handlePlay">
        播放
      </el-button>
      <el-button type="warning" size="mini" icon="el-icon-video-pause" @click="handlePause" :disabled="!isPlaying">
        暂停
      </el-button>
      <el-button type="info" size="mini" icon="el-icon-video-stop" @click="handleStop">
        停止
      </el-button>
    </div>
  </div>
</template>

<script>
import axios from "axios";

export default {
  name: "Pets",
  data() {
    return {
      // 图片相关
      berryPictureFile: null,
      berryPictureUrl: '',
      
      // AI识别结果
      responseTextPicture: '',
      loadingAiResult: false,
      
      // 语音播报相关
      textToSpeak: [],
      isPlaying: false,
      isPaused: false,
      currentIndex: 0,
      
      // 固定高度
      lineHeight: 24,
      linesToShow: 20,
      
      // ✅ 直接在这里写你的API Key
      apiKey: "sk-10510f4f7a4248c789b6029c309b0de0",
      
      // 简单病害知识
      diseasePrompt: "作为金针菇种植专家，识别图像中金针菇的病变情况，给出病害名称、特征、田间管理办法和预防措施。"
    };
  },
  computed: {
    fixedHeight() {
      return this.lineHeight * this.linesToShow + 'px';
    }
  },
  methods: {
    // 取消/重置
    cancel() {
      this.berryPictureFile = null;
      this.berryPictureUrl = '';
      this.responseTextPicture = '';
      this.textToSpeak = [];
      this.loadingAiResult = false;
      this.handleStop();
      
      if (this.berryPictureUrl) {
        URL.revokeObjectURL(this.berryPictureUrl);
      }
    },
    
    // 图片上传
    handlePictureChange(file) {
      if (!file) return;
      
      // 清除旧预览
      if (this.berryPictureUrl) {
        URL.revokeObjectURL(this.berryPictureUrl);
      }
      
      // 保存文件
      this.berryPictureFile = file.raw;
      this.berryPictureUrl = URL.createObjectURL(file.raw);
    },
    
    // 提交识别
    async submitToAI() {
      if (!this.berryPictureFile) {
        this.$message.warning('请先上传图片');
        return;
      }
      
      this.loadingAiResult = true;
      this.responseTextPicture = '';
      
      try {
        // 1. 图片转Base64
        const base64Image = await this.fileToBase64(this.berryPictureFile);
        
        // 2. 调用AI识别
        const result = await this.callAI(base64Image);
        
        // 3. 显示结果
        this.showResult(result);
        
        // 4. 添加到语音队列
        this.textToSpeak.push(result);
        
      } catch (error) {
        console.error('错误:', error);
        this.responseTextPicture = `<p style="color: red;">错误: ${error.message}</p>`;
      } finally {
        this.loadingAiResult = false;
      }
    },
    
    // 图片转Base64
    fileToBase64(file) {
      return new Promise((resolve, reject) => {
        const reader = new FileReader();
        reader.onload = () => {
          const base64 = reader.result.split(',')[1];
          resolve(base64);
        };
        reader.onerror = reject;
        reader.readAsDataURL(file);
      });
    },
    
    // 调用AI接口（简化版）
    async callAI(base64Image) {
      const url = 'https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions';
      
      const response = await axios.post(url, {
        model: "qwen-vl-max",
        messages: [
          {
            role: "user",
            content: [
              {
                type: "image_url",
                image_url: {
                  url: `data:image/jpeg;base64,${base64Image}`
                }
              },
              {
                type: "text",
                text: this.diseasePrompt
              }
            ]
          }
        ]
      }, {
        headers: {
          'Authorization': `Bearer ${this.apiKey}`,
          'Content-Type': 'application/json'
        }
      });
      
      if (response.data.choices && response.data.choices[0]) {
        return response.data.choices[0].message.content;
      } else {
        throw new Error('AI未返回有效结果');
      }
    },
    
    // 显示结果（简单打字机效果）
    showResult(text) {
      this.responseTextPicture = '';
      let index = 0;
      const speed = 30;
      
      const interval = setInterval(() => {
        if (index < text.length) {
          // 简单处理换行和格式
          let displayText = text.substring(0, index + 1);
          displayText = displayText
            .replace(/\n/g, '<br>')
            .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
            .replace(/\*(.*?)\*/g, '<em>$1</em>');
          
          this.responseTextPicture = displayText;
          index++;
        } else {
          clearInterval(interval);
        }
      }, speed);
    },
    
    // 语音播报
    handlePlay() {
      if (this.isPlaying && this.isPaused) {
        speechSynthesis.resume();
        this.isPaused = false;
        return;
      }
      
      if (this.isPlaying || this.textToSpeak.length === 0) return;
      
      this.isPlaying = true;
      this.currentIndex = 0;
      this.playNext();
    },
    
    handlePause() {
      if (this.isPlaying && !this.isPaused) {
        speechSynthesis.pause();
        this.isPaused = true;
      }
    },
    
    handleStop() {
      speechSynthesis.cancel();
      this.isPlaying = false;
      this.isPaused = false;
      this.textToSpeak = [];
      this.currentIndex = 0;
    },
    
    playNext() {
      if (this.currentIndex >= this.textToSpeak.length) {
        this.handleStop();
        return;
      }
      
      const text = this.textToSpeak[this.currentIndex];
      const utterance = new SpeechSynthesisUtterance(text);
      utterance.lang = 'zh-CN';
      
      utterance.onend = () => {
        this.currentIndex++;
        this.playNext();
      };
      
      utterance.onerror = () => {
        this.handleStop();
      };
      
      speechSynthesis.speak(utterance);
    }
  }
};
</script>

<style scoped>
.app-container {
  padding: 20px;
}

.custom-textarea {
  font-family: 'Microsoft YaHei', sans-serif;
  background-color: #f9f9f9;
  border: 1px solid #ddd;
  border-radius: 4px;
  padding: 15px;
  line-height: 1.6;
  overflow-y: auto;
  box-sizing: border-box;
}

.custom-textarea::-webkit-scrollbar {
  width: 6px;
}

.custom-textarea::-webkit-scrollbar-thumb {
  background-color: #409EFF;
  border-radius: 3px;
}

.preview-container {
  width: 100%;
  min-height: 300px;
  display: flex;
  justify-content: center;
  align-items: center;
  border: 2px dashed #dcdfe6;
  border-radius: 6px;
  cursor: pointer;
}

.preview-container:hover {
  border-color: #409EFF;
}

.avatar-resize {
  max-width: 100%;
  max-height: 400px;
  border-radius: 4px;
}

.upload-placeholder {
  text-align: center;
  color: #8c939d;
}

.avatar-uploader-icon {
  font-size: 48px;
  margin-bottom: 10px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #eee;
}
</style>