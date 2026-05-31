-- ============================================================
-- AI-TrashWatch 垃圾分类识别与投放监管系统 - 完整数据库初始化脚本
-- 运行方式: mysql -u root -p < schema.sql
-- ============================================================

-- ============================================================
-- 1. 用户表
-- ============================================================
CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    `password_hash` VARCHAR(255) DEFAULT NULL COMMENT 'BCrypt加密密码',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `role` VARCHAR(20) NOT NULL DEFAULT 'user' COMMENT '角色：admin/user',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1正常 0禁用',
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_role` (`role`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ============================================================
-- 2. 操作日志表
-- ============================================================
CREATE TABLE IF NOT EXISTS `operation_log` (
    `id` INT AUTO_INCREMENT PRIMARY KEY COMMENT '日志ID',
    `user_id` BIGINT NOT NULL COMMENT '操作用户ID',
    `username` VARCHAR(50) NOT NULL COMMENT '操作用户名',
    `action` VARCHAR(50) NOT NULL COMMENT '操作类型',
    `target` VARCHAR(100) DEFAULT NULL COMMENT '操作对象',
    `detail` VARCHAR(500) DEFAULT NULL COMMENT '操作详情',
    `ip` VARCHAR(50) DEFAULT NULL COMMENT 'IP地址',
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_action` (`action`),
    INDEX `idx_create_time` (`create_time`),
    CONSTRAINT `fk_operation_log_user` FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- ============================================================
-- 3. 垃圾分类规则表
-- ============================================================
CREATE TABLE IF NOT EXISTS garbage_rule (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '规则ID',
    `class_name` VARCHAR(50) NOT NULL UNIQUE COMMENT '检测类别英文名',
    `mapped_category` VARCHAR(50) NOT NULL COMMENT '对应中文分类',
    `description` VARCHAR(255) DEFAULT NULL COMMENT '描述',
    `status` TINYINT DEFAULT 1 COMMENT '状态：1启用 0停用'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='垃圾分类规则表';

INSERT INTO garbage_rule (class_name, mapped_category, description) VALUES
('BIODEGRADABLE', '厨余垃圾', '可生物降解垃圾'),
('CARDBOARD', '可回收物', '硬纸板'),
('GLASS', '可回收物', '玻璃制品'),
('METAL', '可回收物', '金属制品'),
('PAPER', '可回收物', '纸张类'),
('PLASTIC', '可回收物', '塑料制品');

-- ============================================================
-- 4. 垃圾投放记录表
-- ============================================================
CREATE TABLE IF NOT EXISTS garbage_record (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `image_name` VARCHAR(255) DEFAULT NULL COMMENT '图片原始名称',
    `image_path` VARCHAR(255) NOT NULL COMMENT '图片存储路径',
    `result_image_path` VARCHAR(255) DEFAULT NULL COMMENT '检测结果图片路径',
    `detected_summary` VARCHAR(255) DEFAULT NULL COMMENT '检测摘要',
    `recommended_category` VARCHAR(50) DEFAULT NULL COMMENT '系统推荐分类',
    `selected_category` VARCHAR(50) DEFAULT NULL COMMENT '用户选择分类',
    `final_category` VARCHAR(50) DEFAULT NULL COMMENT '最终确认分类',
    `is_mixed` TINYINT DEFAULT 0 COMMENT '是否为混合垃圾：0否 1是',
    `is_correct` TINYINT DEFAULT NULL COMMENT '是否正确分类：1正确 0错误',
    `status` VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态：PENDING/REVIEWED',
    `review_comment` VARCHAR(255) DEFAULT NULL COMMENT '审核评语',
    `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_create_time` (`create_time`),
    CONSTRAINT `fk_garbage_record_user` FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='垃圾投放记录表';

-- ============================================================
-- 5. 检测明细表
-- ============================================================
CREATE TABLE IF NOT EXISTS detection_result (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '检测ID',
    `record_id` BIGINT NOT NULL COMMENT '关联投放记录ID',
    `class_name` VARCHAR(50) NOT NULL COMMENT '检测类别',
    `confidence` DECIMAL(5,2) DEFAULT NULL COMMENT '置信度',
    `x_min` INT DEFAULT NULL COMMENT '检测框左上角X',
    `y_min` INT DEFAULT NULL COMMENT '检测框左上角Y',
    `x_max` INT DEFAULT NULL COMMENT '检测框右下角X',
    `y_max` INT DEFAULT NULL COMMENT '检测框右下角Y',
    `mapped_category` VARCHAR(50) DEFAULT NULL COMMENT '映射中文分类',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX `idx_record_id` (`record_id`),
    CONSTRAINT `fk_detection_result_record` FOREIGN KEY (`record_id`) REFERENCES `garbage_record`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='检测结果明细表';

-- ============================================================
-- 6. 违规记录表
-- ============================================================
CREATE TABLE IF NOT EXISTS violation_record (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '违规ID',
    `record_id` BIGINT NOT NULL COMMENT '关联投放记录ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `violation_type` VARCHAR(50) DEFAULT NULL COMMENT '违规类型',
    `description` VARCHAR(255) DEFAULT NULL COMMENT '违规描述',
    `level` VARCHAR(20) DEFAULT NULL COMMENT '违规等级',
    `status` VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态：PENDING/RESOLVED',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX `idx_record_id` (`record_id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_status` (`status`),
    CONSTRAINT `fk_violation_record_user` FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_violation_record_garbage_record` FOREIGN KEY (`record_id`) REFERENCES `garbage_record`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='违规记录表';

-- ============================================================
-- 7. 整改任务表
-- ============================================================
CREATE TABLE IF NOT EXISTS rectification_task (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '任务ID',
    `violation_id` BIGINT NOT NULL COMMENT '关联违规ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `requirement` VARCHAR(255) DEFAULT NULL COMMENT '整改要求',
    `deadline` DATETIME DEFAULT NULL COMMENT '截止日期',
    `status` VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态：PENDING/SUBMITTED/REVIEWED',
    `submit_desc` VARCHAR(255) DEFAULT NULL COMMENT '提交说明',
    `submit_image_path` VARCHAR(255) DEFAULT NULL COMMENT '提交图片路径',
    `review_result` VARCHAR(50) DEFAULT NULL COMMENT '审核结果',
    `review_comment` VARCHAR(255) DEFAULT NULL COMMENT '审核评语',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_violation_id` (`violation_id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_status` (`status`),
    CONSTRAINT `fk_rectification_task_user` FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_rectification_task_violation` FOREIGN KEY (`violation_id`) REFERENCES `violation_record`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='整改任务表';

-- ============================================================
-- 8. 知识库表
-- ============================================================
CREATE TABLE IF NOT EXISTS knowledge_base (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '知识ID',
    `title` VARCHAR(100) DEFAULT NULL COMMENT '标题',
    `garbage_type` VARCHAR(50) DEFAULT NULL COMMENT '垃圾类型',
    `content` TEXT DEFAULT NULL COMMENT '内容',
    `image_path` VARCHAR(255) DEFAULT NULL COMMENT '图片路径',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='垃圾分类知识库表';

-- ============================================================
-- 9. 默认数据
-- ============================================================

-- 管理员账号（密码：admin123，BCrypt hash 强度10轮）
INSERT INTO `user` (`username`, `password_hash`, `role`, `status`) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKtBiMkhcCQ0wOB7jFKnVD2DvzG6', 'admin', 1);

-- 测试用户（密码：admin123）
INSERT INTO `user` (`username`, `password_hash`, `role`, `status`, `email`, `phone`) VALUES
('testuser', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKtBiMkhcCQ0wOB7jFKnVD2DvzG6', 'user', 1, 'test@example.com', '13800138000');
