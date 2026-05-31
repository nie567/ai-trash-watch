package com.example.test.service;

import com.example.dao.RectificationTaskDAO;
import com.example.dao.ViolationRecordDAO;
import com.example.model.RectificationTask;
import com.example.model.ViolationRecord;
import com.example.service.RectificationService;
import com.example.test.BaseTest;
import com.example.util.AppConstants;
import com.example.util.BusinessException;
import org.junit.Test;

import java.sql.SQLException;

import static org.junit.Assert.*;

public class RectificationBusinessLogicTest extends BaseTest {

    private RectificationService rectService = new RectificationService(new RectificationTaskDAO(), new ViolationRecordDAO());
    private ViolationRecordDAO violationDAO = new ViolationRecordDAO();
    private RectificationTaskDAO taskDAO = new RectificationTaskDAO();

    private static final Long TEST_USER_ID = 7001L;
    private static final Long VIOLATION_USER_ID = 7002L;

    @Override
    protected void initTestData() throws SQLException {
        truncateTable("rectification_task");
        truncateTable("violation_record");
        truncateTable("detection_result");
        truncateTable("garbage_record");
        executeSQL("INSERT IGNORE INTO user (id, username, password_hash, role, status) VALUES " +
                "(" + TEST_USER_ID + ", 'rect_user1', 'recthash1', 'user', 1), " +
                "(" + VIOLATION_USER_ID + ", 'rect_user2', 'recthash2', 'user', 1)");
    }

    @Override
    protected void cleanTestData() throws SQLException {
        truncateTable("rectification_task");
        truncateTable("violation_record");
        truncateTable("detection_result");
        truncateTable("garbage_record");
        deleteFromTable("user", "id IN (" + TEST_USER_ID + ", " + VIOLATION_USER_ID + ")");
    }

    @Test
    public void testCreateTaskWithNullUserIdUsesViolationUserId() throws SQLException {
        executeSQL("INSERT INTO garbage_record (id, user_id, image_name, image_path, recommended_category, selected_category, is_correct, is_mixed, status) VALUES " +
                "(8001, " + VIOLATION_USER_ID + ", 'rect.jpg', '/rect.jpg', '可回收物', '有害垃圾', 0, 0, 'PENDING')");

        ViolationRecord violation = new ViolationRecord();
        violation.setRecordId(8001L);
        violation.setUserId(VIOLATION_USER_ID);
        violation.setViolationType("分类错误");
        violation.setDescription("测试整改");
        violation.setLevel(AppConstants.VIOLATION_LEVEL_LOW);
        violation.setStatus(AppConstants.VIOLATION_STATUS_PENDING);
        Long violationId = violationDAO.insert(violation);
        assertNotNull("违规记录应插入成功", violationId);

        Long taskId = rectService.createTask(violationId, null, "请整改分类错误", "2026-12-31");
        assertNotNull("整改任务应创建成功", taskId);

        RectificationTask task = taskDAO.findById(taskId);
        assertNotNull(task);
        assertEquals("userId为null时应使用违规记录的userId",
                VIOLATION_USER_ID, task.getUserId());
    }

    @Test
    public void testReviewRejectedViolationStatusUnchanged() throws SQLException {
        executeSQL("INSERT INTO garbage_record (id, user_id, image_name, image_path, recommended_category, selected_category, is_correct, is_mixed, status) VALUES " +
                "(8002, " + TEST_USER_ID + ", 'reject.jpg', '/reject.jpg', '可回收物', '有害垃圾', 0, 0, 'PENDING')");

        ViolationRecord violation = new ViolationRecord();
        violation.setRecordId(8002L);
        violation.setUserId(TEST_USER_ID);
        violation.setViolationType("分类错误");
        violation.setDescription("整改复核测试");
        violation.setLevel(AppConstants.VIOLATION_LEVEL_LOW);
        violation.setStatus(AppConstants.VIOLATION_STATUS_PENDING);
        Long violationId = violationDAO.insert(violation);
        assertNotNull(violationId);

        Long taskId = rectService.createTask(violationId, TEST_USER_ID, "请整改", "2026-12-31");
        assertNotNull(taskId);

        rectService.submitRectification(taskId, "已完成整改", "/evidence.jpg");

        String statusBefore = violationDAO.findById(violationId).getStatus();

        rectService.reviewTask(taskId, AppConstants.RECT_STATUS_REJECTED, "整改不通过");

        ViolationRecord afterReview = violationDAO.findById(violationId);
        assertNotNull(afterReview);
        assertEquals("复核REJECTED时违规状态不应变为RECTIFIED",
                AppConstants.VIOLATION_STATUS_PENDING, afterReview.getStatus());
    }
}
