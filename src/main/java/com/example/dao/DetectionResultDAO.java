package com.example.dao;

import com.example.model.DetectionResult;
import com.example.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 检测明细数据访问层
 */
public class DetectionResultDAO {
    private static final Logger logger = LoggerFactory.getLogger(DetectionResultDAO.class);


    /**
     * 批量插入检测明细
     */
    public void batchInsert(List<DetectionResult> list) {
        batchInsert(list, null);
    }

    /**
     * 批量插入检测明细（支持外部事务连接）
     */
    public void batchInsert(List<DetectionResult> list, Connection conn) {
        if (list == null || list.isEmpty()) {
            return;
        }
        String sql = "INSERT INTO detection_result (record_id, class_name, confidence, x_min, y_min, x_max, y_max, mapped_category, create_time) VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW())";
        boolean externalConn = conn != null;
        try {
            if (!externalConn) conn = DBUtil.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (DetectionResult dr : list) {
                    ps.setLong(1, dr.getRecordId());
                    ps.setString(2, dr.getClassName());
                    if (dr.getConfidence() != null) {
                        ps.setDouble(3, dr.getConfidence());
                    } else {
                        ps.setNull(3, Types.DOUBLE);
                    }
                    if (dr.getXMin() != null) {
                        ps.setInt(4, dr.getXMin());
                    } else {
                        ps.setNull(4, Types.INTEGER);
                    }
                    if (dr.getYMin() != null) {
                        ps.setInt(5, dr.getYMin());
                    } else {
                        ps.setNull(5, Types.INTEGER);
                    }
                    if (dr.getXMax() != null) {
                        ps.setInt(6, dr.getXMax());
                    } else {
                        ps.setNull(6, Types.INTEGER);
                    }
                    if (dr.getYMax() != null) {
                        ps.setInt(7, dr.getYMax());
                    } else {
                        ps.setNull(7, Types.INTEGER);
                    }
                    ps.setString(8, dr.getMappedCategory());
                    ps.addBatch();
                }
                ps.executeBatch();
            }
        } catch (SQLException e) {
            logger.error("批量插入检测明细失败", e);
        } finally {
            if (!externalConn) try { conn.close(); } catch (SQLException ignored) {}
        }
    }

    /**
     * 按投放记录ID查询检测明细
     */
    public List<DetectionResult> findByRecordId(Long recordId) {
        List<DetectionResult> list = new ArrayList<>();
        String sql = "SELECT * FROM detection_result WHERE record_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, recordId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(extractDetectionResult(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("根据记录ID查询检测明细失败, recordId={}", recordId, e);
        }
        return list;
    }

    /**
     * 从 ResultSet 提取检测明细对象
     */
    private DetectionResult extractDetectionResult(ResultSet rs) throws SQLException {
        DetectionResult dr = new DetectionResult();
        dr.setId(rs.getLong("id"));
        dr.setRecordId(rs.getLong("record_id"));
        dr.setClassName(rs.getString("class_name"));
        dr.setConfidence(rs.getDouble("confidence"));
        dr.setXMin(rs.getInt("x_min"));
        dr.setYMin(rs.getInt("y_min"));
        dr.setXMax(rs.getInt("x_max"));
        dr.setYMax(rs.getInt("y_max"));
        dr.setMappedCategory(rs.getString("mapped_category"));
        dr.setCreateTime(rs.getTimestamp("create_time"));
        return dr;
    }

    /**
     * 按投放记录ID删除检测明细
     */
    public boolean deleteByRecordId(Long recordId) {
        return deleteByRecordId(recordId, null);
    }

    /**
     * 按投放记录ID删除检测明细（支持外部事务连接）
     */
    public boolean deleteByRecordId(Long recordId, Connection conn) {
        String sql = "DELETE FROM detection_result WHERE record_id = ?";
        boolean externalConn = conn != null;
        try {
            if (!externalConn) conn = DBUtil.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, recordId);
                ps.executeUpdate();
                return true;
            }
        } catch (SQLException e) {
            logger.error("删除检测明细失败", e);
        } finally {
            if (!externalConn) try { conn.close(); } catch (SQLException ignored) {}
        }
        return false;
    }
}
