const TIMEOUT = 120000 // 120秒超时，适配ollama等较慢的本地模型

// 动态获取后端地址，适配 localhost 和 IP 访问
function getBaseURL() {
  const saved = localStorage.getItem('BASE_URL')
  if (saved) return saved
  return window.location.protocol + '//' + window.location.hostname + ':8080'
}

// 添加缺失的工具函数
function fetchWithTimeout(url, options = {}) {
  const controller = new AbortController()
  const timeoutId = setTimeout(() => controller.abort(), TIMEOUT)

  return fetch(url, {
    ...options,
    signal: controller.signal
  }).finally(() => {
    clearTimeout(timeoutId)
  })
}

function buildUrl(path, params = {}) {
  const url = new URL(path, getBaseURL())
  Object.keys(params).forEach(key => {
    if (params[key] !== null && params[key] !== undefined) {
      url.searchParams.append(key, params[key])
    }
  })
  return url.toString()
}

export const fileRagAPI = {
  // 上传知识库
  async uploadKnowledgeBase({ file, name, remark }) {
    try {
      const formData = new FormData()
      formData.append('file', file)
      formData.append('name', name)
      formData.append('remark', remark)
      const response = await fetchWithTimeout(`${getBaseURL()}/file-rag/upload`, {
        method: 'POST',
        body: formData
      })
      return response.json()
    } catch (error) {
      console.error('Upload Knowledge Base Error:', error)
      throw error
    }
  },

  // 获取知识库列表
  async listKnowledgeBases() {
    try {
      const response = await fetchWithTimeout(`${getBaseURL()}/file-rag/list`)
      return response.json()
    } catch (error) {
      console.error('List Knowledge Bases Error:', error)
      throw error
    }
  }
}

export const chatAPI = {
  // 发送聊天消息
  async simpleChat(data, chatId) {
    try {
      const url = buildUrl('/simple/chat', { chatId })
      const response = await fetchWithTimeout(url, {
        method: 'POST',
        body: data instanceof FormData ? data : new URLSearchParams({ prompt: data })
      })
      return response.body.getReader()
    } catch (error) {
      console.error('Simple Chat Error:', error)
      throw error
    }
  },

  // 获取聊天的历史会话id列表
  async chatTypeHistoryList(type = 1) {
    try {
      const url = buildUrl('/chat/type/history/list', { type })
      const response = await fetchWithTimeout(url)
      const chats = await response.json()

      return chats.map(chat => ({
        id:chat.chatId,
        title: chat.title === '' || chat.title === null  || chat.title === undefined ? `新的对话` : chat.title
      }))
    } catch (error) {
      console.error('History Chat ID List Error:', error)
      return []
    }
  },

  // 获取具体对话下的历史消息
  async chatHistoryMessageList(chatId, type = 1) {
    try {
      const url = buildUrl('/chat/history/message/list', { chatId, type })
      const response = await fetchWithTimeout(url)
      const messages = await response.json()

      return messages.map(msg => ({
        ...msg,
        timestamp: new Date()
      }))
    } catch (error) {
      console.error('History Chat History List Error:', error)
      return []
    }
  },

  // 发送助手消息
  async sendAssistantMessage(prompt, chatId) {
    try {
      const url = buildUrl('/program/chat', { prompt, chatId })
      const response = await fetchWithTimeout(url)
      return response.body.getReader()
    } catch (error) {
      console.error('Assistant Message Error:', error)
      throw error
    }
  },

  // 发送rag消息
  async sendRagMessage(prompt, chatId) {
    try {
      const url = buildUrl('/program/rag', { prompt, chatId })
      const response = await fetchWithTimeout(url)
      return response.body.getReader()
    } catch (error) {
      console.error('RAG Message Error:', error)
      throw error
    }
  },

  // 删除对话
  async deleteChat(chatId, type = 1) {
    try {
      const url = buildUrl('/chat/delete', { chatId, type })
      await fetchWithTimeout(url)
      return true
    } catch (error) {
      console.error('Delete Chat Error:', error)
      throw error
    }
  }
}

export const ragQueryAPI = {
  // 流式查询版本 - 使用统一的BASE_URL和工具函数
  async ask(question, kbId, chatId = null, topK ) {
    try {
      const response = await fetchWithTimeout(`${getBaseURL()}/rag-query/ask`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          question,
          kbId,
          chatId,
          topK
        })
      })
      return response.body.getReader()
    } catch (error) {
      console.error('RAG Query Error:', error)
      throw error
    }
  }
}
