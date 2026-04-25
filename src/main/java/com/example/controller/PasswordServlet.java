package com.example.controller;

import com.example.dao.UserDAO;
import com.example.dao.OperationLogDAO;
import com.example.model.OperationLog;
import com.example.model.User;
import com.example.util.AppConstants;
import com.example.util.BCryptUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

/**
 * 修改密码控制器
 */
@WebServlet(name = "PasswordServlet", urlPatterns = {"/user/change-password"})
public class PasswordServlet extends HttpServlet {
    
    private UserDAO userDAO = new UserDAO();
    private OperationLogDAO logDAO = new OperationLogDAO();
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.getRequestDispatcher("/WEB-INF/jsp/user/password.jsp").forward(req, resp);
    }
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User loginUser = getLoginUser(req);
        
        String oldPassword = req.getParameter("oldPassword");
        String newPassword = req.getParameter("newPassword");
        String confirmPassword = req.getParameter("confirmPassword");
        
        // 验证输入
        if (oldPassword == null || newPassword == null || confirmPassword == null ||
            oldPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            req.setAttribute("error", "所有字段都不能为空");
            req.getRequestDispatcher("/WEB-INF/jsp/user/password.jsp").forward(req, resp);
            return;
        }
        
        // 验证旧密码
        User user = userDAO.findById(loginUser.getId());
        boolean validOldPassword = false;
        
        if (user.getPasswordHash() != null) {
            validOldPassword = BCryptUtil.verifyPassword(oldPassword, user.getPasswordHash());
        } else if (oldPassword.equals(user.getPassword())) {
            validOldPassword = true;
        }
        
        if (!validOldPassword) {
            req.setAttribute("error", "旧密码错误");
            req.getRequestDispatcher("/WEB-INF/jsp/user/password.jsp").forward(req, resp);
            return;
        }
        
        // 验证新密码长度
        if (!BCryptUtil.isPasswordStrongEnough(newPassword)) {
            req.setAttribute("error", "新密码至少需要6个字符");
            req.getRequestDispatcher("/WEB-INF/jsp/user/password.jsp").forward(req, resp);
            return;
        }
        
        // 验证两次输入一致
        if (!newPassword.equals(confirmPassword)) {
            req.setAttribute("error", "两次输入的新密码不一致");
            req.getRequestDispatcher("/WEB-INF/jsp/user/password.jsp").forward(req, resp);
            return;
        }
        
        // 修改密码
        String newPasswordHash = BCryptUtil.hashPassword(newPassword);
        if (userDAO.changePassword(user.getId(), newPasswordHash)) {
            // 记录日志
            logOperation(user.getId(), user.getUsername(), AppConstants.ACTION_PASSWORD_CHANGE,
                    "USER", "修改密码", req);
            
            resp.sendRedirect(req.getContextPath() + "/user/password?success=changed");
        } else {
            req.setAttribute("error", "密码修改失败，请重试");
            req.getRequestDispatcher("/WEB-INF/jsp/user/password.jsp").forward(req, resp);
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