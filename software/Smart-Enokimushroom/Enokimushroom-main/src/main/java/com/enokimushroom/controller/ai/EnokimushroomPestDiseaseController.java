package com.enokimushroom.controller.ai;

import com.enokimushroom.common.annotation.Log;
import com.enokimushroom.common.core.controller.BaseController;
import com.enokimushroom.common.core.domain.AjaxResult;
import com.enokimushroom.common.core.page.TableDataInfo;
import com.enokimushroom.common.enums.BusinessType;
import com.enokimushroom.common.utils.poi.ExcelUtil;

import com.enokimushroom.entity.EnokimushroomPestDisease;
import com.enokimushroom.service.IEnokimushroomPestDiseaseService;
import com.enokimushroom.service.YoloDetectService;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.*;

/**
 * 病虫害分析Controller - 集成 YOLO 本地识别
 *
 * @author yejianfeng
 * @date 2025-08-18
 */
@RestController
@RequestMapping("/system/disease")
public class EnokimushroomPestDiseaseController extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(EnokimushroomPestDiseaseController.class);

    @Autowired
    private IEnokimushroomPestDiseaseService enokimushroomPestDiseaseService;

    @Autowired
    private YoloDetectService yoloDetectService;

    // ==================== YOLO 识别接口 ====================

    /**
     * YOLO 服务健康检查
     */
    @GetMapping("/yolo/health")
    public AjaxResult health() {
        boolean healthy = yoloDetectService.isHealthy();
        return success().put("healthy", healthy);
    }

    /**
     * 上传图片进行 YOLO 识别
     */
    @PostMapping("/detect")
    public AjaxResult detectByFile(@RequestParam("file") MultipartFile file) {
        try {
            // 调用 YOLO 服务
            Map<String, Object> result = yoloDetectService.detectByFile(file);

            // 构建结果图片 URL
            String resultImage = (String) result.get("result_image");
            if (resultImage != null) {
                result.put("result_image_url", yoloDetectService.getResultImageUrl(resultImage));
            }

            // 自动保存识别结果到数据库
            List<Map<String, Object>> detections = (List<Map<String, Object>>) result.get("detections");
            saveDetections(detections);

            return success().put("data", result);
        } catch (Exception e) {
            log.error("YOLO 识别失败: {}", e.getMessage(), e);
            return error("识别失败: " + e.getMessage());
        }
    }

    /**
     * Base64 图片进行 YOLO 识别
     */
    @PostMapping("/detect/base64")
    public AjaxResult detectByBase64(@RequestBody Map<String, String> body) {
        String base64Image = body.get("image");
        if (base64Image == null || base64Image.isEmpty()) {
            return error("缺少图片数据");
        }

        try {
            Map<String, Object> result = yoloDetectService.detectByBase64(base64Image);

            // 自动保存识别结果到数据库
            List<Map<String, Object>> detections = (List<Map<String, Object>>) result.get("detections");
            saveDetections(detections);

            return success().put("data", result);
        } catch (Exception e) {
            log.error("YOLO Base64 识别失败: {}", e.getMessage(), e);
            return error("识别失败: " + e.getMessage());
        }
    }

    /**
     * 保存检测结果到数据库
     */
    private void saveDetections(List<Map<String, Object>> detections) {
        if (detections == null || detections.isEmpty()) return;

        for (Map<String, Object> det : detections) {
            try {
                EnokimushroomPestDisease disease = new EnokimushroomPestDisease();

                // 设置病虫害类别
                String className = (String) det.getOrDefault("class_name", "未知");
                disease.setPestDiseaseClass(className);

                // 设置置信度
                double confidence = (double) det.getOrDefault("confidence", 0.0);
                disease.setConfidence(BigDecimal.valueOf(confidence));

                // 设置严重程度
                String severity = (String) det.getOrDefault("severity", "low");
                disease.setSeverityLevel(severity);

                // 设置防治建议
                String recommendation = (String) det.getOrDefault("recommendation", "");
                disease.setRecommendation(recommendation);

                // 设置边界框
                List<Double> bbox = (List<Double>) det.getOrDefault("bbox", new ArrayList<>());
                if (bbox.size() == 4) {
                    disease.setBboxXMin(BigDecimal.valueOf(bbox.get(0)));
                    disease.setBboxYMin(BigDecimal.valueOf(bbox.get(1)));
                    disease.setBboxXMax(BigDecimal.valueOf(bbox.get(2)));
                    disease.setBboxYMax(BigDecimal.valueOf(bbox.get(3)));
                }

                // 设置检测时间
                disease.setDetectedTime(new Date());

                // 设置对象类型（根据 class_id 推断）
                int classId = (int) det.getOrDefault("class_id", -1);
                disease.setObjectType(classId >= 0 ? "金针菇" : "未知");

                // 保存到数据库
                enokimushroomPestDiseaseService.insertEnokimushroomPestDisease(disease);
                log.info("已保存检测结果: {} 置信度: {}", className, confidence);
            } catch (Exception e) {
                log.error("保存检测结果失败: {}", e.getMessage());
            }
        }
    }

    // ==================== 原有 CRUD 接口 ====================

    /**
     * 查询病虫害分析列表
     */
    @GetMapping("/list")
    public TableDataInfo list(EnokimushroomPestDisease enokimushroomPestDisease) {
        startPage();
        List<EnokimushroomPestDisease> list = enokimushroomPestDiseaseService.selectEnokimushroomPestDiseaseList(enokimushroomPestDisease);
        return getDataTable(list);
    }

    /**
     * 导出病虫害分析列表
     */
    @PreAuthorize("@ss.hasPermi('system:disease:export')")
    @Log(title = "病虫害分析", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, EnokimushroomPestDisease enokimushroomPestDisease) {
        List<EnokimushroomPestDisease> list = enokimushroomPestDiseaseService.selectEnokimushroomPestDiseaseList(enokimushroomPestDisease);
        ExcelUtil<EnokimushroomPestDisease> util = new ExcelUtil<EnokimushroomPestDisease>(EnokimushroomPestDisease.class);
        util.exportExcel(response, list, "病虫害分析数据");
    }

    /**
     * 获取病虫害分析详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:disease:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(enokimushroomPestDiseaseService.selectEnokimushroomPestDiseaseById(id));
    }

    /**
     * 新增病虫害分析
     */
    @PostMapping
    public AjaxResult add(@RequestBody EnokimushroomPestDisease enokimushroomPestDisease) {
        return toAjax(enokimushroomPestDiseaseService.insertEnokimushroomPestDisease(enokimushroomPestDisease));
    }

    /**
     * 修改病虫害分析
     */
    @PreAuthorize("@ss.hasPermi('system:disease:edit')")
    @Log(title = "病虫害分析", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody EnokimushroomPestDisease enokimushroomPestDisease) {
        return toAjax(enokimushroomPestDiseaseService.updateEnokimushroomPestDisease(enokimushroomPestDisease));
    }

    /**
     * 删除病虫害分析
     */
    @PreAuthorize("@ss.hasPermi('system:disease:remove')")
    @Log(title = "病虫害分析", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(enokimushroomPestDiseaseService.deleteEnokimushroomPestDiseaseByIds(ids));
    }
}
