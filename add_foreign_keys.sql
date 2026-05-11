-- ============================================================
-- 酒店管理系统 - 添加外键约束
-- 目的：让 MySQL Workbench 反向工程自动生成完整 ER 图连线
-- 执行前请先备份数据库！
-- 执行方式：mysql -u root -p hotel_management < add_foreign_keys.sql
-- ============================================================

USE hotel_management;

-- ----------------------------------------------------------
-- 1. 给 room 表添加 hotel_id 外键（酒店 1:N 房间）
--    先添加字段，再设置约束
-- ----------------------------------------------------------
-- 如果 hotel_id 字段已存在则跳过（避免重复执行报错）
SET @exist = (SELECT COUNT(*) FROM information_schema.COLUMNS
              WHERE TABLE_SCHEMA = 'hotel_management'
                AND TABLE_NAME = 'room'
                AND COLUMN_NAME = 'hotel_id');

SET @sql = IF(@exist = 0,
    'ALTER TABLE room ADD COLUMN hotel_id BIGINT NOT NULL DEFAULT 1 COMMENT ''所属酒店ID''',
    'SELECT ''room.hotel_id already exists, skip'' AS info');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 添加外键约束
SET @fk_exist = (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
                 WHERE TABLE_SCHEMA = 'hotel_management'
                   AND TABLE_NAME = 'room'
                   AND CONSTRAINT_NAME = 'fk_room_hotel');

SET @sql = IF(@fk_exist = 0,
    'ALTER TABLE room ADD CONSTRAINT fk_room_hotel FOREIGN KEY (hotel_id) REFERENCES hotel_info(id) ON DELETE CASCADE ON UPDATE CASCADE',
    'SELECT ''fk_room_hotel already exists, skip'' AS info');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;


-- ----------------------------------------------------------
-- 2. 给 room_type 表添加 hotel_id 外键（酒店 1:N 房型）
-- ----------------------------------------------------------
SET @exist = (SELECT COUNT(*) FROM information_schema.COLUMNS
              WHERE TABLE_SCHEMA = 'hotel_management'
                AND TABLE_NAME = 'room_type'
                AND COLUMN_NAME = 'hotel_id');

SET @sql = IF(@exist = 0,
    'ALTER TABLE room_type ADD COLUMN hotel_id BIGINT NOT NULL DEFAULT 1 COMMENT ''所属酒店ID''',
    'SELECT ''room_type.hotel_id already exists, skip'' AS info');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @fk_exist = (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
                 WHERE TABLE_SCHEMA = 'hotel_management'
                   AND TABLE_NAME = 'room_type'
                   AND CONSTRAINT_NAME = 'fk_room_type_hotel');

SET @sql = IF(@fk_exist = 0,
    'ALTER TABLE room_type ADD CONSTRAINT fk_room_type_hotel FOREIGN KEY (hotel_id) REFERENCES hotel_info(id) ON DELETE CASCADE ON UPDATE CASCADE',
    'SELECT ''fk_room_type_hotel already exists, skip'' AS info');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;


-- ----------------------------------------------------------
-- 3. 给 order_table 表添加 hotel_id 外键（酒店 1:N 订单）
-- ----------------------------------------------------------
SET @exist = (SELECT COUNT(*) FROM information_schema.COLUMNS
              WHERE TABLE_SCHEMA = 'hotel_management'
                AND TABLE_NAME = 'order_table'
                AND COLUMN_NAME = 'hotel_id');

SET @sql = IF(@exist = 0,
    'ALTER TABLE order_table ADD COLUMN hotel_id BIGINT NOT NULL DEFAULT 1 COMMENT ''所属酒店ID''',
    'SELECT ''order_table.hotel_id already exists, skip'' AS info');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @fk_exist = (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
                 WHERE TABLE_SCHEMA = 'hotel_management'
                   AND TABLE_NAME = 'order_table'
                   AND CONSTRAINT_NAME = 'fk_order_hotel');

SET @sql = IF(@fk_exist = 0,
    'ALTER TABLE order_table ADD CONSTRAINT fk_order_hotel FOREIGN KEY (hotel_id) REFERENCES hotel_info(id) ON DELETE CASCADE ON UPDATE CASCADE',
    'SELECT ''fk_order_hotel already exists, skip'' AS info');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;


-- ----------------------------------------------------------
-- 执行完成提示
-- ----------------------------------------------------------
SELECT '外键添加完成！现在可以重新在 MySQL Workbench 中执行反向工程。' AS message;
