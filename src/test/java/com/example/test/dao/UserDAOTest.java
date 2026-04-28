package com.example.test.dao;

import com.example.dao.UserDAO;
import com.example.model.User;
import com.example.test.BaseTest;
import com.example.util.BCryptUtil;
import org.junit.Test;

import java.sql.SQLException;
import java.util.List;

import static org.junit.Assert.*;

public class UserDAOTest extends BaseTest {

    private UserDAO userDAO = new UserDAO();

    @Override
    protected void initTestData() throws SQLException {
        executeSQL("DELETE FROM user WHERE username LIKE 'test_user_%'");
        String hash1 = BCryptUtil.hashPassword("password1");
        String hash2 = BCryptUtil.hashPassword("password2");
        String hash3 = BCryptUtil.hashPassword("password3");
        executeSQL("INSERT INTO user (username, password_hash, email, phone, role, status, create_time, update_time) VALUES " +
                "('test_user_a', '" + hash1 + "', 'a@test.com', '13800000001', 'user', 1, NOW(), NOW()), " +
                "('test_user_b', '" + hash2 + "', 'b@test.com', '13800000002', 'user', 1, NOW(), NOW()), " +
                "('test_user_c', '" + hash3 + "', 'c@test.com', '13800000003', 'admin', 0, NOW(), NOW())");
    }

    @Override
    protected void cleanTestData() throws SQLException {
        executeSQL("DELETE FROM user WHERE username LIKE 'test_user_%'");
    }

    @Test
    public void testFindByUsername() {
        User user = userDAO.findByUsername("test_user_a");
        assertNotNull(user);
        assertEquals("test_user_a", user.getUsername());
        assertEquals("a@test.com", user.getEmail());

        assertNull(userDAO.findByUsername("nonexistent_user"));
    }

    @Test
    public void testLogin() {
        User user = userDAO.login("test_user_a", "password1");
        assertNotNull("正确密码应登录成功", user);
        assertEquals("test_user_a", user.getUsername());

        assertNull("错误密码应登录失败", userDAO.login("test_user_a", "wrongpassword"));
        assertNull("不存在的用户应登录失败", userDAO.login("nonexistent", "password1"));
    }

    @Test
    public void testFindById() {
        User user = userDAO.findByUsername("test_user_a");
        assertNotNull(user);

        User found = userDAO.findById(user.getId());
        assertNotNull(found);
        assertEquals("test_user_a", found.getUsername());

        assertNull(userDAO.findById(99999));
    }

    @Test
    public void testExistsByUsername() {
        assertTrue(userDAO.existsByUsername("test_user_a"));
        assertFalse(userDAO.existsByUsername("nonexistent_user"));
    }

    @Test
    public void testExistsByEmail() {
        assertTrue(userDAO.existsByEmail("a@test.com"));
        assertFalse(userDAO.existsByEmail("nonexistent@test.com"));
    }

    @Test
    public void testCreate() {
        User newUser = new User();
        newUser.setUsername("test_user_new");
        newUser.setPasswordHash(BCryptUtil.hashPassword("newpass"));
        newUser.setEmail("new@test.com");
        newUser.setPhone("13800000099");
        newUser.setRole("user");

        assertTrue("创建用户应成功", userDAO.create(newUser));
        assertTrue("创建后应能查到", userDAO.existsByUsername("test_user_new"));

        User created = userDAO.findByUsername("test_user_new");
        assertNotNull(created);
        assertEquals("new@test.com", created.getEmail());
    }

    @Test
    public void testUpdateProfile() {
        User user = userDAO.findByUsername("test_user_a");
        assertNotNull(user);

        user.setEmail("updated@test.com");
        user.setPhone("13900000001");

        assertTrue("更新应成功", userDAO.updateProfile(user));

        User updated = userDAO.findByUsername("test_user_a");
        assertEquals("updated@test.com", updated.getEmail());
        assertEquals("13900000001", updated.getPhone());
    }

    @Test
    public void testUpdatePassword() {
        User user = userDAO.findByUsername("test_user_a");
        assertNotNull(user);

        String newHash = BCryptUtil.hashPassword("newpassword");
        assertTrue("更新密码应成功", userDAO.updatePassword(user.getId(), newHash));

        User afterUpdate = userDAO.login("test_user_a", "newpassword");
        assertNotNull("新密码应能登录", afterUpdate);
    }

    @Test
    public void testUpdateStatus() {
        User user = userDAO.findByUsername("test_user_a");
        assertNotNull(user);

        assertTrue("更新状态应成功", userDAO.updateStatus(user.getId(), 0));

        User updated = userDAO.findByUsername("test_user_a");
        assertEquals(0, (long) updated.getStatus());
    }

    @Test
    public void testCountAll() {
        int count = userDAO.countAll();
        assertTrue("应有至少3个用户", count >= 3);
    }

    @Test
    public void testCountByRole() {
        int userCount = userDAO.countByRole("user");
        int adminCount = userDAO.countByRole("admin");
        assertTrue(userCount >= 2);
        assertTrue(adminCount >= 1);
    }

    @Test
    public void testSearch() {
        List<User> results = userDAO.search("test_user_a", 1, 10);
        assertFalse("应能搜索到用户", results.isEmpty());

        List<User> noResults = userDAO.search("zzz_nonexistent", 1, 10);
        assertTrue("不应搜索到不存在的用户", noResults.isEmpty());
    }

    @Test
    public void testDelete() {
        User user = userDAO.findByUsername("test_user_c");
        assertNotNull(user);

        assertTrue("删除应成功", userDAO.delete(user.getId()));
        assertNull("删除后应查不到", userDAO.findByUsername("test_user_c"));
    }
}
