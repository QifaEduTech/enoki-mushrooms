package com.enokimushroom.service;

import java.util.List;
import com.enokimushroom.entity.AreaManage;
import com.enokimushroom.entity.vo.AreaManageVo;

/**
 * 【请填写功能名称】Service接口
 * 
 * @author ruoyi
 * @date 2024-06-08
 */
public interface IAreaManageService 
{
    /**
     * 查询【请填写功能名称】
     * 
     * @param areaId 【请填写功能名称】主键
     * @return 【请填写功能名称】
     */
    public AreaManage selectAreaManageByAreaId(Long areaId);

    /**
     * 查询【请填写功能名称】列表
     * 
     * @param areaManage 【请填写功能名称】
     * @return 【请填写功能名称】集合
     */
    public List<AreaManage> selectAreaManageList(AreaManage areaManage);

    /**
     * 查询【场地】使用列表
     *
     * @param
     * @return 场地总面积、使用面积、使用率
     */
    public List<AreaManageVo> selectAreaManageListByCount();

    /**
     * 新增【请填写功能名称】
     * 
     * @param areaManage 【请填写功能名称】
     * @return 结果
     */
    public int insertAreaManage(AreaManage areaManage);

    /**
     * 修改【请填写功能名称】
     * 
     * @param areaManage 【请填写功能名称】
     * @return 结果
     */
    public int updateAreaManage(AreaManage areaManage);

    /**
     * 批量删除【请填写功能名称】
     * 
     * @param areaIds 需要删除的【请填写功能名称】主键集合
     * @return 结果
     */
    public int deleteAreaManageByAreaIds(Long[] areaIds);

    /**
     * 删除【请填写功能名称】信息
     * 
     * @param areaId 【请填写功能名称】主键
     * @return 结果
     */
    public int deleteAreaManageByAreaId(Long areaId);
}
