package com.example.test.integration;

import com.example.dao.*;
import com.example.model.*;
import com.example.service.*;
import com.example.test.BaseTest;
import com.example.util.AppConstants;
import com.example.util.BusinessException;
import org.junit.Test;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class RectificationFlowIntegrationTest extends BaseTest {

    private GarbageRecordService recordService = new GarbageRecordService();
    private ViolationService violationService = new ViolationService();
    private RectificationService rectService = new RectificationService();
    private GarbageRecordDAO recordDAO = new GarbageRecordDAO();
    private ViolationRecordDAO violationDAO = new ViolationRecordDAO();
    private RectificationTaskDAO taskDAO = new RectificationTaskDAO();

    @Override
    protected void initTestData() throws SQLException {
        truncateTable("detection_result");
        truncateTable("rectification_task");
        truncateTable("violation_record");
        truncateTable("garbage_record");
        truncateTable("garbage_rule");
        executeSQL("INSERT IGNORE INTO user (id, username, password_hash, role, status) VALUES " +
                "(1, 'flowuser', 'hash1', 'user', 1)");
        executeSQL("INSERT INTO garbage_rule (class_name, mapped_category, description, status) VALUES " +
                "('METAL', '可回收物', '金属制品', 1), " +
                "('BIODEGRADABLE', '厨余垃圾', '可生物降解', 1)");
    }

    @Test
    public void testFullRectificationFlow() {
        GarbageRecordSubmitDTO dto = new GarbageRecordSubmitDTO();
        dto.setImageName("wrong.jpg");
        dto.setImagePath("/input/wrong.jpg");
        dto.setRecommendedCategory("厨余垃圾");
        dto.setSelectedCategory("其他垃圾");
        dto.setIsMixed(0);

        Long recordId = recordService.saveRecord(1L, dto);
        assertNotNull("1. 保存投放记录", recordId);

        GarbageRecord record = recordDAO.findById(recordId);
        assertEquals("2. 应判定为错误投放", Integer.valueOf(0), record.getIsCorrect());

        List<ViolationRecord> violations = violationDAO.findByUserId(1L, 0, 10);
        assertEquals("3. 应自动生成1条违规记录", 1, violations.size());
        ViolationRecord violation = violations.get(0);
        assertEquals(AppConstants.VIOLATION_STATUS_PENDING, violation.getStatus());

        Long taskId = rectService.createTask(violation.getId(), 1L, "请重新分类投放", "2026-05-01");
        assertNotNull("4. 创建整改任务", taskId);

        RectificationTask task = taskDAO.findById(taskId);
        assertEquals(AppConstants.RECT_STATUS_PENDING, task.getStatus());

        rectService.submitRectification(taskId, "已按要求重新分类投放", "/output/rect.jpg");
        task = taskDAO.findById(taskId);
        assertEquals("5. 提交整改后状态应为SUBMITTED", AppConstants.RECT_STATUS_SUBMITTED, task.getStatus());

        rectService.reviewTask(taskId, AppConstants.RECT_STATUS_APPROVED, "整改通过");
        task = taskDAO.findById(taskId);
        assertEquals("6. 复核通过后状态应为APPROVED", AppConstants.RECT_STATUS_APPROVED, task.getStatus());

        violation = violationDAO.findById(violation.getId());
        assertEquals("7. 违规状态应更新为RECTIFIED", AppConstants.VIOLATION_STATUS_RECTIFIED, violation.getStatus());
    }

    @Test
    public void testRectificationRejectedFlow() {
        GarbageRecordSubmitDTO dto = new GarbageRecordSubmitDTO();
        dto.setImageName("reject.jpg");
        dto.setImagePath("/input/reject.jpg");
        dto.setRecommendedCategory("可回收物");
        dto.setSelectedCategory("其他垃圾");
        dto.setIsMixed(0);

        Long recordId = recordService.saveRecord(1L, dto);
        assertNotNull(recordId);

        List<ViolationRecord> violations = violationDAO.findByUserId(1L, 0, 10);
        assertFalse(violations.isEmpty());
        ViolationRecord violation = violations.get(violations.size() - 1);

        Long taskId = rectService.createTask(violation.getId(), 1L, "参加培训", "2026-05-01");
        rectService.submitRectification(taskId, "已完成培训", null);
        rectService.reviewTask(taskId, AppConstants.RECT_STATUS_REJECTED, "整改不充分，请重新整改");

        RectificationTask task = taskDAO.findById(taskId);
        assertEquals(AppConstants.RECT_STATUS_REJECTED, task.getStatus());

        Long newTaskId = rectService.createTask(violation.getId(), 1L, "请再次整改", "2026-05-15");
        assertNotNull("被拒绝后应能创建新整改任务", newTaskId);
    }

    @Test
    public void testReviewCorrectIgnoresViolation() {
        GarbageRecordSubmitDTO dto = new GarbageRecordSubmitDTO();
        dto.setImageName("review_ok.jpg");
        dto.setImagePath("/input/review_ok.jpg");
        dto.setRecommendedCategory("可回收物");
        dto.setSelectedCategory("其他垃圾");
        dto.setIsMixed(0);

        Long recordId = recordService.saveRecord(1L, dto);

        ViolationRecord violation = violationDAO.findByRecordId(recordId);
        assertNotNull("应有违规记录", violation);

        recordService.reviewRecord(recordId, "其他垃圾", "管理员确认用户正确");

        GarbageRecord reviewed = recordDAO.findById(recordId);
        assertEquals(Integer.valueOf(1), reviewed.getIsCorrect());

        violation = violationDAO.findByRecordId(recordId);
        assertEquals("复核正确应忽略违规", AppConstants.VIOLATION_STATUS_IGNORED, violation.getStatus());
    }

    @Test
    public void testCascadeDelete() {
        GarbageRecordSubmitDTO dto = new GarbageRecordSubmitDTO();
        dto.setImageName("cascade.jpg");
        dto.setImagePath("/input/cascade.jpg");
        dto.setRecommendedCategory("厨余垃圾");
        dto.setSelectedCategory("其他垃圾");
        dto.setIsMixed(0);

        Long recordId = recordService.saveRecord(1L, dto);

        ViolationRecord violation = violationDAO.findByRecordId(recordId);
        assertNotNull(violation);

        Long taskId = rectService.createTask(violation.getId(), 1L, "整改", "2026-05-01");
        assertNotNull(taskId);

        recordService.deleteRecord(recordId);

        assertNull("投放记录应被删除", recordDAO.findById(recordId));
        assertNull("违规记录应被删除", violationDAO.findById(violation.getId()));
        assertNull("整改任务应被删除", taskDAO.findById(taskId));
    }

    @Test
    public void testFullCorrectDisposalNoViolation() {
        GarbageRecordSubmitDTO dto = new GarbageRecordSubmitDTO();
        dto.setImageName("correct.jpg");
        dto.setImagePath("/input/correct.jpg");
        dto.setRecommendedCategory("可回收物");
        dto.setSelectedCategory("可回收物");
        dto.setIsMixed(0);

        Long recordId = recordService.saveRecord(1L, dto);
        assertNotNull(recordId);

        GarbageRecord record = recordDAO.findById(recordId);
        assertEquals(Integer.valueOf(1), record.getIsCorrect());

        ViolationRecord violation = violationDAO.findByRecordId(recordId);
        assertNull("正确投放不应有违规记录", violation);

        GarbageRecordDetailVO detail = recordService.getRecordDetail(recordId);
        assertNotNull(detail);
        assertNull(detail.getViolation());
        assertNull(detail.getRectification());
    }
}
