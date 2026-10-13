package com.enokimushroom.service.imp;

import com.enokimushroom.entity.EnokimushroomCluster;
import com.enokimushroom.mapper.EnokimushroomClusterMapper;
import com.enokimushroom.service.IEnokimushroomClusterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 产量预期Service业务层处理
 * 
 * @author yejainfeng
 * @date 2025-08-18
 */
@Service
public class EnokimushroomClusterServiceImpl implements IEnokimushroomClusterService 
{
    @Autowired
    private EnokimushroomClusterMapper enokimushroomClusterMapper;

    /**
     * 查询产量预期
     * 
     * @param id 产量预期主键
     * @return 产量预期
     */
    @Override
    public EnokimushroomCluster selectEnokimushroomClusterById(Long id)
    {
        return enokimushroomClusterMapper.selectEnokimushroomClusterById(id);
    }

    /**
     * 查询产量预期列表
     * 
     * @param enokimushroomCluster 产量预期
     * @return 产量预期
     */
    @Override
    public List<EnokimushroomCluster> selectEnokimushroomClusterList(EnokimushroomCluster enokimushroomCluster)
    {
        return enokimushroomClusterMapper.selectEnokimushroomClusterList(enokimushroomCluster);
    }

    /**
     * 新增产量预期
     * 
     * @param enokimushroomCluster 产量预期
     * @return 结果
     */
    @Override
    public int insertEnokimushroomCluster(EnokimushroomCluster enokimushroomCluster)
    {
        return enokimushroomClusterMapper.insertEnokimushroomCluster(enokimushroomCluster);
    }

    /**
     * 修改产量预期
     * 
     * @param enokimushroomCluster 产量预期
     * @return 结果
     */
    @Override
    public int updateEnokimushroomCluster(EnokimushroomCluster enokimushroomCluster)
    {
        return enokimushroomClusterMapper.updateEnokimushroomCluster(enokimushroomCluster);
    }

    /**
     * 批量删除产量预期
     * 
     * @param ids 需要删除的产量预期主键
     * @return 结果
     */
    @Override
    public int deleteEnokimushroomClusterByIds(Long[] ids)
    {
        return enokimushroomClusterMapper.deleteEnokimushroomClusterByIds(ids);
    }

    /**
     * 删除产量预期信息
     * 
     * @param id 产量预期主键
     * @return 结果
     */
    @Override
    public int deleteEnokimushroomClusterById(Long id)
    {
        return enokimushroomClusterMapper.deleteEnokimushroomClusterById(id);
    }

    /**
     * 根据簇ID查询产量预期
     * 
     * @param clusterId 簇ID
     * @return 产量预期列表
     */
    @Override
    public List<EnokimushroomCluster> selectEnokimushroomClusterByClusterId(String clusterId)
    {
        return enokimushroomClusterMapper.selectEnokimushroomClusterByClusterId(clusterId);
    }
}
