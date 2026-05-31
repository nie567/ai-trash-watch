package com.example.controller;

import com.example.dao.OperationLogDAO;
import com.example.model.OperationLog;
import com.example.model.TrendVO;
import com.example.model.User;
import com.example.service.StatisticsService;
import com.example.service.UserService;
import com.example.util.AppContext;
import com.example.util.RequestUtil;
import com.example.util.Result;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebServlet(name = "AdminDashboardServlet", urlPatterns = {"/admin/dashboard", "/admin/dashboard/api"})
public class AdminDashboardServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(AdminDashboardServlet.class);

    private UserService userService;
    private OperationLogDAO logDAO;
    private StatisticsService statisticsService;

    @Override
    public void init() throws ServletException {
        userService = AppContext.get().getUserService();
        logDAO = AppContext.get().getOperationLogDAO();
        statisticsService = AppContext.get().getStatisticsService();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // JSON API 端点 — 供前端轮询
        if (req.getServletPath().endsWith("/api")) {
            handleApiRequest(req, resp);
            return;
        }

        long t0 = System.currentTimeMillis();

        // 页面渲染
        User loginUser = RequestUtil.getLoginUser(req);
        long t1 = System.currentTimeMillis();

        Map<String, Object> stats = userService.getDashboardStats();
        long t2 = System.currentTimeMillis();

        Map<String, Integer> correctAndWrong = statisticsService.countCorrectAndWrong();
        stats.put("totalRecords", correctAndWrong.getOrDefault("correct", 0) + correctAndWrong.getOrDefault("wrong", 0));
        stats.put("correctCount", correctAndWrong.getOrDefault("correct", 0));
        stats.put("wrongCount", correctAndWrong.getOrDefault("wrong", 0));
        long t3 = System.currentTimeMillis();

        // 计算近7日趋势数据（供JSP初始渲染 + API轮询共用）
        List<TrendVO> trends = statisticsService.countByDate(7);
        Map<String, Integer> dateCountMap = new LinkedHashMap<>();
        for (int i = 6; i >= 0; i--) {
            String dateStr = LocalDate.now().minusDays(i).toString();
            dateCountMap.put(dateStr, 0);
        }
        for (TrendVO t : trends) {
            if (dateCountMap.containsKey(t.getDate())) {
                dateCountMap.put(t.getDate(), t.getCount());
            }
        }
        int[] trendData = new int[7];
        int idx = 0;
        for (int count : dateCountMap.values()) {
            trendData[idx++] = count;
        }

        List<OperationLog> recentLogs = logDAO.findAll(1, 10);
        long t4 = System.currentTimeMillis();

        req.setAttribute("stats", stats);
        req.setAttribute("trendData", trendData);
        req.setAttribute("recentLogs", recentLogs);
        req.setAttribute("loginUser", loginUser);

        logger.info("[Dashboard] getLoginUser={}ms, getDashboardStats={}ms, countCorrectAndWrong={}ms, trend+logs={}ms, total-so-far={}ms",
                t1 - t0, t2 - t1, t3 - t2, t4 - t3, t4 - t0);

        req.getRequestDispatcher("/WEB-INF/jsp/admin/dashboard.jsp").forward(req, resp);
    }

    /**
     * 处理 /admin/dashboard/api 请求，返回 JSON 统计数据
     */
    private void handleApiRequest(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.setHeader("Cache-Control", "no-store");

        try {
            Map<String, Object> stats = userService.getDashboardStats();

            Map<String, Integer> correctAndWrong = statisticsService.countCorrectAndWrong();
            stats.put("totalRecords", correctAndWrong.getOrDefault("correct", 0) + correctAndWrong.getOrDefault("wrong", 0));
            stats.put("correctCount", correctAndWrong.getOrDefault("correct", 0));
            stats.put("wrongCount", correctAndWrong.getOrDefault("wrong", 0));

            List<TrendVO> trends = statisticsService.countByDate(7);
            // 按日期对齐到近7天位置，无数据的天填0
            Map<String, Integer> dateCountMap = new LinkedHashMap<>();
            for (int i = 6; i >= 0; i--) {
                String dateStr = LocalDate.now().minusDays(i).toString(); // yyyy-MM-dd
                dateCountMap.put(dateStr, 0);
            }
            for (TrendVO t : trends) {
                if (dateCountMap.containsKey(t.getDate())) {
                    dateCountMap.put(t.getDate(), t.getCount());
                }
            }
            int[] trendData = new int[7];
            int idx = 0;
            for (int count : dateCountMap.values()) {
                trendData[idx++] = count;
            }
            stats.put("trendData", trendData);

            resp.getWriter().write(Result.success(stats).toJson());
        } catch (Exception e) {
            logger.error("获取管理员仪表盘统计数据失败", e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(Result.error("获取统计数据失败").toJson());
        }
    }
}
