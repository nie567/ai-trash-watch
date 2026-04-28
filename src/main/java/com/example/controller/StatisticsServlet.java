package com.example.controller;

import com.example.model.TrendVO;
import com.example.model.TypeCountVO;
import com.example.model.UserRankVO;
import com.example.service.StatisticsService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 统计分析控制器
 */
@WebServlet("/admin/statistics")
public class StatisticsServlet extends HttpServlet {

    private StatisticsService statisticsService;

    @Override
    public void init() throws ServletException {
        statisticsService = new StatisticsService();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // 类别统计
        List<TypeCountVO> typeCounts = statisticsService.countByGarbageType();
        req.setAttribute("typeCounts", typeCounts);

        // 正确/错误统计
        Map<String, Integer> correctAndWrong = statisticsService.countCorrectAndWrong();
        req.setAttribute("correctCount", correctAndWrong.getOrDefault("correct", 0));
        req.setAttribute("wrongCount", correctAndWrong.getOrDefault("wrong", 0));

        // 近7天趋势
        List<TrendVO> trends = statisticsService.countByDate(7);
        req.setAttribute("trends", trends);

        // 违规排名
        List<UserRankVO> violationRank = statisticsService.getUserViolationRank();
        req.setAttribute("violationRank", violationRank);

        req.getRequestDispatcher("/WEB-INF/jsp/admin/statistics.jsp").forward(req, resp);
    }
}
