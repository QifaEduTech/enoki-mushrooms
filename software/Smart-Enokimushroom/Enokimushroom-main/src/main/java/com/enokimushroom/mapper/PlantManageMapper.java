package com.enokimushroom.mapper;

import java.util.List;
import com.enokimushroom.entity.PlantManage;
import com.enokimushroom.entity.vo.PlantManageVo;

/**
 * 【请填写功能名称】Mapper接口
 * 
 * @author ruoyi
 * @date 2024-06-08
 */
public interface PlantManageMapper 
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
     * @return 【植物数量，占比】集合
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
     * 删除【请填写功能名称】
     * 
     * @param plantId 【请填写功能名称】主键
     * @return 结果
     */
    public int deletePlantManageByPlantId(Long plantId);

    /**
     * 批量删除【请填写功能名称】
     * 
     * @param plantIds 需要删除的数据主键集合selectAreaNameAndId
     * @return 结果
     */
    public int deletePlantManageByPlantIds(Long[] plantIds);

}
