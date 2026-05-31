package com.example.test.service;

import com.example.dao.OperationLogDAO;
import com.example.dao.UserDAO;
import com.example.model.User;
import com.example.service.UserService;
import com.example.test.BaseTest;
import com.example.util.BCryptUtil;
import com.example.util.BusinessException;
import org.junit.Test;

import java.sql.SQLException;

import static org.junit.Assert.*;

public class UserSecurityLogicTest extends BaseTest {

    private UserService userService = new UserService(new UserDAO(), new OperationLogDAO());

    private static final String PREFIX = "sec_test_";
    private static final String PASSWORD = "SecTest@123";

    @Override
    protected void initTestData() throws SQLException {
        executeSQL("DELETE FROM operation_log WHERE username LIKE '" + PREFIX + "%'");
        executeSQL("DELETE FROM user WHERE username LIKE '" + PREFIX + "%'");
        String hash = BCryptUtil.hashPassword(PASSWORD);
        executeSQL("INSERT INTO user (username, password_hash, email, phone, role, status, create_time, update_time) VALUES " +
                "('" + PREFIX + "admin', '" + hash + "', 'admin@sectest.com', '13800000001', 'admin', 1, NOW(), NOW()), " +
                "('" + PREFIX + "user1', '" + hash + "', 'user1@sectest.com', '13800000002', 'user', 1, NOW(), NOW())");
    }

    @Override
    protected void cleanTestData() throws SQLException {
        executeSQL("DELETE FROM operation_log WHERE username LIKE '" + PREFIX + "%'");
        executeSQL("DELETE FROM user WHERE username LIKE '" + PREFIX + "%'");
    }

    @Test
    public void testCannotModifyOwnRole() {
        User admin = userService.login(PREFIX + "admin", PASSWORD, "127.0.0.1");
        assertEquals("admin", admin.getRole());

        User updateData = new User();
        updateData.setId(admin.getId());
        updateData.setUsername(admin.getUsername());
        updateData.setRole("user");
        updateData.setEmail(admin.getEmail());
        updateData.setPhone(admin.getPhone());

        userService.updateUser(updateData, admin, "127.0.0.1");

        User reloaded = userService.getUserById(admin.getId());
        assertEquals("admin", reloaded.getRole());
    }

    @Test
    public void testClearSensitiveFields() {
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("secret");
        user.setPasswordHash("$2a$10$somehashvalue");
        user.setRole("user");

        user.clearSensitiveFields();

        assertNull(user.getPassword());
        assertNull(user.getPasswordHash());
    }

    @Test
    public void testRegisterForcesUserRole() {
        String username = PREFIX + "reguser_" + System.currentTimeMillis();
        User registered = userService.register(username, "RegPass@123", null, null);

        assertNotNull(registered);
        assertEquals("user", registered.getRole());
    }
}
