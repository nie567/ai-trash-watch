package com.example.dao;

import com.example.model.GarbageRule;
import com.example.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 分类规则数据访问层
 */
public class GarbageRuleDAO {

    /**
     * 查询所有规则（包括禁用的），按id排序
     */
    public List<GarbageRule> findAll() {
        List<GarbageRule> list = new ArrayList<>();
        String sql = "SELECT * FROM garbage_rule ORDER BY id";
        try (Connection conn = DBUtil.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(extractRule(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 查询status=1的启用规则
     */
    public List<GarbageRule> findAllEnabled() {
        List<GarbageRule> list = new ArrayList<>();
        String sql = "SELECT * FROM garbage_rule WHERE status = 1 ORDER BY id";
        try (Connection conn = DBUtil.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(extractRule(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 按className查询启用的规则
     */
    public GarbageRule findByClassName(String className) {
        String sql = "SELECT * FROM garbage_rule WHERE class_name = ? AND status = 1";
        try (Connection conn = DBUtil.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, className);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return extractRule(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 新增规则
     */
    public boolean insert(GarbageRule rule) {
        String sql = "INSERT INTO garbage_rule (class_name, mapped_category, description, status) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBUtil.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, rule.getClassName());
            ps.setString(2, rule.getMappedCategory());
            ps.setString(3, rule.getDescription());
            ps.setInt(4, rule.getStatus() != null ? rule.getStatus() : 1);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 更新规则
     */
    public boolean update(GarbageRule rule) {
        String sql = "UPDATE garbage_rule SET class_name = ?, mapped_category = ?, description = ?, status = ? WHERE id = ?";
        try (Connection conn = DBUtil.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, rule.getClassName());
            ps.setString(2, rule.getMappedCategory());
            ps.setString(3, rule.getDescription());
            ps.setInt(4, rule.getStatus() != null ? rule.getStatus() : 1);
            ps.setLong(5, rule.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 从 ResultSet 提取规则对象
     */
    private GarbageRule extractRule(ResultSet rs) throws SQLException {
        GarbageRule rule = new GarbageRule();
        rule.setId(rs.getLong("id"));
        rule.setClassName(rs.getString("class_name"));
        rule.setMappedCategory(rs.getString("mapped_category"));
        rule.setDescription(rs.getString("description"));
        rule.setStatus(rs.getInt("status"));
        return rule;
    }
}
