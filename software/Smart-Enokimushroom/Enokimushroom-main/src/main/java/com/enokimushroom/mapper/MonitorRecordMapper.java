package com.enokimushroom.mapper;

import com.enokimushroom.entity.MonitorRecord;

import java.util.Date;
import java.util.List;

/**
 * 【请填写功能名称】Mapper接口
 * 
 * @author ruoyi
 * @date 2024-06-08
 */
public interface MonitorRecordMapper 
{
    /**
     * 查询【请填写功能名称】
     * 
     * @param recordId 【请填写功能名称】主键
     * @return 【请填写功能名称】
     */
    public MonitorRecord selectMonitorRecordByRecordId(Long recordId);

    /**
     * 查询【请填写功能名称】列表
     * 
     * @param monitorRecord 【请填写功能名称】
     * @return 【请填写功能名称】集合
     */
    public List<MonitorRecord> selectMonitorRecordList(MonitorRecord monitorRecord);

    /**
     * 新增【请填写功能名称】
     * 
     * @param monitorRecord 【请填写功能名称】
     * @return 结果
     */
    public int insertMonitorRecord(MonitorRecord monitorRecord);

    /**
     * 修改【请填写功能名称】
     * 
     * @param monitorRecord 【请填写功能名称】
     * @return 结果
     */
    public int updateMonitorRecord(MonitorRecord monitorRecord);

    /**
     * 删除【请填写功能名称】
     * 
     * @param recordId 【请填写功能名称】主键
     * @return 结果
     */
    public int deleteMonitorRecordByRecordId(Long recordId);

    /**
     * 批量删除【请填写功能名称】
     * 
     * @param recordIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteMonitorRecordByRecordIds(Long[] recordIds);

    /**
     * 根据簇ID和时间范围查询监测记录
     * 
     * @param clusterId 簇ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 监测记录集合
     */
    public List<MonitorRecord> selectMonitorRecordByClusterIdAndTimeRange(String clusterId, Date startTime, Date endTime);
}
