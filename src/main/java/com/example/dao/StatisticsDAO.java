package com.example.dao;

import com.example.model.TrendVO;
import com.example.model.TypeCountVO;
import com.example.model.UserRankVO;
import com.example.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 统计数据访问层
 */
public class StatisticsDAO {

    /**
     * 按垃圾分类统计投放数量
     */
    public List<TypeCountVO> countByGarbageType() {
        List<TypeCountVO> list = new ArrayList<>();
        String sql = "SELECT recommended_category AS type, COUNT(*) AS count FROM garbage_record GROUP BY recommended_category";
        try (Connection conn = DBUtil.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new TypeCountVO(rs.getString("type"), rs.getInt("count")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 统计正确和错误分类数量
     */
    public Map<String, Integer> countCorrectAndWrong() {
        Map<String, Integer> result = new HashMap<>();
        result.put("correct", 0);
        result.put("wrong", 0);
        String sql = "SELECT is_correct, COUNT(*) AS cnt FROM garbage_record GROUP BY is_correct";
        try (Connection conn = DBUtil.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int isCorrect = rs.getInt("is_correct");
                int cnt = rs.getInt("cnt");
                if (isCorrect == 1) {
                    result.put("correct", cnt);
                } else if (isCorrect == 0) {
                    result.put("wrong", cnt);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 按日期统计近N天的投放趋势
     */
    public List<TrendVO> countByDate(int days) {
        List<TrendVO> list = new ArrayList<>();
        String sql = "SELECT DATE(create_time) AS date, COUNT(*) AS count FROM garbage_record WHERE create_time >= DATE_SUB(CURDATE(), INTERVAL ? DAY) GROUP BY DATE(create_time) ORDER BY date";
        try (Connection conn = DBUtil.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, days);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new TrendVO(rs.getString("date"), rs.getInt("count")));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 获取违规用户排名（前10名）
     */
    public List<UserRankVO> getViolationUserRank() {
        List<UserRankVO> list = new ArrayList<>();
        String sql = "SELECT vr.user_id, u.username, COUNT(*) AS violation_count FROM violation_record vr LEFT JOIN users u ON vr.user_id = u.id GROUP BY vr.user_id, u.username ORDER BY violation_count DESC LIMIT 10";
        try (Connection conn = DBUtil.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new UserRankVO(rs.getLong("user_id"), rs.getString("username"), rs.getInt("violation_count")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}
