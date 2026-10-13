package com.enokimushroom.service;

import java.util.List;
import com.enokimushroom.entity.DeviceManage;
import com.enokimushroom.entity.vo.DeviceManageVo;

/**
 * 【请填写功能名称】Service接口
 * 
 * @author ruoyi
 * @date 2024-06-08
 */
public interface IDeviceManageService 
{
    /**
     * 查询【请填写功能名称】
     * 
     * @param deviceId 【请填写功能名称】主键
     * @return 【请填写功能名称】
     */
    public DeviceManage selectDeviceManageByDeviceId(Long deviceId);

    /**
     * 查询【请填写功能名称】列表
     * 
     * @param deviceManage 【请填写功能名称】
     * @return 【请填写功能名称】集合
     */
    public List<DeviceManage> selectDeviceManageList(DeviceManage deviceManage);

    /**
     * 查询【设备综合】列表
     *
     * @param
     * @return 设备总和、在用设备、占比
     */
    public List<DeviceManageVo> selectDeviceManageListByCount();

    /**
     * 新增【请填写功能名称】
     * 
     * @param deviceManage 【请填写功能名称】
     * @return 结果
     */
    public int insertDeviceManage(DeviceManage deviceManage);

    /**
     * 修改【请填写功能名称】
     * 
     * @param deviceManage 【请填写功能名称】
     * @return 结果
     */
    public int updateDeviceManage(DeviceManage deviceManage);

    /**
     * 批量删除【请填写功能名称】
     * 
     * @param deviceIds 需要删除的【请填写功能名称】主键集合
     * @return 结果
     */
    public int deleteDeviceManageByDeviceIds(Long[] deviceIds);

    /**
     * 删除【请填写功能名称】信息
     * 
     * @param deviceId 【请填写功能名称】主键
     * @return 结果
     */
    public int deleteDeviceManageByDeviceId(Long deviceId);
}
