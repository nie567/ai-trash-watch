package com.example.test.service;

import com.example.dao.GarbageRecordDAO;
import com.example.dao.ViolationRecordDAO;
import com.example.model.DetectionResult;
import com.example.model.GarbageRecord;
import com.example.model.PageResult;
import com.example.model.ViolationRecord;
import com.example.service.ViolationService;
import com.example.test.BaseTest;
import com.example.util.AppConstants;
import org.junit.Test;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class ViolationServiceTest extends BaseTest {

    private ViolationService violationService = new ViolationService();
    private GarbageRecordDAO recordDAO = new GarbageRecordDAO();
    private ViolationRecordDAO violationDAO = new ViolationRecordDAO();

    @Override
    protected void initTestData() throws SQLException {
        truncateTable("violation_record");
        truncateTable("garbage_record");
        executeSQL("INSERT IGNORE INTO user (id, username, password_hash, role, status) VALUES " +
                "(9001, 'vuser1', 'hash1', 'user', 1), " +
                "(9002, 'vuser2', 'hash2', 'user', 1)");
        executeSQL("INSERT INTO garbage_record (id, user_id, image_name, image_path, recommended_category, selected_category, is_correct, is_mixed, status) VALUES " +
                "(100, 9001, 'a.jpg', '/a.jpg', '可回收物', '其他垃圾', 0, 0, 'PENDING'), " +
                "(101, 9001, 'b.jpg', '/b.jpg', '混合待分拣', '可回收物', 0, 1, 'PENDING'), " +
                "(102, 9002, 'c.jpg', '/c.jpg', '可回收物', '可回收物', 1, 0, 'PENDING')");
    }

    @Test
    public void testCreateViolationForClassificationError() {
        GarbageRecord record = recordDAO.findById(100L);
        assertNotNull(record);

        List<DetectionResult> details = new ArrayList<>();
        violationService.createViolationIfNeeded(record, details);

        List<ViolationRecord> violations = violationDAO.findByUserId(9001L, 0, 10);
        assertTrue("分类错误应生成违规记录", violations.size() >= 1);

        boolean hasClassificationError = false;
        for (ViolationRecord vr : violations) {
            if ("分类错误".equals(vr.getViolationType())) {
                hasClassificationError = true;
            }
        }
        assertTrue("应有分类错误类型的违规", hasClassificationError);
    }

    @Test
    public void testCreateViolationForMixedDisposal() {
        GarbageRecord record = recordDAO.findById(101L);
        assertNotNull(record);

        List<DetectionResult> details = new ArrayList<>();
        violationService.createViolationIfNeeded(record, details);

        List<ViolationRecord> violations = violationDAO.findByUserId(9001L, 0, 10);
        boolean hasMixedViolation = false;
        for (ViolationRecord vr : violations) {
            if ("混投".equals(vr.getViolationType())) {
                hasMixedViolation = true;
                assertEquals("混投应为MEDIUM级别", AppConstants.VIOLATION_LEVEL_MEDIUM, vr.getLevel());
            }
        }
        assertTrue("混投应生成混投违规", hasMixedViolation);
    }

    @Test
    public void testNoViolationForCorrectDisposal() {
        GarbageRecord record = recordDAO.findById(102L);
        assertNotNull(record);

        List<DetectionResult> details = new ArrayList<>();
        int beforeCount = violationDAO.countByUserId(9002L);

        violationService.createViolationIfNeeded(record, details);

        int afterCount = violationDAO.countByUserId(9002L);
        assertEquals("正确投放不应生成违规记录", beforeCount, afterCount);
    }

    @Test
    public void testGetUserViolations() {
        PageResult<ViolationRecord> page = violationService.getUserViolations(1L, 1, 10);
        assertNotNull(page);
        assertNotNull(page.getData());
    }

    @Test
    public void testGetAllViolations() {
        PageResult<ViolationRecord> page = violationService.getAllViolations(1, 10, null);
        assertNotNull(page);
        assertNotNull(page.getData());
    }

    @Test
    public void testGetById() {
        List<ViolationRecord> all = violationDAO.findAll(0, 10, null);
        if (!all.isEmpty()) {
            Long id = all.get(0).getId();
            ViolationRecord found = violationService.getById(id);
            assertNotNull(found);
        }

        assertNull("null ID应返回null", violationService.getById(null));
    }

    @Test
    public void testViolationLevelDetermination() {
        GarbageRecord record = new GarbageRecord();
        record.setId(999L);
        record.setUserId(9002L);
        record.setRecommendedCategory("可回收物");
        record.setSelectedCategory("有害垃圾");
        record.setIsCorrect(0);
        record.setIsMixed(0);

        List<DetectionResult> details = new ArrayList<>();
        violationService.createViolationIfNeeded(record, details);

        List<ViolationRecord> violations = violationDAO.findByUserId(9002L, 0, 10);
        if (!violations.isEmpty()) {
            ViolationRecord vr = violations.get(violations.size() - 1);
            assertTrue("级别应为LOW或HIGH", 
                    AppConstants.VIOLATION_LEVEL_LOW.equals(vr.getLevel()) || 
                    AppConstants.VIOLATION_LEVEL_HIGH.equals(vr.getLevel()));
        }
    }
}
