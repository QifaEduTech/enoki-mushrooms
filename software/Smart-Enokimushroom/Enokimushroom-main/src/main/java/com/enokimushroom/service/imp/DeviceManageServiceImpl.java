package com.enokimushroom.service.imp;

import java.util.List;

import com.enokimushroom.entity.vo.DeviceManageVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.enokimushroom.mapper.DeviceManageMapper;
import com.enokimushroom.entity.DeviceManage;
import com.enokimushroom.service.IDeviceManageService;

/**
 * 【请填写功能名称】Service业务层处理
 * 
 * @author ruoyi
 * @date 2024-06-08
 */
@Service
public class DeviceManageServiceImpl implements IDeviceManageService 
{
    @Autowired
    private DeviceManageMapper deviceManageMapper;

    /**
     * 查询【请填写功能名称】
     * 
     * @param deviceId 【请填写功能名称】主键
     * @return 【请填写功能名称】
     */
    @Override
    public DeviceManage selectDeviceManageByDeviceId(Long deviceId)
    {
        return deviceManageMapper.selectDeviceManageByDeviceId(deviceId);
    }

    /**
     * 查询【请填写功能名称】列表
     * 
     * @param deviceManage 【请填写功能名称】
     * @return 【请填写功能名称】
     */
    @Override
    public List<DeviceManage> selectDeviceManageList(DeviceManage deviceManage)
    {
        return deviceManageMapper.selectDeviceManageList(deviceManage);
    }

    /**
     * 查询【设备总和、在用设备】列表
     *
     * @param
     * @return 设备总和、在用设备、占比
     */
    @Override
    public List<DeviceManageVo> selectDeviceManageListByCount()
    {
        return deviceManageMapper.selectDeviceManageListByCount();
    }

    /**
     * 新增【请填写功能名称】
     * 
     * @param deviceManage 【请填写功能名称】
     * @return 结果
     */
    @Override
    public int insertDeviceManage(DeviceManage deviceManage)
    {
        return deviceManageMapper.insertDeviceManage(deviceManage);
    }

    /**
     * 修改【请填写功能名称】
     * 
     * @param deviceManage 【请填写功能名称】
     * @return 结果
     */
    @Override
    public int updateDeviceManage(DeviceManage deviceManage)
    {
        return deviceManageMapper.updateDeviceManage(deviceManage);
    }

    /**
     * 批量删除【请填写功能名称】
     * 
     * @param deviceIds 需要删除的【请填写功能名称】主键
     * @return 结果
     */
    @Override
    public int deleteDeviceManageByDeviceIds(Long[] deviceIds)
    {
        return deviceManageMapper.deleteDeviceManageByDeviceIds(deviceIds);
    }

    /**
     * 删除【请填写功能名称】信息
     * 
     * @param deviceId 【请填写功能名称】主键
     * @return 结果
     */
    @Override
    public int deleteDeviceManageByDeviceId(Long deviceId)
    {
        return deviceManageMapper.deleteDeviceManageByDeviceId(deviceId);
    }
}
