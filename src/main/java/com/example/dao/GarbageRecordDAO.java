package com.example.dao;

import com.example.model.GarbageRecord;
import com.example.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 投放记录数据访问层
 */
public class GarbageRecordDAO {

    /**
     * 插入记录，返回自增ID
     */
    public Long insert(GarbageRecord record) {
        String sql = "INSERT INTO garbage_record (user_id, image_name, image_path, result_image_path, detected_summary, recommended_category, selected_category, final_category, is_mixed, is_correct, status, review_comment, remark, create_time) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW())";
        try (Connection conn = DBUtil.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, record.getUserId());
            ps.setString(2, record.getImageName());
            ps.setString(3, record.getImagePath());
            ps.setString(4, record.getResultImagePath());
            ps.setString(5, record.getDetectedSummary());
            ps.setString(6, record.getRecommendedCategory());
            ps.setString(7, record.getSelectedCategory());
            ps.setString(8, record.getFinalCategory());
            if (record.getIsMixed() != null) {
                ps.setInt(9, record.getIsMixed());
            } else {
                ps.setNull(9, Types.INTEGER);
            }
            if (record.getIsCorrect() != null) {
                ps.setInt(10, record.getIsCorrect());
            } else {
                ps.setNull(10, Types.INTEGER);
            }
            ps.setString(11, record.getStatus());
            ps.setString(12, record.getReviewComment());
            ps.setString(13, record.getRemark());
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
    public GarbageRecord findById(Long id) {
        String sql = "SELECT * FROM garbage_record WHERE id = ?";
        try (Connection conn = DBUtil.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return extractRecord(rs);
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
    public List<GarbageRecord> findByUserId(Long userId, int offset, int limit) {
        List<GarbageRecord> list = new ArrayList<>();
        String sql = "SELECT * FROM garbage_record WHERE user_id = ? ORDER BY create_time DESC LIMIT ? OFFSET ?";
        try (Connection conn = DBUtil.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setInt(2, limit);
            ps.setInt(3, offset);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(extractRecord(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 管理员分页查询，支持关键词（搜索image_name或detected_summary）和状态筛选
     */
    public List<GarbageRecord> findAll(int offset, int limit, String keyword, String status) {
        List<GarbageRecord> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM garbage_record WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (image_name LIKE ? OR detected_summary LIKE ?)");
            params.add("%" + keyword.trim() + "%");
            params.add("%" + keyword.trim() + "%");
        }
        if (status != null && !status.trim().isEmpty()) {
            sql.append(" AND status = ?");
            params.add(status.trim());
        }
        sql.append(" ORDER BY create_time DESC LIMIT ? OFFSET ?");
        params.add(limit);
        params.add(offset);

        try (Connection conn = DBUtil.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                Object param = params.get(i);
                if (param instanceof String) {
                    ps.setString(i + 1, (String) param);
                } else if (param instanceof Integer) {
                    ps.setInt(i + 1, (Integer) param);
                }
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(extractRecord(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 按用户ID统计记录数
     */
    public int countByUserId(Long userId) {
        String sql = "SELECT COUNT(*) FROM garbage_record WHERE user_id = ?";
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
     * 管理员统计记录数，支持关键词和状态筛选
     */
    public int countAll(String keyword, String status) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM garbage_record WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (image_name LIKE ? OR detected_summary LIKE ?)");
            params.add("%" + keyword.trim() + "%");
            params.add("%" + keyword.trim() + "%");
        }
        if (status != null && !status.trim().isEmpty()) {
            sql.append(" AND status = ?");
            params.add(status.trim());
        }

        try (Connection conn = DBUtil.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setString(i + 1, (String) params.get(i));
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
     * 更新审核结果
     */
    public void updateReviewResult(Long id, String finalCategory, Integer isCorrect, String status, String reviewComment) {
        String sql = "UPDATE garbage_record SET final_category = ?, is_correct = ?, status = ?, review_comment = ? WHERE id = ?";
        try (Connection conn = DBUtil.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, finalCategory);
            if (isCorrect != null) {
                ps.setInt(2, isCorrect);
            } else {
                ps.setNull(2, Types.INTEGER);
            }
            ps.setString(3, status);
            ps.setString(4, reviewComment);
            ps.setLong(5, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 从 ResultSet 提取记录对象
     */
    private GarbageRecord extractRecord(ResultSet rs) throws SQLException {
        GarbageRecord record = new GarbageRecord();
        record.setId(rs.getLong("id"));
        record.setUserId(rs.getLong("user_id"));
        record.setImageName(rs.getString("image_name"));
        record.setImagePath(rs.getString("image_path"));
        record.setResultImagePath(rs.getString("result_image_path"));
        record.setDetectedSummary(rs.getString("detected_summary"));
        record.setRecommendedCategory(rs.getString("recommended_category"));
        record.setSelectedCategory(rs.getString("selected_category"));
        record.setFinalCategory(rs.getString("final_category"));
        record.setIsMixed(rs.getInt("is_mixed"));
        int isCorrect = rs.getInt("is_correct");
        record.setIsCorrect(rs.wasNull() ? null : isCorrect);
        record.setStatus(rs.getString("status"));
        record.setReviewComment(rs.getString("review_comment"));
        record.setRemark(rs.getString("remark"));
        record.setCreateTime(rs.getTimestamp("create_time"));
        return record;
    }

    /**
     * 根据ID删除记录
     */
    public boolean deleteById(Long id) {
        String sql = "DELETE FROM garbage_record WHERE id = ?";
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
