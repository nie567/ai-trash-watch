package com.example.test.service;

import com.example.dao.StatisticsDAO;
import com.example.model.TrendVO;
import com.example.model.TypeCountVO;
import com.example.service.StatisticsService;
import com.example.test.BaseTest;
import org.junit.Test;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class StatisticsServiceTest extends BaseTest {

    private StatisticsService statisticsService = new StatisticsService();

    @Override
    protected void initTestData() throws SQLException {
        truncateTable("garbage_record");
        executeSQL("INSERT IGNORE INTO user (id, username, password_hash, role, status) VALUES " +
                "(9001, 'ssuser1', 'hash1', 'user', 1)");
        executeSQL("INSERT INTO garbage_record (user_id, image_name, image_path, recommended_category, selected_category, is_correct, status, create_time) VALUES " +
                "(9001, 'a.jpg', '/a.jpg', '可回收物', '可回收物', 1, 'REVIEWED', NOW()), " +
                "(9001, 'b.jpg', '/b.jpg', '厨余垃圾', '其他垃圾', 0, 'REVIEWED', NOW()), " +
                "(9001, 'c.jpg', '/c.jpg', '有害垃圾', '有害垃圾', 1, 'REVIEWED', NOW())");
    }

    @Test
    public void testCountByGarbageType() {
        List<TypeCountVO> list = statisticsService.countByGarbageType();
        assertNotNull(list);
        assertFalse(list.isEmpty());
    }

    @Test
    public void testCountCorrectAndWrong() {
        Map<String, Integer> result = statisticsService.countCorrectAndWrong();
        assertNotNull(result);
        assertEquals(Integer.valueOf(2), result.get("correct"));
        assertEquals(Integer.valueOf(1), result.get("wrong"));
    }

    @Test
    public void testCountByDate() {
        List<TrendVO> trends = statisticsService.countByDate(7);
        assertNotNull(trends);
    }

    @Test
    public void testCountByDateZeroOrNegative() {
        List<TrendVO> trends = statisticsService.countByDate(0);
        assertNotNull("负数或0应默认为7天", trends);

        List<TrendVO> trendsNeg = statisticsService.countByDate(-1);
        assertNotNull(trendsNeg);
    }

    @Test
    public void testGetUserViolationRank() {
        List<com.example.model.UserRankVO> rank = statisticsService.getUserViolationRank();
        assertNotNull(rank);
    }
}
