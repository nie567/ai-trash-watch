package com.example.test.service;

import com.example.dao.DetectionResultDAO;
import com.example.dao.GarbageRecordDAO;
import com.example.dao.ViolationRecordDAO;
import com.example.model.*;
import com.example.service.GarbageRecordService;
import com.example.test.BaseTest;
import com.example.util.AppConstants;
import com.example.util.BusinessException;
import org.junit.Test;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class GarbageRecordServiceTest extends BaseTest {

    private GarbageRecordService recordService = new GarbageRecordService();
    private GarbageRecordDAO recordDAO = new GarbageRecordDAO();
    private ViolationRecordDAO violationDAO = new ViolationRecordDAO();
    private DetectionResultDAO detectionDAO = new DetectionResultDAO();

    @Override
    protected void initTestData() throws SQLException {
        truncateTable("detection_result");
        truncateTable("rectification_task");
        truncateTable("violation_record");
        truncateTable("garbage_record");
        executeSQL("INSERT IGNORE INTO user (id, username, password_hash, role, status) VALUES " +
                "(1, 'gruser1', 'hash1', 'user', 1), " +
                "(2, 'gruser2', 'hash2', 'user', 1)");
    }

    @Test
    public void testSaveRecordCorrect() {
        GarbageRecordSubmitDTO dto = new GarbageRecordSubmitDTO();
        dto.setImageName("recyclable.jpg");
        dto.setImagePath("/input/recyclable.jpg");
        dto.setRecommendedCategory("可回收物");
        dto.setSelectedCategory("可回收物");
        dto.setIsMixed(0);

        Long recordId = recordService.saveRecord(1L, dto);
        assertNotNull("保存成功应返回ID", recordId);

        GarbageRecord record = recordDAO.findById(recordId);
        assertNotNull(record);
        assertEquals(Integer.valueOf(1), record.getIsCorrect());
        assertEquals(AppConstants.RECORD_STATUS_PENDING, record.getStatus());

        List<ViolationRecord> violations = violationDAO.findByUserId(1L, 0, 10);
        assertEquals("正确投放不应有违规", 0, violations.size());
    }

    @Test
    public void testSaveRecordIncorrect() {
        GarbageRecordSubmitDTO dto = new GarbageRecordSubmitDTO();
        dto.setImageName("wrong.jpg");
        dto.setImagePath("/input/wrong.jpg");
        dto.setRecommendedCategory("可回收物");
        dto.setSelectedCategory("其他垃圾");
        dto.setIsMixed(0);

        Long recordId = recordService.saveRecord(1L, dto);
        assertNotNull(recordId);

        GarbageRecord record = recordDAO.findById(recordId);
        assertEquals(Integer.valueOf(0), record.getIsCorrect());

        List<ViolationRecord> violations = violationDAO.findByUserId(1L, 0, 10);
        assertTrue("错误投放应有违规记录", violations.size() >= 1);
    }

    @Test
    public void testSaveRecordWithDetections() {
        GarbageRecordSubmitDTO dto = new GarbageRecordSubmitDTO();
        dto.setImageName("multi.jpg");
        dto.setImagePath("/input/multi.jpg");
        dto.setRecommendedCategory("可回收物");
        dto.setSelectedCategory("可回收物");
        dto.setIsMixed(0);

        List<DetectionResultDTO> detections = new ArrayList<>();
        DetectionResultDTO dr1 = new DetectionResultDTO();
        dr1.setClassName("METAL");
        dr1.setConfidence(0.95);
        dr1.setMappedCategory("可回收物");
        dr1.setXMin(10);
        dr1.setYMin(20);
        dr1.setXMax(100);
        dr1.setYMax(200);
        detections.add(dr1);

        DetectionResultDTO dr2 = new DetectionResultDTO();
        dr2.setClassName("PLASTIC");
        dr2.setConfidence(0.85);
        dr2.setMappedCategory("可回收物");
        detections.add(dr2);

        dto.setDetections(detections);

        Long recordId = recordService.saveRecord(1L, dto);
        assertNotNull(recordId);

        List<DetectionResult> savedDetections = detectionDAO.findByRecordId(recordId);
        assertEquals("应有2条检测明细", 2, savedDetections.size());
    }

    @Test
    public void testSaveRecordNullUserId() {
        try {
            GarbageRecordSubmitDTO dto = new GarbageRecordSubmitDTO();
            dto.setSelectedCategory("可回收物");
            recordService.saveRecord(null, dto);
            fail("应抛出BusinessException");
        } catch (BusinessException e) {
            assertEquals(400, e.getCode());
        }
    }

    @Test
    public void testSaveRecordNullSelectedCategory() {
        try {
            GarbageRecordSubmitDTO dto = new GarbageRecordSubmitDTO();
            dto.setSelectedCategory(null);
            recordService.saveRecord(1L, dto);
            fail("应抛出BusinessException");
        } catch (BusinessException e) {
            assertEquals(400, e.getCode());
        }
    }

    @Test
    public void testGetUserRecords() {
        GarbageRecordSubmitDTO dto = new GarbageRecordSubmitDTO();
        dto.setImageName("test.jpg");
        dto.setImagePath("/input/test.jpg");
        dto.setRecommendedCategory("可回收物");
        dto.setSelectedCategory("可回收物");
        recordService.saveRecord(1L, dto);

        PageResult<GarbageRecord> page = recordService.getUserRecords(1L, 1, 10);
        assertNotNull(page);
        assertNotNull(page.getData());
        assertFalse("应有记录", page.getData().isEmpty());
    }

    @Test
    public void testGetAllRecords() {
        PageResult<GarbageRecord> page = recordService.getAllRecords(1, 10, null, null);
        assertNotNull(page);
    }

    @Test
    public void testGetRecordDetail() {
        GarbageRecordSubmitDTO dto = new GarbageRecordSubmitDTO();
        dto.setImageName("detail.jpg");
        dto.setImagePath("/input/detail.jpg");
        dto.setRecommendedCategory("可回收物");
        dto.setSelectedCategory("可回收物");
        Long recordId = recordService.saveRecord(1L, dto);

        GarbageRecordDetailVO detail = recordService.getRecordDetail(recordId);
        assertNotNull(detail);
        assertNotNull(detail.getRecord());
        assertEquals(recordId, detail.getRecord().getId());
        assertNotNull(detail.getDetections());
    }

    @Test
    public void testGetRecordDetailNonExistent() {
        try {
            recordService.getRecordDetail(99999L);
            fail("应抛出BusinessException");
        } catch (BusinessException e) {
            assertEquals(404, e.getCode());
        }
    }

    @Test
    public void testReviewRecordCorrect() {
        GarbageRecordSubmitDTO dto = new GarbageRecordSubmitDTO();
        dto.setImageName("review.jpg");
        dto.setImagePath("/input/review.jpg");
        dto.setRecommendedCategory("可回收物");
        dto.setSelectedCategory("其他垃圾");
        Long recordId = recordService.saveRecord(1L, dto);

        recordService.reviewRecord(recordId, "其他垃圾", "管理员确认");

        GarbageRecord reviewed = recordDAO.findById(recordId);
        assertEquals(AppConstants.RECORD_STATUS_REVIEWED, reviewed.getStatus());
        assertEquals("其他垃圾", reviewed.getFinalCategory());
        assertEquals(Integer.valueOf(1), reviewed.getIsCorrect());

        ViolationRecord violation = violationDAO.findByRecordId(recordId);
        if (violation != null) {
            assertEquals("复核正确应忽略违规", AppConstants.VIOLATION_STATUS_IGNORED, violation.getStatus());
        }
    }

    @Test
    public void testReviewRecordIncorrect() {
        GarbageRecordSubmitDTO dto = new GarbageRecordSubmitDTO();
        dto.setImageName("review2.jpg");
        dto.setImagePath("/input/review2.jpg");
        dto.setRecommendedCategory("可回收物");
        dto.setSelectedCategory("可回收物");
        Long recordId = recordService.saveRecord(1L, dto);

        recordService.reviewRecord(recordId, "其他垃圾", "管理员确认");

        GarbageRecord reviewed = recordDAO.findById(recordId);
        assertEquals(Integer.valueOf(0), reviewed.getIsCorrect());
    }

    @Test
    public void testDeleteRecord() {
        GarbageRecordSubmitDTO dto = new GarbageRecordSubmitDTO();
        dto.setImageName("delete.jpg");
        dto.setImagePath("/input/delete.jpg");
        dto.setRecommendedCategory("可回收物");
        dto.setSelectedCategory("其他垃圾");
        Long recordId = recordService.saveRecord(1L, dto);

        recordService.deleteRecord(recordId);

        assertNull("删除后应查不到", recordDAO.findById(recordId));
        assertTrue("检测明细应被删除", detectionDAO.findByRecordId(recordId).isEmpty());
    }

    @Test
    public void testDeleteRecordNonExistent() {
        try {
            recordService.deleteRecord(99999L);
            fail("应抛出BusinessException");
        } catch (BusinessException e) {
            assertEquals(404, e.getCode());
        }
    }
}
