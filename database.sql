-- 创建数据库
CREATE DATABASE IF NOT EXISTS hotel_management CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE hotel_management;

-- 创建角色表
CREATE TABLE IF NOT EXISTS role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    NAME VARCHAR(50) NOT NULL,
    description VARCHAR(255)
);

-- 创建用户表
CREATE TABLE IF NOT EXISTS USER (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    PASSWORD VARCHAR(100) NOT NULL,
    NAME VARCHAR(50) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    email VARCHAR(100),
    id_card VARCHAR(20)
);

-- 创建用户-角色关联表
CREATE TABLE IF NOT EXISTS user_role (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES USER(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES role(id) ON DELETE CASCADE
);

-- 创建房型表
CREATE TABLE IF NOT EXISTS room_type (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    NAME VARCHAR(50) NOT NULL,
    description VARCHAR(255),
    price DOUBLE NOT NULL,
    capacity INT NOT NULL,
    facilities VARCHAR(255),
    image TEXT
);

-- 创建房间表
CREATE TABLE IF NOT EXISTS room (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    room_number VARCHAR(20) NOT NULL UNIQUE,
    STATUS VARCHAR(20) NOT NULL,
    room_type_id BIGINT NOT NULL,
    FOREIGN KEY (room_type_id) REFERENCES room_type(id) ON DELETE CASCADE
);

-- 创建订单表
CREATE TABLE IF NOT EXISTS order_table (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_number VARCHAR(50) NOT NULL UNIQUE,
    create_time DATETIME NOT NULL,
    check_in_time DATETIME NOT NULL,
    check_out_time DATETIME NOT NULL,
    total_price DOUBLE NOT NULL,
    STATUS VARCHAR(20) NOT NULL,
    user_id BIGINT NOT NULL,
    room_id BIGINT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES USER(id) ON DELETE CASCADE,
    FOREIGN KEY (room_id) REFERENCES room(id) ON DELETE CASCADE
);

-- 创建酒店信息表
CREATE TABLE IF NOT EXISTS hotel_info (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    NAME VARCHAR(100) NOT NULL,
    address VARCHAR(255),
    phone VARCHAR(20),
    email VARCHAR(100),
    description TEXT
);

-- 创建服务日志表
CREATE TABLE IF NOT EXISTS service_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    TYPE VARCHAR(50) NOT NULL,
    content TEXT,
    STATUS VARCHAR(20),
    create_time DATETIME,
    user_id BIGINT,
    FOREIGN KEY (user_id) REFERENCES USER(id) ON DELETE CASCADE
);

-- 插入初始角色数据
INSERT INTO role (NAME, description) VALUES
('ADMIN', '管理员'),
('FRONT', '前台'),
('USER', '普通用户');

-- 插入初始房型数据
INSERT INTO room_type (NAME, description, price, capacity, facilities) VALUES
('标准间', '标准双人间，配备基本设施', 200.00, 2, '空调、电视、独立卫浴'),
('豪华间', '豪华双人间，配备豪华设施', 350.00, 2, '空调、电视、独立卫浴、迷你吧、保险箱'),
('单人间', '标准单人间，配备基本设施', 150.00, 1, '空调、电视、独立卫浴'),
('套房', '豪华套房，配备齐全设施', 500.00, 4, '空调、电视、独立卫浴、迷你吧、保险箱、客厅');

-- 插入初始房间数据
INSERT INTO room (room_number, STATUS, room_type_id) VALUES
('101', '可用', 1),
('102', '可用', 1),
('103', '可用', 2),
('104', '可用', 2),
('105', '可用', 3),
('106', '可用', 3),
('201', '可用', 4),
('202', '可用', 4);

-- 插入初始用户数据（密码为123456，明文）
INSERT INTO USER (username, PASSWORD, NAME, phone, email, id_card) VALUES
('admin', '123456', '管理员', '13800138000', 'admin@hotel.com', '110101199001010001'),
('front', '123456', '前台', '13800138001', 'front@hotel.com', '110101199001010002'),
('user', '123456', '测试用户', '13800138002', 'user@hotel.com', '110101199001010003');

-- 关联用户和角色
INSERT INTO user_role (user_id, role_id) VALUES
(1, 1), -- 管理员关联ADMIN角色
(2, 2), -- 前台关联FRONT角色
(3, 3); -- 测试用户关联USER角色


-- 插入初始酒店信息
INSERT INTO hotel_info (NAME, address, phone, email, description) VALUES
('翻斗花园酒店', '翻斗花园1-1', '10086', '10086@qq.com', '挺好');


