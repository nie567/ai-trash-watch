package com.example.controller;

import com.example.dao.UserDAO;
import com.example.dao.OperationLogDAO;
import com.example.model.OperationLog;
import com.example.model.User;
import com.example.util.AppConstants;
import com.example.util.BCryptUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

/**
 * 登录控制器
 */
@WebServlet(name = "LoginServlet", urlPatterns = {"/login"})
public class LoginServlet extends HttpServlet {
    
    private UserDAO userDAO = new UserDAO();
    private OperationLogDAO logDAO = new OperationLogDAO();
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // 已登录用户直接跳转
        HttpSession session = req.getSession(false);
        User loginUser = session != null ? (User) session.getAttribute(AppConstants.SESSION_USER) : null;
        if (loginUser != null) {
            if (loginUser.isAdmin()) {
                resp.sendRedirect(req.getContextPath() + "/admin/dashboard");
            } else {
                resp.sendRedirect(req.getContextPath() + "/user/profile");
            }
            return;
        }
        req.getRequestDispatcher("/WEB-INF/jsp/login.jsp").forward(req, resp);
    }
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String username = req.getParameter("username");
        String password = req.getParameter("password");
        String rememberMe = req.getParameter("rememberMe");
        
        // 验证输入
        if (username == null || username.trim().isEmpty() ||
            password == null || password.trim().isEmpty()) {
            req.setAttribute("error", "用户名和密码不能为空");
            req.getRequestDispatcher("/WEB-INF/jsp/login.jsp").forward(req, resp);
            return;
        }
        
        // 登录验证
        User user = userDAO.login(username.trim(), password);
        if (user == null) {
            req.setAttribute("error", "用户名或密码错误");
            req.getRequestDispatcher("/WEB-INF/jsp/login.jsp").forward(req, resp);
            return;
        }
        
        // 检查账号状态
        if (!user.isActive()) {
            req.setAttribute("error", "账号已被禁用，请联系管理员");
            req.getRequestDispatcher("/WEB-INF/jsp/login.jsp").forward(req, resp);
            return;
        }
        
        // 检查当前 session 是否已有其他用户登录
        // 防止同一浏览器多标签页间 session 互相覆盖导致角色混淆
        HttpSession existingSession = req.getSession(false);
        if (existingSession != null) {
            User existingUser = (User) existingSession.getAttribute(AppConstants.SESSION_USER);
            if (existingUser != null && !existingUser.getId().equals(user.getId())) {
                existingSession.invalidate();
            }
        }
        
        // 登录成功：创建 Session
        HttpSession session = req.getSession(true);
        session.setAttribute(AppConstants.SESSION_USER, user);
        session.setAttribute(AppConstants.SESSION_USER_ID, user.getId());
        session.setMaxInactiveInterval(30 * 60); // 30分钟超时
        
        // "记住我"功能：创建持久化 Cookie
        if ("on".equals(rememberMe)) {
            Cookie rememberCookie = new Cookie("rememberUser", username);
            rememberCookie.setMaxAge(7 * 24 * 60 * 60); // 7天
            rememberCookie.setPath(req.getContextPath());
            resp.addCookie(rememberCookie);
        }
        
        // 记录登录日志
        logLogin(user, req);
        
        // 根据角色跳转
        if (user.isAdmin()) {
            resp.sendRedirect(req.getContextPath() + "/admin/dashboard");
        } else {
            resp.sendRedirect(req.getContextPath() + "/user/profile");
        }
    }
    
    private void logLogin(User user, HttpServletRequest req) {
        String ip = getClientIp(req);
        OperationLog log = new OperationLog(user.getId(), user.getUsername(),
                AppConstants.ACTION_LOGIN, "USER", "用户登录", ip);
        logDAO.insert(log);
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