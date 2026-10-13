import request from '@/utils/request'

// 产量分析
export function yieldAnalysis(data) {
  return request({
    url: '/intelligent-analytics/yield-analysis',
    method: 'post',
    data: data
  })
}

// 预测产量
export function predictYield(data) {
  return request({
    url: '/intelligent-analytics/yield-prediction',
    method: 'post',
    data: data,
    timeout: 180000 // 设置3分钟超时，适应LLM生成时间
  })
}

// 病虫害分析
export function pestDiseaseAnalysis(data) {
  return request({
    url: '/intelligent-analytics/pest-disease-analysis',
    method: 'post',
    data: data,
    timeout: 180000 // 设置3分钟超时，适应LLM生成时间
  })
}

// 综合健康分析
export function comprehensiveHealthAnalysis(data) {
  return request({
    url: '/intelligent-analytics/comprehensive-health-analysis',
    method: 'post',
    data: data
  })
}
