package com.enokimushroom.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

/**
 * YOLO 本地模型识别服务
 * 调用 Python Flask YOLO 服务进行病虫害检测
 */
@Service
public class YoloDetectService {

    private static final Logger log = LoggerFactory.getLogger(YoloDetectService.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${yolo.service.url:http://localhost:5000}")
    private String yoloServiceUrl;

    @Value("${yolo.service.timeout:30000}")
    private int timeout;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 健康检查
     */
    public boolean isHealthy() {
        try {
            ResponseEntity<String> resp = restTemplate.getForEntity(
                    yoloServiceUrl + "/api/yolo/health", String.class);
            return resp.getStatusCode() == HttpStatus.OK;
        } catch (Exception e) {
            log.warn("YOLO 服务不可用: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 通过文件上传检测
     */
    public Map<String, Object> detectByFile(MultipartFile file) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", file.getResource());

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    yoloServiceUrl + "/api/yolo/detect",
                    requestEntity,
                    String.class
            );

            return parseResponse(response.getBody());
        } catch (Exception e) {
            log.error("YOLO 文件检测失败: {}", e.getMessage(), e);
            throw new RuntimeException("YOLO 检测服务调用失败: " + e.getMessage());
        }
    }

    /**
     * 通过 Base64 检测
     */
    public Map<String, Object> detectByBase64(String base64Image) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, String> body = new HashMap<>();
            body.put("image", base64Image);

            HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    yoloServiceUrl + "/api/yolo/detect/base64",
                    requestEntity,
                    String.class
            );

            return parseResponse(response.getBody());
        } catch (Exception e) {
            log.error("YOLO Base64 检测失败: {}", e.getMessage(), e);
            throw new RuntimeException("YOLO 检测服务调用失败: " + e.getMessage());
        }
    }

    /**
     * 获取检测结果图片 URL
     */
    public String getResultImageUrl(String resultImagePath) {
        if (resultImagePath == null) return null;
        return yoloServiceUrl + resultImagePath;
    }

    /**
     * 解析 YOLO 服务响应
     */
    private Map<String, Object> parseResponse(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            int code = root.get("code").asInt();

            if (code != 200) {
                String message = root.has("message") ? root.get("message").asText() : "未知错误";
                throw new RuntimeException("YOLO 服务返回错误: " + message);
            }

            JsonNode data = root.get("data");
            Map<String, Object> result = new HashMap<>();
            result.put("count", data.get("count").asInt());
            result.put("elapsed_ms", data.get("elapsed_ms").asDouble());

            if (data.has("result_image")) {
                result.put("result_image", data.get("result_image").asText());
            }

            List<Map<String, Object>> detections = new ArrayList<>();
            if (data.has("detections")) {
                for (JsonNode det : data.get("detections")) {
                    Map<String, Object> detection = new HashMap<>();
                    detection.put("class_id", det.get("class_id").asInt());
                    detection.put("confidence", det.get("confidence").asDouble());
                    detection.put("severity", det.get("severity").asText());
                    detection.put("area_ratio", det.get("area_ratio").asDouble());

                    if (det.has("class_name")) {
                        detection.put("class_name", det.get("class_name").asText());
                    }
                    if (det.has("severity_cn")) {
                        detection.put("severity_cn", det.get("severity_cn").asText());
                    }
                    if (det.has("recommendation")) {
                        detection.put("recommendation", det.get("recommendation").asText());
                    }
                    if (det.has("bbox")) {
                        List<Double> bbox = new ArrayList<>();
                        for (JsonNode v : det.get("bbox")) {
                            bbox.add(v.asDouble());
                        }
                        detection.put("bbox", bbox);
                    }

                    detections.add(detection);
                }
            }
            result.put("detections", detections);

            return result;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("解析 YOLO 响应失败: " + e.getMessage());
        }
    }
}
