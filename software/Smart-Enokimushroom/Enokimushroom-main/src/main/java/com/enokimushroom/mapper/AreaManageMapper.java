package com.enokimushroom.mapper;

import java.util.List;
import com.enokimushroom.entity.AreaManage;
import com.enokimushroom.entity.vo.AreaManageVo;

/**
 * 【请填写功能名称】Mapper接口
 * 
 * @author ruoyi
 * @date 2024-06-08
 */
public interface AreaManageMapper 
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
     * @return 场地总面积、使用面积
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
     * 删除【请填写功能名称】
     * 
     * @param areaId 【请填写功能名称】主键
     * @return 结果
     */
    public int deleteAreaManageByAreaId(Long areaId);

    /**
     * 批量删除【请填写功能名称】
     * 
     * @param areaIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteAreaManageByAreaIds(Long[] areaIds);
}
