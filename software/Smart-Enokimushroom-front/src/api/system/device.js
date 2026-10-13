import request from '@/utils/request'

// 查询【设备】列表
export function listManage (query) {
  return request({
    url: '/system/device/list',
    method: 'get',
    params: query
  })
}

// 查询【设备】列表:设备总和、在用设备和占比
export function listManageByCount () {
  return request({
    url: '/system/device/listByCount',
    method: 'get',

  })
}


// 查询【设备】详细
export function getManage (deviceId) {
  return request({
    url: '/system/device/' + deviceId,
    method: 'get'
  })
}

// 新增【设备】
export function addManage (data) {
  return request({
    url: '/system/device',
    method: 'post',
    data: data
  })
}

// 修改【设备】
export function updateManage (data) {
  return request({
    url: '/system/device',
    method: 'put',
    data: data
  })
}

// 删除【设备】
export function delManage (deviceId) {
  return request({
    url: '/system/device/' + deviceId,
    method: 'delete'
  })
}

// 查询【植物名称】详细
export function getPlantId () {
  return request({
    url: '/system/device/plantId',
    method: 'get'
  })
}

// 查询【场地名称】列表
export function getAreaName () {
  return request({
    url: '/system/device/areaName',
    method: 'get'
  })
}
