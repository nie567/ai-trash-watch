package com.example.controller;

import com.example.dao.OperationLogDAO;
import com.example.model.OperationLog;
import com.example.model.User;
import com.example.util.AppConstants;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

/**
 * 登出控制器
 */
@WebServlet(name = "LogoutServlet", urlPatterns = {"/logout"})
public class LogoutServlet extends HttpServlet {
    
    private OperationLogDAO logDAO = new OperationLogDAO();
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        doPost(req, resp);
    }
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session != null) {
            User user = (User) session.getAttribute(AppConstants.SESSION_USER);
            if (user != null) {
                // 记录登出日志
                String ip = getClientIp(req);
                OperationLog log = new OperationLog(user.getId(), user.getUsername(),
                        AppConstants.ACTION_LOGOUT, "USER", "用户登出", ip);
                logDAO.insert(log);
            }
            session.invalidate();
        }
        // 清除记住我 Cookie
        Cookie[] cookies = req.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("rememberUser".equals(cookie.getName())) {
                    cookie.setMaxAge(0);
                    cookie.setPath(req.getContextPath());
                    resp.addCookie(cookie);
                    break;
                }
            }
        }
        resp.sendRedirect(req.getContextPath() + "/login");
    }
    
    private String getClientIp(HttpServletRequest req) {
        String ip = req.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = req.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = req.getRemoteAddr();
        }
        return ip;
    }
}