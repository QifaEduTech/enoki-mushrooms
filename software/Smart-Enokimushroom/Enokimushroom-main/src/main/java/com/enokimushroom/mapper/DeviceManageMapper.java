package com.enokimushroom.mapper;

import java.util.List;
import com.enokimushroom.entity.DeviceManage;
import com.enokimushroom.entity.vo.DeviceManageVo;

/**
 * 【请填写功能名称】Mapper接口
 * 
 * @author ruoyi
 * @date 2024-06-08
 */
public interface DeviceManageMapper 
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
     * 查询【使用总和】列表
     *
     * @param
     * @return 设备总数，在用设备、占比
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
     * 删除【请填写功能名称】
     * 
     * @param deviceId 【请填写功能名称】主键
     * @return 结果
     */
    public int deleteDeviceManageByDeviceId(Long deviceId);

    /**
     * 批量删除【请填写功能名称】
     * 
     * @param deviceIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteDeviceManageByDeviceIds(Long[] deviceIds);
}
