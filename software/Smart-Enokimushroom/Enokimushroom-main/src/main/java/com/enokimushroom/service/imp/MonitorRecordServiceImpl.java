package com.enokimushroom.service.imp;

import com.enokimushroom.entity.MonitorRecord;
import com.enokimushroom.mapper.MonitorRecordMapper;
import com.enokimushroom.service.IMonitorRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 【请填写功能名称】Service业务层处理
 * 
 * @author ruoyi
 * @date 2024-06-08
 */
@Service
public class MonitorRecordServiceImpl implements IMonitorRecordService 
{
    @Autowired
    private MonitorRecordMapper monitorRecordMapper;

    /**
     * 查询【请填写功能名称】
     * 
     * @param recordId 【请填写功能名称】主键
     * @return 【请填写功能名称】
     */
    @Override
    public MonitorRecord selectMonitorRecordByRecordId(Long recordId)
    {
        return monitorRecordMapper.selectMonitorRecordByRecordId(recordId);
    }

    /**
     * 查询【请填写功能名称】列表
     * 
     * @param monitorRecord 【请填写功能名称】
     * @return 【请填写功能名称】
     */
    @Override
    public List<MonitorRecord> selectMonitorRecordList(MonitorRecord monitorRecord)
    {
        return monitorRecordMapper.selectMonitorRecordList(monitorRecord);
    }

    /**
     * 新增【请填写功能名称】
     * 
     * @param monitorRecord 【请填写功能名称】
     * @return 结果
     */
    @Override
    public int insertMonitorRecord(MonitorRecord monitorRecord)
    {
        return monitorRecordMapper.insertMonitorRecord(monitorRecord);
    }

    /**
     * 修改【请填写功能名称】
     * 
     * @param monitorRecord 【请填写功能名称】
     * @return 结果
     */
    @Override
    public int updateMonitorRecord(MonitorRecord monitorRecord)
    {
        return monitorRecordMapper.updateMonitorRecord(monitorRecord);
    }

    /**
     * 批量删除【请填写功能名称】
     * 
     * @param recordIds 需要删除的【请填写功能名称】主键
     * @return 结果
     */
    @Override
    public int deleteMonitorRecordByRecordIds(Long[] recordIds)
    {
        return monitorRecordMapper.deleteMonitorRecordByRecordIds(recordIds);
    }

    /**
     * 删除【请填写功能名称】信息
     * 
     * @param recordId 【请填写功能名称】主键
     * @return 结果
     */
    @Override
    public int deleteMonitorRecordByRecordId(Long recordId)
    {
        return monitorRecordMapper.deleteMonitorRecordByRecordId(recordId);
    }

    /**
     * 根据簇ID和时间范围查询监测记录
     * 
     * @param clusterId 簇ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 监测记录集合
     */
    @Override
    public List<MonitorRecord> selectMonitorRecordByClusterIdAndTimeRange(String clusterId, Date startTime, Date endTime)
    {
        return monitorRecordMapper.selectMonitorRecordByClusterIdAndTimeRange(clusterId, startTime, endTime);
    }

    /**
     * 根据簇ID查询当天的监测记录
     * 
     * @param clusterId 簇ID
     * @return 监测记录集合
     */
    @Override
    public List<MonitorRecord> selectTodayMonitorRecordByClusterId(String clusterId)
    {
        // 获取今天的开始和结束时间
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date startOfDay = cal.getTime();
        
        cal.add(Calendar.DAY_OF_MONTH, 1);
        Date endOfDay = cal.getTime();
        
        return selectMonitorRecordByClusterIdAndTimeRange(clusterId, startOfDay, endOfDay);
    }
}
