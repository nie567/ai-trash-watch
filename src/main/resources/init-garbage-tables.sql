-- ============================================================
-- 垃圾分类识别与投放监管系统 - 数据库初始化脚本
-- 在 user_management 数据库中执行
-- ============================================================

-- 1. 分类规则表
CREATE TABLE IF NOT EXISTS garbage_rule (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    class_name VARCHAR(50) NOT NULL UNIQUE,
    mapped_category VARCHAR(50) NOT NULL,
    description VARCHAR(255),
    status TINYINT DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 初始化分类规则数据
INSERT INTO garbage_rule (class_name, mapped_category, description) VALUES
('BIODEGRADABLE', '厨余垃圾', '可生物降解垃圾'),
('CARDBOARD', '可回收物', '硬纸板'),
('GLASS', '可回收物', '玻璃制品'),
('METAL', '可回收物', '金属制品'),
('PAPER', '可回收物', '纸张类'),
('PLASTIC', '可回收物', '塑料制品');

-- 2. 投放记录表
CREATE TABLE IF NOT EXISTS garbage_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    image_name VARCHAR(255),
    image_path VARCHAR(255) NOT NULL,
    result_image_path VARCHAR(255),
    detected_summary VARCHAR(255),
    recommended_category VARCHAR(50),
    selected_category VARCHAR(50),
    final_category VARCHAR(50),
    is_mixed TINYINT DEFAULT 0,
    is_correct TINYINT,
    status VARCHAR(20) DEFAULT 'PENDING',
    review_comment VARCHAR(255),
    remark VARCHAR(255),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. 检测明细表
CREATE TABLE IF NOT EXISTS detection_result (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    record_id BIGINT NOT NULL,
    class_name VARCHAR(50) NOT NULL,
    confidence DECIMAL(5,2),
    x_min INT,
    y_min INT,
    x_max INT,
    y_max INT,
    mapped_category VARCHAR(50),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_record_id (record_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4. 违规记录表
CREATE TABLE IF NOT EXISTS violation_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    record_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    violation_type VARCHAR(50),
    description VARCHAR(255),
    level VARCHAR(20),
    status VARCHAR(20) DEFAULT 'PENDING',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_record_id (record_id),
    INDEX idx_user_id (user_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5. 整改任务表
CREATE TABLE IF NOT EXISTS rectification_task (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    violation_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    requirement VARCHAR(255),
    deadline DATETIME,
    status VARCHAR(20) DEFAULT 'PENDING',
    submit_desc VARCHAR(255),
    submit_image_path VARCHAR(255),
    review_result VARCHAR(50),
    review_comment VARCHAR(255),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_violation_id (violation_id),
    INDEX idx_user_id (user_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 6. 知识库表
CREATE TABLE IF NOT EXISTS knowledge_base (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(100),
    garbage_type VARCHAR(50),
    content TEXT,
    image_path VARCHAR(255),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
