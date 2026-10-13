import request from '@/utils/request'

// 查询产量预期列表
export function listStarryCluster(query) {
  return request({
    url: '/system/starryCluster/list',
    method: 'get',
    params: query
  })
}

// 查询产量预期详细
export function getStarryCluster(id) {
  return request({
    url: '/system/starryCluster/' + id,
    method: 'get'
  })
}

// 新增产量预期
export function addStarryCluster(data) {
  return request({
    url: '/system/starryCluster',
    method: 'post',
    data: data
  })
}

// 修改产量预期
export function updateStarryCluster(data) {
  return request({
    url: '/system/starryCluster',
    method: 'put',
    data: data
  })
}

// 删除产量预期
export function delStarryCluster(id) {
  return request({
    url: '/system/starryCluster/' + id,
    method: 'delete'
  })
}
