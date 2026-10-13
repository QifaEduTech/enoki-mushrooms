-- Idempotent menu registration for the traceability database page.
INSERT INTO sys_menu
(menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
 menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES
(2060, CONVERT(0xE6BAAFE6BA90 USING utf8mb4), 0, 10, 'traceability', 'traceability/index', NULL,
 1, 0, 'C', '0', '0', '', 'monitor', 'admin', NOW(), '', NULL,
 CONVERT(0xE6BAAFE6BA90E695B0E68DAEE5BA93E4BFA1E681AFE4B88EE4BA8CE7BBB4E7A081 USING utf8mb4))
ON DUPLICATE KEY UPDATE
 menu_name=VALUES(menu_name), parent_id=VALUES(parent_id), order_num=VALUES(order_num),
 path=VALUES(path), component=VALUES(component), menu_type=VALUES(menu_type),
 visible='0', status='0', icon=VALUES(icon), remark=VALUES(remark);

INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT role_id, 2060 FROM sys_role WHERE status='0';
