package com.example.service;

import com.example.dao.StatisticsDAO;
import com.example.model.TrendVO;
import com.example.model.TypeCountVO;
import com.example.model.UserRankVO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * 统计分析业务服务
 */
public class StatisticsService {

    private static final Logger logger = LoggerFactory.getLogger(StatisticsService.class);

    private final StatisticsDAO statisticsDAO;

    public StatisticsService() {
        this.statisticsDAO = new StatisticsDAO();
    }

    public StatisticsService(StatisticsDAO statisticsDAO) {
        this.statisticsDAO = statisticsDAO;
    }

    /**
     * 按垃圾类别统计投放数量
     */
    public List<TypeCountVO> countByGarbageType() {
        return statisticsDAO.countByGarbageType();
    }

    /**
     * 统计正确/错误投放数量
     */
    public Map<String, Integer> countCorrectAndWrong() {
        return statisticsDAO.countCorrectAndWrong();
    }

    /**
     * 按日期统计近N天投放趋势
     */
    public List<TrendVO> countByDate(int days) {
        if (days <= 0) {
            days = 7;
        }
        return statisticsDAO.countByDate(days);
    }

    /**
     * 用户违规次数排名
     */
    public List<UserRankVO> getUserViolationRank() {
        return statisticsDAO.getViolationUserRank();
    }

    /**
     * 按违规类型统计数量
     */
    public List<TypeCountVO> countByViolationType() {
        return statisticsDAO.countByViolationType();
    }

    /**
     * 按违规等级统计数量
     */
    public List<TypeCountVO> countByViolationLevel() {
        return statisticsDAO.countByViolationLevel();
    }
}
