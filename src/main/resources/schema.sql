-- ================================================
-- 用户管理系统数据库变更脚本 v2.0
-- 双角色权限系统升级
-- ================================================

-- 修改 user 表，增加 role 和 status 字段
ALTER TABLE `user` ADD COLUMN `role` VARCHAR(20) NOT NULL DEFAULT 'user' COMMENT '角色：admin/user';
ALTER TABLE `user` ADD COLUMN `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1正常 0禁用';
ALTER TABLE `user` ADD COLUMN `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;
ALTER TABLE `user` ADD COLUMN `password_hash` VARCHAR(255) COMMENT 'BCrypt加密密码';

-- 为 password_hash 字段设置值（从原有 password 字段迁移并加密，实际部署时需要运行加密脚本）
-- 这里暂时设置为 NULL，后续登录时会自动处理

-- 操作日志表
CREATE TABLE IF NOT EXISTS `operation_log` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `user_id` INT NOT NULL COMMENT '操作用户ID',
    `username` VARCHAR(50) NOT NULL COMMENT '操作用户名',
    `action` VARCHAR(50) NOT NULL COMMENT '操作类型',
    `target` VARCHAR(100) COMMENT '操作对象',
    `detail` VARCHAR(500) COMMENT '操作详情',
    `ip` VARCHAR(50) COMMENT 'IP地址',
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_action` (`action`),
    INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- ================================================
-- 初始化管理员账号
-- 密码: admin123 的 BCrypt hash (强度10轮)
-- 实际hash值: $2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKtBiMkhcCQ0wOB7jFKnVD2DvzG6
-- ================================================
INSERT INTO `user` (`username`, `password_hash`, `role`, `status`) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKtBiMkhcCQ0wOB7jFKnVD2DvzG6', 'admin', 1);

-- 创建测试用普通用户
INSERT INTO `user` (`username`, `password_hash`, `role`, `status`, `email`, `phone`) VALUES
('testuser', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKtBiMkhcCQ0wOB7jFKnVD2DvzG6', 'user', 1, 'test@example.com', '13800138000');