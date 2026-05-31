package com.example.dao;

import com.example.model.ViolationRecord;
import com.example.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 违规记录数据访问层
 */
public class ViolationRecordDAO {
    private static final Logger logger = LoggerFactory.getLogger(ViolationRecordDAO.class);


    /**
     * 插入违规记录，返回自增ID
     */
    public Long insert(ViolationRecord record) {
        return insert(record, null);
    }

    /**
     * 插入违规记录，返回自增ID（支持外部事务连接）
     */
    public Long insert(ViolationRecord record, Connection conn) {
        String sql = "INSERT INTO violation_record (record_id, user_id, violation_type, description, level, status, create_time) VALUES (?, ?, ?, ?, ?, ?, NOW())";
        boolean externalConn = conn != null;
        try {
            if (!externalConn) conn = DBUtil.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setLong(1, record.getRecordId());
                ps.setLong(2, record.getUserId());
                ps.setString(3, record.getViolationType());
                ps.setString(4, record.getDescription());
                ps.setString(5, record.getLevel());
                ps.setString(6, record.getStatus());
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getLong(1);
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("插入违规记录失败", e);
        } finally {
            if (!externalConn) try { conn.close(); } catch (SQLException ignored) {}
        }
        return null;
    }

    /**
     * 根据ID查询
     */
    public ViolationRecord findById(Long id) {
        String sql = "SELECT * FROM violation_record WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return extractViolationRecord(rs);
                }
            }
        } catch (SQLException e) {
            logger.error("根据ID查询违规记录失败, id={}", id, e);
        }
        return null;
    }

    /**
     * 按投放记录ID查询违规
     */
    public ViolationRecord findByRecordId(Long recordId) {
        String sql = "SELECT * FROM violation_record WHERE record_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, recordId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return extractViolationRecord(rs);
                }
            }
        } catch (SQLException e) {
            logger.error("根据投放记录ID查询违规失败, recordId={}", recordId, e);
        }
        return null;
    }

    /**
     * 按用户ID分页查询，按create_time DESC排序
     */
    public List<ViolationRecord> findByUserId(Long userId, int offset, int limit) {
        return findByUserId(userId, offset, limit, null);
    }

    /**
     * 按用户ID分页查询违规记录，支持状态筛选
     */
    public List<ViolationRecord> findByUserId(Long userId, int offset, int limit, String status) {
        List<ViolationRecord> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM violation_record WHERE user_id = ?");
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
                    list.add(extractViolationRecord(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("按用户ID分页查询违规记录失败", e);
        }
        return list;
    }

    /**
     * 管理员分页查询，支持状态筛选，按create_time DESC排序
     */
    public List<ViolationRecord> findAll(int offset, int limit, String status) {
        List<ViolationRecord> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT vr.* FROM violation_record vr " +
            "JOIN user u ON vr.user_id = u.id WHERE u.role != 'admin'");
        List<String> params = new ArrayList<>();

        if (status != null && !status.trim().isEmpty()) {
            sql.append(" AND vr.status = ?");
            params.add(status.trim());
        }
        sql.append(" ORDER BY vr.create_time DESC LIMIT ? OFFSET ?");

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
                    list.add(extractViolationRecord(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("管理员分页查询违规记录失败", e);
        }
        return list;
    }

    /**
     * 按用户ID统计违规记录数
     */
    public int countByUserId(Long userId) {
        return countByUserId(userId, null);
    }

    /**
     * 按用户ID统计违规记录数，支持状态筛选
     */
    public int countByUserId(Long userId, String status) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM violation_record WHERE user_id = ?");
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
            logger.error("按用户ID统计违规记录数失败, userId={}", userId, e);
        }
        return 0;
    }

    /**
     * 管理员统计违规记录数，支持状态筛选
     */
    public int countAll(String status) {
        StringBuilder sql = new StringBuilder(
            "SELECT COUNT(*) FROM violation_record vr " +
            "JOIN user u ON vr.user_id = u.id WHERE u.role != 'admin'");
        List<String> params = new ArrayList<>();

        if (status != null && !status.trim().isEmpty()) {
            sql.append(" AND vr.status = ?");
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
            logger.error("管理员统计违规记录数失败", e);
        }
        return 0;
    }

    /**
     * 更新违规记录状态
     */
    public void updateStatus(Long id, String status) {
        updateStatus(id, status, null);
    }

    /**
     * 更新违规记录状态（支持外部事务连接）
     */
    public void updateStatus(Long id, String status, Connection conn) {
        String sql = "UPDATE violation_record SET status = ? WHERE id = ?";
        boolean externalConn = conn != null;
        try {
            if (!externalConn) conn = DBUtil.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, status);
                ps.setLong(2, id);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            logger.error("更新违规状态失败", e);
        } finally {
            if (!externalConn) try { conn.close(); } catch (SQLException ignored) {}
        }
    }

    /**
     * 统计用户历史总违规次数（所有状态），用于判定级别
     */
    public int countByUserIdAll(Long userId) {
        String sql = "SELECT COUNT(*) FROM violation_record WHERE user_id = ? AND status != 'IGNORED'";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            logger.error("统计用户历史总违规次数失败, userId={}", userId, e);
        }
        return 0;
    }

    /**
     * 从 ResultSet 提取违规记录对象
     */
    private ViolationRecord extractViolationRecord(ResultSet rs) throws SQLException {
        ViolationRecord record = new ViolationRecord();
        record.setId(rs.getLong("id"));
        record.setRecordId(rs.getLong("record_id"));
        record.setUserId(rs.getLong("user_id"));
        record.setViolationType(rs.getString("violation_type"));
        record.setDescription(rs.getString("description"));
        record.setLevel(rs.getString("level"));
        record.setStatus(rs.getString("status"));
        record.setCreateTime(rs.getTimestamp("create_time"));
        return record;
    }

    /**
     * 根据ID删除违规记录
     */
    public boolean deleteById(Long id) {
        return deleteById(id, null);
    }

    /**
     * 根据ID删除违规记录（支持外部事务连接）
     */
    public boolean deleteById(Long id, Connection conn) {
        String sql = "DELETE FROM violation_record WHERE id = ?";
        boolean externalConn = conn != null;
        try {
            if (!externalConn) conn = DBUtil.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, id);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            logger.error("删除违规记录失败", e);
        } finally {
            if (!externalConn) try { conn.close(); } catch (SQLException ignored) {}
        }
        return false;
    }

    /**
     * 按用户ID删除违规记录（支持外部事务连接）
     * 注意：数据库有 ON DELETE CASCADE，此方法作为应用层保障
     */
    public int deleteByUserId(Long userId, Connection conn) {
        String sql = "DELETE FROM violation_record WHERE user_id = ?";
        boolean externalConn = conn != null;
        try {
            if (!externalConn) conn = DBUtil.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, userId);
                return ps.executeUpdate();
            }
        } catch (SQLException e) {
            logger.error("按用户ID删除违规记录失败, userId={}", userId, e);
        } finally {
            if (!externalConn) try { conn.close(); } catch (SQLException ignored) {}
        }
        return 0;
    }
}
