import os
import uuid
import time
from flask import Flask, request, jsonify, send_file
from flask_cors import CORS
from config import Config
from utils.detector import YOLODetector

app = Flask(__name__)
CORS(app)

# 全局检测器实例
detector = None

def get_detector():
    """延迟加载检测器"""
    global detector
    if detector is None:
        detector = YOLODetector(
            model_path=Config.DEFAULT_MODEL,
            conf_threshold=Config.CONF_THRESHOLD,
            iou_threshold=Config.IOU_THRESHOLD,
            img_size=Config.IMG_SIZE
        )
    return detector

def enrich_detections(detections_data):
    """为检测结果添加中文类别名和防治建议"""
    for det in detections_data['detections']:
        class_id = det['class_id']
        class_name_cn = Config.CLASS_NAMES_CN.get(class_id, f'未知类别_{class_id}')
        det['class_name'] = class_name_cn
        det['recommendation'] = Config.RECOMMENDATIONS.get(class_name_cn, '暂无防治建议')
        det['severity_cn'] = Config.SEVERITY_LEVELS.get(det['severity'], '未知')
    return detections_data


@app.route('/api/yolo/health', methods=['GET'])
def health():
    """健康检查"""
    model_exists = os.path.exists(Config.DEFAULT_MODEL)
    return jsonify({
        'status': 'ok',
        'model_loaded': detector is not None,
        'model_file_exists': model_exists,
        'model_path': Config.DEFAULT_MODEL,
    })


@app.route('/api/yolo/detect', methods=['POST'])
def detect():
    """上传图片文件进行检测"""
    if 'file' not in request.files:
        return jsonify({'code': 400, 'message': '未上传文件'}), 400

    file = request.files['file']
    if file.filename == '':
        return jsonify({'code': 400, 'message': '文件名为空'}), 400

    try:
        # 保存临时文件
        ext = os.path.splitext(file.filename)[1] or '.jpg'
        filename = f"{uuid.uuid4().hex}{ext}"
        temp_path = os.path.join(Config.RESULTS_DIR, filename)
        file.save(temp_path)

        # 执行检测
        det = get_detector()
        results = det.detect_image(temp_path)
        results = enrich_detections(results)

        # 绘制结果图片
        result_image_url = None
        if Config.SAVE_RESULTS and results['count'] > 0:
            result_filename = f"detect_{filename}"
            result_path = os.path.join(Config.RESULTS_DIR, result_filename)
            det.draw_results(temp_path, results, result_path)
            result_image_url = f"/api/yolo/results/{result_filename}"

        # 清理原始临时文件
        try:
            os.remove(temp_path)
        except:
            pass

        return jsonify({
            'code': 200,
            'message': '检测成功',
            'data': {
                'detections': results['detections'],
                'count': results['count'],
                'elapsed_ms': results['elapsed_ms'],
                'result_image': result_image_url,
            }
        })

    except Exception as e:
        return jsonify({'code': 500, 'message': f'检测失败: {str(e)}'}), 500


@app.route('/api/yolo/detect/base64', methods=['POST'])
def detect_base64():
    """Base64 图片检测"""
    data = request.get_json()
    if not data or 'image' not in data:
        return jsonify({'code': 400, 'message': '缺少 image 字段'}), 400

    try:
        base64_str = data['image']
        det = get_detector()
        results = det.detect_base64(base64_str)
        results = enrich_detections(results)

        return jsonify({
            'code': 200,
            'message': '检测成功',
            'data': {
                'detections': results['detections'],
                'count': results['count'],
                'elapsed_ms': results['elapsed_ms'],
            }
        })

    except Exception as e:
        return jsonify({'code': 500, 'message': f'检测失败: {str(e)}'}), 500


@app.route('/api/yolo/results/<filename>', methods=['GET'])
def get_result_image(filename):
    """获取检测结果图片"""
    filepath = os.path.join(Config.RESULTS_DIR, filename)
    if not os.path.exists(filepath):
        return jsonify({'code': 404, 'message': '文件不存在'}), 404
    return send_file(filepath, mimetype='image/jpeg')


@app.route('/api/yolo/classes', methods=['GET'])
def get_classes():
    """获取所有病虫害类别"""
    classes = []
    for class_id, name_cn in Config.CLASS_NAMES_CN.items():
        classes.append({
            'class_id': class_id,
            'name_cn': name_cn,
            'recommendation': Config.RECOMMENDATIONS.get(name_cn, ''),
        })
    return jsonify({'code': 200, 'data': classes})


if __name__ == '__main__':
    os.makedirs(Config.RESULTS_DIR, exist_ok=True)
    print(f"YOLO 病虫害识别服务启动中...")
    print(f"模型路径: {Config.DEFAULT_MODEL}")
    print(f"服务地址: http://{Config.HOST}:{Config.PORT}")
    app.run(host=Config.HOST, port=Config.PORT, debug=Config.DEBUG)
