"""
YOLOv8 金针菇病虫害训练脚本
支持多种模型规模和训练参数配置
"""

import os
import yaml
import argparse
from pathlib import Path
from datetime import datetime
from ultralytics import YOLO


# 默认训练配置
DEFAULT_CONFIG = {
    'model': 'yolov8n.pt',      # 预训练模型
    'data': 'config.yaml',       # 数据集配置
    'epochs': 100,               # 训练轮数
    'imgsz': 640,                # 输入图片尺寸
    'batch': 16,                 # 批次大小
    'device': '0',               # GPU 编号
    'workers': 8,                # 数据加载线程数
    'patience': 50,              # 早停耐心值
    'save': True,                # 保存模型
    'save_period': 10,           # 每 N 个 epoch 保存一次
    'project': 'runs/detect',    # 项目目录
    'name': 'train',             # 实验名称
    'exist_ok': True,            # 覆盖已有实验
    'pretrained': True,          # 使用预训练权重
    'optimizer': 'AdamW',        # 优化器
    'lr0': 0.001,                # 初始学习率
    'lrf': 0.01,                 # 最终学习率比例
    'momentum': 0.937,           # SGD 动量
    'weight_decay': 0.0005,      # 权重衰减
    'warmup_epochs': 3,          # 预热轮数
    'warmup_momentum': 0.8,      # 预热动量
    'warmup_bias_lr': 0.1,       # 预热偏置学习率
    'augment': True,             # 启用数据增强
    'hsv_h': 0.015,              # HSV 色调增强
    'hsv_s': 0.7,                # HSV 饱和度增强
    'hsv_v': 0.4,                # HSV 明度增强
    'degrees': 0.0,              # 旋转角度
    'translate': 0.1,            # 平移
    'scale': 0.5,                # 缩放
    'shear': 0.0,                # 剪切
    'perspective': 0.0,          # 透视
    'flipud': 0.0,               # 上下翻转概率
    'fliplr': 0.5,               # 左右翻转概率
    'mosaic': 1.0,               # Mosaic 增强概率
    'mixup': 0.0,                # MixUp 增强概率
    'copy_paste': 0.0,           # 复制粘贴增强概率
}


def load_config(config_path):
    """加载训练配置"""
    if os.path.exists(config_path):
        with open(config_path, 'r') as f:
            user_config = yaml.safe_load(f)
            if user_config:
                DEFAULT_CONFIG.update(user_config)
    return DEFAULT_CONFIG


def train(config):
    """
    执行训练

    Args:
        config: 训练配置字典
    """
    print("=" * 60)
    print("🍄 金针菇病虫害 YOLOv8 训练")
    print("=" * 60)

    # 显示配置
    print("\n训练配置:")
    for key, value in config.items():
        print(f"  {key}: {value}")

    # 加载模型
    model_path = config['model']
    print(f"\n加载模型: {model_path}")
    model = YOLO(model_path)

    # 开始训练
    print("\n开始训练...")
    results = model.train(
        data=config['data'],
        epochs=config['epochs'],
        imgsz=config['imgsz'],
        batch=config['batch'],
        device=config['device'],
        workers=config['workers'],
        patience=config['patience'],
        save=config['save'],
        save_period=config['save_period'],
        project=config['project'],
        name=config['name'],
        exist_ok=config['exist_ok'],
        pretrained=config['pretrained'],
        optimizer=config['optimizer'],
        lr0=config['lr0'],
        lrf=config['lrf'],
        momentum=config['momentum'],
        weight_decay=config['weight_decay'],
        warmup_epochs=config['warmup_epochs'],
        warmup_momentum=config['warmup_momentum'],
        warmup_bias_lr=config['warmup_bias_lr'],
        augment=config['augment'],
        hsv_h=config['hsv_h'],
        hsv_s=config['hsv_s'],
        hsv_v=config['hsv_v'],
        degrees=config['degrees'],
        translate=config['translate'],
        scale=config['scale'],
        shear=config['shear'],
        perspective=config['perspective'],
        flipud=config['flipud'],
        fliplr=config['fliplr'],
        mosaic=config['mosaic'],
        mixup=config['mixup'],
        copy_paste=config['copy_paste'],
    )

    print("\n训练完成！")
    print(f"最佳模型: {config['project']}/{config['name']}/weights/best.pt")
    print(f"最新模型: {config['project']}/{config['name']}/weights/last.pt")

    return results


def resume_train(config, weights_path):
    """
    恢复训练

    Args:
        config: 训练配置
        weights_path: 权重文件路径
    """
    print(f"从 {weights_path} 恢复训练...")
    model = YOLO(weights_path)
    results = model.train(resume=True)
    return results


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description='YOLOv8 金针菇病虫害训练')
    parser.add_argument('--config', default='train_config.yaml', help='训练配置文件')
    parser.add_argument('--model', default=None, help='预训练模型路径')
    parser.add_argument('--data', default=None, help='数据集配置文件')
    parser.add_argument('--epochs', type=int, default=None, help='训练轮数')
    parser.add_argument('--batch', type=int, default=None, help='批次大小')
    parser.add_argument('--imgsz', type=int, default=None, help='输入图片尺寸')
    parser.add_argument('--device', default=None, help='GPU 编号')
    parser.add_argument('--resume', default=None, help='恢复训练的权重路径')
    parser.add_argument('--name', default=None, help='实验名称')

    args = parser.parse_args()

    # 加载配置
    config = load_config(args.config)

    # 命令行参数覆盖
    if args.model:
        config['model'] = args.model
    if args.data:
        config['data'] = args.data
    if args.epochs:
        config['epochs'] = args.epochs
    if args.batch:
        config['batch'] = args.batch
    if args.imgsz:
        config['imgsz'] = args.imgsz
    if args.device:
        config['device'] = args.device
    if args.name:
        config['name'] = args.name

    # 添加时间戳到实验名称
    timestamp = datetime.now().strftime('%Y%m%d_%H%M%S')
    config['name'] = f"{config['name']}_{timestamp}"

    # 执行训练
    if args.resume:
        resume_train(config, args.resume)
    else:
        train(config)
