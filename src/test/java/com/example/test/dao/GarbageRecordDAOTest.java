package com.example.test.dao;

import com.example.dao.GarbageRecordDAO;
import com.example.model.GarbageRecord;
import com.example.test.BaseTest;
import org.junit.Test;

import java.sql.SQLException;
import java.util.List;

import static org.junit.Assert.*;

/**
 * GarbageRecordDAO 测试类
 * 测试投放记录数据访问层
 */
public class GarbageRecordDAOTest extends BaseTest {

    private GarbageRecordDAO recordDAO = new GarbageRecordDAO();

    @Override
    protected void initTestData() throws SQLException {
        // 清空表
        truncateTable("garbage_record");
        
        // 插入测试用户（假设user表已存在）
        executeSQL("INSERT IGNORE INTO user (id, username, password_hash, role, status) VALUES " +
                "(9001, 'testuser1', 'hash1', 'user', 1), " +
                "(9002, 'testuser2', 'hash2', 'user', 1)");
        
        // 插入测试投放记录
        executeSQL("INSERT INTO garbage_record (user_id, image_name, image_path, recommended_category, selected_category, is_correct, status) VALUES " +
                "(9001, 'test1.jpg', '/input/test1.jpg', '可回收物', '可回收物', 1, 'PENDING'), " +
                "(9001, 'test2.jpg', '/input/test2.jpg', '厨余垃圾', '其他垃圾', 0, 'PENDING'), " +
                "(9002, 'test3.jpg', '/input/test3.jpg', '可回收物', '可回收物', 1, 'REVIEWED')");
    }

    /**
     * 测试新增投放记录
     */
    @Test
    public void testInsert() {
        GarbageRecord record = new GarbageRecord();
        record.setUserId(9001L);
        record.setImageName("new.jpg");
        record.setImagePath("/input/new.jpg");
        record.setRecommendedCategory("可回收物");
        record.setSelectedCategory("可回收物");
        record.setIsCorrect(1);
        record.setStatus("PENDING");
        
        Long id = recordDAO.insert(record);
        assertNotNull("新增记录应返回ID", id);
        assertTrue("ID应大于0", id > 0);
        
        // 验证新增的记录
        GarbageRecord inserted = recordDAO.findById(id);
        assertNotNull("新增的记录应能查询到", inserted);
        assertEquals("imageName应正确", "new.jpg", inserted.getImageName());
    }

    /**
     * 测试按ID查询
     */
    @Test
    public void testFindById() {
        // 查询存在的记录
        List<GarbageRecord> all = recordDAO.findAll(0, 10, null, null);
        assertFalse("应有记录", all.isEmpty());
        
        Long id = all.get(0).getId();
        GarbageRecord record = recordDAO.findById(id);
        
        assertNotNull("记录应存在", record);
        assertEquals("ID应匹配", id, record.getId());
        
        // 查询不存在的记录
        GarbageRecord notExist = recordDAO.findById(99999L);
        assertNull("不存在的记录应返回null", notExist);
    }

    /**
     * 测试按用户ID分页查询
     */
    @Test
    public void testFindByUserId() {
        // 查询用户1的记录
        List<GarbageRecord> user1Records = recordDAO.findByUserId(9001L, 0, 10);
        assertNotNull("记录列表不应为null", user1Records);
        assertEquals("用户1应有2条记录", 2, user1Records.size());
        
        // 验证所有记录都属于用户1
        for (GarbageRecord record : user1Records) {
            assertEquals("记录应属于用户1", Long.valueOf(9001L), record.getUserId());
        }
        
        // 查询用户2的记录
        List<GarbageRecord> user2Records = recordDAO.findByUserId(9002L, 0, 10);
        assertEquals("用户2应有1条记录", 1, user2Records.size());
        
        // 测试分页
        List<GarbageRecord> pagedRecords = recordDAO.findByUserId(9001L, 0, 1);
        assertEquals("分页查询应返回1条记录", 1, pagedRecords.size());
    }

    /**
     * 测试管理员分页查询
     */
    @Test
    public void testFindAll() {
        // 查询所有记录
        List<GarbageRecord> allRecords = recordDAO.findAll(0, 10, null, null);
        assertNotNull("记录列表不应为null", allRecords);
        assertEquals("应有3条记录", 3, allRecords.size());
        
        // 按状态筛选
        List<GarbageRecord> pendingRecords = recordDAO.findAll(0, 10, null, "PENDING");
        assertEquals("应有2条PENDING记录", 2, pendingRecords.size());
        
        List<GarbageRecord> reviewedRecords = recordDAO.findAll(0, 10, null, "REVIEWED");
        assertEquals("应有1条REVIEWED记录", 1, reviewedRecords.size());
        
        // 按关键词筛选
        List<GarbageRecord> keywordRecords = recordDAO.findAll(0, 10, "test1", null);
        assertEquals("应有1条包含test1的记录", 1, keywordRecords.size());
    }

    /**
     * 测试统计用户记录数
     */
    @Test
    public void testCountByUserId() {
        int count1 = recordDAO.countByUserId(9001L);
        assertEquals("用户1应有2条记录", 2, count1);
        
        int count2 = recordDAO.countByUserId(9002L);
        assertEquals("用户2应有1条记录", 1, count2);
        
        int count3 = recordDAO.countByUserId(999L);
        assertEquals("不存在的用户应有0条记录", 0, count3);
    }

    /**
     * 测试统计总记录数
     */
    @Test
    public void testCountAll() {
        int total = recordDAO.countAll(null, null);
        assertEquals("应有3条总记录", 3, total);
        
        int pending = recordDAO.countAll(null, "PENDING");
        assertEquals("应有2条PENDING记录", 2, pending);
        
        int reviewed = recordDAO.countAll(null, "REVIEWED");
        assertEquals("应有1条REVIEWED记录", 1, reviewed);
    }

    /**
     * 测试更新复核结果
     */
    @Test
    public void testUpdateReviewResult() {
        // 获取一条PENDING记录
        List<GarbageRecord> pendingRecords = recordDAO.findAll(0, 10, null, "PENDING");
        assertFalse("应有PENDING记录", pendingRecords.isEmpty());
        
        Long id = pendingRecords.get(0).getId();
        
        // 更新复核结果
        recordDAO.updateReviewResult(id, "厨余垃圾", 1, "REVIEWED", "审核通过");
        
        // 验证更新结果
        GarbageRecord updated = recordDAO.findById(id);
        assertNotNull("记录应存在", updated);
        assertEquals("finalCategory应已更新", "厨余垃圾", updated.getFinalCategory());
        assertEquals("isCorrect应已更新", Integer.valueOf(1), updated.getIsCorrect());
        assertEquals("status应已更新", "REVIEWED", updated.getStatus());
        assertEquals("reviewComment应已更新", "审核通过", updated.getReviewComment());
    }
}
