package com.enokimushroom.controller.ai;

import com.enokimushroom.common.core.controller.BaseController;
import com.enokimushroom.common.core.domain.AjaxResult;
import com.enokimushroom.entity.MonitorRecord;
import com.enokimushroom.entity.EnokimushroomCluster;
import com.enokimushroom.entity.EnokimushroomPestDisease;
import com.enokimushroom.service.IMonitorRecordService;
import com.enokimushroom.service.IEnokimushroomClusterService;
import com.enokimushroom.service.IEnokimushroomPestDiseaseService;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 智能分析控制器
 * 结合私人知识库和数据库内容进行LLM分析
 */
@RestController
@RequestMapping("/intelligent-analytics")
public class IntelligentAnalyticsController extends BaseController {

    @Autowired
    private VectorStore vectorStore;

    @Autowired
    private IEnokimushroomClusterService enokimushroomClusterService;

    @Autowired
    private IEnokimushroomPestDiseaseService enokimushroomPestDiseaseService;

    @Autowired
    private IMonitorRecordService monitorRecordService;


    @Resource
    @Qualifier("clusterChatClient")
    private ChatClient clusterChatClient;

    @Resource
    @Qualifier("diseaseChatClient")
    private ChatClient diseaseChatClient;

    /**
     * 产量分析
     */
    @PostMapping("/yield-analysis")
    public AjaxResult yieldAnalysis(@RequestBody Map<String, Object> request) {
        try {
            String clusterId = (String) request.get("clusterId");
            String kbId = (String) request.get("kbId");
            String chatId = (String) request.get("chatId");
            Integer topK = (Integer) request.getOrDefault("topK", 5);

            // 1. 获取金针菇簇数据
            List<EnokimushroomCluster> clusters = enokimushroomClusterService.selectEnokimushroomClusterByClusterId(clusterId);
            if (clusters == null || clusters.isEmpty()) {
                return AjaxResult.error("未找到指定的金针菇簇数据");
            }

            // 2. 获取当天的环境监测数据
            List<MonitorRecord> monitorRecords = getTodayMonitorRecords(clusterId);

            // 3. 构建分析上下文
            String context = buildYieldAnalysisContext(clusters, monitorRecords);

            // 打印检索到的金针菇簇数据与上下文
            logger.info("[yield-analysis] 检索到的金针菇簇数据 ({} 条)：\n{}", clusters.size(), formatClusterSummary(clusters));
            logger.info("[yield-analysis] 发送给LLM的上下文：\n{}", context);

            // 4. 从内存向量存储检索相关知识
            SearchRequest searchRequest = SearchRequest.builder()
                    .query("金针菇产量分析 种植技术 环境因素")
                    .topK(topK * 2) // 先召回更多文档
                    .build();
            List<Document> docs = vectorStore.similaritySearch(searchRequest);

            // 5. 根据kbId过滤并限制数量
            List<Document> knowledgeDocs = docs.stream()
                    .filter(doc -> kbId.equals(doc.getMetadata().get("kbId")))
                    .limit(topK)
                    .collect(Collectors.toList());

            // 6. 整合知识库内容到上下文
            if (!knowledgeDocs.isEmpty()) {
                context += "\n\n=== 相关知识库 ===\n";
                String knowledgeContext = knowledgeDocs.stream()
                        .map(Document::getText)
                        .collect(Collectors.joining("\n"));
                context += knowledgeContext;
            }

            // 7. 调用LLM进行分析
            String analysis = clusterChatClient.prompt()
                    .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, chatId))
                    .call()
                    .content();

            return AjaxResult.success("分析完成", analysis);

        } catch (Exception e) {
            logger.error("产量分析失败", e);
            return AjaxResult.error("分析失败: " + e.getMessage());
        }
    }

    /**
     * 智能预测产量
     */
    @PostMapping("/yield-prediction")
    public AjaxResult yieldPrediction(@RequestBody Map<String, Object> request) {
        try {
            String clusterId = (String) request.get("clusterId");
            String kbId = (String) request.get("kbId");
            String chatId = (String) request.get("chatId");
            Integer topK = (Integer) request.getOrDefault("topK", 10);

            // 参数验证
            if (clusterId == null || clusterId.trim().isEmpty()) {
                return AjaxResult.error("金针菇区域ID不能为空");
            }

            logger.info("开始进行产量预测，clusterId: {}, kbId: {}, chatId: {}", clusterId, kbId, chatId);

            // 1. 获取指定金针菇簇的所有数据
            List<EnokimushroomCluster> clusters = enokimushroomClusterService.selectEnokimushroomClusterByClusterId(clusterId);
            if (clusters == null || clusters.isEmpty()) {
                logger.warn("未找到指定的金针菇簇数据，clusterId: {}", clusterId);
                return AjaxResult.error("未找到指定的金针菇簇数据，请检查clusterId是否正确");
            }

            logger.info("获取到金针菇簇数据 {} 条", clusters.size());

            // 2. 获取当天的环境监测数据（与金针菇簇无关，取全局当天数据）
            List<MonitorRecord> todayEnvRecords = getTodayMonitorRecordsGlobal();
            logger.info("获取到当天环境监测数据 {} 条", todayEnvRecords.size());

            // 3. 构建预测上下文（使用所有金针菇簇数据 + 当天环境数据）
            String context = buildYieldPredictionContext(clusters, todayEnvRecords);

            // 打印检索到的金针菇簇数据与上下文
            logger.info("[yield-prediction] 检索到的金针菇簇数据 ({} 条)：\n{}", clusters.size(), formatClusterSummary(clusters));
            logger.info("[yield-prediction] 发送给LLM的上下文：\n{}", context);

            // 4. 从内存向量存储检索相关知识
            logger.info("开始从知识库检索相关知识...");
            SearchRequest searchRequest = SearchRequest.builder()
                    .query("金针菇产量预测 生长周期 环境因素 产量估算 种植技术")
                    .topK(topK * 2) // 先召回更多文档
                    .build();
            List<Document> docs = vectorStore.similaritySearch(searchRequest);

            // 5. 根据kbId过滤并限制数量
            List<Document> knowledgeDocs = docs.stream()
                    .filter(doc -> kbId.equals(doc.getMetadata().get("kbId")))
                    .limit(topK)
                    .collect(Collectors.toList());
            logger.info("从知识库检索到 {} 条相关知识", 20);

            // 6. 整合知识库内容到上下文
            if (!knowledgeDocs.isEmpty()) {
                context += "\n\n=== 相关知识库 ===\n";
                String knowledgeContext = knowledgeDocs.stream()
                        .map(Document::getText)
                        .collect(Collectors.joining("\n"));
                context += knowledgeContext;
            }

            // 7. 调用LLM进行预测
            logger.info("开始调用LLM进行预测分析，这可能需要1-2分钟...");
            String prediction = clusterChatClient.prompt()
                    .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, chatId))
                    .call()
                    .content();

            logger.info("LLM预测完成，预测结果长度: {}", prediction.length());

            return AjaxResult.success("预测完成", prediction);

        } catch (Exception e) {
            logger.error("产量预测失败，clusterId: {}, error: {}", request.get("clusterId"), e.getMessage(), e);
            return AjaxResult.error("预测失败: " + e.getMessage());
        }
    }

    /**
     * 病虫害分析
     */
    @PostMapping("/pest-disease-analysis")
    public AjaxResult pestDiseaseAnalysis(@RequestBody Map<String, Object> request) {
        try {
            String clusterId = (String) request.get("clusterId");
            String kbId = (String) request.get("kbId");
            String chatId = (String) request.get("chatId");
            Integer topK = (Integer) request.getOrDefault("topK", 5);

            // 1. 获取病虫害检测数据
            List<EnokimushroomPestDisease> pestDiseases = enokimushroomPestDiseaseService
                    .selectEnokimushroomPestDiseaseByClusterId(clusterId);

            if (pestDiseases.isEmpty()) {
                return AjaxResult.success("该区域暂未检测到病虫害");
            }

            // 2. 获取当天的环境监测数据
            List<MonitorRecord> monitorRecords = getTodayMonitorRecords(clusterId);

            // 3. 构建分析上下文
            String context = buildPestDiseaseAnalysisContext(pestDiseases, monitorRecords);

            // 4. 从内存向量存储检索相关知识
            SearchRequest searchRequest = SearchRequest.builder()
                    .query("金针菇病虫害防治 诊断方法 治疗方案")
                    .topK(topK * 2) // 先召回更多文档
                    .build();
            List<Document> docs = vectorStore.similaritySearch(searchRequest);

            // 5. 根据kbId过滤并限制数量
            List<Document> knowledgeDocs = docs.stream()
                    .filter(doc -> kbId.equals(doc.getMetadata().get("kbId")))
                    .limit(topK)
                    .collect(Collectors.toList());

            // 6. 整合知识库内容到上下文
            if (!knowledgeDocs.isEmpty()) {
                context += "\n\n=== 相关知识库 ===\n";
                String knowledgeContext = knowledgeDocs.stream()
                        .map(Document::getText)
                        .collect(Collectors.joining("\n"));
                context += knowledgeContext;
            }

            // 7. 调用LLM进行分析
            String analysis = diseaseChatClient.prompt()
                    .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, chatId))
                    .call()
                    .content();

            return AjaxResult.success("分析完成", analysis);

        } catch (Exception e) {
            logger.error("病虫害分析失败", e);
            return AjaxResult.error("分析失败: " + e.getMessage());
        }
    }

    /**
     * 综合健康分析
     */
    @PostMapping("/comprehensive-health-analysis")
    public AjaxResult comprehensiveHealthAnalysis(@RequestBody Map<String, Object> request) {
        try {
            String clusterId = (String) request.get("clusterId");
            String kbId = (String) request.get("kbId");
            String chatId = (String) request.get("chatId");
            Integer topK = (Integer) request.getOrDefault("topK", 8);

            // 1. 获取所有相关数据
            List<EnokimushroomCluster> clusters = enokimushroomClusterService.selectEnokimushroomClusterByClusterId(clusterId);
            if (clusters == null || clusters.isEmpty()) {
                return AjaxResult.error("未找到指定的金针菇簇数据");
            }
            List<EnokimushroomPestDisease> pestDiseases = enokimushroomPestDiseaseService
                    .selectEnokimushroomPestDiseaseByClusterId(clusterId);
            List<MonitorRecord> monitorRecords = getTodayMonitorRecords(clusterId);

            // 2. 构建综合分析上下文
            String context = buildComprehensiveAnalysisContext(clusters, pestDiseases, monitorRecords);

            // 打印检索到的金针菇簇数据与上下文
            logger.info("[comprehensive-health-analysis] 检索到的金针菇簇数据 ({} 条)：\n{}", clusters.size(), formatClusterSummary(clusters));
            logger.info("[comprehensive-health-analysis] 发送给LLM的上下文：\n{}", context);

            // 3. 从内存向量存储检索相关知识
            SearchRequest searchRequest = SearchRequest.builder()
                    .query("金针菇种植管理 健康监测 综合防治")
                    .topK(topK * 2) // 先召回更多文档
                    .build();
            List<Document> docs = vectorStore.similaritySearch(searchRequest);

            // 4. 根据kbId过滤并限制数量
            List<Document> knowledgeDocs = docs.stream()
                    .filter(doc -> kbId.equals(doc.getMetadata().get("kbId")))
                    .limit(topK)
                    .collect(Collectors.toList());

            // 5. 整合知识库内容到上下文
            if (!knowledgeDocs.isEmpty()) {
                context += "\n\n=== 相关知识库 ===\n";
                String knowledgeContext = knowledgeDocs.stream()
                        .map(Document::getText)
                        .collect(Collectors.joining("\n"));
                context += knowledgeContext;
            }

            // 6. 调用LLM进行分析
            String analysis = clusterChatClient.prompt()
                    .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, chatId))
                    .call()
                    .content();

            return AjaxResult.success("分析完成", analysis);

        } catch (Exception e) {
            logger.error("综合健康分析失败", e);
            return AjaxResult.error("分析失败: " + e.getMessage());
        }
    }
    /**
     * 获取当天的环境监测数据
     */
    private List<MonitorRecord> getTodayMonitorRecords(String clusterId) {
        try {
            // 构建查询条件 - 使用areaId字段，因为MonitorRecord中没有clusterId
            MonitorRecord queryRecord = new MonitorRecord();

            // 注意：这里需要根据实际业务逻辑来确定areaId和clusterId的对应关系
            // 暂时尝试将clusterId转换为areaId，如果失败则返回空列表
            try {
                Long areaId = Long.valueOf(clusterId);
                queryRecord.setAreaId(areaId);
            } catch (NumberFormatException e) {
                logger.warn("clusterId '{}' 无法转换为areaId，跳过环境数据查询", clusterId);
                return new ArrayList<>();
            }

            // 获取当天的开始和结束时间
        Date today = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(today);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date startOfDay = cal.getTime();

        cal.add(Calendar.DAY_OF_MONTH, 1);
        Date endOfDay = cal.getTime();

            // 查询当天的监测记录
            List<MonitorRecord> records = monitorRecordService.selectMonitorRecordList(queryRecord);

            // 过滤当天的数据，使用createDate字段
            return records.stream()
                .filter(record -> {
                    Date recordTime = record.getCreateDate();
                    return recordTime != null &&
                           recordTime.after(startOfDay) &&
                           recordTime.before(endOfDay);
                })
                .collect(Collectors.toList());

        } catch (Exception e) {
            logger.error("获取监测记录失败", e);
            return new ArrayList<>();
        }
    }

    /**
     * 获取指定金针菇簇的最近N天环境监测数据
     */
    private List<MonitorRecord> getRecentMonitorRecords(String clusterId, int days) {
        try {
            // 构建查询条件 - 使用areaId字段，因为MonitorRecord中没有clusterId
            MonitorRecord queryRecord = new MonitorRecord();

            // 注意：这里需要根据实际业务逻辑来确定areaId和clusterId的对应关系
            // 暂时尝试将clusterId转换为areaId，如果失败则返回空列表
            try {
                Long areaId = Long.valueOf(clusterId);
                queryRecord.setAreaId(areaId);
            } catch (NumberFormatException e) {
                logger.warn("clusterId '{}' 无法转换为areaId，跳过环境数据查询", clusterId);
        return new ArrayList<>();
            }

            // 获取N天前的开始时间
            Date startDate = new Date(System.currentTimeMillis() - days * 24 * 60 * 60 * 1000);

            // 查询N天内的监测记录
            List<MonitorRecord> records = monitorRecordService.selectMonitorRecordList(queryRecord);

            // 过滤N天内的数据，使用createDate字段
            return records.stream()
                .filter(record -> {
                    Date recordTime = record.getCreateDate();
                    return recordTime != null &&
                           recordTime.after(startDate);
                })
                .collect(Collectors.toList());

        } catch (Exception e) {
            logger.error("获取最近监测记录失败", e);
            return new ArrayList<>();
        }
    }

    /**
     * 获取当天的环境监测数据（全局，不按cluster过滤）
     */
    private List<MonitorRecord> getTodayMonitorRecordsGlobal() {
        try {
            // 不设置任何区域/簇过滤条件，取全部记录后按当天过滤
            MonitorRecord queryRecord = new MonitorRecord();

            // 当天起止时间
            Date today = new Date();
            Calendar cal = Calendar.getInstance();
            cal.setTime(today);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            Date startOfDay = cal.getTime();

            cal.add(Calendar.DAY_OF_MONTH, 1);
            Date endOfDay = cal.getTime();

            List<MonitorRecord> records = monitorRecordService.selectMonitorRecordList(queryRecord);
            return records.stream()
                .filter(record -> {
                    Date recordTime = record.getCreateDate();
                    return recordTime != null &&
                           recordTime.after(startOfDay) &&
                           recordTime.before(endOfDay);
                })
                .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("获取当天全局监测记录失败", e);
            return new ArrayList<>();
        }
    }

    /**
     * 构建产量分析上下文
     */
    private String buildYieldAnalysisContext(List<EnokimushroomCluster> clusters, List<MonitorRecord> monitorRecords) {
        StringBuilder context = new StringBuilder();
        context.append("金针菇簇产量数据（共").append(clusters.size()).append("个簇）：\n\n");
        
        // 遍历所有金针菇簇
        for (int i = 0; i < clusters.size(); i++) {
            EnokimushroomCluster cluster = clusters.get(i);
            context.append("=== 金针菇簇 ").append(i + 1).append(" ===\n");
            context.append("- 簇ID: ").append(cluster.getClusterId()).append("\n");
            context.append("- 簇名称: ").append(cluster.getClusterName()).append("\n");
            context.append("- 总数量: ").append(cluster.getTotalCount()).append("\n");
            context.append("- 已成熟: ").append(cluster.getRipeCount()).append("\n");
            context.append("- 半成熟: ").append(cluster.getHalfRipeCount()).append("\n");
            context.append("- 未成熟: ").append(cluster.getUnripeCount()).append("\n");
            context.append("- 平均大小: ").append(cluster.getAvgSizeMm()).append("mm\n");
            context.append("- 位置区域: ").append(cluster.getLocationArea()).append("\n");
            context.append("- 最后更新: ").append(cluster.getLastUpdate()).append("\n\n");
        }

        // 计算汇总数据
        int totalCount = (int) clusters.stream().mapToLong(EnokimushroomCluster::getTotalCount).sum();
        int totalRipe = (int) clusters.stream().mapToLong(EnokimushroomCluster::getRipeCount).sum();
        int totalHalfRipe = (int) clusters.stream().mapToLong(EnokimushroomCluster::getHalfRipeCount).sum();
        int totalUnripe = (int) clusters.stream().mapToLong(EnokimushroomCluster::getUnripeCount).sum();
        double avgSize = clusters.stream().mapToDouble(cluster -> cluster.getAvgSizeMm() != null ? cluster.getAvgSizeMm().doubleValue() : 0.0).average().orElse(0.0);
        
        context.append("=== 汇总数据 ===\n");
        context.append("- 总簇数: ").append(clusters.size()).append("\n");
        context.append("- 总数量: ").append(totalCount).append("\n");
        context.append("- 总已成熟: ").append(totalRipe).append("\n");
        context.append("- 总半成熟: ").append(totalHalfRipe).append("\n");
        context.append("- 总未成熟: ").append(totalUnripe).append("\n");
        context.append("- 平均大小: ").append(String.format("%.2f", avgSize)).append("mm\n\n");

        if (!monitorRecords.isEmpty()) {
            context.append("环境监测数据：\n");
            MonitorRecord latest = monitorRecords.get(0); // 假设按时间排序
            context.append("- 空气温度: ").append(latest.getAirTemperature()).append("°C\n");
            context.append("- 空气湿度: ").append(latest.getAirHumidity()).append("%\n");
            context.append("- 光照强度: ").append(latest.getLightLux()).append("Lux\n");
            context.append("- 土壤温度: ").append(latest.getSoilTemperature()).append("°C\n");
            context.append("- 土壤湿度: ").append(latest.getSoilHumidity()).append("%\n");
            context.append("- 土壤pH值: ").append(latest.getSoilPh()).append("\n");
            context.append("- 氮含量: ").append(latest.getSoilN()).append("mg/kg\n");
            context.append("- 磷含量: ").append(latest.getSoilP()).append("mg/kg\n");
            context.append("- 钾含量: ").append(latest.getSoilK()).append("mg/kg\n");
        }

        return context.toString();
    }

    /**
     * 构建病虫害分析上下文
     */
    private String buildPestDiseaseAnalysisContext(List<EnokimushroomPestDisease> pestDiseases,
                                                  List<MonitorRecord> monitorRecords) {
        StringBuilder context = new StringBuilder();
        context.append("病虫害检测数据：\n");

        for (EnokimushroomPestDisease pd : pestDiseases) {
            context.append("- 检测对象: ").append(pd.getObjectType()).append("\n");
            context.append("- 病虫害类型: ").append(pd.getPestDiseaseClass()).append("\n");
            context.append("- 严重程度: ").append(pd.getSeverityLevel()).append("\n");
            context.append("- 置信度: ").append(pd.getConfidence()).append("\n");
            context.append("- 检测时间: ").append(pd.getDetectedTime()).append("\n");
            context.append("- 建议: ").append(pd.getRecommendation()).append("\n\n");
        }

        if (!monitorRecords.isEmpty()) {
            context.append("环境监测数据：\n");
            MonitorRecord latest = monitorRecords.get(0);
            context.append("- 空气温度: ").append(latest.getAirTemperature()).append("°C\n");
            context.append("- 空气湿度: ").append(latest.getAirHumidity()).append("%\n");
            context.append("- 土壤温度: ").append(latest.getSoilTemperature()).append("°C\n");
            context.append("- 土壤湿度: ").append(latest.getSoilHumidity()).append("%\n");
            context.append("- 土壤pH值: ").append(latest.getSoilPh()).append("\n");
        }

        return context.toString();
    }

    /**
     * 构建综合分析上下文
     */
    private String buildComprehensiveAnalysisContext(List<EnokimushroomCluster> clusters,
                                                   List<EnokimushroomPestDisease> pestDiseases,
                                                   List<MonitorRecord> monitorRecords) {
        StringBuilder context = new StringBuilder();

        // 产量数据
        context.append("=== 产量数据 ===\n");
        context.append(buildYieldAnalysisContext(clusters, new ArrayList<>()));

        // 病虫害数据
        if (!pestDiseases.isEmpty()) {
            context.append("\n=== 病虫害数据 ===\n");
            context.append(buildPestDiseaseAnalysisContext(pestDiseases, new ArrayList<>()));
        }

        // 环境数据
        if (!monitorRecords.isEmpty()) {
            context.append("\n=== 环境监测数据 ===\n");
            MonitorRecord latest = monitorRecords.get(0);
            context.append("- 空气温度: ").append(latest.getAirTemperature()).append("°C\n");
            context.append("- 空气湿度: ").append(latest.getAirHumidity()).append("%\n");
            context.append("- 光照强度: ").append(latest.getLightLux()).append("Lux\n");
            context.append("- 二氧化碳: ").append(latest.getAirCo2()).append("ppm\n");
            context.append("- 土壤温度: ").append(latest.getSoilTemperature()).append("°C\n");
            context.append("- 土壤湿度: ").append(latest.getSoilHumidity()).append("%\n");
            context.append("- 土壤电导率: ").append(latest.getSoilConductivity()).append("us/cm\n");
            context.append("- 土壤pH值: ").append(latest.getSoilPh()).append("\n");
            context.append("- 氮含量: ").append(latest.getSoilN()).append("mg/kg\n");
            context.append("- 磷含量: ").append(latest.getSoilP()).append("mg/kg\n");
            context.append("- 钾含量: ").append(latest.getSoilK()).append("mg/kg\n");
        }

        return context.toString();
    }



    /**
     * 构建产量预测上下文
     */
    private String buildYieldPredictionContext(List<EnokimushroomCluster> clusters, List<MonitorRecord> monitorRecords) {
        StringBuilder context = new StringBuilder();
        context.append("金针菇簇当前状态（共").append(clusters.size()).append("个簇）：\n\n");
        
        // 遍历所有金针菇簇
        for (int i = 0; i < clusters.size(); i++) {
            EnokimushroomCluster cluster = clusters.get(i);
            context.append("=== 金针菇簇 ").append(i + 1).append(" ===\n");
            context.append("- 簇ID: ").append(cluster.getClusterId()).append("\n");
            context.append("- 簇名称: ").append(cluster.getClusterName()).append("\n");
            context.append("- 总数量: ").append(cluster.getTotalCount()).append("\n");
            context.append("- 已成熟: ").append(cluster.getRipeCount()).append("\n");
            context.append("- 半成熟: ").append(cluster.getHalfRipeCount()).append("\n");
            context.append("- 未成熟: ").append(cluster.getUnripeCount()).append("\n");
            context.append("- 平均大小: ").append(cluster.getAvgSizeMm()).append("mm\n");
            context.append("- 位置区域: ").append(cluster.getLocationArea()).append("\n");
            context.append("- 最后更新: ").append(cluster.getLastUpdate()).append("\n\n");
        }

        // 计算汇总数据
        int totalCount = (int) clusters.stream().mapToLong(EnokimushroomCluster::getTotalCount).sum();
        int totalRipe = (int) clusters.stream().mapToLong(EnokimushroomCluster::getRipeCount).sum();
        int totalHalfRipe = (int) clusters.stream().mapToLong(EnokimushroomCluster::getHalfRipeCount).sum();
        int totalUnripe = (int) clusters.stream().mapToLong(EnokimushroomCluster::getUnripeCount).sum();
        double avgSize = clusters.stream().mapToDouble(cluster -> cluster.getAvgSizeMm() != null ? cluster.getAvgSizeMm().doubleValue() : 0.0).average().orElse(0.0);
        
        context.append("=== 汇总数据 ===\n");
        context.append("- 总簇数: ").append(clusters.size()).append("\n");
        context.append("- 总数量: ").append(totalCount).append("\n");
        context.append("- 总已成熟: ").append(totalRipe).append("\n");
        context.append("- 总半成熟: ").append(totalHalfRipe).append("\n");
        context.append("- 总未成熟: ").append(totalUnripe).append("\n");
        context.append("- 平均大小: ").append(String.format("%.2f", avgSize)).append("mm\n\n");

        if (!monitorRecords.isEmpty()) {
            context.append("环境监测数据：\n");
            MonitorRecord latest = monitorRecords.get(0);
            context.append("- 空气温度: ").append(latest.getAirTemperature()).append("°C\n");
            context.append("- 空气湿度: ").append(latest.getAirHumidity()).append("%\n");
            context.append("- 光照强度: ").append(latest.getLightLux()).append("Lux\n");
            context.append("- 土壤温度: ").append(latest.getSoilTemperature()).append("°C\n");
            context.append("- 土壤湿度: ").append(latest.getSoilHumidity()).append("%\n");
            context.append("- 土壤pH值: ").append(latest.getSoilPh()).append("\n");
            context.append("- 氮含量: ").append(latest.getSoilN()).append("mg/kg\n");
            context.append("- 磷含量: ").append(latest.getSoilP()).append("mg/kg\n");
            context.append("- 钾含量: ").append(latest.getSoilK()).append("mg/kg\n");
        }

        return context.toString();
    }



    /**
     * 构建完整的产量预测上下文
     */
    private String buildCompleteYieldPredictionContext(List<EnokimushroomCluster> clusters,
                                                      List<MonitorRecord> monitorRecords,
                                                      List<EnokimushroomPestDisease> pestDiseases) {
        StringBuilder context = new StringBuilder();
        context.append("金针菇簇当前状态：\n");
        context.append(buildYieldPredictionContext(clusters, monitorRecords));

        if (!pestDiseases.isEmpty()) {
            context.append("\n病虫害历史数据：\n");
            for (EnokimushroomPestDisease pd : pestDiseases) {
                context.append("- 检测对象: ").append(pd.getObjectType()).append("\n");
                context.append("- 病虫害类型: ").append(pd.getPestDiseaseClass()).append("\n");
                context.append("- 严重程度: ").append(pd.getSeverityLevel()).append("\n");
                context.append("- 置信度: ").append(pd.getConfidence()).append("\n");
                context.append("- 检测时间: ").append(pd.getDetectedTime()).append("\n");
                context.append("- 建议: ").append(pd.getRecommendation()).append("\n\n");
            }
        }

        return context.toString();
    }


    /**
     * 将金针菇簇关键信息格式化为便于阅读的日志字符串
     */
    private String formatClusterSummary(List<EnokimushroomCluster> clusters) {
        if (clusters == null || clusters.isEmpty()) {
            return "<空>";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < clusters.size(); i++) {
            EnokimushroomCluster c = clusters.get(i);
            sb.append("#").append(i + 1).append(" ")
              .append("[簇ID=").append(c.getClusterId()).append(", 簇名称=").append(c.getClusterName()).append("]\n")
              .append("  总数量=").append(c.getTotalCount())
              .append(", 已成熟=").append(c.getRipeCount())
              .append(", 半成熟=").append(c.getHalfRipeCount())
              .append(", 未成熟=").append(c.getUnripeCount()).append("\n")
              .append("  平均大小=").append(c.getAvgSizeMm()).append("mm, 位置区域=").append(c.getLocationArea())
              .append(", 最后更新=").append(c.getLastUpdate()).append("\n");
        }
        return sb.toString();
    }

}
