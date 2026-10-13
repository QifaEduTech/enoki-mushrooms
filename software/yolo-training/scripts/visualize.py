"""
训练过程可视化脚本
功能：
1. 绘制训练损失曲线
2. 绘制 mAP 曲线
3. 绘制学习率曲线
4. 生成训练报告
"""

import os
import pandas as pd
import matplotlib.pyplot as plt
import seaborn as sns
from pathlib import Path


def plot_training_results(csv_path, output_dir=None):
    """
    绘制训练结果曲线

    Args:
        csv_path: 训练结果 CSV 文件路径
        output_dir: 输出目录
    """
    # 读取 CSV
    df = pd.read_csv(csv_path)
    df.columns = df.columns.str.strip()  # 去除列名空格

    if output_dir is None:
        output_dir = Path(csv_path).parent
    else:
        output_dir = Path(output_dir)
        output_dir.mkdir(parents=True, exist_ok=True)

    # 设置绘图风格
    sns.set_style("whitegrid")
    plt.rcParams['figure.figsize'] = (12, 8)
    plt.rcParams['font.size'] = 12

    # 1. 损失曲线
    fig, axes = plt.subplots(2, 2, figsize=(16, 12))

    loss_cols = [col for col in df.columns if 'loss' in col.lower()]
    if loss_cols:
        ax = axes[0, 0]
        for col in loss_cols:
            ax.plot(df['epoch'], df[col], label=col, linewidth=2)
        ax.set_xlabel('Epoch')
        ax.set_ylabel('Loss')
        ax.set_title('训练损失曲线')
        ax.legend()
        ax.grid(True)

    # 2. mAP 曲线
    map_cols = [col for col in df.columns if 'map' in col.lower()]
    if map_cols:
        ax = axes[0, 1]
        for col in map_cols:
            ax.plot(df['epoch'], df[col], label=col, linewidth=2)
        ax.set_xlabel('Epoch')
        ax.set_ylabel('mAP')
        ax.set_title('mAP 曲线')
        ax.legend()
        ax.grid(True)

    # 3. Precision 和 Recall
    pr_cols = [col for col in df.columns if col.strip() in ['precision', 'recall']]
    if pr_cols:
        ax = axes[1, 0]
        for col in pr_cols:
            ax.plot(df['epoch'], df[col], label=col, linewidth=2)
        ax.set_xlabel('Epoch')
        ax.set_ylabel('Value')
        ax.set_title('Precision & Recall 曲线')
        ax.legend()
        ax.grid(True)

    # 4. 学习率曲线
    lr_cols = [col for col in df.columns if 'lr' in col.lower() or 'pg' in col.lower()]
    if lr_cols:
        ax = axes[1, 1]
        for col in lr_cols:
            ax.plot(df['epoch'], df[col], label=col, linewidth=2)
        ax.set_xlabel('Epoch')
        ax.set_ylabel('Learning Rate')
        ax.set_title('学习率曲线')
        ax.legend()
        ax.grid(True)

    plt.tight_layout()
    save_path = output_dir / 'training_curves.png'
    plt.savefig(save_path, dpi=150, bbox_inches='tight')
    print(f"训练曲线已保存: {save_path}")
    plt.close()

    return df


def generate_training_report(csv_path, output_path=None):
    """
    生成训练报告

    Args:
        csv_path: 训练结果 CSV 文件路径
        output_path: 报告输出路径
    """
    df = pd.read_csv(csv_path)
    df.columns = df.columns.str.strip()

    total_epochs = len(df)
    best_epoch = df['epoch'].iloc[-1]

    # 找到最佳 mAP
    map_col = [col for col in df.columns if 'map50-95' in col.lower() or col.strip() == 'metrics/mAP50-95(B)']
    if map_col:
        best_map_idx = df[map_col[0]].idxmax()
        best_map = df[map_col[0]].max()
        best_epoch = df.loc[best_map_idx, 'epoch']
    else:
        best_map = 'N/A'

    # 生成报告
    report = f"""
# 🍄 金针菇病虫害 YOLOv8 训练报告

## 训练概览
- **总训练轮数**: {total_epochs}
- **最佳 Epoch**: {best_epoch}
- **最佳 mAP50-95**: {best_map}

## 最终指标
"""

    # 添加最终指标
    final_row = df.iloc[-1]
    for col in df.columns:
        if col != 'epoch':
            report += f"- **{col}**: {final_row[col]}\n"

    report += """
## 训练建议
1. 如果 mAP 还在上升，可以增加 epochs 继续训练
2. 如果过拟合（训练损失下降但验证损失上升），增加数据增强或减少模型复杂度
3. 如果欠拟合，尝试更大的模型（yolov8s → yolov8m → yolov8l）
"""

    if output_path is None:
        output_path = Path(csv_path).parent / 'training_report.md'

    with open(output_path, 'w', encoding='utf-8') as f:
        f.write(report)

    print(f"训练报告已保存: {output_path}")
    return report


def plot_confusion_matrix(confusion_matrix_path, output_dir=None):
    """
    绘制混淆矩阵热力图

    Args:
        confusion_matrix_path: 混淆矩阵 CSV 文件路径
        output_dir: 输出目录
    """
    import numpy as np

    df = pd.read_csv(confusion_matrix_path, index_col=0)

    plt.figure(figsize=(10, 8))
    sns.heatmap(df, annot=True, fmt='d', cmap='Blues')
    plt.title('混淆矩阵')
    plt.xlabel('预测类别')
    plt.ylabel('真实类别')
    plt.tight_layout()

    if output_dir is None:
        output_dir = Path(confusion_matrix_path).parent
    save_path = Path(output_dir) / 'confusion_matrix_heatmap.png'
    plt.savefig(save_path, dpi=150, bbox_inches='tight')
    print(f"混淆矩阵已保存: {save_path}")
    plt.close()


if __name__ == '__main__':
    import argparse

    parser = argparse.ArgumentParser(description='训练可视化')
    parser.add_argument('--csv', required=True, help='训练结果 CSV 文件')
    parser.add_argument('--output', default=None, help='输出目录')
    parser.add_argument('--report', action='store_true', help='生成训练报告')

    args = parser.parse_args()

    # 绘制训练曲线
    plot_training_results(args.csv, args.output)

    # 生成报告
    if args.report:
        generate_training_report(args.csv)
