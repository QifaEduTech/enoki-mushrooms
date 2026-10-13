package com.enokimushroom.service;

import java.util.List;
import com.enokimushroom.entity.PlantManage;
import com.enokimushroom.entity.vo.PlantManageVo;

/**
 * 【请填写功能名称】Service接口
 * 
 * @author ruoyi
 * @date 2024-06-08
 */
public interface IPlantManageService 
{
    /**
     * 查询【请填写功能名称】
     * 
     * @param plantId 【请填写功能名称】主键
     * @return 【请填写功能名称】
     */
    public PlantManage selectPlantManageByPlantId(Long plantId);

    /**
     * 查询【请填写功能名称】列表
     * 
     * @param plantManage 【请填写功能名称】
     * @return 【请填写功能名称】集合
     */
    public List<PlantManage> selectPlantManageList(PlantManage plantManage);

    /**
     * 查询【植物数量、占比】列表
     *
     * @param
     * @return 【植物数量、占比】集合
     */
    public List<PlantManageVo> selectPlantManageListByCount();

    /**
     * 新增【请填写功能名称】
     * 
     * @param plantManage 【请填写功能名称】
     * @return 结果
     */
    public int insertPlantManage(PlantManage plantManage);

    /**
     * 修改【请填写功能名称】
     * 
     * @param plantManage 【请填写功能名称】
     * @return 结果
     */
    public int updatePlantManage(PlantManage plantManage);

    /**
     * 批量删除【请填写功能名称】
     * 
     * @param plantIds 需要删除的【请填写功能名称】主键集合
     * @return 结果
     */
    public int deletePlantManageByPlantIds(Long[] plantIds);

    /**
     * 删除【请填写功能名称】信息
     * 
     * @param plantId 【请填写功能名称】主键
     * @return 结果
     */
    public int deletePlantManageByPlantId(Long plantId);
}
