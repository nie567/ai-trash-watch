package com.example.test;

import com.example.util.DBUtil;
import org.junit.After;
import org.junit.Before;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * 测试基类
 * 提供数据库初始化和清理功能
 */
public abstract class BaseTest {

    protected Connection connection;

    @Before
    public void setUp() throws SQLException {
        // 获取数据库连接
        connection = DBUtil.getConnection();
        
        // 初始化测试数据
        initTestData();
    }

    @After
    public void tearDown() throws SQLException {
        // 清理测试数据
        cleanTestData();
        
        // 关闭连接
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    /**
     * 初始化测试数据
     * 子类可以重写此方法
     */
    protected void initTestData() throws SQLException {
        // 默认不初始化，子类按需重写
    }

    /**
     * 清理测试数据
     * 子类可以重写此方法
     */
    protected void cleanTestData() throws SQLException {
        // 默认不清理，子类按需重写
    }

    /**
     * 执行SQL语句
     */
    protected void executeSQL(String sql) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }

    /**
     * 清空指定表数据（处理外键约束）
     * 临时禁用外键检查以支持有外键引用的表
     */
    protected void truncateTable(String tableName) throws SQLException {
        executeSQL("SET FOREIGN_KEY_CHECKS=0");
        try {
            executeSQL("TRUNCATE TABLE " + tableName);
        } finally {
            executeSQL("SET FOREIGN_KEY_CHECKS=1");
        }
    }

    /**
     * 删除指定表中符合条件的数据
     */
    protected void deleteFromTable(String tableName, String condition) throws SQLException {
        executeSQL("DELETE FROM " + tableName + " WHERE " + condition);
    }
}
