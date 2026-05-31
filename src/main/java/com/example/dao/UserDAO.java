package com.example.dao;

import com.example.model.User;
import com.example.util.BCryptUtil;
import com.example.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 用户数据访问层
 */
public class UserDAO {
    private static final Logger logger = LoggerFactory.getLogger(UserDAO.class);
    private static final String USER_COLUMNS = "id, username, email, phone, role, status, create_time, update_time";

    
    /**
     * 用户登录验证
     */
    public User login(String username, String password) {
        User user = findByUsername(username);
        if (user == null) {
            return null;
        }

        if (user.getPasswordHash() != null) {
            if (BCryptUtil.verifyPassword(password, user.getPasswordHash())) {
                return user;
            }
        }

        return null;
    }

    /**
     * 根据用户名查询
     */
    public User findByUsername(String username) {
        String sql = "SELECT * FROM user WHERE username = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return extractUser(rs);
                }
            }
        } catch (SQLException e) {
            logger.error("登录查询失败, username={}", username, e);
        }
        return null;
    }

    /**
     * 根据ID查询
     */
    public User findById(Integer id) {
        String sql = "SELECT * FROM user WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return extractUser(rs);
                }
            }
        } catch (SQLException e) {
            logger.error("根据ID查询用户失败, id={}", id, e);
        }
        return null;
    }

    /**
     * 分页查询所有用户（管理员功能）
     */
    public List<User> findAll(int page, int pageSize) {
        List<User> users = new ArrayList<>();
        String sql = "SELECT " + USER_COLUMNS + " FROM user ORDER BY id LIMIT ? OFFSET ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pageSize);
            ps.setInt(2, (page - 1) * pageSize);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    users.add(extractUserSafe(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("分页查询所有用户失败, page={}, pageSize={}", page, pageSize, e);
        }
        return users;
    }

    /**
     * 搜索用户
     * NOTE: %keyword% 前缀通配符导致全表扫描，数据量 >1万时建议添加 FULLTEXT 索引或使用 Elasticsearch
     */
    public List<User> search(String keyword, int page, int pageSize) {
        List<User> users = new ArrayList<>();
        String sql = "SELECT " + USER_COLUMNS + " FROM user WHERE username LIKE ? ORDER BY id LIMIT ? OFFSET ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String likeKeyword = "%" + keyword + "%";
            ps.setString(1, likeKeyword);
            ps.setInt(2, pageSize);
            ps.setInt(3, (page - 1) * pageSize);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    users.add(extractUserSafe(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("搜索用户失败, keyword={}", keyword, e);
        }
        return users;
    }

    /**
     * 统计用户总数
     */
    public int countAll() {
        String sql = "SELECT COUNT(*) FROM user";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            logger.error("统计用户总数失败", e);
        }
        return 0;
    }

    /**
     * 按状态统计用户数
     */
    public int countByStatus(int status) {
        String sql = "SELECT COUNT(*) FROM user WHERE status = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, status);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            logger.error("按状态统计用户数失败, status={}", status, e);
        }
        return 0;
    }

    /**
     * 按角色统计用户数
     */
    public int countByRole(String role) {
        String sql = "SELECT COUNT(*) FROM user WHERE role = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, role);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            logger.error("按角色统计用户数失败, role={}", role, e);
        }
        return 0;
    }

    public Map<String, Integer> countDashboardStats() {
        String sql = "SELECT COUNT(*) AS totalUsers, SUM(status = 1) AS activeUsers, " +
                "SUM(status = 0) AS disabledUsers, SUM(role = 'admin') AS adminCount, " +
                "SUM(role = 'user') AS userCount FROM user";
        Map<String, Integer> stats = new HashMap<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                stats.put("totalUsers", rs.getInt("totalUsers"));
                stats.put("activeUsers", rs.getInt("activeUsers"));
                stats.put("disabledUsers", rs.getInt("disabledUsers"));
                stats.put("adminCount", rs.getInt("adminCount"));
                stats.put("userCount", rs.getInt("userCount"));
            }
        } catch (SQLException e) {
            logger.error("查询仪表盘统计失败", e);
        }
        return stats;
    }

    /**
     * 统计今日新增用户
     */
    public int countTodayNew() {
        String sql = "SELECT COUNT(*) FROM user WHERE DATE(create_time) = CURDATE()";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            logger.error("统计今日新增用户失败", e);
        }
        return 0;
    }

    /**
     * 搜索结果计数
     */
    public int countSearch(String keyword) {
        String sql = "SELECT COUNT(*) FROM user WHERE username LIKE ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String likeKeyword = "%" + keyword + "%";
            ps.setString(1, likeKeyword);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            logger.error("搜索用户计数失败, keyword={}", keyword, e);
        }
        return 0;
    }

    /**
     * 检查用户名是否存在
     */
    public boolean existsByUsername(String username) {
        String sql = "SELECT 1 FROM user WHERE username = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            logger.error("检查用户名存在失败, username={}", username, e);
        }
        return false;
    }

    /**
     * 检查邮箱是否存在
     */
    public boolean existsByEmail(String email) {
        String sql = "SELECT 1 FROM user WHERE email = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            logger.error("检查邮箱存在失败, email={}", email, e);
        }
        return false;
    }

    /**
     * 创建用户（注册）
     */
    public boolean create(User user) {
        String sql = "INSERT INTO user (username, password_hash, email, phone, role, status, create_time, update_time) VALUES (?, ?, ?, ?, ?, ?, NOW(), NOW())";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPasswordHash());
            ps.setString(3, user.getEmail());
            ps.setString(4, user.getPhone());
            ps.setString(5, user.getRole() != null ? user.getRole() : "user");
            ps.setInt(6, 1);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("创建用户失败, username={}", user.getUsername(), e);
        }
        return false;
    }

    /**
     * 更新用户信息
     */
    public boolean update(User user) {
        String sql = "UPDATE user SET email = ?, phone = ?, update_time = NOW() WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getPhone());
            ps.setInt(3, user.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("更新用户信息失败, id={}", user.getId(), e);
        }
        return false;
    }

    /**
     * 更新密码
     */
    public boolean updatePassword(Integer userId, String newPasswordHash) {
        String sql = "UPDATE user SET password_hash = ?, update_time = NOW() WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newPasswordHash);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("更新密码失败, userId={}", userId, e);
        }
        return false;
    }

    /**
     * 更新用户状态
     */
    public boolean updateStatus(Integer userId, int status) {
        String sql = "UPDATE user SET status = ?, update_time = NOW() WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, status);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("更新用户状态失败, userId={}, status={}", userId, status, e);
        }
        return false;
    }

    /**
     * 删除用户
     */
    public boolean delete(Integer userId) {
        return delete(userId, null);
    }

    /**
     * 删除用户（支持外部事务连接）
     */
    public boolean delete(Integer userId, Connection conn) {
        String sql = "DELETE FROM user WHERE id = ?";
        boolean externalConn = conn != null;
        try {
            if (!externalConn) conn = DBUtil.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, userId);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            logger.error("删除用户失败, userId={}", userId, e);
        } finally {
            if (!externalConn) try { conn.close(); } catch (SQLException ignored) {}
        }
        return false;
    }

    /**
     * 更新用户资料（同 update）
     * @deprecated 使用 {@link #update(User)} 代替
     */
    @Deprecated
    public boolean updateProfile(User user) {
        return update(user);
    }

    /**
     * 按用户名模糊搜索（分页）
     */
    public List<User> findByUsernamePrefix(String prefix, int page, int pageSize) {
        List<User> users = new ArrayList<>();
        String sql = "SELECT " + USER_COLUMNS + " FROM user WHERE username LIKE ? ORDER BY id LIMIT ? OFFSET ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, prefix + "%");
            ps.setInt(2, pageSize);
            ps.setInt(3, (page - 1) * pageSize);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    users.add(extractUserSafe(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("按前缀查询用户失败, prefix={}", prefix, e);
        }
        return users;
    }

    /**
     * 按用户名前缀统计数量
     */
    public int countByUsernamePrefix(String prefix) {
        String sql = "SELECT COUNT(*) FROM user WHERE username LIKE ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, prefix + "%");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            logger.error("按前缀统计用户数失败, prefix={}", prefix, e);
        }
        return 0;
    }

    /**
     * 修改密码（同 updatePassword）
     * @deprecated 使用 {@link #updatePassword(Integer, String)} 代替
     */
    @Deprecated
    public boolean changePassword(Integer userId, String newPasswordHash) {
        return updatePassword(userId, newPasswordHash);
    }

    /**
     * 从 ResultSet 提取用户对象
     */
    private User extractUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setEmail(rs.getString("email"));
        user.setPhone(rs.getString("phone"));
        user.setRole(rs.getString("role"));
        user.setStatus(rs.getInt("status"));
        user.setCreateTime(rs.getTimestamp("create_time"));
        user.setUpdateTime(rs.getTimestamp("update_time"));
        return user;
    }

    private User extractUserSafe(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setUsername(rs.getString("username"));
        user.setEmail(rs.getString("email"));
        user.setPhone(rs.getString("phone"));
        user.setRole(rs.getString("role"));
        user.setStatus(rs.getInt("status"));
        user.setCreateTime(rs.getTimestamp("create_time"));
        user.setUpdateTime(rs.getTimestamp("update_time"));
        return user;
    }
}