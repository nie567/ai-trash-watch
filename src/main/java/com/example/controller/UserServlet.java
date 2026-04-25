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
import java.util.List;

/**
 * 用户个人中心控制器
 */
@WebServlet(name = "UserServlet", urlPatterns = {"/user/info", "/user/info/*"})
public class UserServlet extends HttpServlet {
    
    private UserDAO userDAO = new UserDAO();
    private OperationLogDAO logDAO = new OperationLogDAO();
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        
        if (pathInfo == null || pathInfo.equals("/") || pathInfo.equals("")) {
            showProfile(req, resp);
        } else if (pathInfo.equals("/edit")) {
            showEditProfile(req, resp);
        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        
        if (pathInfo != null && pathInfo.equals("/edit")) {
            updateProfile(req, resp);
        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
    
    /**
     * 显示个人资料页面
     */
    private void showProfile(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User loginUser = getLoginUser(req);
        User user = userDAO.findById(loginUser.getId());
        req.setAttribute("user", user);
        req.getRequestDispatcher("/WEB-INF/jsp/user/profile.jsp").forward(req, resp);
    }
    
    /**
     * 显示编辑资料页面
     */
    private void showEditProfile(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User loginUser = getLoginUser(req);
        User user = userDAO.findById(loginUser.getId());
        req.setAttribute("user", user);
        req.getRequestDispatcher("/WEB-INF/jsp/user/profile-edit.jsp").forward(req, resp);
    }
    
    /**
     * 更新个人资料
     */
    private void updateProfile(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User loginUser = getLoginUser(req);
        User user = userDAO.findById(loginUser.getId());
        
        String email = req.getParameter("email");
        String phone = req.getParameter("phone");
        
        // 验证邮箱格式
        if (email != null && !email.isEmpty() && !email.matches("^[\\w.-]+@[\\w.-]+\\.\\w+$")) {
            req.setAttribute("error", "邮箱格式不正确");
            req.setAttribute("user", user);
            req.getRequestDispatcher("/WEB-INF/jsp/user/profile-edit.jsp").forward(req, resp);
            return;
        }
        
        // 验证手机号格式
        if (phone != null && !phone.isEmpty() && !phone.matches("^1[3-9]\\d{9}$")) {
            req.setAttribute("error", "手机号格式不正确");
            req.setAttribute("user", user);
            req.getRequestDispatcher("/WEB-INF/jsp/user/profile-edit.jsp").forward(req, resp);
            return;
        }
        
        user.setEmail(email);
        user.setPhone(phone);
        
        if (userDAO.updateProfile(user)) {
            // 更新 Session 中的用户信息
            HttpSession session = req.getSession();
            session.setAttribute(AppConstants.SESSION_USER, user);
            
            // 记录日志
            logOperation(user.getId(), user.getUsername(), AppConstants.ACTION_UPDATE,
                    "PROFILE", "更新个人资料", req);
            
            resp.sendRedirect(req.getContextPath() + "/user/profile?success=updated");
        } else {
            req.setAttribute("error", "更新失败，请重试");
            req.setAttribute("user", user);
            req.getRequestDispatcher("/WEB-INF/jsp/user/profile-edit.jsp").forward(req, resp);
        }
    }
    
    private User getLoginUser(HttpServletRequest req) {
        return (User) req.getSession().getAttribute(AppConstants.SESSION_USER);
    }
    
    private void logOperation(Integer userId, String username, String action,
                              String target, String detail, HttpServletRequest req) {
        String ip = getClientIp(req);
        OperationLog log = new OperationLog(userId, username, action, target, detail, ip);
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