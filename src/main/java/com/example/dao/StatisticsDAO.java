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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 统计数据访问层
 */
public class StatisticsDAO {
    private static final Logger logger = LoggerFactory.getLogger(StatisticsDAO.class);


    /**
     * 按垃圾分类统计投放数量
     */
    public List<TypeCountVO> countByGarbageType() {
        List<TypeCountVO> list = new ArrayList<>();
        String sql = "SELECT recommended_category AS type, COUNT(*) AS count FROM garbage_record GROUP BY recommended_category";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new TypeCountVO(rs.getString("type"), rs.getInt("count")));
            }
        } catch (SQLException e) {
            logger.error("按垃圾分类统计投放数量失败", e);
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
        try (Connection conn = DBUtil.getConnection();
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
            logger.error("查询投放趋势数据失败", e);
        }
        return result;
    }

    /**
     * 按日期统计近N天的投放趋势（包含今天，无数据的天返回0）
     * 返回格式：date 为 "yyyy-MM-dd" 字符串
     */
    public List<TrendVO> countByDate(int days) {
        List<TrendVO> list = new ArrayList<>();
        // INTERVAL (days-1) DAY 确保"近N天"包含今天，共N天
        String sql = "SELECT DATE(create_time) AS date, COUNT(*) AS count " +
            "FROM garbage_record " +
            "WHERE create_time >= DATE_SUB(CURDATE(), INTERVAL ? DAY) " +
            "GROUP BY DATE(create_time) ORDER BY date";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, days - 1);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new TrendVO(rs.getString("date"), rs.getInt("count")));
                }
            }
        } catch (SQLException e) {
            logger.error("查询投放趋势数据失败", e);
        }
        return list;
    }

    /**
     * 按违规类型统计数量
     */
    public List<TypeCountVO> countByViolationType() {
        List<TypeCountVO> list = new ArrayList<>();
        String sql = "SELECT violation_type AS type, COUNT(*) AS count FROM violation_record WHERE violation_type IS NOT NULL AND violation_type != '' GROUP BY violation_type ORDER BY count DESC";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new TypeCountVO(rs.getString("type"), rs.getInt("count")));
            }
        } catch (SQLException e) {
            logger.error("按违规类型统计数量失败", e);
        }
        return list;
    }

    /**
     * 按违规等级统计数量
     */
    public List<TypeCountVO> countByViolationLevel() {
        List<TypeCountVO> list = new ArrayList<>();
        String sql = "SELECT level AS type, COUNT(*) AS count FROM violation_record WHERE level IS NOT NULL AND level != '' GROUP BY level ORDER BY count DESC";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new TypeCountVO(rs.getString("type"), rs.getInt("count")));
            }
        } catch (SQLException e) {
            logger.error("按违规等级统计数量失败", e);
        }
        return list;
    }

    /**
     * 获取违规用户排名（前10名）
     */
    public List<UserRankVO> getViolationUserRank() {
        List<UserRankVO> list = new ArrayList<>();
        String sql = "SELECT vr.user_id, u.username, COUNT(*) AS violation_count " +
            "FROM violation_record vr " +
            "JOIN user u ON vr.user_id = u.id " +
            "WHERE u.role != 'admin' AND vr.status != 'IGNORED' " +
            "GROUP BY vr.user_id, u.username ORDER BY violation_count DESC LIMIT 10";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new UserRankVO(rs.getLong("user_id"), rs.getString("username"), rs.getInt("violation_count")));
            }
        } catch (SQLException e) {
            logger.error("查询用户违规排名失败", e);
        }
        return list;
    }
}
