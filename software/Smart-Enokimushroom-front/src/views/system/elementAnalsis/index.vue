<template>
  <div class="app-container">

    <!-- <el-dialog title="AI识别病虫害" :visible.sync="openberryStraw" width="1000px" append-to-body> -->
    <el-form ref="form" label-width="120px">
      <el-row>
        <!-- 左侧：上传图片  <!-- 不实际上传，用于本地预览 -->
        <el-col :span="12">
          <el-form-item label="上传金针菇图片">
            <div>
              <el-upload class="avatar-uploader" action="#" :show-file-list="false" :on-change="handlePictureChange"
                :auto-upload="false" accept="image/*">
                <div class="preview-container">
                  <img v-if="berryPictureUrl" :src="berryPictureUrl" class="avatar-resize" />
                  <i v-else class="el-icon-plus avatar-uploader-icon"></i>
                </div>
              </el-upload>
            </div>
          </el-form-item>

        </el-col>

        <!-- 右侧：AI识别结果 -->
        <el-col :span="12" style="padding-left: 20px;">
          <div class="custom-textarea" :style="{ height: fixedHeight, overflowY: 'auto' }">
            <p v-if="!responseTextPicture" style="color: #999;">等待AI识别结果...</p>
            <div v-else v-html="responseTextPicture"></div>
          </div>
        </el-col>
      </el-row>
    </el-form>
    <!-- 对话框底部按钮 -->
    <div slot="footer" class="dialog-footer">
      <div class="footer-right">
        <el-button type="primary" size="mini" @click="submitToAI" :loading="loadingAiResult">
          {{ isSubmit ? '等待AI识别结果' : '提交识别' }}
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
    <!-- </el-dialog> -->

  </div>
</template>

<script>
import { setTimeout } from "core-js";
import marked from 'marked';
import axios from "axios";
import OpenAI from "openai";

export default {
  name: "Pets",
  data () {
    return {
      markdownTextBasic: null,
      splitBasic: null,
      openView: false,
      responseTextBasic: null,

      //垂直滑块
      lineHeight: 24, // 单行高度（px）
      linesToShow: 20, // 固定显示 20 行

      //Ai识别图片对话框
      openberryStraw: false,

      //Ai识别图片
      berryPicture: null,

      berryPictureFile: null,     // 用于保存原始文件对象（推荐）

      berryPictureUrl: '',           // 用于图片预览的临时 URL

      responseTextPicture: '',      // 存储 AI 返回的识别结果
      // fixedHeight: '300px',         // 右侧展示区域高度
      loadingAiResult: false,        // 是否正在加载 AI 结果,给出田间管理办法、预防措施
      image: "@/assets/images/14.JPG",
      isPromptPicture: "作为植物专家，识别图像中金针菇发病可能缺少的营养元素,给出田间管理办法、预防措施",
      //1、缺氮
      miss_dan: "氮是金针菇生长的必需养分之一，它是每个活细胞的组成部分，是构成蛋白质的主要成分，能促进细胞分裂和增长，叶绿素a和叶绿素b中都有含氮化合物，还是植物体内维生素和能量系统的组成部分，对植物生长发育的影响十分明显。"
        + "缺氮时，生长缓慢，植株矮小，叶色缺绿发黄，花芽分化延迟，花芽数减少，果实小，坐果少或不结果，产量低，品质差。"
        + "金针菇植株开始缺氮时，生长盛期的叶子其颜色会逐渐变淡，呈淡绿色，随着缺氮症状的不断加重，叶片变为黄色且有部分枯焦。幼叶呈现出不同的症状，随着缺氮程度的不断增加其叶片颜色逐渐加深，至成熟早期呈锯齿状红色，老叶的叶柄和花萼呈微红色。"
        + "要施足底肥，以满足金针菇生长期短而集中的生长特点。发现缺氮时，每亩追施硝酸铵11.5公斤或尿素8.5公斤，施后立即灌水。花期也可喷叶面肥0.3％~0.5％的尿素溶液1~2次，每亩喷99%高纯磷酸二氢钾(磷钾源库)2~3次。",
      //2、缺磷
      miss_lin: "磷能够促进花芽分化，提早开花结果，促进幼苗根系生长和改善果实品质。磷还在ATP反应、糖类代谢、蛋白质代谢和脂肪代谢中起着重要的作用。"
        + "磷能够促进金针菇各种代谢正常进行，生长发育良好，同时提高金针菇植株的抗寒性和抗旱性。"
        + "当金针菇叶片中的含磷量低于0.2% 时就会出现缺磷症。金针菇初期缺磷时，症状表现为叶色深，近叶缘处出现紫褐色斑点。缺磷加重时，上部叶片外观呈现紫红的斑点，导致开花期和成熟期延迟，产量降低，抗性减弱。"
        + "金针菇缺磷时蛋白质合成也会受阻，引起细胞分裂素含量的降低，影响细胞分裂，抑制花芽分化，分枝或分蘖减少，植株矮小。"
        + "金针菇在含钙较多或酸度高的土壤条件下，磷素不能被吸收时易出现缺磷症状，而在疏松的砂土或有机质多的土壤也比较易发生缺磷现象。"
        + "金针菇种植在植株开始出现症状时，应冲施万物春多多靓2",
      //3、缺钾
      miss_jia: "钾能促进植株茎杆健壮，改善果实品质，增强植株抗寒能力，提高果实的糖分和维生素C的含量。在植物体内，钾能保持叶绿体内类囊体膜的正常结构，而缺钾时，类囊体膜结构松散，"
        + "影响光合作用的正常进行，同时钾作为活化剂能够促进酶的活化，促进光合作用，改善能量代谢，从而增强植株体内物质的合成和转运，增强植株的抗冻、抗旱、抗盐等能力。"
        + "金针菇植株缺钾都是从新成熟的上部叶片开始，缺钾的叶片边缘经常呈黑色或褐色，叶片干枯，最终导致灼伤。灼烧斑可在大部分的叶片和叶脉之间向中心发展为害，最终导致叶片与叶柄发暗并干枯或坏死。"
        + "金针菇缺钾症状易与光照灼伤相混，应注意区分，灼伤的叶片其叶柄一般呈棕色至暗棕色，且有轻度损害，最后逐渐凋萎。"
        + "砂土、有机肥少的土壤或氮肥施用过量而产生拮抗作用时，会发生缺钾现象。"
        + "施用充足的有机肥料，在生长期钾不足时，每亩可追施硫酸钾7.5千克，或每亩追施氮磷钾复合肥50~100千克。发现缺钾，叶面喷施倍尔滋糖醇有机钾1000倍液，2~3次，隔7~10天喷一次。"
        + "增施有机肥能提高土壤肥力，改善土壤理化性状，增强土壤中钙的活性，提高钙的利用率。叶面喷施倍尔滋益植钙溶液，可减轻缺钙现象。另外应及时适当浇水，保证水分供应。",
      //4、缺镁
      miss_mei: "镁作为植物生长发育的必需元素之一对金针菇幼苗的形态发育与生理生化有着重要影响。在植物体内镁以离子或有机物结合的形式存在，"
        + "它是叶绿素的重要组成成分。镁可以在光合作用和呼吸作用的过程中活化各种酶类和核酸。"
        + "金针菇出现缺镁时，在老叶的叶脉间出现暗褐色的斑点，一开始上部的叶片边缘逐渐变黄，呈枯焦状。叶脉之间的绿色褪淡并出现暗褐色的斑点，最终成为黄色具有死斑的叶片。随着叶龄的增长和缺镁症状的加重，枯焦现象也逐渐加重，但是一般在新叶中不会表现出这种症状。金针菇缺镁时，果实的味道较淡且较软，并伴有白化现象。"
        + "一般在沙质地栽培金针菇或氮肥、钾肥施用过多时易出现缺镁症。"
        + "增施有机肥，合理配施氮、磷肥，可采用配方施肥。及时调节土壤酸碱度，改良土壤，避免低温。缺镁时，可叶面喷施益植镁溶液2~3次，每10天左右一次。",
      //5、缺铁
      miss_tie: "铁元素在植物的光合作用、生物固氮和呼吸作用等过程中起着电子传递的作用，是一种非常重要的元素。"
        + "在金针菇植株缺铁时，叶片和叶脉间会缺绿，与缺镁的症状正好相反，一般缺铁易发生于嫩叶。金针菇植株缺铁的典型特征为绿色复原现象，具体表现为中度缺铁时，"
        + "植株会由开始的失绿或黄化转为绿色。当植株缺铁严重时，新长出的小叶和新成熟的小叶都会变白或黄化，叶片边缘坏死，叶片边缘的叶脉间变褐坏死。"
        + "调节土壤酸碱度，使土壤pH值达到6~6.5，叶面喷施绿多靓铁溶液2~3次。缺铁严重时，用根基营养素B淋根，改善根际环境，促进根系生长，提升根系吸收能力。",
      //5、缺钙
      miss_gai: "钙是细胞壁胶层中果胶钙的重要成分，与膜的功用有着密切关系，可使相邻的细胞壁相互联结，增大细胞的坚硬性，降低膜的透性，改变膜对离子的亲和性与选择性。"
        + "钙还是ATP的水解酶、磷脂水解酶等酶的活化剂，是细胞酶和液泡膜结构中的黏结剂，因此钙素的缺乏会引起金针菇细胞壁以及细胞膜等的改变，使细胞不能正常分裂，使细胞膜难以保持稳定，使金针菇细胞更易遭受不良环境的侵袭。"
        + "钙还参与了植物的成花过程，缺钙会影响金针菇花的形成。"
        + "钙素在金针菇植株体内不易移动和重新分配，大量的钙存于叶片中，老叶中多，而幼叶中少，因此缺钙会使嫩叶和分生区首先受害。"
        + "金针菇缺钙一般多发生在金针菇开花前现蕾时，新叶端部产生褐变或干枯，小叶展开后不恢复正常。金针菇缺钙主要症状是叶焦病，果实发育着色减慢，幼叶叶缘坏死，根尖生长受阻和生长点受害。注意与病毒病区分。"
        + "缺钙导致金针菇植株的根短粗、色暗，随着缺钙加重而逐渐呈淡黑色，浆果表面会有密集的种子覆盖甚至可布满整个果面，果实组织变硬、味酸。"
        + "草毒缺钙症主要与品种、土壤基质有关。栽培管理不当同样会引发和加重缺钙症状，如温度过高、氮肥施用过量，大水漫灌等。"
        + "增施有机肥能提高土壤肥力，改善土壤理化性状，增强土壤中钙的活性，提高钙的利用率。叶面喷施倍尔滋益植钙溶液，可减轻缺钙现象。另外应及时适当浇水，保证水分供应。",
      // 语音播报文本队列
      textToSpeak: [],

      // 是否正在播放
      isPlaying: false,

      // 已播报过的 ID 缓存（用于去重）
      spokenIds: new Set(),

      isPaused: false,        // 是否已暂停
      currentIndex: 0,        // 当前播放到的索引
      utteranceQueue: [],     // 存储 utterance 对象（用于取消）?

      isSubmit: false,

    };
  },
  computed: {
    // 计算最大高度（20行 × 每行高度）
    fixedHeight () {
      return this.lineHeight * this.linesToShow + 'px';
    }
  },
  methods: {

    // 取消按钮
    cancel () {
      this.splitBasic = '';
      this.responseTextBasic = '';
      this.responseTextPicture = '';
      this.berryPicture = '';
      this.berryPictureFile = '';
      this.berryPictureUrl = '';
      //this.reset();
    },

    //打开识别图片对话框
    handleAiPicture () {
      this.openberryStraw = true;
    },

    // 图片上传监听并预览
    handlePictureChange (file) {
      // ✅ 1. 保留原始 File 对象（用于后续上传）
      this.berryPictureFile = file.raw; // 注意：是 file.raw 对象，不是名字！

      // ✅ 2. 使用 File 对象创建本地预览 URL
      const objectUrl = URL.createObjectURL(file.raw);

      // ✅ 3. 清除旧的预览 URL，防止内存泄漏
      if (this.berryPictureUrl) {
        URL.revokeObjectURL(this.berryPictureUrl);
      }

      // ✅ 4. 设置预览图片 URL（用于 <img :src="berryPictureUrl">）
      this.berryPictureUrl = objectUrl;

      // ✅ 5. 可选：保存文件名或原始信息
      this.berryPictureFileName = file.raw.name;

      //console.log('预览 URL:', this.berryPictureUrl);
      //console.log('文件对象:', this.berryPictureFile); // 可用于上传
    },

    async callDashScopePicture (neirong) {
      if (!this.berryPictureFile) {
        this.$message.warning('请先上传一张图片');
        return;
      }

      const appId = '63a611fa23ff44adaac9fcc5947719a9';
      const apiKey = 'sk-622c86d7232749c4a48e818cf8c2f1ef';
      const url = `https://dashscope.aliyuncs.com/api/v1/apps/${appId}/completion`;

      // ✅ 将 File/Blob 转为 Base64
      const fileToBase64 = (file) => {
        return new Promise((resolve, reject) => {
          const reader = new FileReader();
          reader.onload = () => {
            // 去掉 data:image/xxx;base64, 前缀
            const base64 = reader.result.split(',')[1];
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
        this.$message.error('图片转 Base64 失败');
        return;
      }

      const data = {
        input: {
          prompt: neirong,
          image: base64Image, // ✅ 正确：传 Base64 字符串（不含前缀）
        },
        parameters: {
          // rag_options: {
          //   file_ids: [this.miss_dan, this.miss_lin, this.miss_jia, this.miss_gai, this.miss_mei, this.miss_tie].filter(id => id),
          // }
        },
        debug: {}
      };

      try {
        const response = await axios.post(url, data, {
          headers: {
            'Authorization': `Bearer ${apiKey}`,
            'Content-Type': 'application/json'
          }
        });
        console.log('识别结果:', response.data);

        console.log(response);
        if (response.status === 200) {
          const parsed = response.data; // 解析 JSON 字符串
          const output1 = parsed.output; // 获取 output 对象
          if (parsed.output.finish_reason == "stop" && !parsed.output.reject_status) {
            const markdownText = output1?.text || '[无内容]';
            //console.log(markdownText);
            // this.markdownTextBasic = marked.parse(markdownText);
            this.markdownTextBasic = marked(markdownText);
            // 调用打字机效果方法
            this.typewriterEffectPicture(markdownText, 30);
          }
          //console.log(`${response.data.output.text}`);
        } else {
          console.log(`request_id=${response.headers['request_id']}`);
          console.log(`code=${response.status}`);
          console.log(`message=${response.data.message}`);
        }
      } catch (error) {
        console.error('调用失败:', error.response?.data || error.message);
        this.$message.error('调用大模型失败');
        console.error(`Error calling DashScope: ${error.message}`);
        if (error.response) {
          console.error(`Response status: ${error.response.status}`);
          console.error(`Response data: ${JSON.stringify(error.response.data, null, 2)}`);
        }
      }
    },
    //通义千问 模型调用 打字机效果
    typewriterEffectPicture (fullText, speed = 30) {
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
    //阿里通义百炼视觉模型
    async callQwenVl () {
      this.isSubmit = true;

      const openai = new OpenAI(
        {
          // 若没有配置环境变量，请用阿里云百炼API Key将下行替换为：apiKey: "sk-xxx",
          apiKey: process.env.VUE_APP_DASHSCOPE_API_KEY,
          dangerouslyAllowBrowser: true,
          baseURL: "https://dashscope.aliyuncs.com/compatible-mode/v1"
        }
      );
      //console.log(process.env.VUE_APP_DASHSCOPE_API_KEY);
      // ✅ 将 File/Blob 转为 Base64
      const fileToBase64 = (file) => {
        return new Promise((resolve, reject) => {
          const reader = new FileReader();
          reader.onload = () => {
            // 去掉 data:image/xxx;base64, 前缀
            const base64 = reader.result.split(',')[1];
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
        this.$message.error('图片转 Base64 失败');
        return;
      }
      // 将xxx/eagle.png替换为你本地图像的绝对路径
      const completion = await openai.chat.completions.create({
        model: "qwen-vl-max",  // 此处以qwen-vl-max为例，可按需更换模型名称。模型列表：https://help.aliyun.com/zh/model-studio/models
        messages: [
          {
            "role": "system",
            "content": [
              {
                "type": "text",
                "text": "作为金针菇种植专家."
              }
            ]
          },
          {
            "role": "user",
            "content": [
              {
                "type": "image_url",
                // 需要注意，传入Base64，图像格式（即image/{format}）需要与支持的图片列表中的Content Type保持一致。
                // PNG图像：  data:image/png;base64,${base64Image}
                // JPEG图像： data:image/jpeg;base64,${base64Image}
                // WEBP图像： data:image/webp;base64,${base64Image}
                "image_url": { "url": `data:image/jpeg;base64,${base64Image}` },
              },
              {
                "type": "text",
                "text": this.isPromptPicture
              }
            ]
          }]
      });
      //console.log(completion.choices[0].message.content);
      const str = completion.choices[0].message.content;
      if (str && str.trim() !== '') {
        this.loadingAiResult = false;
        const result = str.replace(/#+/g, ',');
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
    submitToAI () {
      //console.log(this.berryPicture);
      this.loadingAiResult = true;
      this.responseTextPicture = '';

      this.callQwenVl();
      // 模拟调用后端接口（替换为真实 API 地址）
      // try {
      //   this.callDashScopePicture(this.isPromptPicture);
      // } catch (error) {
      //   this.$message.error('AI识别失败，请重试');
      //   this.loadingAiResult = false;
      // }
    },
    // 播放语音
    speakText (text) {
      if ('speechSynthesis' in window) {
        const utterance = new SpeechSynthesisUtterance(text);
        utterance.lang = 'zh-CN';
        speechSynthesis.speak(utterance);
      } else {
        alert("您的浏览器不支持语音合成");
      }
    },

    // 【播放】按钮：开始或继续播放
    handlePlay () {
      if (this.isPlaying && this.isPaused) {
        // 如果是暂停状态，恢复播放
        speechSynthesis.resume();
        this.isPaused = false;
        return;
      }

      if (this.isPlaying) return; // 正在播放中

      if (!Array.isArray(this.textToSpeak) || this.textToSpeak.length === 0) {
        // alert('没有可播放的内容');
        return;
      }

      this.isPlaying = true;
      this.isPaused = false;
      this.currentIndex = 0;

      this.playNext();
    },

    // 【暂停】按钮
    handlePause () {
      if (this.isPlaying && !this.isPaused) {
        speechSynthesis.pause();
        this.isPaused = true;
      }
    },

    // 【停止】按钮
    handleStop () {
      speechSynthesis.cancel(); // 立即停止所有
      this.isPlaying = false;
      this.isPaused = false;
      this.textToSpeak.splice(0); // 清空队列
      this.utteranceQueue = [];
      this.currentIndex = 0;
    },

    // 播放下一条
    playNext () {
      if (this.currentIndex >= this.textToSpeak.length) {
        this.handleStop();
        // alert('全部内容播放完毕');
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
      this.utteranceQueue.push(utterance); // 可选：记录用于管理
    },


    handleSpeakMessages () {
      if (this.isPlaying) {
        alert('正在播放中，请稍后再试');
        return;
      }

      if (!Array.isArray(this.textToSpeak) || this.textToSpeak.length === 0) {
        //alert('没有可播放的内容');
        return;
      }

      this.isPlaying = true;
      let index = 0;

      const playNext = () => {
        if (index < this.textToSpeak.length) {
          const text = this.textToSpeak[index];
          const utterance = new SpeechSynthesisUtterance(text);
          utterance.lang = 'zh-CN';

          utterance.onend = () => {
            index++;
            playNext();
          };

          speechSynthesis.speak(utterance);
        } else {
          this.isPlaying = false;
          this.textToSpeak.splice(0); // 清空播报队列
          //alert('全部内容播放完毕');
        }
      };

      playNext();
    },
  }

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
  background-color: #409EFF;
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
  border-color: #409EFF;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 100%;
  height: auto;
  line-height: normal;
  text-align: center;
}

/* 使用 flex 布局，将按钮组右对齐 */
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 10px;
  margin-top: 20px;
  /* ⬅️ 关键：与上方内容保持 20px 距离 */
  padding: 10px 0;
  /* 可选：添加边框分割线 */
  /* border-top: 1px solid #dcdfe6; */
}

.footer-right {
  display: flex;
  justify-content: flex-end;
  /* 主轴对齐：右对齐 */
  align-items: center;
  /* 垂直居中 */
  gap: 10px;
  /* 按钮之间留点空隙（推荐） */
}

/* 如果 .dialog-footer 是 Element UI 的 el-dialog 默认 slot，可能需要深度选择器 */
</style>