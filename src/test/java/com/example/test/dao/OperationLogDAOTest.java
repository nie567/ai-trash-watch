package com.example.test.dao;

import com.example.dao.OperationLogDAO;
import com.example.model.OperationLog;
import com.example.test.BaseTest;
import com.example.util.DBUtil;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static org.junit.Assert.*;

/**
 * OperationLogDAO 测试类
 * 测试操作日志数据访问层
 */
public class OperationLogDAOTest extends BaseTest {

    private OperationLogDAO logDAO = new OperationLogDAO();

    @Override
    protected void initTestData() throws SQLException {
        truncateTable("operation_log");
        // 先确保测试用户存在（operation_log 有外键约束）
        executeSQL("INSERT IGNORE INTO user (id, username, password_hash, role, status) VALUES " +
                "(9001, 'loguser1', 'hash1', 'user', 1), " +
                "(9002, 'loguser2', 'hash2', 'admin', 1)");
        executeSQL("INSERT INTO operation_log (user_id, username, action, target, detail, ip, create_time) VALUES " +
                "(9001, 'loguser1', 'LOGIN', '系统', '用户登录', '192.168.1.1', NOW()), " +
                "(9001, 'loguser1', 'CREATE', '投放记录', '新增投放记录', '192.168.1.1', NOW()), " +
                "(9002, 'loguser2', 'DELETE', '用户', '删除用户test', '192.168.1.2', NOW())");
    }

    @Override
    protected void cleanTestData() throws SQLException {
        truncateTable("operation_log");
    }

    /**
     * 测试插入操作日志
     */
    @Test
    public void testInsert() {
        OperationLog log = new OperationLog();
        log.setUserId(9001);
        log.setUsername("loguser1");
        log.setAction("UPDATE");
        log.setTarget("分类规则");
        log.setDetail("修改METAL映射分类");
        log.setIp("10.0.0.1");

        boolean result = logDAO.insert(log);
        assertTrue("插入操作日志应成功", result);

        // 验证能查到新记录
        List<OperationLog> logs = logDAO.findByUserId(9001, 10);
        assertTrue("应有至少3条loguser1的日志", logs.size() >= 3);
    }

    /**
     * 测试分页查询
     */
    @Test
    public void testFindAll() {
        List<OperationLog> page1 = logDAO.findAll(1, 2);
        assertNotNull("分页结果不应为null", page1);
        assertEquals("第1页应有2条记录", 2, page1.size());

        List<OperationLog> page2 = logDAO.findAll(2, 2);
        assertNotNull("第2页不应为null", page2);
        assertEquals("第2页应有1条记录", 1, page2.size());

        List<OperationLog> page3 = logDAO.findAll(3, 2);
        assertNotNull("超出范围的页应为空列表", page3);
        assertTrue("超出范围应为空", page3.isEmpty());
    }

    /**
     * 测试统计总数
     */
    @Test
    public void testCountAll() {
        int count = logDAO.countAll();
        assertEquals("应有3条日志", 3, count);
    }

    /**
     * 测试按用户查询
     */
    @Test
    public void testFindByUserId() {
        List<OperationLog> user1Logs = logDAO.findByUserId(9001, 10);
        assertNotNull("用户日志列表不应为null", user1Logs);
        assertEquals("loguser1应有2条日志", 2, user1Logs.size());

        // 验证日志内容
        OperationLog firstLog = user1Logs.get(0);
        assertEquals("username应为loguser1", "loguser1", firstLog.getUsername());
        assertNotNull("action不应为null", firstLog.getAction());
        assertNotNull("ip不应为null", firstLog.getIp());

        List<OperationLog> user2Logs = logDAO.findByUserId(9002, 10);
        assertEquals("loguser2应有1条日志", 1, user2Logs.size());
        assertEquals("DELETE", user2Logs.get(0).getAction());
    }

    /**
     * 测试按用户查询 - limit参数
     */
    @Test
    public void testFindByUserIdWithLimit() {
        // 先插入更多日志
        for (int i = 0; i < 5; i++) {
            OperationLog log = new OperationLog();
            log.setUserId(9001);
            log.setUsername("loguser1");
            log.setAction("UPDATE");
            log.setTarget("测试对象" + i);
            log.setDetail("测试详情" + i);
            log.setIp("10.0.0.1");
            logDAO.insert(log);
        }

        List<OperationLog> limited = logDAO.findByUserId(9001, 3);
        assertEquals("limit=3应返回3条", 3, limited.size());
    }

    /**
     * 测试按用户删除日志（使用外部连接）
     */
    @Test
    public void testDeleteByUserIdWithExternalConn() throws SQLException {
        Connection conn = DBUtil.getConnection();
        try {
            DBUtil.beginTransaction(conn);
            int deleted = logDAO.deleteByUserId(9001L, conn);
            DBUtil.commitTransaction(conn);
            assertEquals("应删除2条loguser1的日志", 2, deleted);

            // 验证删除结果
            List<OperationLog> remaining = logDAO.findByUserId(9001, 10);
            assertTrue("删除后loguser1应无日志", remaining.isEmpty());

            // loguser2的日志应不受影响
            List<OperationLog> user2Logs = logDAO.findByUserId(9002, 10);
            assertEquals("loguser2的日志应保留", 1, user2Logs.size());
        } finally {
            if (conn != null) try { conn.close(); } catch (SQLException ignored) {}
        }
    }

    /**
     * 测试按用户删除日志（不使用外部连接，conn=null）
     */
    @Test
    public void testDeleteByUserIdWithNullConn() {
        int deleted = logDAO.deleteByUserId(9002L, null);
        assertEquals("应删除1条loguser2的日志", 1, deleted);

        List<OperationLog> remaining = logDAO.findByUserId(9002, 10);
        assertTrue("删除后loguser2应无日志", remaining.isEmpty());
    }

    /**
     * 测试删除不存在的用户日志
     */
    @Test
    public void testDeleteByUserIdNonExistent() {
        int deleted = logDAO.deleteByUserId(99999L, null);
        assertEquals("删除不存在的用户日志应返回0", 0, deleted);
    }

    /**
     * 测试日志字段完整性
     */
    @Test
    public void testLogFieldIntegrity() {
        List<OperationLog> logs = logDAO.findByUserId(9001, 10);
        assertFalse("应有日志记录", logs.isEmpty());

        OperationLog log = logs.get(0);
        assertNotNull("id不应为null", log.getId());
        assertTrue("userId应为1", log.getUserId() == 9001);
        assertEquals("username应为loguser1", "loguser1", log.getUsername());
        assertNotNull("action不应为null", log.getAction());
        assertNotNull("createTime不应为null", log.getCreateTime());
    }
}
