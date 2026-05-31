package com.example.dao;

import com.example.model.RectificationTask;
import com.example.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 整改任务数据访问层
 */
public class RectificationTaskDAO {
    private static final Logger logger = LoggerFactory.getLogger(RectificationTaskDAO.class);


    /**
     * 插入整改任务，返回自增ID
     */
    public Long insert(RectificationTask task) {
        return insert(task, null);
    }

    /**
     * 插入整改任务，返回自增ID（支持外部事务连接）
     */
    public Long insert(RectificationTask task, Connection conn) {
        String sql = "INSERT INTO rectification_task (violation_id, user_id, requirement, deadline, status, create_time, update_time) VALUES (?, ?, ?, ?, ?, NOW(), NOW())";
        boolean externalConn = conn != null;
        try {
            if (!externalConn) conn = DBUtil.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setLong(1, task.getViolationId());
                ps.setLong(2, task.getUserId());
                ps.setString(3, task.getRequirement());
                ps.setTimestamp(4, task.getDeadline());
                ps.setString(5, task.getStatus());
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getLong(1);
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("插入整改任务失败", e);
        } finally {
            if (!externalConn) try { conn.close(); } catch (SQLException ignored) {}
        }
        return null;
    }

    /**
     * 根据ID查询
     */
    public RectificationTask findById(Long id) {
        String sql = "SELECT * FROM rectification_task WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return extractTask(rs);
                }
            }
        } catch (SQLException e) {
            logger.error("根据ID查询整改任务失败, id={}", id, e);
        }
        return null;
    }

    /**
     * 按违规ID查询是否已有整改任务
     */
    public RectificationTask findByViolationId(Long violationId) {
        String sql = "SELECT * FROM rectification_task WHERE violation_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, violationId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return extractTask(rs);
                }
            }
        } catch (SQLException e) {
            logger.error("根据违规ID查询整改任务失败, violationId={}", violationId, e);
        }
        return null;
    }

    /**
     * 按用户ID分页查询，按create_time DESC排序
     */
    public List<RectificationTask> findByUserId(Long userId, int offset, int limit) {
        return findByUserId(userId, offset, limit, null);
    }

    /**
     * 按用户ID分页查询整改任务，支持状态筛选
     */
    public List<RectificationTask> findByUserId(Long userId, int offset, int limit, String status) {
        List<RectificationTask> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM rectification_task WHERE user_id = ?");
        List<String> params = new ArrayList<>();

        if (status != null && !status.trim().isEmpty()) {
            sql.append(" AND status = ?");
            params.add(status.trim());
        }
        sql.append(" ORDER BY create_time DESC LIMIT ? OFFSET ?");

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int idx = 1;
            ps.setLong(idx++, userId);
            for (String param : params) {
                ps.setString(idx++, param);
            }
            ps.setInt(idx++, limit);
            ps.setInt(idx, offset);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(extractTask(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("按用户ID分页查询整改任务失败", e);
        }
        return list;
    }

    /**
     * 管理员分页查询，支持状态筛选
     */
    public List<RectificationTask> findAll(int offset, int limit, String status) {
        List<RectificationTask> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM rectification_task WHERE 1=1");
        List<String> params = new ArrayList<>();

        if (status != null && !status.trim().isEmpty()) {
            sql.append(" AND status = ?");
            params.add(status.trim());
        }
        sql.append(" ORDER BY create_time DESC LIMIT ? OFFSET ?");

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int idx = 1;
            for (String param : params) {
                ps.setString(idx++, param);
            }
            ps.setInt(idx++, limit);
            ps.setInt(idx, offset);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(extractTask(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("管理员分页查询整改任务失败", e);
        }
        return list;
    }

    /**
     * 按用户ID统计整改任务数
     */
    public int countByUserId(Long userId) {
        return countByUserId(userId, null);
    }

    /**
     * 按用户ID统计整改任务数，支持状态筛选
     */
    public int countByUserId(Long userId, String status) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM rectification_task WHERE user_id = ?");
        List<String> params = new ArrayList<>();

        if (status != null && !status.trim().isEmpty()) {
            sql.append(" AND status = ?");
            params.add(status.trim());
        }

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int idx = 1;
            ps.setLong(idx++, userId);
            for (String param : params) {
                ps.setString(idx++, param);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            logger.error("按用户ID统计整改任务数失败, userId={}", userId, e);
        }
        return 0;
    }

    /**
     * 管理员统计整改任务数，支持状态筛选
     */
    public int countAll(String status) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM rectification_task WHERE 1=1");
        List<String> params = new ArrayList<>();

        if (status != null && !status.trim().isEmpty()) {
            sql.append(" AND status = ?");
            params.add(status.trim());
        }

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setString(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            logger.error("管理员统计整改任务数失败", e);
        }
        return 0;
    }

    /**
     * 用户提交整改
     */
    public void submit(Long id, String submitDesc, String submitImagePath, String status) {
        String sql = "UPDATE rectification_task SET submit_desc = ?, submit_image_path = ?, status = ?, update_time = NOW() WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, submitDesc);
            ps.setString(2, submitImagePath);
            ps.setString(3, status);
            ps.setLong(4, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.error("更新整改任务状态失败", e);
        }
    }

    /**
     * 管理员审核整改
     */
    public void review(Long id, String reviewResult, String reviewComment, String status) {
        review(id, reviewResult, reviewComment, status, null);
    }

    /**
     * 管理员审核整改（支持外部事务连接）
     */
    public void review(Long id, String reviewResult, String reviewComment, String status, Connection conn) {
        String sql = "UPDATE rectification_task SET review_result = ?, review_comment = ?, status = ?, update_time = NOW() WHERE id = ?";
        boolean externalConn = conn != null;
        try {
            if (!externalConn) conn = DBUtil.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, reviewResult);
                ps.setString(2, reviewComment);
                ps.setString(3, status);
                ps.setLong(4, id);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            logger.error("审核整改任务失败", e);
        } finally {
            if (!externalConn) try { conn.close(); } catch (SQLException ignored) {}
        }
    }

    /**
     * 从 ResultSet 提取整改任务对象
     */
    private RectificationTask extractTask(ResultSet rs) throws SQLException {
        RectificationTask task = new RectificationTask();
        task.setId(rs.getLong("id"));
        task.setViolationId(rs.getLong("violation_id"));
        task.setUserId(rs.getLong("user_id"));
        task.setRequirement(rs.getString("requirement"));
        task.setDeadline(rs.getTimestamp("deadline"));
        task.setStatus(rs.getString("status"));
        task.setSubmitDesc(rs.getString("submit_desc"));
        task.setSubmitImagePath(rs.getString("submit_image_path"));
        task.setReviewResult(rs.getString("review_result"));
        task.setReviewComment(rs.getString("review_comment"));
        task.setCreateTime(rs.getTimestamp("create_time"));
        task.setUpdateTime(rs.getTimestamp("update_time"));
        return task;
    }

    /**
     * 根据ID删除整改任务
     */
    public boolean deleteById(Long id) {
        return deleteById(id, null);
    }

    /**
     * 根据ID删除整改任务（支持外部事务连接）
     */
    public boolean deleteById(Long id, Connection conn) {
        String sql = "DELETE FROM rectification_task WHERE id = ?";
        boolean externalConn = conn != null;
        try {
            if (!externalConn) conn = DBUtil.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, id);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            logger.error("删除整改任务失败", e);
        } finally {
            if (!externalConn) try { conn.close(); } catch (SQLException ignored) {}
        }
        return false;
    }

    /**
     * 按用户ID删除整改任务（支持外部事务连接）
     * 注意：数据库有 ON DELETE CASCADE，此方法作为应用层保障
     */
    public int deleteByUserId(Long userId, Connection conn) {
        String sql = "DELETE FROM rectification_task WHERE user_id = ?";
        boolean externalConn = conn != null;
        try {
            if (!externalConn) conn = DBUtil.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, userId);
                return ps.executeUpdate();
            }
        } catch (SQLException e) {
            logger.error("按用户ID删除整改任务失败, userId={}", userId, e);
        } finally {
            if (!externalConn) try { conn.close(); } catch (SQLException ignored) {}
        }
        return 0;
    }
}
