package com.enokimushroom.service.imp;

import java.util.List;

import com.enokimushroom.entity.vo.PlantManageVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.enokimushroom.mapper.PlantManageMapper;
import com.enokimushroom.entity.PlantManage;
import com.enokimushroom.service.IPlantManageService;

/**
 * 【请填写功能名称】Service业务层处理
 * 
 * @author ruoyi
 * @date 2024-06-08
 */
@Service
public class PlantManageServiceImpl implements IPlantManageService 
{
    @Autowired
    private PlantManageMapper plantManageMapper;

    /**
     * 查询【请填写功能名称】
     * 
     * @param plantId 【请填写功能名称】主键
     * @return 【请填写功能名称】
     */
    @Override
    public PlantManage selectPlantManageByPlantId(Long plantId)
    {
        return plantManageMapper.selectPlantManageByPlantId(plantId);
    }

    /**
     * 查询【请填写功能名称】列表
     * 
     * @param plantManage 【请填写功能名称】
     * @return 【请填写功能名称】
     */
    @Override
    public List<PlantManage> selectPlantManageList(PlantManage plantManage)
    {
        return plantManageMapper.selectPlantManageList(plantManage);
    }

    /**
     * 查询【植物数量、占比】列表
     *
     * @param
     * @return 【植物数量、占比】
     */
    @Override
    public List<PlantManageVo> selectPlantManageListByCount()
    {
        return plantManageMapper.selectPlantManageListByCount();
    }

    /**
     * 新增【请填写功能名称】
     * 
     * @param plantManage 【请填写功能名称】
     * @return 结果
     */
    @Override
    public int insertPlantManage(PlantManage plantManage)
    {
        return plantManageMapper.insertPlantManage(plantManage);
    }

    /**
     * 修改【请填写功能名称】
     * 
     * @param plantManage 【请填写功能名称】
     * @return 结果
     */
    @Override
    public int updatePlantManage(PlantManage plantManage)
    {
        return plantManageMapper.updatePlantManage(plantManage);
    }

    /**
     * 批量删除【请填写功能名称】
     * 
     * @param plantIds 需要删除的【请填写功能名称】主键
     * @return 结果
     */
    @Override
    public int deletePlantManageByPlantIds(Long[] plantIds)
    {
        return plantManageMapper.deletePlantManageByPlantIds(plantIds);
    }

    /**
     * 删除【请填写功能名称】信息
     * 
     * @param plantId 【请填写功能名称】主键
     * @return 结果
     */
    @Override
    public int deletePlantManageByPlantId(Long plantId)
    {
        return plantManageMapper.deletePlantManageByPlantId(plantId);
    }
}
