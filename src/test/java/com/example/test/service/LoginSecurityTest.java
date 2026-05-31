package com.example.test.service;

import com.example.dao.OperationLogDAO;
import com.example.dao.UserDAO;
import com.example.model.User;
import com.example.service.UserService;
import com.example.test.BaseTest;
import com.example.util.AppConstants;
import com.example.util.BCryptUtil;
import com.example.util.BusinessException;
import org.junit.Test;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.Assert.*;

public class LoginSecurityTest extends BaseTest {

    private UserService userService = new UserService(new UserDAO(), new OperationLogDAO());
    private UserDAO userDAO = new UserDAO();

    private static final String PREFIX = "login_sec_";
    private static final String PASSWORD = "Test@1234";

    @Override
    protected void initTestData() throws SQLException {
        executeSQL("DELETE FROM operation_log WHERE username LIKE '" + PREFIX + "%'");
        executeSQL("DELETE FROM user WHERE username LIKE '" + PREFIX + "%'");
        String hash = BCryptUtil.hashPassword(PASSWORD);
        executeSQL("INSERT INTO user (username, password_hash, email, phone, role, status, create_time, update_time) VALUES " +
                "(" + q(PREFIX + "admin") + ", '" + hash + "', 'admin@loginsec.com', '13900000001', 'admin', 1, NOW(), NOW()), " +
                "(" + q(PREFIX + "user1") + ", '" + hash + "', 'user1@loginsec.com', '13900000002', 'user', 1, NOW(), NOW()), " +
                "(" + q(PREFIX + "disabled") + ", '" + hash + "', 'disabled@loginsec.com', '13900000003', 'user', 0, NOW(), NOW())");
    }

    @Override
    protected void cleanTestData() throws SQLException {
        executeSQL("DELETE FROM operation_log WHERE username LIKE '" + PREFIX + "%'");
        executeSQL("DELETE FROM user WHERE username LIKE '" + PREFIX + "%'");
    }

    private static String q(String s) {
        return "'" + s + "'";
    }

    @Test
    public void testLoginEmptyUsername() {
        try {
            userService.login("", PASSWORD, "127.0.0.1");
            fail("空用户名应抛出异常");
        } catch (BusinessException e) {
            assertEquals(400, e.getCode());
        }
    }

    @Test
    public void testLoginEmptyPassword() {
        try {
            userService.login(PREFIX + "user1", "", "127.0.0.1");
            fail("空密码应抛出异常");
        } catch (BusinessException e) {
            assertEquals(400, e.getCode());
        }
    }

    @Test
    public void testLoginDisabledAccount() {
        try {
            userService.login(PREFIX + "disabled", PASSWORD, "127.0.0.1");
            fail("禁用账号应抛出异常");
        } catch (BusinessException e) {
            assertEquals(401, e.getCode());
            assertTrue("应提示账号已被禁用", e.getMessage().contains("禁用"));
        }
    }

    @Test
    public void testPlaintextPasswordAutoMigration() throws SQLException {
        String plainPwd = "Plain@123";
        String migrateUser = PREFIX + "migrate";
        executeSQL("INSERT INTO user (username, password, password_hash, email, phone, role, status, create_time, update_time) VALUES " +
                "(" + q(migrateUser) + ", " + q(plainPwd) + ", NULL, 'migrate@loginsec.com', '13900000099', 'user', 1, NOW(), NOW())");

        User user = userService.login(migrateUser, plainPwd, "127.0.0.1");
        assertNotNull("明文密码登录应成功", user);

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT password_hash FROM user WHERE username = " + q(migrateUser))) {
            assertTrue("应能查到用户", rs.next());
            String newHash = rs.getString("password_hash");
            assertNotNull("密码hash不应为null", newHash);
            assertTrue("密码hash应已迁移为BCrypt格式（以$2a$开头）", newHash.startsWith("$2a$"));
        }

        executeSQL("DELETE FROM user WHERE username = " + q(migrateUser));
    }

    @Test
    public void testCannotDeleteSelf() {
        User admin = userService.login(PREFIX + "admin", PASSWORD, "127.0.0.1");
        assertNotNull(admin);

        try {
            userService.deleteUser(admin.getId(), admin, "127.0.0.1");
            fail("不能删除自己的账号");
        } catch (BusinessException e) {
            assertEquals(400, e.getCode());
            assertTrue("应提示不能删除自己", e.getMessage().contains("不能删除"));
        }
    }

    @Test
    public void testCannotDisableSelf() {
        User admin = userService.login(PREFIX + "admin", PASSWORD, "127.0.0.1");
        assertNotNull(admin);

        try {
            userService.updateUserStatus(admin.getId(), AppConstants.STATUS_DISABLED, admin, "127.0.0.1");
            fail("不能禁用自己的账号");
        } catch (BusinessException e) {
            assertEquals(400, e.getCode());
            assertTrue("应提示不能修改自己的状态", e.getMessage().contains("不能修改"));
        }
    }
}
