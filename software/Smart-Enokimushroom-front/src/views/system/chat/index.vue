<template>
  <div class="app-container chat-page">
    <div class="chat-wrapper">
      <!-- 左侧历史记录 -->
      <div class="sidebar" :class="{ collapsed: sidebarCollapsed }">
        <div class="sidebar-header">
          <button class="toggle-btn" @click="sidebarCollapsed = !sidebarCollapsed">
            <i :class="sidebarCollapsed ? 'el-icon-s-unfold' : 'el-icon-s-fold'"></i>
          </button>
          <transition name="fade">
            <button v-show="!sidebarCollapsed" class="new-chat-btn" @click="initiateNewConversation">
              <i class="el-icon-plus"></i>
              <span>新对话</span>
            </button>
          </transition>
        </div>

        <transition name="slide">
          <div v-show="!sidebarCollapsed" class="history-list">
            <div
              v-for="chat in conversationHistory"
              :key="chat.id"
              class="history-item"
              :class="{ active: activeConversationId === chat.id }"
              @click="switchConversation(chat.id)"
            >
              <i class="el-icon-chat-dot-round"></i>
              <span class="title">{{ chat.title || "新对话" }}</span>
              <button class="delete-btn" @click.stop="removeConversation(chat.id)" title="删除对话">
                <i class="el-icon-delete"></i>
              </button>
            </div>
            <div v-if="conversationHistory.length === 0" class="empty-tip">
              <i class="el-icon-chat-line-round"></i>
              <p>暂无对话记录</p>
            </div>
          </div>
        </transition>
      </div>

      <!-- 右侧聊天区域 -->
      <div class="chat-main">
        <!-- 顶部信息栏 -->
        <div class="chat-header">
          <div class="header-left">
            <div class="bot-avatar">
              <i class="el-icon-monitor"></i>
            </div>
            <div class="bot-info">
              <h3>金针菇专家知识库</h3>
              <span class="status">基于知识库的智能问答</span>
            </div>
          </div>
          <div class="header-right">
            <el-select
              v-model="selectedKbId"
              placeholder="选择知识库"
              size="small"
              @change="onKbChange"
            >
              <el-option label="默认知识库" value="1"></el-option>
              <el-option
                v-for="kb in knowledgeBases"
                :key="kb.id"
                :label="kb.name"
                :value="String(kb.id)"
              >
                <span>{{ kb.name }}</span>
                <span class="kb-remark">{{ kb.remark }}</span>
              </el-option>
            </el-select>
          </div>
        </div>

        <!-- 消息区域 -->
        <div class="messages-area" ref="messagesContainer">
          <!-- 欢迎消息 -->
          <div v-if="activeMessages.length === 0" class="welcome-section">
            <div class="welcome-icon">
              <i class="el-icon-s-custom"></i>
            </div>
            <h2>金针菇种植智能助手</h2>
            <p>我是您的金针菇种植专家，可以回答关于金针菇病虫害、种植技术、管理方法等问题</p>
            <div class="quick-questions">
              <div class="question-card" @click="submitMessage('金针菇常见病害有哪些？')">
                <i class="el-icon-warning-outline"></i>
                <span>金针菇常见病害有哪些？</span>
              </div>
              <div class="question-card" @click="submitMessage('如何防治金针菇白粉病？')">
                <i class="el-icon-first-aid-kit"></i>
                <span>如何防治金针菇白粉病？</span>
              </div>
              <div class="question-card" @click="submitMessage('金针菇定植后怎么管理？')">
                <i class="el-icon-s-management"></i>
                <span>金针菇定植后怎么管理？</span>
              </div>
            </div>
          </div>

          <!-- 消息列表 -->
          <Chat
            v-for="(message, index) in activeMessages"
            :key="index"
            :message="message"
            :is-stream="isProcessing && index === activeMessages.length - 1"
          />

          <!-- 加载动画 -->
          <div v-if="isProcessing && activeMessages.length > 0 && activeMessages[activeMessages.length - 1].role === 'assistant'"
               class="loading-indicator">
            <div class="loading-dots">
              <span></span><span></span><span></span>
            </div>
            <span>AI 正在思考...</span>
          </div>
        </div>

        <!-- 输入区域 -->
        <div class="input-area">
          <div class="input-wrapper">
            <textarea
              v-model="userQuery"
              @keydown.enter.exact.prevent="submitMessage()"
              @input="adjustTextareaHeight"
              placeholder="输入您的问题，按 Enter 发送..."
              rows="1"
              ref="queryInput"
              :disabled="isProcessing"
            ></textarea>
            <div class="input-actions">
              <span class="char-count" v-if="userQuery.length > 0">{{ userQuery.length }}</span>
              <button
                class="send-btn"
                @click="submitMessage()"
                :disabled="isProcessing || !userQuery.trim()"
              >
                <i v-if="!isProcessing" class="el-icon-s-promotion"></i>
                <i v-else class="el-icon-loading"></i>
              </button>
            </div>
          </div>
          <div class="input-tip">
            <span>基于知识库回答，内容仅供参考</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import Chat from "@/components/Chat.vue";
import { chatAPI, fileRagAPI, ragQueryAPI } from "@/api/system/api";

export default {
  name: "ChatPage",
  components: { Chat },
  data() {
    return {
      userQuery: "",
      isProcessing: false,
      activeConversationId: null,
      activeMessages: [],
      conversationHistory: [],
      knowledgeBases: [],
      selectedKbId: "1",
      sidebarCollapsed: false,
    };
  },
  async mounted() {
    await this.loadKnowledgeBases();
    await this.loadConversationHistory();
    this.adjustTextareaHeight();
  },
  methods: {
    async loadKnowledgeBases() {
      try {
        const res = await fileRagAPI.listKnowledgeBases();
        if (res && Array.isArray(res)) {
          this.knowledgeBases = res;
        } else if (res && Array.isArray(res.data)) {
          this.knowledgeBases = res.data;
        } else {
          this.knowledgeBases = [];
        }
      } catch (error) {
        console.error("加载知识库列表失败:", error);
        this.knowledgeBases = [];
      }
    },

    onKbChange() {
      this.initiateNewConversation();
    },

    adjustTextareaHeight() {
      const textarea = this.$refs.queryInput;
      if (textarea) {
        textarea.style.height = "auto";
        textarea.style.height = Math.min(textarea.scrollHeight, 120) + "px";
      }
    },

    async scrollToBottom() {
      await this.$nextTick();
      if (this.$refs.messagesContainer) {
        this.$refs.messagesContainer.scrollTop = this.$refs.messagesContainer.scrollHeight;
      }
    },

    async submitMessage(content) {
      if (this.isProcessing || (!content && !this.userQuery.trim())) return;

      const messageContent = content || this.userQuery.trim();
      this.activeMessages.push({
        role: "user",
        content: messageContent,
        timestamp: new Date(),
      });

      if (!content) {
        this.userQuery = "";
        this.adjustTextareaHeight();
      }
      await this.scrollToBottom();

      const assistantMessage = {
        role: "assistant",
        content: "",
        timestamp: new Date(),
        isMarkdown: true,
      };
      this.activeMessages.push(assistantMessage);
      this.isProcessing = true;

      let totalContent = "";

      try {
        const currentKb = this.knowledgeBases.find(kb => kb.id == this.selectedKbId);
        const topK = currentKb ? currentKb.topK : 5;

        const reader = await ragQueryAPI.ask(
          messageContent,
          this.selectedKbId || "1",
          this.activeConversationId,
          topK
        );

        const decoder = new TextDecoder("utf-8");
        let buffer = "";
        let streamEnded = false;

        const parseSSEEvent = (eventText) => {
          const lines = eventText.split('\n');
          let eventData = '';
          for (const line of lines) {
            if (line.startsWith(':')) continue;
            if (line.startsWith('data:')) {
              let data = line.substring(5);
              if (data.startsWith(' ')) data = data.substring(1);
              eventData += (eventData ? '\n' : '') + data;
            }
          }
          return { data: eventData, done: eventData === '[DONE]' };
        };

        while (!streamEnded) {
          const { value, done } = await reader.read();
          if (done) break;

          buffer += decoder.decode(value, { stream: true });
          const events = buffer.split('\n\n');
          buffer = events.pop() || '';

          let hasUpdate = false;
          for (const eventText of events) {
            if (!eventText.trim()) continue;
            const result = parseSSEEvent(eventText);
            if (result.done) { streamEnded = true; break; }
            if (result.data) { totalContent += result.data; hasUpdate = true; }
          }

          if (hasUpdate) {
            await this.$nextTick(() => {
              const lastIndex = this.activeMessages.length - 1;
              this.$set(this.activeMessages, lastIndex, {
                ...assistantMessage,
                content: totalContent,
                isMarkdown: true,
              });
            });
            await this.scrollToBottom();
          }
        }

        if (!streamEnded && buffer.trim()) {
          const result = parseSSEEvent(buffer);
          if (!result.done && result.data) {
            totalContent += result.data;
            await this.$nextTick(() => {
              const lastIndex = this.activeMessages.length - 1;
              this.$set(this.activeMessages, lastIndex, {
                ...assistantMessage,
                content: totalContent,
                isMarkdown: true,
              });
            });
          }
        }

        await this.refreshHistoryAndSetActive();
      } catch (error) {
        console.error("发送消息失败:", error);
        const lastIndex = this.activeMessages.length - 1;
        this.$set(this.activeMessages, lastIndex, {
          ...assistantMessage,
          content: "抱歉，发送消息时出现错误，请稍后重试。",
          isMarkdown: false,
        });
      } finally {
        this.isProcessing = false;
        await this.scrollToBottom();
      }
    },

    async switchConversation(chatId) {
      this.activeConversationId = chatId;
      try {
        const messages = await chatAPI.chatHistoryMessageList(chatId, 4);
        this.activeMessages = messages.map((msg) => ({
          ...msg,
          isMarkdown: msg.role === "assistant",
        }));
      } catch (error) {
        console.error("加载对话消息失败:", error);
        this.activeMessages = [];
      }
    },

    async loadConversationHistory() {
      try {
        const history = await chatAPI.chatTypeHistoryList(4);
        this.conversationHistory = history || [];
        if (history && history.length > 0) {
          await this.switchConversation(history[0].id);
        } else {
          this.activeConversationId = null;
          this.activeMessages = [];
        }
      } catch (error) {
        console.error("加载聊天历史失败:", error);
        this.conversationHistory = [];
        this.activeConversationId = null;
        this.activeMessages = [];
      }
    },

    async initiateNewConversation() {
      const newChatId = Date.now().toString();
      this.activeConversationId = newChatId;
      this.activeMessages = [];
      this.conversationHistory = [{
        id: newChatId,
        title: "新的知识库对话",
      }, ...this.conversationHistory];
    },

    async removeConversation(chatId) {
      try {
        await this.$confirm("确定删除该对话记录？", "提示", {
          confirmButtonText: "确定",
          cancelButtonText: "取消",
          type: "warning",
        });
      } catch {
        return;
      }

      try {
        await chatAPI.deleteChat(chatId, 4);
        this.conversationHistory = this.conversationHistory.filter(c => c.id !== chatId);
        if (this.activeConversationId === chatId) {
          if (this.conversationHistory.length > 0) {
            await this.switchConversation(this.conversationHistory[0].id);
          } else {
            await this.initiateNewConversation();
          }
        }
        this.$message.success("删除成功");
      } catch (error) {
        console.error("删除对话失败:", error);
        this.$message.error("删除失败");
      }
    },

    async refreshHistoryAndSetActive() {
      try {
        const history = await chatAPI.chatTypeHistoryList(4);
        this.conversationHistory = history || [];
        if (history && history.length > 0) {
          this.activeConversationId = history[0].id;
        }
      } catch (error) {
        console.error("刷新历史失败:", error);
      }
    },
  },
};
</script>

<style scoped lang="scss">
.chat-page {
  padding: 0 !important;
  height: calc(100vh - 84px);
  overflow: hidden;
}

.chat-wrapper {
  display: flex;
  height: 100%;
  gap: 0;
  background: #f5f7fa;
}

// 左侧边栏
.sidebar {
  width: 260px;
  background: #fff;
  border-right: 1px solid #e4e7ed;
  display: flex;
  flex-direction: column;
  transition: width 0.3s ease;
  flex-shrink: 0;

  &.collapsed {
    width: 56px;

    .sidebar-header {
      padding: 12px 8px;
      justify-content: center;
    }

    .toggle-btn {
      margin: 0;
    }
  }

  .sidebar-header {
    padding: 16px;
    display: flex;
    align-items: center;
    gap: 8px;
    border-bottom: 1px solid #e4e7ed;
    flex-shrink: 0;
  }

  .toggle-btn {
    width: 36px;
    height: 36px;
    border: none;
    background: #f5f7fa;
    border-radius: 8px;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #606266;
    font-size: 18px;
    flex-shrink: 0;
    transition: all 0.2s;

    &:hover {
      background: #ecf5ff;
      color: #409eff;
    }
  }

  .new-chat-btn {
    flex: 1;
    height: 36px;
    border: 1px solid #409eff;
    background: #ecf5ff;
    color: #409eff;
    border-radius: 8px;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 4px;
    font-size: 14px;
    transition: all 0.2s;

    &:hover {
      background: #409eff;
      color: #fff;
    }

    i {
      font-size: 14px;
    }
  }

  .history-list {
    flex: 1;
    overflow-y: auto;
    padding: 8px;

    &::-webkit-scrollbar {
      width: 4px;
    }

    &::-webkit-scrollbar-thumb {
      background: #dcdfe6;
      border-radius: 2px;
    }
  }

  .history-item {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 10px 12px;
    border-radius: 8px;
    cursor: pointer;
    transition: all 0.2s;
    margin-bottom: 2px;

    i:first-child {
      color: #909399;
      font-size: 16px;
      flex-shrink: 0;
    }

    .title {
      flex: 1;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
      font-size: 14px;
      color: #303133;
    }

    .delete-btn {
      opacity: 0;
      border: none;
      background: none;
      padding: 4px;
      cursor: pointer;
      color: #909399;
      border-radius: 4px;
      transition: all 0.2s;

      &:hover {
        color: #f56c6c;
        background: #fef0f0;
      }
    }

    &:hover {
      background: #f5f7fa;

      .delete-btn {
        opacity: 1;
      }
    }

    &.active {
      background: #ecf5ff;

      i:first-child {
        color: #409eff;
      }

      .title {
        color: #409eff;
        font-weight: 500;
      }
    }
  }

  .empty-tip {
    text-align: center;
    padding: 40px 20px;
    color: #c0c4cc;

    i {
      font-size: 40px;
      margin-bottom: 12px;
      display: block;
    }

    p {
      font-size: 13px;
    }
  }
}

// 右侧聊天区域
.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
  background: #fff;

  .chat-header {
    padding: 12px 20px;
    border-bottom: 1px solid #e4e7ed;
    display: flex;
    align-items: center;
    justify-content: space-between;
    flex-shrink: 0;
    background: #fff;

    .header-left {
      display: flex;
      align-items: center;
      gap: 12px;
    }

    .bot-avatar {
      width: 40px;
      height: 40px;
      background: linear-gradient(135deg, #409eff, #66b1ff);
      border-radius: 10px;
      display: flex;
      align-items: center;
      justify-content: center;
      color: #fff;
      font-size: 20px;
    }

    .bot-info {
      h3 {
        font-size: 15px;
        font-weight: 600;
        color: #303133;
        margin: 0;
      }

      .status {
        font-size: 12px;
        color: #67c23a;
      }
    }

    .kb-remark {
      font-size: 12px;
      color: #909399;
      margin-left: 8px;
    }
  }

  .messages-area {
    flex: 1;
    overflow-y: auto;
    padding: 20px;
    background: #f5f7fa;

    &::-webkit-scrollbar {
      width: 6px;
    }

    &::-webkit-scrollbar-thumb {
      background: #dcdfe6;
      border-radius: 3px;
    }
  }

  .input-area {
    padding: 16px 20px;
    background: #fff;
    border-top: 1px solid #e4e7ed;
    flex-shrink: 0;

    .input-wrapper {
      display: flex;
      align-items: flex-end;
      gap: 12px;
      background: #f5f7fa;
      border: 1px solid #dcdfe6;
      border-radius: 12px;
      padding: 8px 12px;
      transition: all 0.2s;

      &:focus-within {
        border-color: #409eff;
        background: #fff;
        box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.1);
      }

      textarea {
        flex: 1;
        border: none;
        background: none;
        resize: none;
        outline: none;
        font-size: 14px;
        line-height: 1.5;
        max-height: 120px;
        color: #303133;
        font-family: inherit;

        &::placeholder {
          color: #c0c4cc;
        }

        &:disabled {
          cursor: not-allowed;
          opacity: 0.7;
        }
      }

      .input-actions {
        display: flex;
        align-items: center;
        gap: 8px;
        flex-shrink: 0;
      }

      .char-count {
        font-size: 12px;
        color: #909399;
      }

      .send-btn {
        width: 36px;
        height: 36px;
        border: none;
        background: #409eff;
        color: #fff;
        border-radius: 8px;
        cursor: pointer;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 18px;
        transition: all 0.2s;

        &:hover:not(:disabled) {
          background: #66b1ff;
          transform: scale(1.05);
        }

        &:active:not(:disabled) {
          transform: scale(0.95);
        }

        &:disabled {
          background: #dcdfe6;
          cursor: not-allowed;
        }

        .el-icon-loading {
          animation: spin 1s linear infinite;
        }
      }
    }

    .input-tip {
      text-align: center;
      margin-top: 8px;

      span {
        font-size: 12px;
        color: #c0c4cc;
      }
    }
  }
}

// 欢迎页面
.welcome-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  padding: 40px;
  text-align: center;

  .welcome-icon {
    width: 80px;
    height: 80px;
    background: linear-gradient(135deg, #409eff, #66b1ff);
    border-radius: 20px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
    font-size: 40px;
    margin-bottom: 24px;
    box-shadow: 0 12px 32px rgba(64, 158, 255, 0.3);
  }

  h2 {
    font-size: 24px;
    color: #303133;
    margin: 0 0 8px;
  }

  > p {
    font-size: 14px;
    color: #909399;
    margin: 0 0 32px;
  }

  .quick-questions {
    display: flex;
    gap: 12px;
    flex-wrap: wrap;
    justify-content: center;
    max-width: 600px;
  }

  .question-card {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 12px 16px;
    background: #fff;
    border: 1px solid #e4e7ed;
    border-radius: 10px;
    cursor: pointer;
    transition: all 0.2s;

    i {
      color: #409eff;
      font-size: 16px;
    }

    span {
      font-size: 13px;
      color: #606266;
    }

    &:hover {
      border-color: #409eff;
      background: #ecf5ff;
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(64, 158, 255, 0.15);
    }
  }
}

// 加载动画
.loading-indicator {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  color: #909399;
  font-size: 13px;

  .loading-dots {
    display: flex;
    gap: 4px;

    span {
      width: 6px;
      height: 6px;
      background: #409eff;
      border-radius: 50%;
      animation: bounce 1.4s infinite ease-in-out both;

      &:nth-child(1) { animation-delay: -0.32s; }
      &:nth-child(2) { animation-delay: -0.16s; }
    }
  }
}

// 动画
.fade-enter-active, .fade-leave-active {
  transition: opacity 0.2s;
}
.fade-enter, .fade-leave-to {
  opacity: 0;
}

.slide-enter-active, .slide-leave-active {
  transition: opacity 0.2s;
}
.slide-enter, .slide-leave-to {
  opacity: 0;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

@keyframes bounce {
  0%, 80%, 100% {
    transform: scale(0);
    opacity: 0.3;
  }
  40% {
    transform: scale(1);
    opacity: 1;
  }
}
</style>
