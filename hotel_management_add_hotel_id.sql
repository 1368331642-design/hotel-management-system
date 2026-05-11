DESCRIBE room;
DESCRIBE order_table;
DESCRIBE service_log;

-- 给 room 表添加 hotel_id 字段
ALTER TABLE room ADD COLUMN hotel_id BIGINT;
-- 添加外键约束，关联 hotel_info 表的主键
ALTER TABLE room 
ADD CONSTRAINT fk_room_hotel 
FOREIGN KEY (hotel_id) REFERENCES hotel_info(id);

-- 给 order_table 表添加 hotel_id 字段和外键
ALTER TABLE order_table ADD COLUMN hotel_id BIGINT;
ALTER TABLE order_table 
ADD CONSTRAINT fk_order_hotel 
FOREIGN KEY (hotel_id) REFERENCES hotel_info(id);

-- 给 service_log 表添加 hotel_id 字段和外键
ALTER TABLE service_log ADD COLUMN hotel_id BIGINT;
ALTER TABLE service_log 
ADD CONSTRAINT fk_service_hotel 
FOREIGN KEY (hotel_id) REFERENCES hotel_info(id);

