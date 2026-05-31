package com.example.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

/**
 * 数据库连接工具类（基于 HikariCP）。
 *
 * 配置优先级：系统属性 > 环境变量 > classpath:db.properties。
 * 为避免把生产密码带进源码仓库，db.password 没有默认值，缺失则抛错，
 * 需通过 -Ddb.password=xxx、DB_PASSWORD 环境变量或部署机上的 db.properties 提供。
 */
public class DBUtil {

    private static final HikariDataSource DATA_SOURCE;

    static {
        Properties fileProps = loadPropertiesFile();

        String driver = resolve("db.driver", "DB_DRIVER", fileProps, "com.mysql.cj.jdbc.Driver");
        String url = resolve("db.url", "DB_URL", fileProps, null);
        String username = resolve("db.username", "DB_USERNAME", fileProps, null);
        String password = resolve("db.password", "DB_PASSWORD", fileProps, null);

        if (url == null || url.isEmpty()) {
            throw new IllegalStateException("数据库连接未配置：请设置 db.url / DB_URL 或在 db.properties 中提供");
        }
        if (username == null || username.isEmpty()) {
            throw new IllegalStateException("数据库账号未配置：请设置 db.username / DB_USERNAME 或在 db.properties 中提供");
        }
        if (password == null) {
            throw new IllegalStateException("数据库密码未配置：请设置 db.password / DB_PASSWORD 或在 db.properties 中提供（禁止使用硬编码默认值）");
        }

        HikariConfig config = new HikariConfig();
        config.setDriverClassName(driver);
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);
        config.setPoolName("ai-trashwatch-pool");
        config.setMaximumPoolSize(parseInt(resolve("db.pool.maxSize", "DB_POOL_MAX", fileProps, "10"), 10));
        config.setMinimumIdle(parseInt(resolve("db.pool.minIdle", "DB_POOL_MIN", fileProps, "2"), 2));
        config.setConnectionTimeout(parseInt(resolve("db.pool.connTimeoutMs", "DB_POOL_CONN_TIMEOUT", fileProps, "10000"), 10000));
        config.setIdleTimeout(parseInt(resolve("db.pool.idleTimeoutMs", "DB_POOL_IDLE_TIMEOUT", fileProps, "600000"), 600000));
        config.setMaxLifetime(parseInt(resolve("db.pool.maxLifetimeMs", "DB_POOL_MAX_LIFETIME", fileProps, "1800000"), 1800000));

        DATA_SOURCE = new HikariDataSource(config);
    }

    private DBUtil() {}

    public static Connection getConnection() throws SQLException {
        return DATA_SOURCE.getConnection();
    }

    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException ignored) {
            }
        }
    }

    /**
     * 预热连接池 — 提前初始化 HikariCP，避免首次请求卡顿
     */
    public static void warmUp() throws SQLException {
        try (Connection conn = DATA_SOURCE.getConnection()) {
            // 获取并立即归还连接，触发连接池初始化
        }
    }

    private static Properties loadPropertiesFile() {
        Properties props = new Properties();
        try (InputStream in = DBUtil.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (in != null) {
                props.load(in);
            }
        } catch (IOException ignored) {
        }
        return props;
    }

    private static String resolve(String sysKey, String envKey, Properties fileProps, String defaultValue) {
        String v = System.getProperty(sysKey);
        if (v != null && !v.isEmpty()) return v;
        v = System.getenv(envKey);
        if (v != null && !v.isEmpty()) return v;
        v = fileProps.getProperty(sysKey);
        if (v != null && !v.isEmpty()) return v;
        return defaultValue;
    }

    private static int parseInt(String s, int defaultValue) {
        try {
            return Integer.parseInt(s);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * 开启手动事务（关闭自动提交）
     */
    public static void beginTransaction(Connection conn) throws SQLException {
        if (conn != null) {
            conn.setAutoCommit(false);
        }
    }

    /**
     * 提交事务并恢复自动提交
     */
    public static void commitTransaction(Connection conn) throws SQLException {
        if (conn != null) {
            conn.commit();
            conn.setAutoCommit(true);
        }
    }

    /**
     * 回滚事务并恢复自动提交
     */
    public static void rollbackTransaction(Connection conn) {
        if (conn != null) {
            try {
                conn.rollback();
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                // rollback 失败只记录，不抛出
            }
        }
    }
}
