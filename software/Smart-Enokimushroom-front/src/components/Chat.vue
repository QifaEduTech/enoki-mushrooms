<template>
  <div class="message" :class="{ 'message-user': isUser }">
    <div class="avatar">
      <UserIcon v-if="isUser" class="icon" />
      <LaptopIcon v-else class="icon assistant" />
    </div>
    <div class="content">
      <div class="text-container">
        <button
          v-if="isUser"
          class="user-copy-button"
          @click="copyContent"
          :title="copyButtonTitle"
        >
          <svg
            v-if="!copied"
            class="copy-icon"
            xmlns="http://www.w3.org/2000/svg"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
          >
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="2"
              d="M8 16H6a2 2 0 01-2-2V6a2 2 0 012-2h8a2 2 0 012 2v2m-6 12h8a2 2 0 002-2v-8a2 2 0 00-2-2h-8a2 2 0 00-2 2v8a2 2 0 002 2z"
            />
          </svg>
          <svg
            v-else
            class="copy-icon copied"
            xmlns="http://www.w3.org/2000/svg"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
          >
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="2"
              d="M5 13l4 4L19 7"
            />
          </svg>
        </button>
        <div class="text" ref="contentRef" v-if="isUser">
          {{ message.content }}
        </div>
        <div
          class="text markdown-content"
          ref="contentRef"
          v-else
          v-html="processedContent"
        ></div>
      </div>
      <div class="message-footer" v-if="!isUser">
        <button
          class="copy-button"
          @click="copyContent"
          :title="copyButtonTitle"
        >
          <svg
            v-if="!copied"
            class="copy-icon"
            xmlns="http://www.w3.org/2000/svg"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
          >
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="2"
              d="M8 16H6a2 2 0 01-2-2V6a2 2 0 012-2h8a2 2 0 012 2v2m-6 12h8a2 2 0 002-2v-8a2 2 0 00-2-2h-8a2 2 0 00-2 2v8a2 2 0 002 2z"
            />
          </svg>
          <svg
            v-else
            class="copy-icon copied"
            xmlns="http://www.w3.org/2000/svg"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
          >
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="2"
              d="M5 13l4 4L19 7"
            />
          </svg>
        </button>
      </div>
    </div>
  </div>
</template>

<script>
import { marked } from "marked";
import LaptopIcon from "./icons/LaptopIcon.vue";
import UserIcon from "./icons/UserIcon.vue";

// 配置 marked
marked.setOptions({
  breaks: true,
  gfm: true,
  sanitize: false,
});

export default {
  name: "MessageComponent",
  components: {
    LaptopIcon,
    UserIcon,
  },
  props: {
    message: {
      type: Object,
      required: true,
    },
    isStream: {
      type: Boolean,
      default: false,
    },
  },
  data() {
    return {
      copied: false,
    };
  },
  computed: {
    isUser() {
      return this.message.role === "user";
    },
    copyButtonTitle() {
      return this.copied ? "已复制" : "复制内容";
    },
    processedContent() {
      if (!this.message.content) return "";
      return this.processContent(this.message.content);
    },
  },
  mounted() {
    this.$nextTick(() => {
      this.setupCodeBlockCopyButtons();
    });
  },
  updated() {
    this.$nextTick(() => {
      this.setupCodeBlockCopyButtons();
    });
  },
  methods: {
    processContent(content) {
      if (!content) return "";

      let result = "";
      let isInThinkBlock = false;
      let currentBlock = "";

      // 逐字符分析，处理 think 标签
      for (let i = 0; i < content.length; i++) {
        if (content.slice(i, i + 7) === "<think>") {
          isInThinkBlock = true;
          if (currentBlock) {
            result += marked.parse(currentBlock);
          }
          currentBlock = "";
          i += 6;
          continue;
        }

        if (content.slice(i, i + 8) === "</think>") {
          isInThinkBlock = false;
          result += `<div class="think-block">${marked.parse(
            currentBlock
          )}</div>`;
          currentBlock = "";
          i += 7;
          continue;
        }

        currentBlock += content[i];
      }

      // 处理剩余内容
      if (currentBlock) {
        if (isInThinkBlock) {
          result += `<div class="think-block">${marked.parse(
            currentBlock
          )}</div>`;
        } else {
          result += marked.parse(currentBlock);
        }
      }

      // 添加代码块复制按钮
      const tempDiv = document.createElement("div");
      tempDiv.innerHTML = result;

      const preElements = tempDiv.querySelectorAll("pre");
      preElements.forEach((pre) => {
        const code = pre.querySelector("code");
        if (code) {
          const wrapper = document.createElement("div");
          wrapper.className = "code-block-wrapper";

          const copyBtn = document.createElement("button");
          copyBtn.className = "code-copy-button";
          copyBtn.title = "复制代码";
          copyBtn.innerHTML = `
            <svg xmlns="http://www.w3.org/2000/svg" class="code-copy-icon" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 16H6a2 2 0 01-2-2V6a2 2 0 012-2h8a2 2 0 012 2v2m-6 12h8a2 2 0 002-2v-8a2 2 0 00-2-2h-8a2 2 0 00-2 2v8a2 2 0 002 2z" />
            </svg>
          `;

          const successMsg = document.createElement("div");
          successMsg.className = "copy-success-message";
          successMsg.textContent = "已复制!";

          wrapper.appendChild(copyBtn);
          wrapper.appendChild(pre.cloneNode(true));
          wrapper.appendChild(successMsg);

          pre.parentNode.replaceChild(wrapper, pre);
        }
      });

      return tempDiv.innerHTML;
    },

    setupCodeBlockCopyButtons() {
      if (!this.$refs.contentRef) return;

      const codeBlocks = this.$refs.contentRef.querySelectorAll(
        ".code-block-wrapper"
      );
      codeBlocks.forEach((block) => {
        const copyButton = block.querySelector(".code-copy-button");
        const codeElement = block.querySelector("code");
        const successMessage = block.querySelector(".copy-success-message");

        if (copyButton && codeElement) {
          const newCopyButton = copyButton.cloneNode(true);
          copyButton.parentNode.replaceChild(newCopyButton, copyButton);

          newCopyButton.addEventListener("click", async (e) => {
            e.preventDefault();
            e.stopPropagation();
            try {
              const code = codeElement.textContent || "";
              await navigator.clipboard.writeText(code);

              if (successMessage) {
                successMessage.classList.add("visible");
                setTimeout(() => {
                  successMessage.classList.remove("visible");
                }, 2000);
              }
            } catch (err) {
              console.error("复制代码失败:", err);
            }
          });
        }
      });
    },

    async copyContent() {
      try {
        let textToCopy = this.message.content;

        if (!this.isUser && this.$refs.contentRef) {
          const tempDiv = document.createElement("div");
          tempDiv.innerHTML = this.processedContent;
          textToCopy = tempDiv.textContent || tempDiv.innerText || "";
        }

        await navigator.clipboard.writeText(textToCopy);
        this.copied = true;

        setTimeout(() => {
          this.copied = false;
        }, 3000);
      } catch (err) {
        console.error("复制失败:", err);
      }
    },
  },
};
</script>

<style scoped lang="scss">
.message {
  display: flex;
  margin-bottom: 1.5rem;
  gap: 1rem;

  &.message-user {
    flex-direction: row-reverse;

    .content {
      align-items: flex-end;

      .text-container {
        position: relative;

        .text {
          background: #f0f7ff;
          color: #333;
          border-radius: 1rem 1rem 0 1rem;
        }

        .user-copy-button {
          position: absolute;
          left: -30px;
          top: 50%;
          transform: translateY(-50%);
          background: transparent;
          border: none;
          width: 24px;
          height: 24px;
          display: flex;
          align-items: center;
          justify-content: center;
          cursor: pointer;
          opacity: 0;
          transition: opacity 0.2s;

          .copy-icon {
            width: 16px;
            height: 16px;
            color: #666;

            &.copied {
              color: #4ade80;
            }
          }
        }

        &:hover .user-copy-button {
          opacity: 1;
        }
      }
    }
  }

  .avatar {
    width: 40px;
    height: 40px;
    flex-shrink: 0;

    .icon {
      width: 100%;
      height: 100%;
      color: #666;
      padding: 4px;
      border-radius: 8px;
      transition: all 0.3s ease;

      &.assistant {
        color: #333;
        background: #f0f0f0;

        &:hover {
          background: #e0e0e0;
          transform: scale(1.05);
        }
      }
    }
  }

  .content {
    display: flex;
    flex-direction: column;
    gap: 0.25rem;
    max-width: 80%;

    .text-container {
      position: relative;
    }

    .message-footer {
      display: flex;
      align-items: center;
      margin-top: 0.25rem;

      .copy-button {
        display: flex;
        align-items: center;
        gap: 0.25rem;
        background: transparent;
        border: none;
        font-size: 0.75rem;
        color: #666;
        padding: 0.25rem 0.5rem;
        border-radius: 4px;
        cursor: pointer;
        margin-right: auto;
        transition: background-color 0.2s;

        &:hover {
          background-color: rgba(0, 0, 0, 0.05);
        }

        .copy-icon {
          width: 14px;
          height: 14px;

          &.copied {
            color: #4ade80;
          }
        }
      }
    }

    .text {
      padding: 1rem;
      border-radius: 1rem 1rem 1rem 0;
      line-height: 1.5;
      white-space: pre-wrap;
      color: var(--text-color);
    }
  }
}

// 深度选择器样式
.markdown-content {
  ::v-deep .think-block {
    position: relative;
    padding: 0.75rem 1rem 0.75rem 1.5rem;
    margin: 0.5rem 0;
    color: #666;
    font-style: italic;
    border-left: 4px solid #ddd;
    background-color: rgba(0, 0, 0, 0.03);
    border-radius: 0 0.5rem 0.5rem 0;
    transition: opacity 0.3s ease, transform 0.3s ease;

    &::before {
      content: "思考";
      position: absolute;
      top: -0.75rem;
      left: 1rem;
      padding: 0 0.5rem;
      font-size: 0.75rem;
      background: #f5f5f5;
      border-radius: 0.25rem;
      color: #999;
      font-style: normal;
    }
  }

  ::v-deep pre {
    background: #f6f8fa;
    padding: 1rem;
    border-radius: 0.5rem;
    overflow-x: auto;
    margin: 0.5rem 0;
    border: 1px solid #e1e4e8;
  }

  ::v-deep code {
    background: rgba(0, 0, 0, 0.05);
    padding: 0.2em 0.4em;
    border-radius: 3px;
    font-size: 0.9em;
    font-family: ui-monospace, monospace;
  }

  ::v-deep pre code {
    background: transparent;
    padding: 0;
  }

  ::v-deep .code-block-wrapper {
    position: relative;
    margin: 1rem 0;
    border-radius: 6px;
    overflow: hidden;

    .code-copy-button {
      position: absolute;
      top: 0.5rem;
      right: 0.5rem;
      background: rgba(255, 255, 255, 0.1);
      border: none;
      color: #e6e6e6;
      cursor: pointer;
      padding: 0.25rem;
      border-radius: 4px;
      display: flex;
      align-items: center;
      justify-content: center;
      opacity: 0;
      transition: opacity 0.2s, background-color 0.2s;
      z-index: 10;

      &:hover {
        background-color: rgba(255, 255, 255, 0.2);
      }

      .code-copy-icon {
        width: 16px;
        height: 16px;
      }
    }

    &:hover .code-copy-button {
      opacity: 0.8;
    }

    pre {
      margin: 0;
      padding: 1rem;
      background: #1e1e1e;
      overflow-x: auto;

      code {
        background: transparent;
        padding: 0;
        font-family: ui-monospace, monospace;
      }
    }

    .copy-success-message {
      position: absolute;
      top: 0.5rem;
      right: 0.5rem;
      background: rgba(74, 222, 128, 0.9);
      color: white;
      padding: 0.25rem 0.5rem;
      border-radius: 4px;
      font-size: 0.75rem;
      opacity: 0;
      transform: translateY(-10px);
      transition: opacity 0.3s, transform 0.3s;
      pointer-events: none;
      z-index: 20;

      &.visible {
        opacity: 1;
        transform: translateY(0);
      }
    }
  }
}

// 流式输出光标动画
.streaming-cursor {
  &::after {
    content: '▋';
    display: inline-block;
    color: #409EFF;
    animation: blink 0.8s infinite;
    margin-left: 2px;
    font-weight: normal;
  }
}

@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0; }
}

// 流式输出时的打字效果
.message.assistant.streaming .markdown-content {
  .text {
    position: relative;
  }
}
</style>
