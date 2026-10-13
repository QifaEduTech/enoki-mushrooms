-- ============================================================
-- 金针菇可信溯源 补充数据表
-- 说明：主系统(Smart-Enokimushroom)没有包装/销售/信用/质检
--       相关业务表，溯源页面这些栏目会缺失。
--       本表按追溯码 PLANT-<plant_id> 存放补充信息，
--       修改版 demo 从本表读取这些栏目，主系统数据仍实时查询。
-- 导入方式：mysql -u root -p enokimushroom < trace_supplement.sql
-- 或由 db\init-db.bat 自动追加导入。
-- 幂等：表不存在才创建，样例数据仅在新插入时生效，
--       不会覆盖已有的用户改动。
-- ============================================================

CREATE TABLE IF NOT EXISTS trace_supplement (
    trace_code        VARCHAR(64)  NOT NULL COMMENT '追溯码 PLANT-<主系统植物ID>',
    product_name      VARCHAR(128) NULL COMMENT '产品名称(覆盖显示)',
    strain_name       VARCHAR(128) NULL COMMENT '产品品种/菌种',
    produce_date      DATE         NULL COMMENT '生产日期',
    pack_date         DATE         NULL COMMENT '包装日期',
    pack_qty          INT          NULL COMMENT '包装数量(袋/箱)',
    pack_weight       DECIMAL(10,2) NULL COMMENT '总重量(公斤)',
    packaging_image   VARCHAR(255) NULL COMMENT '包装图片(绝对路径或URL)',
    report_no         VARCHAR(64)  NULL COMMENT '质检报告编号',
    inspector         VARCHAR(64)  NULL COMMENT '送检人',
    sale_customer     VARCHAR(128) NULL COMMENT '销售客户单位',
    sale_location     VARCHAR(64)  NULL COMMENT '销售去向(城市/市场)',
    sale_date         DATE         NULL COMMENT '销售日期',
    sale_quantity     VARCHAR(64)  NULL COMMENT '销售数量',
    sale_recorder     VARCHAR(64)  NULL COMMENT '记录人员',
    sale_note         VARCHAR(255) NULL COMMENT '销售说明',
    credit_level      VARCHAR(16)  NULL COMMENT '信用等级(信用良好/守信示范/失信惩戒)',
    credit_record     VARCHAR(32)  NULL COMMENT '信用状态(无失信记录/有失信记录)',
    credit_sample     VARCHAR(16)  NULL COMMENT '抽检合格率(≥98%/95%～98%/<95%)',
    credit_complaint  VARCHAR(32)  NULL COMMENT '投诉记录(无投诉/1～2次/3次以上)',
    credit_penalty    VARCHAR(8)   NULL COMMENT '行政处罚(无/有)',
    credit_lawsuit    VARCHAR(8)   NULL COMMENT '涉诉情况(无/有)',
    credit_tax        VARCHAR(8)   NULL COMMENT '纳税信用(A/B/C/D级)',
    updated_at        DATETIME     NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (trace_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='溯源补充数据(包装/销售/信用/质检)';

-- ------------------------------------------------------------
-- 样例数据：主系统已有 plant_id=1 羊肚菌 / 2 金针菇 / 3 花生 / 4 月季
-- 可自行增删改；字段留 NULL 的栏目在页面上自动隐藏。
-- ------------------------------------------------------------
INSERT IGNORE INTO trace_supplement (
    trace_code, product_name, strain_name, produce_date, pack_date, pack_qty, pack_weight,
    packaging_image, report_no, inspector,
    sale_customer, sale_location, sale_date, sale_quantity, sale_recorder, sale_note,
    credit_level, credit_record, credit_sample, credit_complaint, credit_penalty, credit_lawsuit, credit_tax
) VALUES
('PLANT-1', '羊肚菌', '六妹羊肚菌', '2024-02-10', '2024-03-05', 2000, 1200.00,
 NULL, 'JZJ-2024-0012', '王质检',
 '华北农贸市场', '北京', '2024-03-20', '1500公斤', '张销售', '首批发货',
 '守信示范', '无失信记录', '≥98%', '无投诉', '无', '无', 'A级'),
('PLANT-2', '金针菇', '白金针菇', '2024-01-25', '2024-02-18', 5000, 3500.00,
 NULL, 'JZJ-2024-0034', '李质检',
 '东北生鲜超市', '沈阳', '2024-03-02', '3200公斤', '赵销售', '商超专供',
 '信用良好', '无失信记录', '≥98%', '无投诉', '无', '无', 'A级'),
('PLANT-3', '花生', '四粒红花生', '2024-05-20', '2024-06-15', 3000, 1800.00,
 NULL, 'JZJ-2024-0045', '王质检',
 '粮油批发市场', '郑州', '2024-06-28', '2000公斤', '钱销售', '散装批发',
 '守信示范', '无失信记录', '95%～98%', '1～2次投诉', '无', '无', 'B级'),
('PLANT-4', '月季', '丰花月季', '2024-03-12', '2024-04-01', 1000, 400.00,
 NULL, 'JZJ-2024-0056', '孙质检',
 '花卉市场', '广州', '2024-04-20', '800盆', '周销售', '观赏花卉',
 '守信示范', '无失信记录', '≥98%', '无投诉', '无', '无', 'A级'
);
