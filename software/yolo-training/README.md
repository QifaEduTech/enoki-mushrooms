# 🍄 金针菇病虫害 YOLOv8 训练项目

基于 YOLOv8 的金针菇病虫害目标检测模型训练项目，支持从数据准备到模型部署的完整流程。

## 📋 目录

- [环境要求](#环境要求)
- [项目结构](#项目结构)
- [快速开始](#快速开始)
- [数据准备](#数据准备)
- [模型训练](#模型训练)
- [模型评估](#模型评估)
- [模型导出](#模型导出)
- [常见问题](#常见问题)

## 环境要求

- Python >= 3.8
- PyTorch >= 2.0
- CUDA >= 11.7（推荐使用 GPU 训练）
- ultralytics >= 8.0

### 安装依赖

```bash
pip install -r requirements.txt
```

### GPU 环境检查

```python
import torch
print(f"PyTorch: {torch.__version__}")
print(f"CUDA available: {torch.cuda.is_available()}")
print(f"GPU: {torch.cuda.get_device_name(0) if torch.cuda.is_available() else 'N/A'}")
```

## 项目结构

```
yolo-training/
├── README.md                    # 本说明文档
├── requirements.txt             # Python 依赖
├── config.yaml                  # 数据集配置
├── train_config.yaml            # 训练参数配置
├── data/                        # 数据集目录
│   ├── train/
│   │   ├── images/             # 训练图片
│   │   └── labels/             # 训练标签（YOLO 格式）
│   ├── val/
│   │   ├── images/             # 验证图片
│   │   └── labels/             # 验证标签
│   ├── test/
│   │   ├── images/             # 测试图片
│   │   └── labels/             # 测试标签
│   └── classes.txt             # 类别名称文件
├── scripts/
│   ├── prepare_data.py         # 数据预处理脚本
│   ├── train.py                # 训练脚本
│   ├── evaluate.py             # 评估脚本
│   ├── export_model.py         # 模型导出脚本
│   └── visualize.py            # 训练可视化脚本
├── models/                     # 预训练模型存放
├── runs/                       # 训练运行记录
│   └── detect/
│       └── train_YYYYMMDD_HHMMSS/
│           ├── weights/
│           │   ├── best.pt    # 最佳模型
│           │   └── last.pt    # 最新模型
│           ├── results.csv    # 训练指标
│           ├── confusion_matrix.png
│           └── results.png    # 训练曲线
├── exports/                    # 导出的模型
└── notebooks/                  # Jupyter notebooks
```

## 数据集类别

| 类别ID | 英文名 | 中文名 | 说明 |
|--------|--------|--------|------|
| 0 | powdery_mildew | 金针菇白粉病 | 叶片白色粉状霉层 |
| 1 | gray_mold | 金针菇灰霉病 | 果实灰色霉变 |
| 2 | anthracnose | 金针菇炭疽病 | 叶片/果实黑色病斑 |
| 3 | fusarium_wilt | 金针菇枯萎病 | 植株枯萎 |
| 4 | leaf_spot | 金针菇叶斑病 | 叶片褐色斑点 |
| 5 | spider_mite | 金针菇红蜘蛛 | 叶片背面红蜘蛛 |
| 6 | aphid | 金针菇蚜虫 | 蚜虫聚集 |
| 7 | healthy | 健康金针菇 | 无病害 |

## 快速开始

### 1. 准备数据

将你的数据集按以下格式组织：

```
your_dataset/
├── images/
│   ├── img001.jpg
│   ├── img002.jpg
│   └── ...
└── labels/
    ├── img001.txt    # 与图片同名的标注文件
    ├── img002.txt
    └── ...
```

**标注文件格式**（每行一个目标）：
```
class_id x_center y_center width height
```
所有坐标值需要归一化到 0-1 范围。

### 2. 划分数据集

```bash
python scripts/prepare_data.py split \
    --source your_dataset \
    --output data \
    --train 0.7 \
    --val 0.2 \
    --test 0.1
```

### 3. 开始训练

```bash
# 使用默认配置训练
python scripts/train.py

# 指定参数训练
python scripts/train.py \
    --model yolov8n.pt \
    --epochs 100 \
    --batch 16 \
    --device 0

# 使用配置文件
python scripts/train.py --config train_config.yaml
```

### 4. 评估模型

```bash
python scripts/evaluate.py val \
    --model runs/detect/train_YYYYMMDD_HHMMSS/weights/best.pt \
    --data config.yaml \
    --split test
```

### 5. 导出模型

```bash
# 导出为 ONNX（推荐）
python scripts/export_model.py \
    --model runs/detect/train_YYYYMMDD_HHMMSS/weights/best.pt \
    --format onnx \
    --copy

# 导出为多种格式
python scripts/export_model.py \
    --model runs/detect/train_YYYYMMDD_HHMMSS/weights/best.pt \
    --format all
```

## 数据准备

### 标注工具推荐

- [LabelImg](https://github.com/heartexlabs/labelImg) - 简单易用
- [CVAT](https://github.com/openvinotoolkit/cvat) - 功能强大
- [Roboflow](https://roboflow.com/) - 在线标注 + 数据增强

### 标注格式转换

```bash
# VOC 转 YOLO
python scripts/prepare_data.py voc2yolo \
    --voc-dir /path/to/voc \
    --output data

# COCO 转 YOLO
python scripts/prepare_data.py coco2yolo \
    --coco-json /path/to/annotations.json \
    --output data
```

### 数据统计

```bash
python scripts/prepare_data.py analyze --data data
```

## 模型训练

### 模型选择建议

| 模型 | 参数量 | 速度 | 精度 | 适用场景 |
|------|--------|------|------|----------|
| yolov8n | 3.2M | ⚡⚡⚡ | ★★ | 边缘设备、实时检测 |
| yolov8s | 11.2M | ⚡⚡ | ★★★ | 移动端、轻量级部署 |
| yolov8m | 25.9M | ⚡ | ★★★★ | 服务器部署 |
| yolov8l | 43.7M | 🐢 | ★★★★★ | 高精度需求 |
| yolov8x | 68.2M | 🐢🐢 | ★★★★★ | 最高精度 |

### 训练参数调优

**如果过拟合**（训练损失下降但验证损失上升）：
- 增加数据增强强度
- 减小模型复杂度
- 增加 dropout
- 使用更多训练数据

**如果欠拟合**（训练损失和验证损失都很高）：
- 增加训练轮数
- 增大模型复杂度
- 增大学习率
- 检查数据质量

### 恢复训练

```bash
python scripts/train.py --resume runs/detect/train_YYYYMMDD_HHMMSS/weights/last.pt
```

## 模型评估

### 评估指标说明

- **mAP50**: IoU=0.5 时的平均精度
- **mAP50-95**: IoU=0.5:0.95 的平均精度（主要指标）
- **Precision**: 精确率
- **Recall**: 召回率

### 速度基准测试

```bash
python scripts/evaluate.py benchmark \
    --model runs/detect/train_YYYYMMDD_HHMMSS/weights/best.pt \
    --device 0
```

## 模型导出

### 导出格式

| 格式 | 后缀 | 说明 | 适用场景 |
|------|------|------|----------|
| ONNX | .onnx | 通用格式 | 跨平台部署 |
| TorchScript | .torchscript | PyTorch 原生 | PyTorch 部署 |
| TensorRT | .engine | NVIDIA 优化 | NVIDIA GPU |
| OpenVINO | _openvino_model | Intel 优化 | Intel CPU |

### 部署到 YOLO 服务

导出 ONNX 后，使用 `--copy` 参数会自动复制到 `yolo-service/models/` 目录：

```bash
python scripts/export_model.py \
    --model runs/detect/train_YYYYMMDD_HHMMSS/weights/best.pt \
    --format onnx \
    --copy
```

## 训练可视化

```bash
# 绘制训练曲线
python scripts/visualize.py --csv runs/detect/train_YYYYMMDD_HHMMSS/results.csv

# 生成训练报告
python scripts/visualize.py --csv runs/detect/train_YYYYMMDD_HHMMSS/results.csv --report
```

## 常见问题

### Q: GPU 显存不足 (Out of Memory)

```bash
# 减小批次大小
python scripts/train.py --batch 8

# 或者使用更小的模型
python scripts/train.py --model yolov8n.pt
```

### Q: 训练速度太慢

1. 确保使用 GPU 训练
2. 减小输入图片尺寸：`--imgsz 416`
3. 减少数据加载线程：`--workers 4`

### Q: 如何使用自己的数据集

1. 准备图片和 YOLO 格式标注
2. 修改 `config.yaml` 中的类别名称
3. 运行数据划分脚本
4. 开始训练

### Q: 如何提高模型精度

1. 增加训练数据量
2. 使用更大的模型（yolov8s → yolov8m）
3. 增加训练轮数
4. 使用数据增强
5. 确保标注质量

## 参考资料

- [Ultralytics YOLOv8 文档](https://docs.ultralytics.com/)
- [YOLOv8 GitHub](https://github.com/ultralytics/ultralytics)
- [金针菇病虫害数据集](https://universe.roboflow.com/)

## 许可证

本项目仅用于学习和研究目的。
