package com.example.controller;

import com.example.dao.UserDAO;
import com.example.dao.OperationLogDAO;
import com.example.model.OperationLog;
import com.example.model.User;
import com.example.util.AppConstants;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理员仪表盘控制器
 */
@WebServlet(name = "AdminDashboardServlet", urlPatterns = {"/admin/dashboard"})
public class AdminDashboardServlet extends HttpServlet {
    
    private UserDAO userDAO = new UserDAO();
    private OperationLogDAO logDAO = new OperationLogDAO();
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User loginUser = getLoginUser(req);
        
        // 获取统计数据
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", userDAO.countAll());
        stats.put("activeUsers", userDAO.countByStatus(1));
        stats.put("disabledUsers", userDAO.countByStatus(0));
        stats.put("todayNew", userDAO.countTodayNew());
        stats.put("adminCount", userDAO.countByRole("admin"));
        stats.put("userCount", userDAO.countByRole("user"));
        
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