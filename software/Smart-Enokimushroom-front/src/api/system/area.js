import request from '@/utils/request'

// 查询【场地】列表
export function listManage (query) {
  return request({
    url: '/system/area/list',
    method: 'get',
    params: query
  })
}

// 查询【场地使用】列表
export function listManageByCount () {
  return request({
    url: '/system/area/listByCount',
    method: 'get',
  })
}

// 查询【请填写功能名称】列表
export function listArea (query) {
  return request({
    url: '/system/area/list',
    method: 'get',
    params: query
  })
}

// 查询【请填写功能名称】详细
export function getManage (areaId) {
  return request({
    url: '/system/area/' + areaId,
    method: 'get'
  })
}

// 新增【请填写功能名称】
export function addManage (data) {
  return request({
    url: '/system/area',
    method: 'post',
    data: data
  })
}

// 修改【请填写功能名称】
export function updateManage (data) {
  return request({
    url: '/system/area',
    method: 'put',
    data: data
  })
}

// 删除【请填写功能名称】
export function delManage (areaId) {
  return request({
    url: '/system/area/' + areaId,
    method: 'delete'
  })
}
