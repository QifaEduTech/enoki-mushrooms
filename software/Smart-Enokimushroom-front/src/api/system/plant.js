import request from '@/utils/request'

// 查询【植物】列表
export function listManage (query) {
  return request({
    url: '/system/plant/list',
    method: 'get',
    params: query
  })
}

// 查询【植物总数、占比】列表
export function listManageByCount () {
  return request({
    url: '/system/plant/listByCount',
    method: 'get',
  })
}


// 查询【植物】列表
export function listPlant (query) {
  return request({
    url: '/system/plant/list',
    method: 'get',
    params: query
  })
}

// 查询【植物，按照ID】详细
export function getManage (plantId) {
  return request({
    url: '/system/plant/' + plantId,
    method: 'get'
  })
}

// 新增【植物】
export function addManage (data) {
  return request({
    url: '/system/plant',
    method: 'post',
    data: data
  })
}

// 修改【植物】
export function updateManage (data) {
  return request({
    url: '/system/plant',
    method: 'put',
    data: data
  })
}

// 删除【植物】
export function delManage (plantId) {
  return request({
    url: '/system/plant/' + plantId,
    method: 'delete'
  })
}

// 查询【场地名称】列表
export function getAreaName () {
  return request({
    url: '/system/plant/areaName',
    method: 'get'
  })
}
