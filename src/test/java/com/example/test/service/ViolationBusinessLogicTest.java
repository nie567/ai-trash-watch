package com.example.test.service;

import com.example.dao.GarbageRecordDAO;
import com.example.dao.RectificationTaskDAO;
import com.example.dao.ViolationRecordDAO;
import com.example.model.DetectionResult;
import com.example.model.GarbageRecord;
import com.example.model.ViolationRecord;
import com.example.service.GarbageRecordService;
import com.example.service.ViolationService;
import com.example.test.BaseTest;
import com.example.util.AppConstants;
import com.example.util.BusinessException;
import org.junit.Test;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class ViolationBusinessLogicTest extends BaseTest {

    private ViolationService violationService = new ViolationService(new ViolationRecordDAO(), new RectificationTaskDAO());
    private GarbageRecordService recordService = new GarbageRecordService();
    private ViolationRecordDAO violationDAO = new ViolationRecordDAO();
    private GarbageRecordDAO recordDAO = new GarbageRecordDAO();

    private static final Long TEST_USER_ID = 8001L;
    private static final Long ADMIN_USER_ID = 8002L;
    private static final Long TEST_USER_ID_2 = 8003L;

    @Override
    protected void initTestData() throws SQLException {
        truncateTable("rectification_task");
        truncateTable("violation_record");
        truncateTable("detection_result");
        truncateTable("garbage_record");
        executeSQL("INSERT IGNORE INTO user (id, username, password_hash, role, status) VALUES " +
                "(" + ADMIN_USER_ID + ", 'vbl_admin', 'hashadmin', 'admin', 1), " +
                "(" + TEST_USER_ID + ", 'vbl_user1', 'hashuser1', 'user', 1), " +
                "(" + TEST_USER_ID_2 + ", 'vbl_user2', 'hashuser2', 'user', 1)");
    }

    @Override
    protected void cleanTestData() throws SQLException {
        truncateTable("rectification_task");
        truncateTable("violation_record");
        truncateTable("detection_result");
        truncateTable("garbage_record");
        deleteFromTable("user", "id IN (" + ADMIN_USER_ID + ", " + TEST_USER_ID + ", " + TEST_USER_ID_2 + ")");
    }

    @Test
    public void testMixedDisposalWithoutMixedCategory() throws SQLException {
        executeSQL("INSERT INTO garbage_record (id, user_id, image_name, image_path, recommended_category, selected_category, is_correct, is_mixed, status) VALUES " +
                "(5001, " + TEST_USER_ID + ", 'mixed.jpg', '/mixed.jpg', '混合待分拣', '可回收物', 1, 1, 'PENDING')");

        GarbageRecord record = recordDAO.findById(5001L);
        assertNotNull(record);

        List<DetectionResult> details = new ArrayList<>();
        violationService.createViolationIfNeeded(record, details);

        List<ViolationRecord> violations = violationDAO.findByUserId(TEST_USER_ID, 0, 10);
        assertFalse("混投应生成违规记录", violations.isEmpty());

        boolean hasMixedType = false;
        for (ViolationRecord vr : violations) {
            if ("混投".equals(vr.getViolationType()) && vr.getRecordId().equals(5001L)) {
                hasMixedType = true;
            }
        }
        assertTrue("isMixed=1且selectedCategory不是'混合待分拣'应生成混投违规", hasMixedType);
    }

    @Test
    public void testAdminExemption() throws SQLException {
        executeSQL("INSERT INTO garbage_record (id, user_id, image_name, image_path, recommended_category, selected_category, is_correct, is_mixed, status) VALUES " +
                "(5002, " + ADMIN_USER_ID + ", 'admin.jpg', '/admin.jpg', '可回收物', '其他垃圾', 0, 0, 'PENDING')");

        GarbageRecord record = recordDAO.findById(5002L);
        assertNotNull(record);

        int beforeCount = violationDAO.countByUserId(ADMIN_USER_ID);

        List<DetectionResult> details = new ArrayList<>();
        violationService.createViolationIfNeeded(record, details);

        int afterCount = violationDAO.countByUserId(ADMIN_USER_ID);
        assertEquals("管理员投放不应生成违规记录", beforeCount, afterCount);
    }

    @Test
    public void testThreeStrikeRuleHighLevel() throws SQLException {
        for (int i = 1; i <= 3; i++) {
            long recId = 6000L + i;
            executeSQL("INSERT INTO garbage_record (id, user_id, image_name, image_path, recommended_category, selected_category, is_correct, is_mixed, status) VALUES " +
                    "(" + recId + ", " + TEST_USER_ID_2 + ", 'hist" + i + ".jpg', '/hist" + i + ".jpg', '可回收物', '其他垃圾', 0, 0, 'PENDING')");
            ViolationRecord v = new ViolationRecord();
            v.setRecordId(recId);
            v.setUserId(TEST_USER_ID_2);
            v.setViolationType("分类错误");
            v.setDescription("历史违规" + i);
            v.setLevel(AppConstants.VIOLATION_LEVEL_LOW);
            v.setStatus(AppConstants.VIOLATION_STATUS_PENDING);
            violationDAO.insert(v);
        }

        GarbageRecord record = new GarbageRecord();
        record.setId(9003L);
        record.setUserId(TEST_USER_ID_2);
        record.setIsMixed(0);
        record.setSelectedCategory("有害垃圾");
        record.setRecommendedCategory("可回收物");
        record.setIsCorrect(0);
        record.setStatus("PENDING");

        executeSQL("INSERT INTO garbage_record (id, user_id, image_name, image_path, recommended_category, selected_category, is_correct, is_mixed, status) VALUES " +
                "(9003, " + TEST_USER_ID_2 + ", 'strike4.jpg', '/strike4.jpg', '可回收物', '有害垃圾', 0, 0, 'PENDING')");

        List<DetectionResult> details = new ArrayList<>();
        violationService.createViolationIfNeeded(record, details);

        List<ViolationRecord> allViolations = violationDAO.findByUserId(TEST_USER_ID_2, 0, 10);
        ViolationRecord latest = null;
        for (ViolationRecord vr : allViolations) {
            if (vr.getRecordId() != null && vr.getRecordId().equals(9003L)) {
                latest = vr;
            }
        }
        assertNotNull("第3次违规应被创建", latest);
        assertEquals("累计3次历史后再犯应为HIGH级别", AppConstants.VIOLATION_LEVEL_HIGH, latest.getLevel());
    }

    @Test
    public void testViolationStatusRestoreFromIgnored() throws SQLException {
        executeSQL("INSERT INTO garbage_record (id, user_id, image_name, image_path, recommended_category, selected_category, is_correct, is_mixed, status) VALUES " +
                "(7001, " + TEST_USER_ID + ", 'restore.jpg', '/restore.jpg', '可回收物', '有害垃圾', 0, 0, 'PENDING')");

        GarbageRecord record = recordDAO.findById(7001L);
        assertNotNull(record);

        List<DetectionResult> details = new ArrayList<>();
        violationService.createViolationIfNeeded(record, details);

        ViolationRecord violation = violationDAO.findByRecordId(7001L);
        assertNotNull("应生成违规记录", violation);

        executeSQL("UPDATE violation_record SET status = 'IGNORED' WHERE id = " + violation.getId());

        recordService.reviewRecord(7001L, "可回收物", "复核确认错误");

        ViolationRecord restored = violationDAO.findById(violation.getId());
        assertNotNull(restored);
        assertEquals("IGNORED状态在复核确认错误后应恢复为PENDING",
                AppConstants.VIOLATION_STATUS_PENDING, restored.getStatus());
    }

    @Test
    public void testReviewIncorrectCreatesViolation() throws SQLException {
        executeSQL("INSERT INTO garbage_record (id, user_id, image_name, image_path, recommended_category, selected_category, is_correct, is_mixed, status) VALUES " +
                "(7002, " + TEST_USER_ID + ", 'review.jpg', '/review.jpg', '可回收物', '可回收物', 1, 0, 'PENDING')");

        ViolationRecord existingViolation = violationDAO.findByRecordId(7002L);
        assertNull("正确投放不应有违规记录", existingViolation);

        recordService.reviewRecord(7002L, "有害垃圾", "复核确认用户选错了");

        ViolationRecord newViolation = violationDAO.findByRecordId(7002L);
        assertNotNull("复核确认错误应创建新违规记录", newViolation);
    }
}
