"""
模型评估脚本
功能：
1. 在测试集上评估模型性能
2. 生成混淆矩阵和 PR 曲线
3. 导出评估报告
"""

import os
import json
import argparse
from pathlib import Path
from ultralytics import YOLO


def evaluate(model_path, data_config, split='test', imgsz=640, batch=16, device='0'):
    """
    评估模型

    Args:
        model_path: 模型权重路径
        data_config: 数据集配置文件
        split: 评估数据集划分 (val/test)
        imgsz: 输入图片尺寸
        batch: 批次大小
        device: GPU 编号

    Returns:
        评估结果字典
    """
    print("=" * 60)
    print("📊 模型评估")
    print("=" * 60)

    # 加载模型
    print(f"加载模型: {model_path}")
    model = YOLO(model_path)

    # 运行评估
    print(f"\n在 {split} 集上评估...")
    results = model.val(
        data=data_config,
        imgsz=imgsz,
        batch=batch,
        device=device,
        split=split,
        save_json=True,
        save_hybrid=True,
        conf=0.25,
        iou=0.6,
        plots=True,
    )

    # 打印结果
    print("\n评估结果:")
    print(f"  mAP50:     {results.box.map50:.4f}")
    print(f"  mAP50-95:  {results.box.map:.4f}")
    print(f"  Precision: {results.box.mp:.4f}")
    print(f"  Recall:    {results.box.mr:.4f}")

    # 每个类别的结果
    print("\n各类别性能:")
    names = results.names
    for i, (p, r, ap50, ap) in enumerate(
        zip(results.box.p, results.box.r, results.box.ap50, results.box.ap)
    ):
        name = names.get(i, f'class_{i}')
        print(f"  {name:20s} | P: {p:.3f} | R: {r:.3f} | mAP50: {ap50:.3f} | mAP50-95: {ap:.3f}")

    return {
        'mAP50': results.box.map50,
        'mAP50-95': results.box.map,
        'precision': results.box.mp,
        'recall': results.box.mr,
        'per_class': {
            names.get(i, f'class_{i}'): {
                'precision': float(p),
                'recall': float(r),
                'mAP50': float(ap50),
                'mAP50-95': float(ap),
            }
            for i, (p, r, ap50, ap) in enumerate(
                zip(results.box.p, results.box.r, results.box.ap50, results.box.ap)
            )
        }
    }


def predict_sample(model_path, image_path, conf=0.25, save=True):
    """
    对单张图片进行预测

    Args:
        model_path: 模型权重路径
        image_path: 图片路径
        conf: 置信度阈值
        save: 是否保存结果图片
    """
    model = YOLO(model_path)

    results = model.predict(
        source=image_path,
        conf=conf,
        save=save,
        show_labels=True,
        show_conf=True,
    )

    for result in results:
        print(f"\n图片: {result.path}")
        print(f"检测到 {len(result.boxes)} 个目标:")

        for box in result.boxes:
            cls_id = int(box.cls[0])
            conf = float(box.conf[0])
            cls_name = result.names[cls_id]
            xyxy = box.xyxy[0].tolist()
            print(f"  - {cls_name}: {conf:.2%} [{xyxy[0]:.0f}, {xyxy[1]:.0f}, {xyxy[2]:.0f}, {xyxy[3]:.0f}]")

    return results


def benchmark(model_path, imgsz=640, batch=1, device='0', rounds=100):
    """
    模型推理速度基准测试

    Args:
        model_path: 模型权重路径
        imgsz: 输入图片尺寸
        batch: 批次大小
        device: GPU 编号
        rounds: 测试轮数
    """
    import time
    import numpy as np

    print("=" * 60)
    print("⚡ 推理速度基准测试")
    print("=" * 60)

    model = YOLO(model_path)

    # 预热
    print("预热中...")
    import torch
    dummy_input = torch.randn(batch, 3, imgsz, imgsz).to(device)
    for _ in range(10):
        model.predict(dummy_input, verbose=False)

    # 测试
    print(f"测试 {rounds} 轮...")
    times = []
    for _ in range(rounds):
        start = time.perf_counter()
        model.predict(dummy_input, verbose=False)
        elapsed = time.perf_counter() - start
        times.append(elapsed)

    times = np.array(times)

    print(f"\n结果:")
    print(f"  平均延迟: {times.mean() * 1000:.2f} ms")
    print(f"  最小延迟: {times.min() * 1000:.2f} ms")
    print(f"  最大延迟: {times.max() * 1000:.2f} ms")
    print(f"  标准差:   {times.std() * 1000:.2f} ms")
    print(f"  吞吐量:   {batch / times.mean():.1f} FPS")


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description='YOLOv8 模型评估')
    subparsers = parser.add_subparsers(dest='command', help='可用命令')

    # 评估
    eval_parser = subparsers.add_parser('val', help='评估模型')
    eval_parser.add_argument('--model', required=True, help='模型权重路径')
    eval_parser.add_argument('--data', default='config.yaml', help='数据集配置')
    eval_parser.add_argument('--split', default='test', help='评估数据集划分')
    eval_parser.add_argument('--imgsz', type=int, default=640, help='输入图片尺寸')
    eval_parser.add_argument('--batch', type=int, default=16, help='批次大小')
    eval_parser.add_argument('--device', default='0', help='GPU 编号')

    # 预测
    pred_parser = subparsers.add_parser('predict', help='预测单张图片')
    pred_parser.add_argument('--model', required=True, help='模型权重路径')
    pred_parser.add_argument('--image', required=True, help='图片路径')
    pred_parser.add_argument('--conf', type=float, default=0.25, help='置信度阈值')
    pred_parser.add_argument('--no-save', action='store_true', help='不保存结果图片')

    # 基准测试
    bench_parser = subparsers.add_parser('benchmark', help='推理速度基准测试')
    bench_parser.add_argument('--model', required=True, help='模型权重路径')
    bench_parser.add_argument('--imgsz', type=int, default=640, help='输入图片尺寸')
    bench_parser.add_argument('--batch', type=int, default=1, help='批次大小')
    bench_parser.add_argument('--device', default='0', help='GPU 编号')
    bench_parser.add_argument('--rounds', type=int, default=100, help='测试轮数')

    args = parser.parse_args()

    if args.command == 'val':
        results = evaluate(args.model, args.data, args.split, args.imgsz, args.batch, args.device)
        # 保存结果
        output_path = Path(args.model).parent.parent / 'evaluation_results.json'
        with open(output_path, 'w') as f:
            json.dump(results, f, indent=2, ensure_ascii=False)
        print(f"\n评估结果已保存到: {output_path}")

    elif args.command == 'predict':
        predict_sample(args.model, args.image, args.conf, not args.no_save)

    elif args.command == 'benchmark':
        benchmark(args.model, args.imgsz, args.batch, args.device, args.rounds)

    else:
        parser.print_help()
