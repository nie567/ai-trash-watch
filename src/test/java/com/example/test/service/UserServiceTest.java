package com.example.test.service;

import com.example.dao.OperationLogDAO;
import com.example.dao.UserDAO;
import com.example.model.PageResult;
import com.example.model.User;
import com.example.service.UserService;
import com.example.test.BaseTest;
import com.example.util.AppConstants;
import com.example.util.BCryptUtil;
import com.example.util.BusinessException;
import org.junit.Test;

import java.sql.SQLException;
import java.util.Map;

import static org.junit.Assert.*;

public class UserServiceTest extends BaseTest {

    private UserService userService = new UserService(new UserDAO(), new OperationLogDAO());

    private static final String PREFIX = "svc_test_";
    private static final String PASSWORD = "Test@1234";

    @Override
    protected void initTestData() throws SQLException {
        executeSQL("DELETE FROM operation_log WHERE username LIKE '" + PREFIX + "%'");
        executeSQL("DELETE FROM user WHERE username LIKE '" + PREFIX + "%'");
        String hash = BCryptUtil.hashPassword(PASSWORD);
        executeSQL("INSERT INTO user (username, password_hash, email, phone, role, status, create_time, update_time) VALUES " +
                "(" + sqlStr(PREFIX + "admin") + ", '" + hash + "', 'admin@svctest.com', '13800000001', 'admin', 1, NOW(), NOW()), " +
                "(" + sqlStr(PREFIX + "user1") + ", '" + hash + "', 'user1@svctest.com', '13800000002', 'user', 1, NOW(), NOW()), " +
                "(" + sqlStr(PREFIX + "disabled") + ", '" + hash + "', 'disabled@svctest.com', '13800000003', 'user', 0, NOW(), NOW())");
    }

    @Override
    protected void cleanTestData() throws SQLException {
        executeSQL("DELETE FROM operation_log WHERE username LIKE '" + PREFIX + "%'");
        executeSQL("DELETE FROM user WHERE username LIKE '" + PREFIX + "%'");
    }

    private static String sqlStr(String s) {
        return "'" + s + "'";
    }

    @Test
    public void testLogin_success() {
        User user = userService.login(PREFIX + "user1", PASSWORD, "127.0.0.1");
        assertNotNull(user);
        assertEquals(PREFIX + "user1", user.getUsername());
        assertTrue(user.isActive());
    }

    @Test
    public void testLogin_wrongPassword() {
        try {
            userService.login(PREFIX + "user1", "WrongPass@99", "127.0.0.1");
            fail("密码错误应抛出异常");
        } catch (BusinessException e) {
            assertEquals(401, e.getCode());
        }
    }

    @Test
    public void testLogin_nonExistent() {
        try {
            userService.login(PREFIX + "nonexistent", PASSWORD, "127.0.0.1");
            fail("用户不存在应抛出异常");
        } catch (BusinessException e) {
            assertEquals(401, e.getCode());
        }
    }

    @Test
    public void testCreateUser_success() {
        User newUser = new User();
        newUser.setUsername(PREFIX + "newuser");
        newUser.setPassword("NewPass@123");
        newUser.setEmail("newuser@svctest.com");
        newUser.setPhone("13800000999");

        User operator = userService.login(PREFIX + "admin", PASSWORD, "127.0.0.1");
        User created = userService.createUser(newUser, operator, "127.0.0.1");

        assertNotNull(created);
        assertEquals(PREFIX + "newuser", created.getUsername());
    }

    @Test
    public void testCreateUser_duplicateUsername() {
        User dup = new User();
        dup.setUsername(PREFIX + "user1");
        dup.setPassword("DupPass@123");

        User operator = userService.login(PREFIX + "admin", PASSWORD, "127.0.0.1");
        try {
            userService.createUser(dup, operator, "127.0.0.1");
            fail("重复用户名应抛出异常");
        } catch (BusinessException e) {
            assertEquals(400, e.getCode());
        }
    }

    @Test
    public void testUpdateUser() {
        User operator = userService.login(PREFIX + "admin", PASSWORD, "127.0.0.1");
        User user = userService.login(PREFIX + "user1", PASSWORD, "127.0.0.1");

        user.setEmail("updated@svctest.com");
        user.setPhone("13900000001");
        userService.updateUser(user, operator, "127.0.0.1");

        User updated = userService.getUserById(user.getId());
        assertEquals("updated@svctest.com", updated.getEmail());
        assertEquals("13900000001", updated.getPhone());
    }

    @Test
    public void testDeleteUser() {
        User operator = userService.login(PREFIX + "admin", PASSWORD, "127.0.0.1");
        User target = userService.login(PREFIX + "user1", PASSWORD, "127.0.0.1");

        userService.deleteUser(target.getId(), operator, "127.0.0.1");

        try {
            userService.getUserById(target.getId());
            fail("删除后应查不到");
        } catch (BusinessException e) {
            assertEquals(404, e.getCode());
        }
    }

    @Test
    public void testUpdateUserStatus() {
        User operator = userService.login(PREFIX + "admin", PASSWORD, "127.0.0.1");
        User target = userService.login(PREFIX + "user1", PASSWORD, "127.0.0.1");

        userService.updateUserStatus(target.getId(), AppConstants.STATUS_DISABLED, operator, "127.0.0.1");

        User updated = userService.getUserById(target.getId());
        assertEquals(Integer.valueOf(AppConstants.STATUS_DISABLED), updated.getStatus());
    }

    @Test
    public void testChangePassword_success() {
        User user = userService.login(PREFIX + "user1", PASSWORD, "127.0.0.1");
        String newPassword = "NewPass@999";

        userService.changePassword(user.getId(), PASSWORD, newPassword);

        User afterChange = userService.login(PREFIX + "user1", newPassword, "127.0.0.1");
        assertNotNull("新密码应能登录", afterChange);
    }

    @Test
    public void testChangePassword_wrongOldPassword() {
        User user = userService.login(PREFIX + "user1", PASSWORD, "127.0.0.1");
        try {
            userService.changePassword(user.getId(), "WrongOld@99", "NewPass@999");
            fail("旧密码错误应抛出异常");
        } catch (BusinessException e) {
            assertEquals(400, e.getCode());
        }
    }

    @Test
    public void testUpdateProfile() {
        User user = userService.login(PREFIX + "user1", PASSWORD, "127.0.0.1");

        userService.updateProfile(user.getId(), "profile@svctest.com", "13700000001");

        User updated = userService.getUserById(user.getId());
        assertEquals("profile@svctest.com", updated.getEmail());
        assertEquals("13700000001", updated.getPhone());
    }

    @Test
    public void testGetDashboardStats() {
        Map<String, Object> stats = userService.getDashboardStats();
        assertNotNull(stats);
        assertTrue(stats.containsKey("totalUsers"));
        assertTrue(stats.containsKey("todayNew"));
        assertTrue(stats.containsKey("adminCount"));
        assertTrue(stats.containsKey("userCount"));
        assertTrue(stats.containsKey("activeUsers"));
        assertTrue(stats.containsKey("disabledUsers"));
    }

    @Test
    public void testGetUserPage() {
        PageResult<User> page = userService.getUserPage(1, 10, null);
        assertNotNull(page);
        assertNotNull(page.getData());
    }
}
