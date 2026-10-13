"""
模型导出脚本
功能：
1. 导出为 ONNX 格式（通用部署）
2. 导出为 TorchScript 格式
3. 导出为 TensorRT 格式（NVIDIA GPU 加速）
4. 复制模型到部署目录
"""

import os
import shutil
import argparse
from pathlib import Path
from ultralytics import YOLO


def export_onnx(model_path, imgsz=640, simplify=True, opset=11):
    """
    导出为 ONNX 格式

    Args:
        model_path: 模型权重路径
        imgsz: 输入图片尺寸
        simplify: 是否简化模型
        opset: ONNX 算子集版本

    Returns:
        导出文件路径
    """
    print("导出 ONNX 格式...")
    model = YOLO(model_path)
    export_path = model.export(
        format='onnx',
        imgsz=imgsz,
        simplify=simplify,
        opset=opset,
    )
    print(f"ONNX 导出完成: {export_path}")
    return export_path


def export_torchscript(model_path, imgsz=640):
    """
    导出为 TorchScript 格式

    Args:
        model_path: 模型权重路径
        imgsz: 输入图片尺寸

    Returns:
        导出文件路径
    """
    print("导出 TorchScript 格式...")
    model = YOLO(model_path)
    export_path = model.export(
        format='torchscript',
        imgsz=imgsz,
    )
    print(f"TorchScript 导出完成: {export_path}")
    return export_path


def export_tensorrt(model_path, imgsz=640, half=True, workspace=4):
    """
    导出为 TensorRT 格式（需要 NVIDIA GPU）

    Args:
        model_path: 模型权重路径
        imgsz: 输入图片尺寸
        half: 是否使用 FP16
        workspace: 工作空间大小 (GB)

    Returns:
        导出文件路径
    """
    print("导出 TensorRT 格式...")
    model = YOLO(model_path)
    export_path = model.export(
        format='engine',
        imgsz=imgsz,
        half=half,
        workspace=workspace,
    )
    print(f"TensorRT 导出完成: {export_path}")
    return export_path


def export_openvino(model_path, imgsz=640):
    """
    导出为 OpenVINO 格式（Intel CPU 优化）

    Args:
        model_path: 模型权重路径
        imgsz: 输入图片尺寸

    Returns:
        导出文件路径
    """
    print("导出 OpenVINO 格式...")
    model = YOLO(model_path)
    export_path = model.export(
        format='openvino',
        imgsz=imgsz,
    )
    print(f"OpenVINO 导出完成: {export_path}")
    return export_path


def copy_to_deploy(exported_path, deploy_dir):
    """
    复制导出的模型到部署目录

    Args:
        exported_path: 导出的模型路径
        deploy_dir: 部署目录
    """
    deploy_dir = Path(deploy_dir)
    deploy_dir.mkdir(parents=True, exist_ok=True)

    src = Path(exported_path)
    dst = deploy_dir / src.name

    shutil.copy2(src, dst)
    print(f"已复制到部署目录: {dst}")

    # 如果是 ONNX，同时复制到 yolo-service/models/
    if src.suffix == '.onnx':
        yolo_service_dir = Path(__file__).parent.parent.parent / 'Smart-Jzg' / 'yolo-service' / 'models'
        if yolo_service_dir.exists():
            shutil.copy2(src, yolo_service_dir / src.name)
            print(f"已复制到 YOLO 服务目录: {yolo_service_dir / src.name}")


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description='YOLOv8 模型导出')
    parser.add_argument('--model', required=True, help='模型权重路径')
    parser.add_argument('--format', default='onnx',
                       choices=['onnx', 'torchscript', 'engine', 'openvino', 'all'],
                       help='导出格式')
    parser.add_argument('--imgsz', type=int, default=640, help='输入图片尺寸')
    parser.add_argument('--half', action='store_true', help='使用 FP16（仅 TensorRT）')
    parser.add_argument('--deploy-dir', default='../exports', help='部署目录')
    parser.add_argument('--copy', action='store_true', help='复制到部署目录')

    args = parser.parse_args()

    exported_paths = []

    if args.format == 'onnx' or args.format == 'all':
        path = export_onnx(args.model, args.imgsz)
        exported_paths.append(path)

    if args.format == 'torchscript' or args.format == 'all':
        path = export_torchscript(args.model, args.imgsz)
        exported_paths.append(path)

    if args.format == 'engine' or args.format == 'all':
        try:
            path = export_tensorrt(args.model, args.imgsz, args.half)
            exported_paths.append(path)
        except Exception as e:
            print(f"TensorRT 导出失败（可能缺少 GPU）: {e}")

    if args.format == 'openvino' or args.format == 'all':
        try:
            path = export_openvino(args.model, args.imgsz)
            exported_paths.append(path)
        except Exception as e:
            print(f"OpenVINO 导出失败: {e}")

    # 复制到部署目录
    if args.copy and exported_paths:
        for path in exported_paths:
            copy_to_deploy(path, args.deploy_dir)

    print("\n导出完成！")
    for path in exported_paths:
        print(f"  - {path}")
