package com.example.test.dao;

import com.example.dao.StatisticsDAO;
import com.example.model.TrendVO;
import com.example.model.TypeCountVO;
import com.example.test.BaseTest;
import org.junit.Test;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class StatisticsDAOTest extends BaseTest {

    private StatisticsDAO statisticsDAO = new StatisticsDAO();

    @Override
    protected void initTestData() throws SQLException {
        truncateTable("garbage_record");
        executeSQL("INSERT IGNORE INTO user (id, username, password_hash, role, status) VALUES " +
                "(1, 'statuser1', 'hash1', 'user', 1), " +
                "(2, 'statuser2', 'hash2', 'user', 1)");
        executeSQL("INSERT INTO garbage_record (user_id, image_name, image_path, recommended_category, selected_category, is_correct, status, create_time) VALUES " +
                "(1, 'a.jpg', '/a.jpg', '可回收物', '可回收物', 1, 'REVIEWED', NOW()), " +
                "(1, 'b.jpg', '/b.jpg', '厨余垃圾', '厨余垃圾', 1, 'REVIEWED', NOW()), " +
                "(1, 'c.jpg', '/c.jpg', '可回收物', '其他垃圾', 0, 'REVIEWED', NOW()), " +
                "(2, 'd.jpg', '/d.jpg', '有害垃圾', '有害垃圾', 1, 'REVIEWED', NOW()), " +
                "(2, 'e.jpg', '/e.jpg', '其他垃圾', '其他垃圾', 1, 'REVIEWED', NOW())");
    }

    @Test
    public void testCountByGarbageType() {
        List<TypeCountVO> list = statisticsDAO.countByGarbageType();
        assertNotNull(list);
        assertFalse("应有统计数据", list.isEmpty());

        int recyclableCount = 0;
        for (TypeCountVO vo : list) {
            if ("可回收物".equals(vo.getType())) {
                recyclableCount = vo.getCount();
            }
        }
        assertEquals("可回收物应有2条", 2, recyclableCount);
    }

    @Test
    public void testCountCorrectAndWrong() {
        Map<String, Integer> result = statisticsDAO.countCorrectAndWrong();
        assertNotNull(result);
        assertEquals("正确投放应有4条", Integer.valueOf(4), result.get("correct"));
        assertEquals("错误投放应有1条", Integer.valueOf(1), result.get("wrong"));
    }

    @Test
    public void testCountByDate() {
        List<TrendVO> trends = statisticsDAO.countByDate(7);
        assertNotNull(trends);
        assertFalse("近7天应有数据", trends.isEmpty());

        int todayCount = 0;
        for (TrendVO vo : trends) {
            todayCount += vo.getCount();
        }
        assertEquals("今天应有5条投放", 5, todayCount);
    }

    @Test
    public void testCountByDateWithZeroDays() {
        List<TrendVO> trends = statisticsDAO.countByDate(0);
        assertNotNull(trends);
    }

    @Test
    public void testGetViolationUserRank() {
        List<com.example.model.UserRankVO> rank = statisticsDAO.getViolationUserRank();
        assertNotNull(rank);
    }
}
