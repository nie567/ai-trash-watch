package com.example.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * 数据库连接工具类
 * 单例模式管理数据库连接
 */
public class DBUtil {
    private static String url;
    private static String username;
    private static String password;
    private static String driverClass;
    
    private static volatile DBUtil instance;
    
    static {
        // 从环境变量或系统属性读取配置
        url = System.getProperty("db.url", "jdbc:mysql://localhost:3306/user_management?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf8");
        username = System.getProperty("db.username", "root");
        password = System.getProperty("db.password", "123456");
        driverClass = System.getProperty("db.driver", "com.mysql.cj.jdbc.Driver");
        
        try {
            Class.forName(driverClass);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("数据库驱动加载失败", e);
        }
    }
    
    private DBUtil() {}
    
    public static DBUtil getInstance() {
        if (instance == null) {
            synchronized (DBUtil.class) {
                if (instance == null) {
                    instance = new DBUtil();
                }
            }
        }
        return instance;
    }
    
    /**
     * 获取数据库连接
     */
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }
    
    /**
     * 关闭连接
     */
    public void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}