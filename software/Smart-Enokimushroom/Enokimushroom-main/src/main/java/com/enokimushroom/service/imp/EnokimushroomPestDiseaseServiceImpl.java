package com.enokimushroom.service.imp;

import com.enokimushroom.entity.EnokimushroomPestDisease;
import com.enokimushroom.mapper.EnokimushroomPestDiseaseMapper;
import com.enokimushroom.service.IEnokimushroomPestDiseaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 病虫害分析Service业务层处理
 * 
 * @author yejianfeng
 * @date 2025-08-18
 */
@Service
public class EnokimushroomPestDiseaseServiceImpl implements IEnokimushroomPestDiseaseService 
{
    @Autowired
    private EnokimushroomPestDiseaseMapper enokimushroomPestDiseaseMapper;

    /**
     * 查询病虫害分析
     * 
     * @param id 病虫害分析主键
     * @return 病虫害分析
     */
    @Override
    public EnokimushroomPestDisease selectEnokimushroomPestDiseaseById(Long id)
    {
        return enokimushroomPestDiseaseMapper.selectEnokimushroomPestDiseaseById(id);
    }

    /**
     * 查询病虫害分析列表
     * 
     * @param enokimushroomPestDisease 病虫害分析
     * @return 病虫害分析
     */
    @Override
    public List<EnokimushroomPestDisease> selectEnokimushroomPestDiseaseList(EnokimushroomPestDisease enokimushroomPestDisease)
    {
        return enokimushroomPestDiseaseMapper.selectEnokimushroomPestDiseaseList(enokimushroomPestDisease);
    }

    /**
     * 新增病虫害分析
     * 
     * @param enokimushroomPestDisease 病虫害分析
     * @return 结果
     */
    @Override
    public int insertEnokimushroomPestDisease(EnokimushroomPestDisease enokimushroomPestDisease)
    {
        return enokimushroomPestDiseaseMapper.insertEnokimushroomPestDisease(enokimushroomPestDisease);
    }

    /**
     * 修改病虫害分析
     * 
     * @param enokimushroomPestDisease 病虫害分析
     * @return 结果
     */
    @Override
    public int updateEnokimushroomPestDisease(EnokimushroomPestDisease enokimushroomPestDisease)
    {
        return enokimushroomPestDiseaseMapper.updateEnokimushroomPestDisease(enokimushroomPestDisease);
    }

    /**
     * 批量删除病虫害分析
     * 
     * @param ids 需要删除的病虫害分析主键
     * @return 结果
     */
    @Override
    public int deleteEnokimushroomPestDiseaseByIds(Long[] ids)
    {
        return enokimushroomPestDiseaseMapper.deleteEnokimushroomPestDiseaseByIds(ids);
    }

    /**
     * 删除病虫害分析信息
     * 
     * @param id 病虫害分析主键
     * @return 结果
     */
    @Override
    public int deleteEnokimushroomPestDiseaseById(Long id)
    {
        return enokimushroomPestDiseaseMapper.deleteEnokimushroomPestDiseaseById(id);
    }

    /**
     * 根据簇ID查询病虫害分析列表
     * 
     * @param clusterId 簇ID
     * @return 病虫害分析集合
     */
    @Override
    public List<EnokimushroomPestDisease> selectEnokimushroomPestDiseaseByClusterId(String clusterId)
    {
        return enokimushroomPestDiseaseMapper.selectEnokimushroomPestDiseaseByClusterId(clusterId);
    }
}
