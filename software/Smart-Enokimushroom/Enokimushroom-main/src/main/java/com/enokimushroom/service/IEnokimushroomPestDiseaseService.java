package com.enokimushroom.service;

import com.enokimushroom.entity.EnokimushroomPestDisease;

import java.util.List;

/**
 * 病虫害分析Service接口
 * 
 * @author yejianfeng
 * @date 2025-08-18
 */
public interface IEnokimushroomPestDiseaseService 
{
    /**
     * 查询病虫害分析
     * 
     * @param id 病虫害分析主键
     * @return 病虫害分析
     */
    public EnokimushroomPestDisease selectEnokimushroomPestDiseaseById(Long id);

    /**
     * 查询病虫害分析列表
     * 
     * @param enokimushroomPestDisease 病虫害分析
     * @return 病虫害分析集合
     */
    public List<EnokimushroomPestDisease> selectEnokimushroomPestDiseaseList(EnokimushroomPestDisease enokimushroomPestDisease);

    /**
     * 新增病虫害分析
     * 
     * @param enokimushroomPestDisease 病虫害分析
     * @return 结果
     */
    public int insertEnokimushroomPestDisease(EnokimushroomPestDisease enokimushroomPestDisease);

    /**
     * 修改病虫害分析
     * 
     * @param enokimushroomPestDisease 病虫害分析
     * @return 结果
     */
    public int updateEnokimushroomPestDisease(EnokimushroomPestDisease enokimushroomPestDisease);

    /**
     * 批量删除病虫害分析
     * 
     * @param ids 需要删除的病虫害分析主键集合
     * @return 结果
     */
    public int deleteEnokimushroomPestDiseaseByIds(Long[] ids);

    /**
     * 删除病虫害分析信息
     * 
     * @param id 病虫害分析主键
     * @return 结果
     */
    public int deleteEnokimushroomPestDiseaseById(Long id);

    /**
     * 根据簇ID查询病虫害分析列表
     * 
     * @param clusterId 簇ID
     * @return 病虫害分析集合
     */
    public List<EnokimushroomPestDisease> selectEnokimushroomPestDiseaseByClusterId(String clusterId);
}
