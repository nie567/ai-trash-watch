package com.example.dao;

import com.example.model.OperationLog;
import com.example.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 操作日志数据访问层
 */
public class OperationLogDAO {
    
    /**
     * 记录操作日志
     */
    public boolean insert(OperationLog log) {
        String sql = "INSERT INTO operation_log (user_id, username, action, target, detail, ip) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBUtil.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, log.getUserId());
            ps.setString(2, log.getUsername());
            ps.setString(3, log.getAction());
            ps.setString(4, log.getTarget());
            ps.setString(5, log.getDetail());
            ps.setString(6, log.getIp());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * 分页查询日志
     */
    public List<OperationLog> findAll(int page, int pageSize) {
        List<OperationLog> logs = new ArrayList<>();
        String sql = "SELECT * FROM operation_log ORDER BY create_time DESC LIMIT ? OFFSET ?";
        try (Connection conn = DBUtil.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pageSize);
            ps.setInt(2, (page - 1) * pageSize);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    logs.add(extractLog(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return logs;
    }
    
    /**
     * 统计总数
     */
    public int countAll() {
        String sql = "SELECT COUNT(*) FROM operation_log";
        try (Connection conn = DBUtil.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    
    /**
     * 查询指定用户的操作日志
     */
    public List<OperationLog> findByUserId(Integer userId, int limit) {
        List<OperationLog> logs = new ArrayList<>();
        String sql = "SELECT * FROM operation_log WHERE user_id = ? ORDER BY create_time DESC LIMIT ?";
        try (Connection conn = DBUtil.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    logs.add(extractLog(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return logs;
    }
    
    private OperationLog extractLog(ResultSet rs) throws SQLException {
        OperationLog log = new OperationLog();
        log.setId(rs.getInt("id"));
        log.setUserId(rs.getInt("user_id"));
        log.setUsername(rs.getString("username"));
        log.setAction(rs.getString("action"));
        log.setTarget(rs.getString("target"));
        log.setDetail(rs.getString("detail"));
        log.setIp(rs.getString("ip"));
        log.setCreateTime(rs.getTimestamp("create_time"));
        return log;
    }
}