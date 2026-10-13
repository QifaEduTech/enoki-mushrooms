"""
数据准备脚本
功能：
1. 划分训练/验证/测试集
2. 转换标注格式（VOC/COCO → YOLO）
3. 数据统计和可视化
"""

import os
import json
import shutil
import random
from pathlib import Path
from collections import Counter
import xml.etree.ElementTree as ET

# 类别映射
CLASS_NAMES = {
    0: 'powdery_mildew',
    1: 'gray_mold',
    2: 'anthracnose',
    3: 'fusarium_wilt',
    4: 'leaf_spot',
    5: 'spider_mite',
    6: 'aphid',
    7: 'healthy',
}

CLASS_NAME_TO_ID = {v: k for k, v in CLASS_NAMES.items()}


def split_dataset(source_dir, output_dir, train_ratio=0.7, val_ratio=0.2, test_ratio=0.1, seed=42):
    """
    划分数据集

    Args:
        source_dir: 源数据目录，包含 images/ 和 labels/ 子目录
        output_dir: 输出目录
        train_ratio: 训练集比例
        val_ratio: 验证集比例
        test_ratio: 测试集比例
        seed: 随机种子
    """
    random.seed(seed)

    source_images = Path(source_dir) / 'images'
    source_labels = Path(source_dir) / 'labels'

    # 获取所有图片文件
    image_extensions = {'.jpg', '.jpeg', '.png', '.bmp', '.webp'}
    images = [f for f in source_images.iterdir()
              if f.suffix.lower() in image_extensions]

    # 过滤掉没有对应标注的图片
    images = [img for img in images
              if (source_labels / (img.stem + '.txt')).exists()]

    print(f"找到 {len(images)} 张带标注的图片")

    # 打乱顺序
    random.shuffle(images)

    # 计算划分数量
    total = len(images)
    train_count = int(total * train_ratio)
    val_count = int(total * val_ratio)

    train_images = images[:train_count]
    val_images = images[train_count:train_count + val_count]
    test_images = images[train_count + val_count:]

    print(f"训练集: {len(train_images)} 张")
    print(f"验证集: {len(val_images)} 张")
    print(f"测试集: {len(test_images)} 张")

    # 复制文件
    for split_name, split_images in [('train', train_images), ('val', val_images), ('test', test_images)]:
        img_dir = Path(output_dir) / split_name / 'images'
        lbl_dir = Path(output_dir) / split_name / 'labels'
        img_dir.mkdir(parents=True, exist_ok=True)
        lbl_dir.mkdir(parents=True, exist_ok=True)

        for img_path in split_images:
            # 复制图片
            shutil.copy2(img_path, img_dir / img_path.name)
            # 复制标注
            label_path = source_labels / (img_path.stem + '.txt')
            if label_path.exists():
                shutil.copy2(label_path, lbl_dir / label_path.name)

    print("数据集划分完成！")
    return len(train_images), len(val_images), len(test_images)


def voc_to_yolo(xml_path, img_width, img_height):
    """
    将 VOC XML 格式转换为 YOLO TXT 格式

    Args:
        xml_path: XML 文件路径
        img_width: 图片宽度
        img_height: 图片高度

    Returns:
        YOLO 格式的标注列表
    """
    tree = ET.parse(xml_path)
    root = tree.getroot()

    yolo_lines = []
    for obj in root.findall('object'):
        name = obj.find('name').text
        if name not in CLASS_NAME_TO_ID:
            print(f"警告: 未知类别 '{name}'，跳过")
            continue

        class_id = CLASS_NAME_TO_ID[name]
        bbox = obj.find('bndbox')
        xmin = float(bbox.find('xmin').text)
        ymin = float(bbox.find('ymin').text)
        xmax = float(bbox.find('xmax').text)
        ymax = float(bbox.find('ymax').text)

        # 转换为 YOLO 格式 (中心点 + 宽高，归一化)
        x_center = ((xmin + xmax) / 2) / img_width
        y_center = ((ymin + ymax) / 2) / img_height
        width = (xmax - xmin) / img_width
        height = (ymax - ymin) / img_height

        yolo_lines.append(f"{class_id} {x_center:.6f} {y_center:.6f} {width:.6f} {height:.6f}")

    return yolo_lines


def convert_voc_to_yolo(voc_dir, output_dir):
    """
    批量转换 VOC 格式到 YOLO 格式

    Args:
        voc_dir: VOC 数据目录，包含 Annotations/ 和 JPEGImages/
        output_dir: 输出目录
    """
    annotations_dir = Path(voc_dir) / 'Annotations'
    images_dir = Path(voc_dir) / 'JPEGImages'

    output_labels = Path(output_dir) / 'labels'
    output_labels.mkdir(parents=True, exist_ok=True)

    xml_files = list(annotations_dir.glob('*.xml'))
    print(f"找到 {len(xml_files)} 个 XML 标注文件")

    converted = 0
    for xml_path in xml_files:
        try:
            tree = ET.parse(xml_path)
            root = tree.getroot()

            # 获取图片尺寸
            size = root.find('size')
            img_width = int(size.find('width').text)
            img_height = int(size.find('height').text)

            # 转换
            yolo_lines = voc_to_yolo(xml_path, img_width, img_height)

            if yolo_lines:
                output_path = output_labels / (xml_path.stem + '.txt')
                with open(output_path, 'w') as f:
                    f.write('\n'.join(yolo_lines))
                converted += 1

        except Exception as e:
            print(f"转换 {xml_path.name} 失败: {e}")

    print(f"成功转换 {converted}/{len(xml_files)} 个文件")


def coco_to_yolo(coco_json_path, output_dir):
    """
    将 COCO JSON 格式转换为 YOLO TXT 格式

    Args:
        coco_json_path: COCO JSON 文件路径
        output_dir: 输出目录
    """
    with open(coco_json_path, 'r') as f:
        coco = json.load(f)

    # 构建类别映射
    coco_categories = {cat['id']: cat['name'] for cat in coco['categories']}
    coco_to_yolo_id = {}
    for cat_id, cat_name in coco_categories.items():
        if cat_name in CLASS_NAME_TO_ID:
            coco_to_yolo_id[cat_id] = CLASS_NAME_TO_ID[cat_name]

    # 构建图片信息
    images_info = {img['id']: img for img in coco['images']}

    # 按图片分组标注
    annotations_by_image = {}
    for ann in coco['annotations']:
        img_id = ann['image_id']
        if img_id not in annotations_by_image:
            annotations_by_image[img_id] = []
        annotations_by_image[img_id].append(ann)

    output_labels = Path(output_dir) / 'labels'
    output_labels.mkdir(parents=True, exist_ok=True)

    converted = 0
    for img_id, anns in annotations_by_image.items():
        img_info = images_info[img_id]
        img_width = img_info['width']
        img_height = img_info['height']

        yolo_lines = []
        for ann in anns:
            cat_id = ann['category_id']
            if cat_id not in coco_to_yolo_id:
                continue

            class_id = coco_to_yolo_id[cat_id]
            x, y, w, h = ann['bbox']  # COCO: [x, y, width, height]

            # 转换为 YOLO 格式
            x_center = (x + w / 2) / img_width
            y_center = (y + h / 2) / img_height
            norm_w = w / img_width
            norm_h = h / img_height

            yolo_lines.append(f"{class_id} {x_center:.6f} {y_center:.6f} {norm_w:.6f} {norm_h:.6f}")

        if yolo_lines:
            img_name = Path(img_info['file_name']).stem
            output_path = output_labels / (img_name + '.txt')
            with open(output_path, 'w') as f:
                f.write('\n'.join(yolo_lines))
            converted += 1

    print(f"成功转换 {converted} 个文件")


def analyze_dataset(data_dir):
    """
    统计数据集信息

    Args:
        data_dir: 数据集目录
    """
    print("=" * 50)
    print("数据集统计")
    print("=" * 50)

    for split in ['train', 'val', 'test']:
        labels_dir = Path(data_dir) / split / 'labels'
        images_dir = Path(data_dir) / split / 'images'

        if not labels_dir.exists():
            print(f"\n{split}: 目录不存在")
            continue

        label_files = list(labels_dir.glob('*.txt'))
        image_files = list(images_dir.glob('*'))

        class_counts = Counter()
        total_boxes = 0

        for label_file in label_files:
            with open(label_file, 'r') as f:
                for line in f:
                    parts = line.strip().split()
                    if len(parts) >= 1:
                        class_id = int(parts[0])
                        class_counts[class_id] += 1
                        total_boxes += 1

        print(f"\n{split}:")
        print(f"  图片数量: {len(image_files)}")
        print(f"  标注文件: {len(label_files)}")
        print(f"  标注框数: {total_boxes}")

        if class_counts:
            print(f"  类别分布:")
            for class_id in sorted(class_counts.keys()):
                name = CLASS_NAMES.get(class_id, f'unknown_{class_id}')
                count = class_counts[class_id]
                print(f"    {name}: {count}")


if __name__ == '__main__':
    import argparse

    parser = argparse.ArgumentParser(description='金针菇病虫害数据准备工具')
    subparsers = parser.add_subparsers(dest='command', help='可用命令')

    # 划分数据集
    split_parser = subparsers.add_parser('split', help='划分数据集')
    split_parser.add_argument('--source', required=True, help='源数据目录')
    split_parser.add_argument('--output', required=True, help='输出目录')
    split_parser.add_argument('--train', type=float, default=0.7, help='训练集比例')
    split_parser.add_argument('--val', type=float, default=0.2, help='验证集比例')
    split_parser.add_argument('--test', type=float, default=0.1, help='测试集比例')

    # VOC 转 YOLO
    voc_parser = subparsers.add_parser('voc2yolo', help='VOC 格式转 YOLO 格式')
    voc_parser.add_argument('--voc-dir', required=True, help='VOC 数据目录')
    voc_parser.add_argument('--output', required=True, help='输出目录')

    # COCO 转 YOLO
    coco_parser = subparsers.add_parser('coco2yolo', help='COCO 格式转 YOLO 格式')
    coco_parser.add_argument('--coco-json', required=True, help='COCO JSON 文件路径')
    coco_parser.add_argument('--output', required=True, help='输出目录')

    # 统计分析
    analyze_parser = subparsers.add_parser('analyze', help='统计数据集信息')
    analyze_parser.add_argument('--data', required=True, help='数据集目录')

    args = parser.parse_args()

    if args.command == 'split':
        split_dataset(args.source, args.output, args.train, args.val, args.test)
    elif args.command == 'voc2yolo':
        convert_voc_to_yolo(args.voc_dir, args.output)
    elif args.command == 'coco2yolo':
        coco_to_yolo(args.coco_json, args.output)
    elif args.command == 'analyze':
        analyze_dataset(args.data)
    else:
        parser.print_help()
