-- Smart-Enokimushroom 金针菇专用近期演示数据（可重复执行）
-- 保留历史金针菇批次 PLANT-2，清除非金针菇及测试业务数据。

START TRANSACTION;

DELETE FROM monitor_record WHERE plant_id IN (1, 3, 4) OR area_id IN (1, 3, 4, 6);
DELETE FROM device_manage WHERE plant_id IN (1, 3, 4) OR area_id IN (1, 3, 4, 6);
DELETE FROM enokimushroom_cluster WHERE cluster_id IS NULL OR cluster_id IN ('1', '3', '4');
DELETE FROM enokimushroom_pest_disease WHERE cluster_id IN ('1', '3', '4');
DELETE FROM trace_supplement WHERE trace_code IN ('PLANT-1', 'PLANT-3', 'PLANT-4');
DELETE FROM plant_manage WHERE plant_id IN (1, 3, 4);
DELETE FROM area_manage WHERE area_id IN (1, 3, 4, 6);

UPDATE plant_manage
SET plant_name='金针菇', plant_type='菌类', plant_lifeCycle=2,
    update_date='2026-08-12 10:00:00', update_by='admin'
WHERE plant_id=2;

INSERT INTO plant_manage
    (plant_id, plant_name, plant_type, plant_lifeCycle, area_id, user_id,
     create_date, create_by, update_date, update_by, del_flag)
VALUES
    (5, '金针菇', '菌类', 2, 2, 100, '2026-07-08 08:20:00', 'admin', '2026-07-29 15:30:00', 'admin', 0),
    (6, '金针菇', '菌类', 2, 2, 100, '2026-07-20 08:35:00', 'admin', '2026-08-09 16:10:00', 'admin', 0),
    (7, '金针菇', '菌类', 2, 2, 100, '2026-07-28 09:10:00', 'admin', '2026-08-12 09:45:00', 'admin', 0)
ON DUPLICATE KEY UPDATE
    plant_name=VALUES(plant_name), plant_type=VALUES(plant_type),
    plant_lifeCycle=VALUES(plant_lifeCycle), area_id=VALUES(area_id),
    user_id=VALUES(user_id), create_date=VALUES(create_date),
    update_date=VALUES(update_date), update_by=VALUES(update_by), del_flag=0;

INSERT INTO trace_supplement (
    trace_code, product_name, strain_name, produce_date, pack_date, pack_qty, pack_weight,
    packaging_image, report_no, inspector,
    sale_customer, sale_location, sale_date, sale_quantity, sale_recorder, sale_note,
    credit_level, credit_record, credit_sample, credit_complaint,
    credit_penalty, credit_lawsuit, credit_tax
) VALUES
('PLANT-5', '金针菇', '白色优选金针菇', '2026-07-08', '2026-07-27', 4200, 2940.00,
 NULL, 'JZJ-2026-0701', '王质检', '长春鲜品配送中心', '长春', '2026-07-29', '2800公斤', '赵销售', '冷链整车配送',
 '守信示范', '无失信记录', '≥98%', '无投诉', '无', '无', 'A级'),
('PLANT-6', '金针菇', '白色优选金针菇', '2026-07-20', '2026-08-07', 4600, 3220.00,
 NULL, 'JZJ-2026-0801', '李质检', '吉林省生鲜供应链', '吉林', '2026-08-09', '3000公斤', '孙销售', '商超冷链专供',
 '守信示范', '无失信记录', '≥98%', '无投诉', '无', '无', 'A级'),
('PLANT-7', '金针菇', '黄金针菇', '2026-07-28', '2026-08-11', 3800, 2660.00,
 NULL, 'JZJ-2026-0802', '周质检', '东北农产品交易中心', '哈尔滨', '2026-08-12', '2400公斤', '钱销售', '当日冷链发运',
 '信用良好', '无失信记录', '≥98%', '无投诉', '无', '无', 'A级')
ON DUPLICATE KEY UPDATE
    product_name=VALUES(product_name), strain_name=VALUES(strain_name),
    produce_date=VALUES(produce_date), pack_date=VALUES(pack_date),
    pack_qty=VALUES(pack_qty), pack_weight=VALUES(pack_weight),
    report_no=VALUES(report_no), inspector=VALUES(inspector),
    sale_customer=VALUES(sale_customer), sale_location=VALUES(sale_location),
    sale_date=VALUES(sale_date), sale_quantity=VALUES(sale_quantity),
    sale_recorder=VALUES(sale_recorder), sale_note=VALUES(sale_note),
    credit_level=VALUES(credit_level), credit_record=VALUES(credit_record),
    credit_sample=VALUES(credit_sample), credit_complaint=VALUES(credit_complaint),
    credit_penalty=VALUES(credit_penalty), credit_lawsuit=VALUES(credit_lawsuit),
    credit_tax=VALUES(credit_tax);

INSERT INTO device_manage
    (device_number, device_name, device_type, plant_id, area_id, device_state,
     user_id, create_date, create_by, update_date, update_by, del_flag)
SELECT CONCAT('JZG-ENV-', p.plant_id), '菇房环境采集终端', '采集设备', p.plant_id, 2, 1,
       100, p.create_date, 'admin', '2026-08-12 10:00:00', 'admin', 0
FROM plant_manage p
WHERE p.plant_id IN (5, 6, 7)
  AND NOT EXISTS (SELECT 1 FROM device_manage d WHERE d.device_number=CONCAT('JZG-ENV-', p.plant_id));

DELETE FROM monitor_record WHERE plant_id IN (5, 6, 7);
INSERT INTO monitor_record
    (address_id, air_temperature, air_humidity, light_lux, air_co2,
     soil_temperature, soil_humidity, soil_conductivity, soil_PH,
     soil_N, soil_P, soil_K, create_date, plant_id, area_id,
     user_id, create_by, update_date, update_by, del_flag)
VALUES
('JZG-5', 7.8, 91.2, 115, 780, 8.1, 84.0, 620, 6.5, 42, 31, 48, '2026-07-25 08:00:00', 5, 2, 100, 'sensor', '2026-07-25 08:00:00', 'sensor', 0),
('JZG-5', 8.2, 90.5, 120, 795, 8.4, 83.2, 625, 6.5, 43, 30, 47, '2026-07-26 08:00:00', 5, 2, 100, 'sensor', '2026-07-26 08:00:00', 'sensor', 0),
('JZG-5', 8.0, 92.0, 110, 770, 8.2, 85.1, 618, 6.4, 42, 32, 49, '2026-07-27 08:00:00', 5, 2, 100, 'sensor', '2026-07-27 08:00:00', 'sensor', 0),
('JZG-6', 7.5, 92.4, 105, 760, 7.9, 85.0, 615, 6.4, 41, 30, 46, '2026-08-05 08:00:00', 6, 2, 100, 'sensor', '2026-08-05 08:00:00', 'sensor', 0),
('JZG-6', 7.9, 91.8, 112, 775, 8.1, 84.3, 622, 6.5, 42, 31, 47, '2026-08-06 08:00:00', 6, 2, 100, 'sensor', '2026-08-06 08:00:00', 'sensor', 0),
('JZG-6', 8.1, 91.0, 118, 790, 8.3, 83.8, 628, 6.5, 43, 31, 48, '2026-08-07 08:00:00', 6, 2, 100, 'sensor', '2026-08-07 08:00:00', 'sensor', 0),
('JZG-7', 7.6, 93.0, 100, 750, 7.8, 86.0, 610, 6.4, 41, 29, 46, '2026-08-10 08:00:00', 7, 2, 100, 'sensor', '2026-08-10 08:00:00', 'sensor', 0),
('JZG-7', 7.8, 92.6, 108, 765, 8.0, 85.4, 616, 6.4, 42, 30, 47, '2026-08-11 08:00:00', 7, 2, 100, 'sensor', '2026-08-11 08:00:00', 'sensor', 0),
('JZG-7', 8.0, 92.1, 114, 780, 8.2, 84.8, 624, 6.5, 43, 31, 48, '2026-08-12 08:00:00', 7, 2, 100, 'sensor', '2026-08-12 08:00:00', 'sensor', 0);

DELETE FROM enokimushroom_cluster WHERE cluster_id IN ('5', '6', '7');
INSERT INTO enokimushroom_cluster
    (cluster_id, cluster_name, total_count, ripe_count, half_ripe_count,
     unripe_count, avg_size_mm, latitude, longitude, recommendation, last_update)
VALUES
('5', '一号菇房A区', 16800, 15600, 900, 300, 142, 43.8921, 125.4278, '成熟度良好，已完成采收包装', '2026-07-27 09:00:00'),
('6', '一号菇房B区', 18400, 17100, 1000, 300, 145, 43.8923, 125.4281, '长势均匀，已完成采收包装', '2026-08-07 09:00:00'),
('7', '二号菇房A区', 15200, 13900, 1000, 300, 140, 43.8926, 125.4285, '已达到商品成熟标准', '2026-08-11 09:00:00');

DELETE FROM enokimushroom_pest_disease WHERE cluster_id IN ('5', '6', '7');
INSERT INTO enokimushroom_pest_disease
    (cluster_id, object_type, bbox_x_min, bbox_y_min, bbox_x_max, bbox_y_max,
     confidence, pest_disease_class, severity_level, recommendation, detected_time)
VALUES
('5', '金针菇子实体', 0, 0, 0, 0, 0.99, '质检合格', '正常', '未发现病虫害，符合出库标准', '2026-07-27 10:20:00'),
('6', '金针菇子实体', 0, 0, 0, 0, 0.99, '质检合格', '正常', '未发现病虫害，符合出库标准', '2026-08-07 10:15:00'),
('7', '金针菇子实体', 0, 0, 0, 0, 0.99, '质检合格', '正常', '未发现病虫害，符合出库标准', '2026-08-11 10:30:00');

COMMIT;
