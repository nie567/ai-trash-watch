package com.example.controller;

import com.example.model.User;
import com.example.service.OperationLogService;
import com.example.service.UserService;
import com.example.util.AppConstants;
import com.example.util.AppContext;
import com.example.util.BusinessException;
import com.example.util.RequestUtil;
import com.example.util.Result;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebServlet(name = "AdminUserServlet", urlPatterns = {"/admin/users", "/admin/users/*"})
public class AdminUserServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(AdminUserServlet.class);

    private UserService userService;
    private OperationLogService logService;
    private static final int DEFAULT_PAGE_SIZE = 10;

    @Override
    public void init() throws ServletException {
        userService = AppContext.get().getUserService();
        logService = AppContext.get().getOperationLogService();
    }

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
        String keyword = req.getParameter("keyword");

        List<User> users;
        int totalCount;

        if (keyword != null && !keyword.trim().isEmpty()) {
            users = userService.search(keyword.trim(), page, pageSize);
            totalCount = userService.countSearch(keyword.trim());
        } else {
            users = userService.findAll(page, pageSize);
            totalCount = userService.countAll();
        }

        int totalPages = (int) Math.ceil((double) totalCount / pageSize);

        req.setAttribute("users", users);
        req.setAttribute("keyword", keyword);
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

        try {
            User user = userService.getUserById(userId);
            req.setAttribute("user", user);
            req.getRequestDispatcher("/WEB-INF/jsp/admin/user-edit.jsp").forward(req, resp);
        } catch (BusinessException e) {
            resp.sendRedirect(req.getContextPath() + "/admin/users");
        }
    }

    private void createUser(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User loginUser = RequestUtil.getLoginUser(req);

        String username = req.getParameter("username");
        String password = req.getParameter("password");
        String email = req.getParameter("email");
        String phone = req.getParameter("phone");
        String role = req.getParameter("role");

        if (username == null || username.trim().isEmpty() ||
            password == null || password.trim().isEmpty()) {
            req.setAttribute("error", "用户名和密码不能为空");
            req.getRequestDispatcher("/WEB-INF/jsp/admin/user-create.jsp").forward(req, resp);
            return;
        }

        User user = new User();
        user.setUsername(username.trim());
        user.setPassword(password);
        user.setEmail(email);
        user.setPhone(phone);
        user.setRole(role != null && role.equals("admin") ? "admin" : "user");
        user.setStatus(1);

        try {
            userService.createUser(user, loginUser, RequestUtil.getClientIp(req));
            resp.sendRedirect(req.getContextPath() + "/admin/users?success=created");
        } catch (BusinessException e) {
            req.setAttribute("error", e.getMessage());
            req.getRequestDispatcher("/WEB-INF/jsp/admin/user-create.jsp").forward(req, resp);
        }
    }

    private void updateUser(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User loginUser = RequestUtil.getLoginUser(req);

        int userId = getIntParameter(req, "id", 0);
        String email = req.getParameter("email");
        String phone = req.getParameter("phone");
        String role = req.getParameter("role");

        if (userId == 0) {
            resp.sendRedirect(req.getContextPath() + "/admin/users");
            return;
        }

        try {
            User user = userService.getUserById(userId);
            user.setEmail(email);
            user.setPhone(phone);
            user.setRole(role != null && role.equals("admin") ? "admin" : "user");
            userService.updateUser(user, loginUser, RequestUtil.getClientIp(req));
            resp.sendRedirect(req.getContextPath() + "/admin/users?success=updated");
        } catch (BusinessException e) {
            req.setAttribute("error", e.getMessage());
            req.getRequestDispatcher("/WEB-INF/jsp/admin/user-edit.jsp").forward(req, resp);
        }
    }

    private void deleteUser(HttpServletRequest req, HttpServletResponse resp, String pathInfo)
            throws ServletException, IOException {
        User loginUser = RequestUtil.getLoginUser(req);
        Integer userId = Integer.parseInt(pathInfo.substring(1));

        try {
            userService.deleteUser(userId, loginUser, RequestUtil.getClientIp(req));
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write(Result.success(200, "删除成功").toJson());
        } catch (BusinessException e) {
            resp.setStatus(e.getCode() == 400 ? HttpServletResponse.SC_BAD_REQUEST : HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(Result.error(e.getCode(), e.getMessage()).toJson());
        }
    }

    private void updateUserStatus(HttpServletRequest req, HttpServletResponse resp, String pathInfo)
            throws ServletException, IOException {
        User loginUser = RequestUtil.getLoginUser(req);

        String[] parts = pathInfo.split("/");
        int userId = Integer.parseInt(parts[1]);
        int newStatus = getIntParameter(req, "status", -1);

        if (newStatus == -1 || (newStatus != 0 && newStatus != 1)) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write(Result.error(400, "无效的状态值").toJson());
            return;
        }

        try {
            userService.updateUserStatus(userId, newStatus, loginUser, RequestUtil.getClientIp(req));
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write(Result.success(200, "状态更新成功").toJson());
        } catch (BusinessException e) {
            resp.setStatus(e.getCode() == 400 ? HttpServletResponse.SC_BAD_REQUEST : HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(Result.error(e.getCode(), e.getMessage()).toJson());
        }
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
}
