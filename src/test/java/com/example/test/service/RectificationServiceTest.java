package com.example.test.service;

import com.example.dao.RectificationTaskDAO;
import com.example.dao.ViolationRecordDAO;
import com.example.model.PageResult;
import com.example.model.RectificationTask;
import com.example.model.ViolationRecord;
import com.example.service.RectificationService;
import com.example.test.BaseTest;
import com.example.util.AppConstants;
import com.example.util.BusinessException;
import org.junit.Test;

import java.sql.SQLException;
import java.util.List;

import static org.junit.Assert.*;

public class RectificationServiceTest extends BaseTest {

    private RectificationService rectService = new RectificationService();
    private ViolationRecordDAO violationDAO = new ViolationRecordDAO();
    private RectificationTaskDAO taskDAO = new RectificationTaskDAO();

    @Override
    protected void initTestData() throws SQLException {
        truncateTable("rectification_task");
        truncateTable("violation_record");
        truncateTable("garbage_record");
        executeSQL("INSERT IGNORE INTO user (id, username, password_hash, role, status) VALUES " +
                "(1, 'rectuser1', 'hash1', 'user', 1)");
        executeSQL("INSERT INTO garbage_record (id, user_id, image_name, image_path, recommended_category, selected_category, is_correct, status) VALUES " +
                "(100, 1, 'a.jpg', '/a.jpg', '可回收物', '其他垃圾', 0, 'PENDING')");
        executeSQL("INSERT INTO violation_record (id, record_id, user_id, violation_type, description, level, status, create_time) VALUES " +
                "(200, 100, 1, '分类错误', '测试违规', 'LOW', 'PENDING', NOW())");
    }

    @Test
    public void testCreateTask() {
        Long taskId = rectService.createTask(200L, 1L, "请重新分类投放", "2026-05-01");
        assertNotNull("创建任务应返回ID", taskId);

        RectificationTask task = taskDAO.findById(taskId);
        assertNotNull(task);
        assertEquals("请重新分类投放", task.getRequirement());
        assertEquals(AppConstants.RECT_STATUS_PENDING, task.getStatus());
    }

    @Test
    public void testCreateTaskNullViolationId() {
        try {
            rectService.createTask(null, 1L, "要求", "2026-05-01");
            fail("应抛出BusinessException");
        } catch (BusinessException e) {
            assertEquals(400, e.getCode());
        }
    }

    @Test
    public void testCreateTaskEmptyRequirement() {
        try {
            rectService.createTask(200L, 1L, "", "2026-05-01");
            fail("应抛出BusinessException");
        } catch (BusinessException e) {
            assertEquals(400, e.getCode());
        }
    }

    @Test
    public void testCreateTaskNonExistentViolation() {
        try {
            rectService.createTask(99999L, 1L, "要求", "2026-05-01");
            fail("应抛出BusinessException");
        } catch (BusinessException e) {
            assertEquals(404, e.getCode());
        }
    }

    @Test
    public void testCreateDuplicateTask() {
        rectService.createTask(200L, 1L, "第一次要求", "2026-05-01");

        try {
            rectService.createTask(200L, 1L, "重复要求", "2026-05-01");
            fail("已有未完成任务时应抛出异常");
        } catch (BusinessException e) {
            assertEquals(400, e.getCode());
        }
    }

    @Test
    public void testSubmitRectification() {
        Long taskId = rectService.createTask(200L, 1L, "要求", "2026-05-01");
        assertNotNull(taskId);

        rectService.submitRectification(taskId, "已完成整改", "/output/rect.jpg");

        RectificationTask task = taskDAO.findById(taskId);
        assertEquals(AppConstants.RECT_STATUS_SUBMITTED, task.getStatus());
        assertEquals("已完成整改", task.getSubmitDesc());
    }

    @Test
    public void testSubmitNullTaskId() {
        try {
            rectService.submitRectification(null, "说明", null);
            fail("应抛出BusinessException");
        } catch (BusinessException e) {
            assertEquals(400, e.getCode());
        }
    }

    @Test
    public void testSubmitEmptyDesc() {
        Long taskId = rectService.createTask(200L, 1L, "要求", "2026-05-01");
        try {
            rectService.submitRectification(taskId, "", null);
            fail("应抛出BusinessException");
        } catch (BusinessException e) {
            assertEquals(400, e.getCode());
        }
    }

    @Test
    public void testReviewTaskApproved() {
        Long taskId = rectService.createTask(200L, 1L, "要求", "2026-05-01");
        rectService.submitRectification(taskId, "已完成", "/output/rect.jpg");

        rectService.reviewTask(taskId, AppConstants.RECT_STATUS_APPROVED, "通过");

        RectificationTask task = taskDAO.findById(taskId);
        assertEquals(AppConstants.RECT_STATUS_APPROVED, task.getStatus());

        ViolationRecord violation = violationDAO.findById(200L);
        assertEquals("复核通过应更新违规状态为RECTIFIED", 
                AppConstants.VIOLATION_STATUS_RECTIFIED, violation.getStatus());
    }

    @Test
    public void testReviewTaskRejected() {
        Long taskId = rectService.createTask(200L, 1L, "要求", "2026-05-01");
        rectService.submitRectification(taskId, "已整改", "/output/rect.jpg");

        rectService.reviewTask(taskId, AppConstants.RECT_STATUS_REJECTED, "整改不充分");

        RectificationTask task = taskDAO.findById(taskId);
        assertEquals(AppConstants.RECT_STATUS_REJECTED, task.getStatus());
    }

    @Test
    public void testReviewNotSubmittedTask() {
        Long taskId = rectService.createTask(200L, 1L, "要求", "2026-05-01");

        try {
            rectService.reviewTask(taskId, AppConstants.RECT_STATUS_APPROVED, "通过");
            fail("PENDING状态不应允许复核");
        } catch (BusinessException e) {
            assertEquals(400, e.getCode());
        }
    }

    @Test
    public void testGetUserTasks() {
        PageResult<RectificationTask> page = rectService.getUserTasks(1L, 1, 10);
        assertNotNull(page);
    }

    @Test
    public void testGetAllTasks() {
        PageResult<RectificationTask> page = rectService.getAllTasks(1, 10, null);
        assertNotNull(page);
    }

    @Test
    public void testGetById() {
        Long taskId = rectService.createTask(200L, 1L, "要求", "2026-05-01");
        RectificationTask found = rectService.getById(taskId);
        assertNotNull(found);

        assertNull("null ID应返回null", rectService.getById(null));
    }
}
