package com.example.dao;

import com.example.model.RectificationTask;
import com.example.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 整改任务数据访问层
 */
public class RectificationTaskDAO {

    /**
     * 插入整改任务，返回自增ID
     */
    public Long insert(RectificationTask task) {
        String sql = "INSERT INTO rectification_task (violation_id, user_id, requirement, deadline, status, create_time, update_time) VALUES (?, ?, ?, ?, ?, NOW(), NOW())";
        try (Connection conn = DBUtil.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
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
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 根据ID查询
     */
    public RectificationTask findById(Long id) {
        String sql = "SELECT * FROM rectification_task WHERE id = ?";
        try (Connection conn = DBUtil.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return extractTask(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 按违规ID查询是否已有整改任务
     */
    public RectificationTask findByViolationId(Long violationId) {
        String sql = "SELECT * FROM rectification_task WHERE violation_id = ?";
        try (Connection conn = DBUtil.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, violationId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return extractTask(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 按用户ID分页查询，按create_time DESC排序
     */
    public List<RectificationTask> findByUserId(Long userId, int offset, int limit) {
        List<RectificationTask> list = new ArrayList<>();
        String sql = "SELECT * FROM rectification_task WHERE user_id = ? ORDER BY create_time DESC LIMIT ? OFFSET ?";
        try (Connection conn = DBUtil.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setInt(2, limit);
            ps.setInt(3, offset);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(extractTask(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
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

        try (Connection conn = DBUtil.getInstance().getConnection();
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
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 按用户ID统计整改任务数
     */
    public int countByUserId(Long userId) {
        String sql = "SELECT COUNT(*) FROM rectification_task WHERE user_id = ?";
        try (Connection conn = DBUtil.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
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

        try (Connection conn = DBUtil.getInstance().getConnection();
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
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 用户提交整改
     */
    public void submit(Long id, String submitDesc, String submitImagePath, String status) {
        String sql = "UPDATE rectification_task SET submit_desc = ?, submit_image_path = ?, status = ?, update_time = NOW() WHERE id = ?";
        try (Connection conn = DBUtil.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, submitDesc);
            ps.setString(2, submitImagePath);
            ps.setString(3, status);
            ps.setLong(4, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 管理员审核整改
     */
    public void review(Long id, String reviewResult, String reviewComment, String status) {
        String sql = "UPDATE rectification_task SET review_result = ?, review_comment = ?, status = ?, update_time = NOW() WHERE id = ?";
        try (Connection conn = DBUtil.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, reviewResult);
            ps.setString(2, reviewComment);
            ps.setString(3, status);
            ps.setLong(4, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
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
        String sql = "DELETE FROM rectification_task WHERE id = ?";
        try (Connection conn = DBUtil.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
