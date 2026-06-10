-- 数据血缘管理系统初始化脚本

-- 创建数据库
CREATE DATABASE IF NOT EXISTS lineage_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE lineage_db;

-- 插入管理员用户 (密码: admin123，已使用BCrypt加密)
INSERT INTO sys_user (username, password, real_name, email, phone, role, status, create_time, update_time) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '系统管理员', 'admin@lineage.com', '13800138000', 'ADMIN', 1, NOW(), NOW()),
('user1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '张三', 'zhangsan@lineage.com', '13800138001', 'USER', 1, NOW(), NOW());

-- 插入示例数据源
INSERT INTO data_source (name, type, host, port, `database`, username, password, description, status, create_time, update_time, create_by) VALUES
('MySQL生产库', 'MySQL', 'mysql-prod.example.com', 3306, 'production_db', 'root', 'password', '生产环境MySQL数据库', 1, NOW(), NOW(), 1),
('MySQL测试库', 'MySQL', 'mysql-test.example.com', 3306, 'test_db', 'root', 'password', '测试环境MySQL数据库', 1, NOW(), NOW(), 1),
('PostgreSQL数仓', 'PostgreSQL', 'pg-warehouse.example.com', 5432, 'warehouse_db', 'postgres', 'password', 'PostgreSQL数据仓库', 1, NOW(), NOW(), 1),
('Hive大数据', 'Hive', 'hive-server.example.com', 10000, 'default', 'hive', 'password', 'Hive大数据平台', 1, NOW(), NOW(), 1);

-- 插入示例数据表
INSERT INTO data_table (source_id, table_name, table_comment, column_count, row_count, table_type, description, create_time, update_time) VALUES
(1, 'user_info', '用户信息表', 8, 10000, 'TABLE', '存储用户基本信息', NOW(), NOW()),
(1, 'order_info', '订单信息表', 10, 50000, 'TABLE', '存储订单详细信息', NOW(), NOW()),
(1, 'product_info', '商品信息表', 6, 5000, 'TABLE', '存储商品基本信息', NOW(), NOW()),
(2, 'test_user', '测试用户表', 8, 100, 'TABLE', '测试环境用户表', NOW(), NOW()),
(3, 'dw_user_dim', '用户维度表', 12, 10000, 'TABLE', '数据仓库用户维度', NOW(), NOW()),
(3, 'dw_order_fact', '订单事实表', 15, 100000, 'TABLE', '数据仓库订单事实表', NOW(), NOW()),
(4, 'ods_user_info', 'ODS用户表', 8, 10000, 'TABLE', '原始数据层用户表', NOW(), NOW()),
(4, 'dwd_user_info', 'DWD用户表', 10, 10000, 'TABLE', '明细数据层用户表', NOW(), NOW());

-- 插入示例数据列
INSERT INTO data_column (table_id, column_name, data_type, column_length, column_comment, is_primary_key, is_nullable, default_value, column_order, create_time) VALUES
(1, 'user_id', 'BIGINT', 20, '用户ID', 1, 0, NULL, 1, NOW()),
(1, 'username', 'VARCHAR', 50, '用户名', 0, 0, NULL, 2, NOW()),
(1, 'email', 'VARCHAR', 100, '邮箱', 0, 1, NULL, 3, NOW()),
(1, 'phone', 'VARCHAR', 20, '手机号', 0, 1, NULL, 4, NOW()),
(1, 'status', 'TINYINT', 1, '状态', 0, 0, '1', 5, NOW()),
(1, 'create_time', 'DATETIME', NULL, '创建时间', 0, 0, 'CURRENT_TIMESTAMP', 6, NOW()),
(1, 'update_time', 'DATETIME', NULL, '更新时间', 0, 0, 'CURRENT_TIMESTAMP', 7, NOW()),
(1, 'is_deleted', 'TINYINT', 1, '是否删除', 0, 0, '0', 8, NOW()),

(2, 'order_id', 'BIGINT', 20, '订单ID', 1, 0, NULL, 1, NOW()),
(2, 'user_id', 'BIGINT', 20, '用户ID', 0, 0, NULL, 2, NOW()),
(2, 'product_id', 'BIGINT', 20, '商品ID', 0, 0, NULL, 3, NOW()),
(2, 'order_amount', 'DECIMAL', 10, '订单金额', 0, 0, NULL, 4, NOW()),
(2, 'order_status', 'TINYINT', 1, '订单状态', 0, 0, '0', 5, NOW()),
(2, 'create_time', 'DATETIME', NULL, '创建时间', 0, 0, 'CURRENT_TIMESTAMP', 6, NOW());

-- 插入示例血缘关系
INSERT INTO data_lineage (source_table_id, target_table_id, source_column_id, target_column_id, lineage_type, transform_rule, description, create_time, create_by) VALUES
(1, 5, NULL, NULL, 'TABLE', 'ETL抽取转换', '生产用户表到数仓用户维度表', NOW(), 1),
(2, 6, NULL, NULL, 'TABLE', 'ETL抽取转换', '生产订单表到数仓订单事实表', NOW(), 1),
(1, 7, NULL, NULL, 'TABLE', '直接同步', '生产用户表到ODS用户表', NOW(), 1),
(7, 8, NULL, NULL, 'TABLE', '清洗转换', 'ODS用户表到DWD用户表', NOW(), 1),
(3, 6, NULL, NULL, 'TABLE', 'JOIN关联', '商品表关联到订单事实表', NOW(), 1);

-- 查询验证
SELECT '数据初始化完成' AS status;
SELECT COUNT(*) AS user_count FROM sys_user;
SELECT COUNT(*) AS datasource_count FROM data_source;
SELECT COUNT(*) AS table_count FROM data_table;
SELECT COUNT(*) AS lineage_count FROM data_lineage;
