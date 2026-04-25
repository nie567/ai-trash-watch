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
import java.util.List;

/**
 * 管理员用户管理控制器
 */
@WebServlet(name = "AdminUserServlet", urlPatterns = {"/admin/users", "/admin/users/*"})
public class AdminUserServlet extends HttpServlet {
    
    private UserDAO userDAO = new UserDAO();
    private OperationLogDAO logDAO = new OperationLogDAO();
    private static final int DEFAULT_PAGE_SIZE = 10;
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        
        if (pathInfo == null || pathInfo.equals("/")) {
            listUsers(req, resp);
        } else if (pathInfo.equals("/create")) {
            showCreateForm(req, resp);
        } else if (pathInfo.equals("/edit")) {
            showEditForm(req, resp);
        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        
        if (pathInfo != null && pathInfo.equals("/create")) {
            createUser(req, resp);
        } else if (pathInfo != null && pathInfo.equals("/edit")) {
            updateUser(req, resp);
        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
    
    /**
     * DELETE /admin/users/{id}
     */
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo != null && pathInfo.matches("/\\d+")) {
            deleteUser(req, resp, pathInfo);
        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
    
    /**
     * PUT /admin/users/{id}/status
     */
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo != null && pathInfo.matches("/\\d+/status")) {
            updateUserStatus(req, resp, pathInfo);
        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
    
    private void listUsers(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        int page = getIntParameter(req, "page", 1);
        int pageSize = getIntParameter(req, "pageSize", DEFAULT_PAGE_SIZE);
        
        List<User> users = userDAO.findAll(page, pageSize);
        int totalCount = userDAO.countAll();
        int totalPages = (int) Math.ceil((double) totalCount / pageSize);
        
        req.setAttribute("users", users);
        req.setAttribute("currentPage", page);
        req.setAttribute("totalPages", totalPages);
        req.setAttribute("totalCount", totalCount);
        
        req.getRequestDispatcher("/WEB-INF/jsp/admin/user-list.jsp").forward(req, resp);
    }
    
    private void showCreateForm(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.getRequestDispatcher("/WEB-INF/jsp/admin/user-create.jsp").forward(req, resp);
    }
    
    private void showEditForm(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        int userId = getIntParameter(req, "id", 0);
        if (userId == 0) {
            resp.sendRedirect(req.getContextPath() + "/admin/users");
            return;
        }
        
        User user = userDAO.findById(userId);
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/admin/users");
            return;
        }
        
        req.setAttribute("user", user);
        req.getRequestDispatcher("/WEB-INF/jsp/admin/user-edit.jsp").forward(req, resp);
    }
    
    private void createUser(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User loginUser = getLoginUser(req);
        
        String username = req.getParameter("username");
        String password = req.getParameter("password");
        String email = req.getParameter("email");
        String phone = req.getParameter("phone");
        String role = req.getParameter("role");
        
        // 验证必填字段
        if (username == null || username.trim().isEmpty() ||
            password == null || password.trim().isEmpty()) {
            req.setAttribute("error", "用户名和密码不能为空");
            req.getRequestDispatcher("/WEB-INF/jsp/admin/user-create.jsp").forward(req, resp);
            return;
        }
        
        // 验证用户名是否已存在
        if (userDAO.findByUsername(username.trim()) != null) {
            req.setAttribute("error", "用户名已存在");
            req.getRequestDispatcher("/WEB-INF/jsp/admin/user-create.jsp").forward(req, resp);
            return;
        }
        
        // 验证密码强度
        if (!BCryptUtil.isPasswordStrongEnough(password)) {
            req.setAttribute("error", "密码至少需要6个字符");
            req.getRequestDispatcher("/WEB-INF/jsp/admin/user-create.jsp").forward(req, resp);
            return;
        }
        
        User user = new User();
        user.setUsername(username.trim());
        user.setPasswordHash(BCryptUtil.hashPassword(password));
        user.setEmail(email);
        user.setPhone(phone);
        user.setRole(role != null && role.equals("admin") ? "admin" : "user");
        user.setStatus(1);
        
        if (userDAO.create(user)) {
            // 记录日志
            logOperation(loginUser.getId(), loginUser.getUsername(), AppConstants.ACTION_CREATE,
                    "USER:" + user.getId(), "创建用户 " + user.getUsername(), req);
            
            resp.sendRedirect(req.getContextPath() + "/admin/users?success=created");
        } else {
            req.setAttribute("error", "创建用户失败，请重试");
            req.getRequestDispatcher("/WEB-INF/jsp/admin/user-create.jsp").forward(req, resp);
        }
    }
    
    private void updateUser(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User loginUser = getLoginUser(req);
        
        int userId = getIntParameter(req, "id", 0);
        String email = req.getParameter("email");
        String phone = req.getParameter("phone");
        String role = req.getParameter("role");
        
        if (userId == 0) {
            resp.sendRedirect(req.getContextPath() + "/admin/users");
            return;
        }
        
        User user = userDAO.findById(userId);
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/admin/users");
            return;
        }
        
        user.setEmail(email);
        user.setPhone(phone);
        user.setRole(role != null && role.equals("admin") ? "admin" : "user");
        
        if (userDAO.updateProfile(user)) {
            // 记录日志
            logOperation(loginUser.getId(), loginUser.getUsername(), AppConstants.ACTION_UPDATE,
                    "USER:" + user.getId(), "更新用户 " + user.getUsername(), req);
            
            resp.sendRedirect(req.getContextPath() + "/admin/users?success=updated");
        } else {
            req.setAttribute("error", "更新用户失败，请重试");
            req.setAttribute("user", user);
            req.getRequestDispatcher("/WEB-INF/jsp/admin/user-edit.jsp").forward(req, resp);
        }
    }
    
    private void deleteUser(HttpServletRequest req, HttpServletResponse resp, String pathInfo)
            throws ServletException, IOException {
        User loginUser = getLoginUser(req);
        
        Integer userId = Integer.parseInt(pathInfo.substring(1));
        
        // 不能删除自己
        if (userId.equals(loginUser.getId())) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"code\":400,\"message\":\"不能删除当前登录账号\"}");
            return;
        }
        
        User user = userDAO.findById(userId);
        if (user == null) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.getWriter().write("{\"code\":404,\"message\":\"用户不存在\"}");
            return;
        }
        
        if (userDAO.delete(userId)) {
            logOperation(loginUser.getId(), loginUser.getUsername(), AppConstants.ACTION_DELETE,
                    "USER:" + userId, "删除用户 " + user.getUsername(), req);
            
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write("{\"code\":200,\"message\":\"删除成功\"}");
        } else {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"code\":500,\"message\":\"删除失败\"}");
        }
    }
    
    private void updateUserStatus(HttpServletRequest req, HttpServletResponse resp, String pathInfo)
            throws ServletException, IOException {
        User loginUser = getLoginUser(req);
        
        String[] parts = pathInfo.split("/");
        int userId = Integer.parseInt(parts[1]);
        int newStatus = getIntParameter(req, "status", -1);
        
        // 不能禁用自己
        if (userId == loginUser.getId()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"code\":400,\"message\":\"不能禁用当前登录账号\"}");
            return;
        }
        
        if (newStatus == -1 || (newStatus != 0 && newStatus != 1)) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"code\":400,\"message\":\"无效的状态值\"}");
            return;
        }
        
        User user = userDAO.findById(userId);
        if (user == null) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.getWriter().write("{\"code\":404,\"message\":\"用户不存在\"}");
            return;
        }
        
        String action = newStatus == 1 ? AppConstants.ACTION_ENABLE : AppConstants.ACTION_DISABLE;
        
        if (userDAO.updateStatus(userId, newStatus)) {
            logOperation(loginUser.getId(), loginUser.getUsername(), action,
                    "USER:" + userId, (newStatus == 1 ? "启用" : "禁用") + "用户 " + user.getUsername(), req);
            
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write("{\"code\":200,\"message\":\"状态更新成功\"}");
        } else {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"code\":500,\"message\":\"状态更新失败\"}");
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
    
    private int getIntParameter(HttpServletRequest req, String name, int defaultValue) {
        String value = req.getParameter(name);
        if (value == null || value.isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
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