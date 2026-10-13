"""
快速开始脚本
一键完成数据准备、训练、评估、导出的完整流程
"""

import os
import sys
import argparse
from pathlib import Path

# 添加 scripts 目录到路径
sys.path.insert(0, str(Path(__file__).parent))

from prepare_data import split_dataset, analyze_dataset
from train import train, DEFAULT_CONFIG
from evaluate import evaluate
from export_model import export_onnx, copy_to_deploy


def quick_start(data_dir, model='yolov8n.pt', epochs=50, batch=16, device='0'):
    """
    快速开始训练流程

    Args:
        data_dir: 数据集目录（包含 images/ 和 labels/）
        model: 预训练模型
        epochs: 训练轮数
        batch: 批次大小
        device: GPU 编号
    """
    print("=" * 60)
    print("🍄 金针菇病虫害 YOLOv8 快速开始")
    print("=" * 60)

    project_root = Path(__file__).parent.parent
    data_path = Path(data_dir)

    # 1. 检查数据
    images_dir = data_path / 'images'
    labels_dir = data_path / 'labels'

    if not images_dir.exists() or not labels_dir.exists():
        print(f"错误: 数据目录不存在")
        print(f"  期望 images/: {images_dir.exists()}")
        print(f"  期望 labels/: {labels_dir.exists()}")
        return

    image_count = len(list(images_dir.glob('*')))
    label_count = len(list(labels_dir.glob('*.txt')))
    print(f"\n数据检查:")
    print(f"  图片: {image_count} 张")
    print(f"  标注: {label_count} 个")

    if image_count == 0:
        print("错误: 没有找到图片文件")
        return

    # 2. 划分数据集
    print("\n[1/4] 划分数据集...")
    output_dir = project_root / 'data'
    train_count, val_count, test_count = split_dataset(
        data_dir, output_dir, train_ratio=0.7, val_ratio=0.2, test_ratio=0.1
    )

    # 3. 数据统计
    print("\n[2/4] 数据统计...")
    analyze_dataset(output_dir)

    # 4. 训练
    print("\n[3/4] 开始训练...")
    config = DEFAULT_CONFIG.copy()
    config['model'] = model
    config['data'] = str(project_root / 'config.yaml')
    config['epochs'] = epochs
    config['batch'] = batch
    config['device'] = device
    config['name'] = 'quick_start'

    results = train(config)

    # 5. 评估
    print("\n[4/4] 评估模型...")
    best_model = project_root / 'runs' / 'detect' / 'quick_start' / 'weights' / 'best.pt'
    if best_model.exists():
        eval_results = evaluate(
            str(best_model),
            str(project_root / 'config.yaml'),
            split='test'
        )

        # 导出 ONNX
        print("\n导出 ONNX 模型...")
        onnx_path = export_onnx(str(best_model))

        # 复制到部署目录
        deploy_dir = project_root / 'exports'
        copy_to_deploy(onnx_path, deploy_dir)

        print("\n" + "=" * 60)
        print("✅ 快速开始完成！")
        print("=" * 60)
        print(f"\n最佳模型: {best_model}")
        print(f"ONNX 模型: {deploy_dir / Path(onnx_path).name}")
        print(f"\nmAP50-95: {eval_results['mAP50-95']:.4f}")
        print(f"mAP50:    {eval_results['mAP50']:.4f}")
        print(f"\n下一步:")
        print(f"  1. 将 ONNX 模型复制到 yolo-service/models/best.onnx")
        print(f"  2. 修改 yolo-service/config.py 中的模型路径")
        print(f"  3. 启动 YOLO 服务: python app.py")
    else:
        print(f"错误: 未找到训练好的模型 {best_model}")


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description='快速开始训练')
    parser.add_argument('--data', required=True, help='数据集目录（包含 images/ 和 labels/）')
    parser.add_argument('--model', default='yolov8n.pt', help='预训练模型')
    parser.add_argument('--epochs', type=int, default=50, help='训练轮数')
    parser.add_argument('--batch', type=int, default=16, help='批次大小')
    parser.add_argument('--device', default='0', help='GPU 编号')

    args = parser.parse_args()
    quick_start(args.data, args.model, args.epochs, args.batch, args.device)
