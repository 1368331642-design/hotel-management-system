-- 性能优化：为常用查询添加索引
-- 执行方式：mysql -u root -p hotel_management < database_indexes.sql

-- 订单表索引
-- 按用户ID查询订单
CREATE INDEX IF NOT EXISTS idx_order_user_id ON order_table(user_id);
-- 按状态查询订单（用于待支付订单清理）
CREATE INDEX IF NOT EXISTS idx_order_status ON order_table(status);
-- 按创建时间查询订单
CREATE INDEX IF NOT EXISTS idx_order_create_time ON order_table(create_time);
-- 按入住时间查询订单
CREATE INDEX IF NOT EXISTS idx_order_check_in_time ON order_table(check_in_time);
-- 按退房时间查询订单
CREATE INDEX IF NOT EXISTS idx_order_check_out_time ON order_table(check_out_time);
-- 复合索引：用户+状态（活跃订单查询）
CREATE INDEX IF NOT EXISTS idx_order_user_status ON order_table(user_id, status);

-- 房间表索引
-- 按房间类型查询
CREATE INDEX IF NOT EXISTS idx_room_type ON room(room_type_id);
-- 按状态查询可用房间
CREATE INDEX IF NOT EXISTS idx_room_status ON room(status);

-- 房间类型表索引
-- 按名称查询
CREATE INDEX IF NOT EXISTS idx_room_type_name ON room_type(name);

-- 用户表索引
-- 按用户名查询（登录验证）
CREATE INDEX IF NOT EXISTS idx_user_username ON user(username);