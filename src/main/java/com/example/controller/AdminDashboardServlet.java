package com.example.controller;

import com.example.dao.UserDAO;
import com.example.dao.OperationLogDAO;
import com.example.model.OperationLog;
import com.example.model.User;
import com.example.service.StatisticsService;
import com.example.util.AppConstants;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理员仪表盘控制器
 * 增加垃圾分类统计概览
 */
@WebServlet(name = "AdminDashboardServlet", urlPatterns = {"/admin/dashboard"})
public class AdminDashboardServlet extends HttpServlet {
    
    private UserDAO userDAO = new UserDAO();
    private OperationLogDAO logDAO = new OperationLogDAO();
    private StatisticsService statisticsService = new StatisticsService();
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User loginUser = getLoginUser(req);
        
        // 获取用户统计数据
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", userDAO.countAll());
        stats.put("activeUsers", userDAO.countByStatus(1));
        stats.put("disabledUsers", userDAO.countByStatus(0));
        stats.put("todayNew", userDAO.countTodayNew());
        stats.put("adminCount", userDAO.countByRole("admin"));
        stats.put("userCount", userDAO.countByRole("user"));
        
        // 获取垃圾分类统计概览
        Map<String, Integer> correctAndWrong = statisticsService.countCorrectAndWrong();
        stats.put("totalRecords", correctAndWrong.getOrDefault("correct", 0) + correctAndWrong.getOrDefault("wrong", 0));
        stats.put("correctCount", correctAndWrong.getOrDefault("correct", 0));
        stats.put("wrongCount", correctAndWrong.getOrDefault("wrong", 0));
        
        // 获取最近操作日志
        List<OperationLog> recentLogs = logDAO.findAll(1, 10);
        
        req.setAttribute("stats", stats);
        req.setAttribute("recentLogs", recentLogs);
        req.setAttribute("loginUser", loginUser);
        
        req.getRequestDispatcher("/WEB-INF/jsp/admin/dashboard.jsp").forward(req, resp);
    }
    
    private User getLoginUser(HttpServletRequest req) {
        return (User) req.getSession().getAttribute(AppConstants.SESSION_USER);
    }
}
