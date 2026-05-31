package com.example.service;

import com.example.dao.RectificationTaskDAO;
import com.example.dao.UserDAO;
import com.example.dao.ViolationRecordDAO;
import com.example.model.DetectionResult;
import com.example.model.GarbageRecord;
import com.example.model.PageResult;
import com.example.model.RectificationTask;
import com.example.model.User;
import com.example.model.ViolationRecord;
import com.example.util.AppConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.util.List;

/**
 * 违规记录业务服务
 */
public class ViolationService {

    private static final Logger logger = LoggerFactory.getLogger(ViolationService.class);

    private final ViolationRecordDAO violationDAO;
    private final RectificationTaskDAO rectTaskDAO;
    private final UserDAO userDAO;

    public ViolationService() {
        this.violationDAO = new ViolationRecordDAO();
        this.rectTaskDAO = new RectificationTaskDAO();
        this.userDAO = new UserDAO();
    }

    public ViolationService(ViolationRecordDAO violationDAO, RectificationTaskDAO rectTaskDAO) {
        this.violationDAO = violationDAO;
        this.rectTaskDAO = rectTaskDAO;
        this.userDAO = new UserDAO();
    }

    /**
     * 判断是否需要生成违规记录，若需要则创建（无外部事务）
     */
    public void createViolationIfNeeded(GarbageRecord record, List<DetectionResult> details) {
        createViolationIfNeeded(record, details, null);
    }

    /**
     * 判断是否需要生成违规记录，若需要则创建（支持外部事务连接）
     * 触发条件: isCorrect==0 或 (isMixed==1 且用户选择单一类别)
     */
    public void createViolationIfNeeded(GarbageRecord record, List<DetectionResult> details, Connection conn) {
        // 管理员投放不生成违规记录（管理员负责监督，不是被监管对象）
        if (record.getUserId() != null) {
            User user = userDAO.findById(record.getUserId().intValue());
            if (user != null && user.isAdmin()) {
                logger.info("管理员投放记录不生成违规, userId={}, recordId={}", record.getUserId(), record.getId());
                return;
            }
        }

        boolean needViolation = false;
        String violationType;

        if (record.getIsCorrect() != null && record.getIsCorrect() == 0) {
            needViolation = true;
            // 判断违规类型
            if (record.getIsMixed() != null && record.getIsMixed() == 1) {
                violationType = "混投";
            } else {
                violationType = "分类错误";
            }
        } else if (record.getIsMixed() != null && record.getIsMixed() == 1
                && record.getSelectedCategory() != null
                && !AppConstants.CATEGORY_MIXED.equals(record.getSelectedCategory())) {
            needViolation = true;
            violationType = "混投";
        } else {
            return;
        }

        if (!needViolation) return;

        // 确定违规级别
        String level = determineLevel(record.getUserId(), violationType);

        // 构建违规描述
        String description = buildDescription(record, violationType);

        // 创建违规记录
        ViolationRecord violation = new ViolationRecord();
        violation.setRecordId(record.getId());
        violation.setUserId(record.getUserId());
        violation.setViolationType(violationType);
        violation.setDescription(description);
        violation.setLevel(level);
        violation.setStatus(AppConstants.VIOLATION_STATUS_PENDING);

        violationDAO.insert(violation, conn);
    }

    /**
     * 确定违规级别
     * 首次→LOW，混投→MEDIUM，累计3次以上→HIGH
     */
    private String determineLevel(Long userId, String violationType) {
        int historyCount = violationDAO.countByUserIdAll(userId);

        if ("混投".equals(violationType)) {
            return AppConstants.VIOLATION_LEVEL_MEDIUM;
        }
        if (historyCount >= 3) {
            return AppConstants.VIOLATION_LEVEL_HIGH;
        }
        return AppConstants.VIOLATION_LEVEL_LOW;
    }

    /**
     * 构建违规描述
     */
    private String buildDescription(GarbageRecord record, String violationType) {
        StringBuilder sb = new StringBuilder();
        sb.append(violationType);
        if (record.getRecommendedCategory() != null) {
            sb.append("，推荐类别：").append(record.getRecommendedCategory());
        }
        if (record.getSelectedCategory() != null) {
            sb.append("，选择类别：").append(record.getSelectedCategory());
        }
        return sb.toString();
    }

    /**
     * 用户违规记录分页
     */
    public PageResult<ViolationRecord> getUserViolations(Long userId, int page, int pageSize) {
        return getUserViolations(userId, page, pageSize, null);
    }

    /**
     * 用户违规记录分页，支持状态筛选
     */
    public PageResult<ViolationRecord> getUserViolations(Long userId, int page, int pageSize, String status) {
        if (page < 1) page = AppConstants.DEFAULT_PAGE_NUM;
        if (pageSize < 1) pageSize = AppConstants.DEFAULT_PAGE_SIZE;
        if (pageSize > AppConstants.MAX_PAGE_SIZE) pageSize = AppConstants.MAX_PAGE_SIZE;

        int offset = (page - 1) * pageSize;
        List<ViolationRecord> list = violationDAO.findByUserId(userId, offset, pageSize, status);
        int total = violationDAO.countByUserId(userId, status);
        return new PageResult<>(list, total, page, pageSize);
    }

    /**
     * 管理员违规记录分页
     */
    public PageResult<ViolationRecord> getAllViolations(int page, int pageSize, String status) {
        if (page < 1) page = AppConstants.DEFAULT_PAGE_NUM;
        if (pageSize < 1) pageSize = AppConstants.DEFAULT_PAGE_SIZE;
        if (pageSize > AppConstants.MAX_PAGE_SIZE) pageSize = AppConstants.MAX_PAGE_SIZE;

        int offset = (page - 1) * pageSize;
        List<ViolationRecord> list = violationDAO.findAll(offset, pageSize, status);
        int total = violationDAO.countAll(status);
        return new PageResult<>(list, total, page, pageSize);
    }

    /**
     * 按ID查询违规记录
     */
    public ViolationRecord getById(Long id) {
        if (id == null) {
            return null;
        }
        return violationDAO.findById(id);
    }
}
