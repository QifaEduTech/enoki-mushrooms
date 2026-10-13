package com.enokimushroom.service;

import com.enokimushroom.entity.EnokimushroomCluster;

import java.util.List;

/**
 * 产量预期Service接口
 * 
 * @author yejainfeng
 * @date 2025-08-18
 */
public interface IEnokimushroomClusterService 
{
    /**
     * 查询产量预期
     * 
     * @param id 产量预期主键
     * @return 产量预期
     */
    public EnokimushroomCluster selectEnokimushroomClusterById(Long id);

    /**
     * 查询产量预期列表
     * 
     * @param enokimushroomCluster 产量预期
     * @return 产量预期集合
     */
    public List<EnokimushroomCluster> selectEnokimushroomClusterList(EnokimushroomCluster enokimushroomCluster);

    /**
     * 新增产量预期
     * 
     * @param enokimushroomCluster 产量预期
     * @return 结果
     */
    public int insertEnokimushroomCluster(EnokimushroomCluster enokimushroomCluster);

    /**
     * 修改产量预期
     * 
     * @param enokimushroomCluster 产量预期
     * @return 结果
     */
    public int updateEnokimushroomCluster(EnokimushroomCluster enokimushroomCluster);

    /**
     * 批量删除产量预期
     * 
     * @param ids 需要删除的产量预期主键集合
     * @return 结果
     */
    public int deleteEnokimushroomClusterByIds(Long[] ids);

    /**
     * 删除产量预期信息
     * 
     * @param id 产量预期主键
     * @return 结果
     */
    public int deleteEnokimushroomClusterById(Long id);

    /**
     * 根据簇ID查询产量预期
     * 
     * @param clusterId 簇ID
     * @return 产量预期列表
     */
    public List<EnokimushroomCluster> selectEnokimushroomClusterByClusterId(String clusterId);
}
