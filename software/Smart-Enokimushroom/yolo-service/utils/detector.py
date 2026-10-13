import os
import cv2
import numpy as np
from ultralytics import YOLO
from PIL import Image
import base64
import io
import time

class YOLODetector:
    """YOLOv8 病虫害检测器"""

    def __init__(self, model_path, conf_threshold=0.25, iou_threshold=0.45, img_size=640):
        self.model_path = model_path
        self.conf_threshold = conf_threshold
        self.iou_threshold = iou_threshold
        self.img_size = img_size
        self.model = None
        self._load_model()

    def _load_model(self):
        """加载 YOLO 模型"""
        if not os.path.exists(self.model_path):
            raise FileNotFoundError(f"模型文件不存在: {self.model_path}")
        print(f"正在加载模型: {self.model_path}")
        self.model = YOLO(self.model_path)
        print("模型加载完成")

    def detect_image(self, image_path):
        """检测图片文件"""
        if not os.path.exists(image_path):
            raise FileNotFoundError(f"图片文件不存在: {image_path}")

        start_time = time.time()
        results = self.model.predict(
            source=image_path,
            conf=self.conf_threshold,
            iou=self.iou_threshold,
            imgsz=self.img_size,
            verbose=False
        )
        elapsed = time.time() - start_time

        return self._parse_results(results, elapsed)

    def detect_numpy(self, image_np):
        """检测 numpy 数组格式的图片"""
        start_time = time.time()
        results = self.model.predict(
            source=image_np,
            conf=self.conf_threshold,
            iou=self.iou_threshold,
            imgsz=self.img_size,
            verbose=False
        )
        elapsed = time.time() - start_time

        return self._parse_results(results, elapsed)

    def detect_base64(self, base64_str):
        """检测 Base64 编码的图片"""
        # 解码 Base64
        if ',' in base64_str:
            base64_str = base64_str.split(',')[1]

        image_bytes = base64.b64decode(base64_str)
        image = Image.open(io.BytesIO(image_bytes))
        image_np = np.array(image)

        # RGB to BGR (OpenCV 格式)
        if len(image_np.shape) == 3 and image_np.shape[2] == 3:
            image_np = cv2.cvtColor(image_np, cv2.COLOR_RGB2BGR)

        return self.detect_numpy(image_np)

    def _parse_results(self, results, elapsed):
        """解析 YOLO 检测结果"""
        detections = []

        for result in results:
            boxes = result.boxes
            if boxes is None:
                continue

            for i in range(len(boxes)):
                box = boxes[i]
                x1, y1, x2, y2 = box.xyxy[0].cpu().numpy()
                confidence = float(box.conf[0].cpu().numpy())
                class_id = int(box.cls[0].cpu().numpy())

                # 计算面积占比
                img_area = result.orig_shape[0] * result.orig_shape[1]
                box_area = (x2 - x1) * (y2 - y1)
                area_ratio = box_area / img_area

                # 根据面积和置信度判断严重程度
                severity = self._calc_severity(confidence, area_ratio)

                detection = {
                    'class_id': class_id,
                    'confidence': round(confidence, 4),
                    'bbox': [round(float(x1), 2), round(float(y1), 2),
                             round(float(x2), 2), round(float(y2), 2)],
                    'area_ratio': round(area_ratio, 4),
                    'severity': severity,
                }
                detections.append(detection)

        return {
            'detections': detections,
            'count': len(detections),
            'elapsed_ms': round(elapsed * 1000, 2),
        }

    def _calc_severity(self, confidence, area_ratio):
        """根据置信度和面积占比计算严重程度"""
        score = confidence * 0.6 + area_ratio * 0.4
        if score >= 0.6:
            return 'high'
        elif score >= 0.3:
            return 'medium'
        else:
            return 'low'

    def draw_results(self, image_path, results, output_path):
        """在图片上绘制检测结果框"""
        image = cv2.imread(image_path)
        if image is None:
            return None

        for det in results['detections']:
            x1, y1, x2, y2 = [int(v) for v in det['bbox']]
            conf = det['confidence']
            class_id = det['class_id']
            severity = det['severity']

            # 颜色映射
            color_map = {
                'low': (0, 255, 0),      # 绿色
                'medium': (0, 165, 255),  # 橙色
                'high': (0, 0, 255),      # 红色
            }
            color = color_map.get(severity, (255, 255, 255))

            # 绘制边框
            cv2.rectangle(image, (x1, y1), (x2, y2), color, 2)

            # 绘制标签
            label = f"ID:{class_id} {conf:.2f} {severity}"
            label_size, _ = cv2.getTextSize(label, cv2.FONT_HERSHEY_SIMPLEX, 0.6, 2)
            cv2.rectangle(image, (x1, y1 - label_size[1] - 10),
                         (x1 + label_size[0], y1), color, -1)
            cv2.putText(image, label, (x1, y1 - 5),
                       cv2.FONT_HERSHEY_SIMPLEX, 0.6, (255, 255, 255), 2)

        cv2.imwrite(output_path, image)
        return output_path
