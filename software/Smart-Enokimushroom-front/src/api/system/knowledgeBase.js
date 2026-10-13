const TIMEOUT = 120000 // 120秒超时，适配ollama等较慢的本地模型

// 动态获取后端地址，适配 localhost 和 IP 访问
function getBaseURL() {
  const saved = localStorage.getItem('BASE_URL')
  if (saved) return saved
  return window.location.protocol + '//' + window.location.hostname + ':8080'
}

// 添加超时fetch函数
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

export const knowledgeBaseAPI = {
  // 分页查询
  async page({ page = 1, size = 10, name = '' }) {
    try {
      const params = new URLSearchParams({
        page: page.toString(),
        size: size.toString()
      })

      // 只有当name不为空时才添加到参数中
      if (name.trim()) {
        params.append('name', name)
      }

      const url = `${getBaseURL()}/file-rag/page?${params.toString()}`
      const response = await fetchWithTimeout(url)

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`)
      }

      const data = await response.json()
      
      // 确保返回的数据结构正确
      if (data && typeof data === 'object') {
        // 如果后端返回的是直接的数组，包装成标准格式
        if (Array.isArray(data)) {
          return {
            data: {
              list: data,
              total: data.length,
              page: page,
              size: size
            }
          }
        }
        // 如果后端返回的是标准格式，直接返回
        if (data.data && Array.isArray(data.data.list)) {
          return data
        }
        // 如果后端返回的是其他格式，尝试适配
        if (data.list && Array.isArray(data.list)) {
          return {
            data: {
              list: data.list,
              total: data.total || data.list.length,
              page: page,
              size: size
            }
          }
        }
      }
      
      // 如果数据格式不符合预期，返回空数据
      console.warn('Unexpected API response format:', data)
      return {
        data: {
          list: [],
          total: 0,
          page: page,
          size: size
        }
      }
    } catch (error) {
      console.error('Failed to fetch knowledge base data:', error)
      // 返回空数据而不是抛出错误，避免页面崩溃
      return {
        data: {
          list: [],
          total: 0,
          page: page,
          size: size
        }
      }
    }
  },

  // 删除
  async delete(id) {
    try {
      const url = `${getBaseURL()}/file-rag/delete`
      const response = await fetchWithTimeout(url, {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: new URLSearchParams({ id: id.toString() })
      })

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`)
      }

      return response.json()
    } catch (error) {
      console.error('Failed to delete knowledge base:', error)
      throw error
    }
  },

  // 修改
  async update({ id, name, remark, topK }) {
    try {
      const url = `${getBaseURL()}/file-rag/update`
      const response = await fetchWithTimeout(url, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          id,
          name,
          remark: remark || '',
          topK: topK || null
        })
      })

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`)
      }

      return response.json()
    } catch (error) {
      console.error('Failed to update knowledge base:', error)
      throw error
    }
  }
}
