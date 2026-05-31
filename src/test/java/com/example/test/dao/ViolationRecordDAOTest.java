package com.example.test.dao;

import com.example.dao.ViolationRecordDAO;
import com.example.model.ViolationRecord;
import com.example.test.BaseTest;
import org.junit.Test;

import java.sql.SQLException;
import java.util.List;

import static org.junit.Assert.*;

public class ViolationRecordDAOTest extends BaseTest {

    private ViolationRecordDAO violationDAO = new ViolationRecordDAO();

    @Override
    protected void initTestData() throws SQLException {
        truncateTable("violation_record");
        truncateTable("garbage_record");
        executeSQL("INSERT IGNORE INTO user (id, username, password_hash, role, status) VALUES " +
                "(9001, 'testuser1', 'hash1', 'user', 1), " +
                "(9002, 'testuser2', 'hash2', 'user', 1)");
        executeSQL("INSERT INTO garbage_record (id, user_id, image_name, image_path, recommended_category, selected_category, is_correct, status) VALUES " +
                "(100, 9001, 'test1.jpg', '/input/test1.jpg', '可回收物', '其他垃圾', 0, 'PENDING'), " +
                "(101, 9002, 'test2.jpg', '/input/test2.jpg', '厨余垃圾', '其他垃圾', 0, 'PENDING')");
        executeSQL("INSERT INTO violation_record (record_id, user_id, violation_type, description, level, status, create_time) VALUES " +
                "(100, 9001, '分类错误', '推荐：可回收物，选择：其他垃圾', 'LOW', 'PENDING', NOW()), " +
                "(100, 9001, '混投', '混合投放', 'MEDIUM', 'RECTIFIED', NOW()), " +
                "(101, 9002, '分类错误', '推荐：厨余垃圾，选择：其他垃圾', 'LOW', 'PENDING', NOW())");
    }

    @Test
    public void testInsert() {
        ViolationRecord vr = new ViolationRecord();
        vr.setRecordId(100L);
        vr.setUserId(9001L);
        vr.setViolationType("分类错误");
        vr.setDescription("测试违规");
        vr.setLevel("HIGH");
        vr.setStatus("PENDING");

        Long id = violationDAO.insert(vr);
        assertNotNull("插入应返回ID", id);
        assertTrue("ID应大于0", id > 0);

        ViolationRecord inserted = violationDAO.findById(id);
        assertNotNull("应能查到新记录", inserted);
        assertEquals("分类错误", inserted.getViolationType());
    }

    @Test
    public void testFindById() {
        List<ViolationRecord> all = violationDAO.findAll(0, 10, null);
        assertFalse("应有记录", all.isEmpty());

        Long id = all.get(0).getId();
        ViolationRecord found = violationDAO.findById(id);
        assertNotNull(found);
        assertEquals(id, found.getId());

        assertNull("不存在的ID应返回null", violationDAO.findById(99999L));
    }

    @Test
    public void testFindByRecordId() {
        ViolationRecord vr = violationDAO.findByRecordId(100L);
        assertNotNull("recordId=100应有违规记录", vr);
        assertEquals(Long.valueOf(100L), vr.getRecordId());

        assertNull("不存在的recordId应返回null", violationDAO.findByRecordId(99999L));
    }

    @Test
    public void testFindByUserId() {
        List<ViolationRecord> user1List = violationDAO.findByUserId(9001L, 0, 10);
        assertEquals("用户1应有2条违规", 2, user1List.size());

        List<ViolationRecord> user2List = violationDAO.findByUserId(9002L, 0, 10);
        assertEquals("用户2应有1条违规", 1, user2List.size());

        List<ViolationRecord> paged = violationDAO.findByUserId(9001L, 0, 1);
        assertEquals("分页查询应返回1条", 1, paged.size());
    }

    @Test
    public void testFindAllWithStatus() {
        List<ViolationRecord> all = violationDAO.findAll(0, 10, null);
        assertEquals("应有3条总记录", 3, all.size());

        List<ViolationRecord> pending = violationDAO.findAll(0, 10, "PENDING");
        assertEquals("应有2条PENDING", 2, pending.size());

        List<ViolationRecord> rectified = violationDAO.findAll(0, 10, "RECTIFIED");
        assertEquals("应有1条RECTIFIED", 1, rectified.size());
    }

    @Test
    public void testCountByUserId() {
        assertEquals(2, violationDAO.countByUserId(9001L));
        assertEquals(1, violationDAO.countByUserId(9002L));
        assertEquals(0, violationDAO.countByUserId(999L));
    }

    @Test
    public void testCountAll() {
        assertEquals(3, violationDAO.countAll(null));
        assertEquals(2, violationDAO.countAll("PENDING"));
        assertEquals(1, violationDAO.countAll("RECTIFIED"));
    }

    @Test
    public void testUpdateStatus() {
        List<ViolationRecord> pending = violationDAO.findAll(0, 10, "PENDING");
        assertFalse(pending.isEmpty());

        Long id = pending.get(0).getId();
        violationDAO.updateStatus(id, "RECTIFIED");

        ViolationRecord updated = violationDAO.findById(id);
        assertEquals("RECTIFIED", updated.getStatus());
    }

    @Test
    public void testCountByUserIdAll() {
        assertEquals(2, violationDAO.countByUserIdAll(9001L));
        assertEquals(1, violationDAO.countByUserIdAll(9002L));
    }

    @Test
    public void testDeleteById() {
        List<ViolationRecord> all = violationDAO.findAll(0, 10, null);
        Long id = all.get(0).getId();

        assertTrue("删除应成功", violationDAO.deleteById(id));
        assertNull("删除后应查不到", violationDAO.findById(id));
        assertFalse("重复删除应返回false", violationDAO.deleteById(id));
    }
}
