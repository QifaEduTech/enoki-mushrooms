package com.enokimushroom.service.imp;

import java.util.List;

import com.enokimushroom.entity.vo.AreaManageVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.enokimushroom.mapper.AreaManageMapper;
import com.enokimushroom.entity.AreaManage;
import com.enokimushroom.service.IAreaManageService;

/**
 * 【请填写功能名称】Service业务层处理
 * 
 * @author ruoyi
 * @date 2024-06-08
 */
@Service
public class AreaManageServiceImpl implements IAreaManageService 
{
    @Autowired
    private AreaManageMapper areaManageMapper;

    /**
     * 查询【请填写功能名称】
     * 
     * @param areaId 【请填写功能名称】主键
     * @return 【请填写功能名称】
     */
    @Override
    public AreaManage selectAreaManageByAreaId(Long areaId)
    {
        return areaManageMapper.selectAreaManageByAreaId(areaId);
    }

    /**
     * 查询【请填写功能名称】列表
     * 
     * @param areaManage 【请填写功能名称】
     * @return 【请填写功能名称】
     */
    @Override
    public List<AreaManage> selectAreaManageList(AreaManage areaManage)
    {
        return areaManageMapper.selectAreaManageList(areaManage);
    }

    /**
     * 查询【请填写功能名称】列表
     *
     * @param
     * @return 场地总面积、使用面积、使用率
     */
    @Override
    public List<AreaManageVo> selectAreaManageListByCount()
    {
        return areaManageMapper.selectAreaManageListByCount();
    }

    /**
     * 新增【请填写功能名称】
     * 
     * @param areaManage 【请填写功能名称】
     * @return 结果
     */
    @Override
    public int insertAreaManage(AreaManage areaManage)
    {
        return areaManageMapper.insertAreaManage(areaManage);
    }

    /**
     * 修改【请填写功能名称】
     * 
     * @param areaManage 【请填写功能名称】
     * @return 结果
     */
    @Override
    public int updateAreaManage(AreaManage areaManage)
    {
        return areaManageMapper.updateAreaManage(areaManage);
    }

    /**
     * 批量删除【请填写功能名称】
     * 
     * @param areaIds 需要删除的【请填写功能名称】主键
     * @return 结果
     */
    @Override
    public int deleteAreaManageByAreaIds(Long[] areaIds)
    {
        return areaManageMapper.deleteAreaManageByAreaIds(areaIds);
    }

    /**
     * 删除【请填写功能名称】信息
     * 
     * @param areaId 【请填写功能名称】主键
     * @return 结果
     */
    @Override
    public int deleteAreaManageByAreaId(Long areaId)
    {
        return areaManageMapper.deleteAreaManageByAreaId(areaId);
    }
}
