import request from '@/utils/request'

// 查询病虫害分析列表
export function listDisease(query) {
  return request({
    url: '/system/disease/list',
    method: 'get',
    params: query
  })
}

// 查询病虫害分析详细
export function getDisease(id) {
  return request({
    url: '/system/disease/' + id,
    method: 'get'
  })
}

// 新增病虫害分析
export function addDisease(data) {
  return request({
    url: '/system/disease',
    method: 'post',
    data: data
  })
}

// 修改病虫害分析
export function updateDisease(data) {
  return request({
    url: '/system/disease',
    method: 'put',
    data: data
  })
}

// 删除病虫害分析
export function delDisease(id) {
  return request({
    url: '/system/disease/' + id,
    method: 'delete'
  })
}

// ==================== YOLO 识别接口 ====================

// YOLO 服务健康检查
export function yoloHealth() {
  return request({
    url: '/system/disease/yolo/health',
    method: 'get'
  })
}

// 上传图片进行 YOLO 识别
export function yoloDetect(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/system/disease/detect',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    timeout: 60000 // 60秒超时，模型推理需要时间
  })
}

// Base64 图片进行 YOLO 识别
export function yoloDetectBase64(base64Image) {
  return request({
    url: '/system/disease/detect/base64',
    method: 'post',
    data: { image: base64Image },
    timeout: 60000
  })
}
