package com.example.test.dao;

import com.example.dao.RectificationTaskDAO;
import com.example.model.RectificationTask;
import com.example.test.BaseTest;
import org.junit.Test;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

import static org.junit.Assert.*;

public class RectificationTaskDAOTest extends BaseTest {

    private RectificationTaskDAO taskDAO = new RectificationTaskDAO();

    @Override
    protected void initTestData() throws SQLException {
        truncateTable("rectification_task");
        truncateTable("violation_record");
        truncateTable("garbage_record");
        executeSQL("INSERT IGNORE INTO user (id, username, password_hash, role, status) VALUES " +
                "(9001, 'testuser1', 'hash1', 'user', 1), " +
                "(9002, 'testuser2', 'hash2', 'user', 1)");
        executeSQL("INSERT INTO garbage_record (id, user_id, image_name, image_path, recommended_category, selected_category, is_correct, status) VALUES " +
                "(100, 9001, 'test1.jpg', '/input/test1.jpg', '可回收物', '其他垃圾', 0, 'PENDING')");
        executeSQL("INSERT INTO violation_record (id, record_id, user_id, violation_type, description, level, status, create_time) VALUES " +
                "(200, 100, 9001, '分类错误', '测试违规', 'LOW', 'PENDING', NOW()), " +
                "(201, 100, 9002, '分类错误', '测试违规2', 'LOW', 'PENDING', NOW())");
        executeSQL("INSERT INTO rectification_task (violation_id, user_id, requirement, deadline, status, create_time, update_time) VALUES " +
                "(200, 9001, '重新分类投放', '2026-05-01 00:00:00', 'PENDING', NOW(), NOW()), " +
                "(201, 9002, '参加培训', '2026-05-01 00:00:00', 'SUBMITTED', NOW(), NOW())");
    }

    @Test
    public void testInsert() {
        RectificationTask task = new RectificationTask();
        task.setViolationId(200L);
        task.setUserId(9001L);
        task.setRequirement("整改要求");
        task.setDeadline(Timestamp.valueOf("2026-06-01 00:00:00"));
        task.setStatus("PENDING");

        Long id = taskDAO.insert(task);
        assertNotNull("插入应返回ID", id);
        assertTrue(id > 0);

        RectificationTask inserted = taskDAO.findById(id);
        assertNotNull(inserted);
        assertEquals("整改要求", inserted.getRequirement());
    }

    @Test
    public void testFindById() {
        List<RectificationTask> all = taskDAO.findAll(0, 10, null);
        assertFalse(all.isEmpty());

        Long id = all.get(0).getId();
        RectificationTask found = taskDAO.findById(id);
        assertNotNull(found);
        assertEquals(id, found.getId());

        assertNull(taskDAO.findById(99999L));
    }

    @Test
    public void testFindByViolationId() {
        RectificationTask task = taskDAO.findByViolationId(200L);
        assertNotNull("violationId=200应有整改任务", task);
        assertEquals(Long.valueOf(200L), task.getViolationId());

        assertNull(taskDAO.findByViolationId(99999L));
    }

    @Test
    public void testFindByUserId() {
        List<RectificationTask> user1Tasks = taskDAO.findByUserId(9001L, 0, 10);
        assertEquals(1, user1Tasks.size());

        List<RectificationTask> user2Tasks = taskDAO.findByUserId(9002L, 0, 10);
        assertEquals(1, user2Tasks.size());
    }

    @Test
    public void testFindAllWithStatus() {
        List<RectificationTask> all = taskDAO.findAll(0, 10, null);
        assertEquals(2, all.size());

        List<RectificationTask> pending = taskDAO.findAll(0, 10, "PENDING");
        assertEquals(1, pending.size());

        List<RectificationTask> submitted = taskDAO.findAll(0, 10, "SUBMITTED");
        assertEquals(1, submitted.size());
    }

    @Test
    public void testCountByUserId() {
        assertEquals(1, taskDAO.countByUserId(9001L));
        assertEquals(1, taskDAO.countByUserId(9002L));
        assertEquals(0, taskDAO.countByUserId(999L));
    }

    @Test
    public void testCountAll() {
        assertEquals(2, taskDAO.countAll(null));
        assertEquals(1, taskDAO.countAll("PENDING"));
        assertEquals(1, taskDAO.countAll("SUBMITTED"));
    }

    @Test
    public void testSubmit() {
        List<RectificationTask> pending = taskDAO.findAll(0, 10, "PENDING");
        assertFalse(pending.isEmpty());

        Long id = pending.get(0).getId();
        taskDAO.submit(id, "已完成整改", "/output/rect.jpg", "SUBMITTED");

        RectificationTask updated = taskDAO.findById(id);
        assertEquals("SUBMITTED", updated.getStatus());
        assertEquals("已完成整改", updated.getSubmitDesc());
        assertEquals("/output/rect.jpg", updated.getSubmitImagePath());
    }

    @Test
    public void testReview() {
        List<RectificationTask> submitted = taskDAO.findAll(0, 10, "SUBMITTED");
        assertFalse(submitted.isEmpty());

        Long id = submitted.get(0).getId();
        taskDAO.review(id, "APPROVED", "整改通过", "APPROVED");

        RectificationTask updated = taskDAO.findById(id);
        assertEquals("APPROVED", updated.getStatus());
        assertEquals("APPROVED", updated.getReviewResult());
        assertEquals("整改通过", updated.getReviewComment());
    }

    @Test
    public void testDeleteById() {
        List<RectificationTask> all = taskDAO.findAll(0, 10, null);
        Long id = all.get(0).getId();

        assertTrue(taskDAO.deleteById(id));
        assertNull(taskDAO.findById(id));
        assertFalse(taskDAO.deleteById(id));
    }
}
