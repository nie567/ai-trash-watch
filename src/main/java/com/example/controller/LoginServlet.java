package com.example.controller;

import com.example.model.User;
import com.example.service.OperationLogService;
import com.example.service.UserService;
import com.example.util.AppConstants;
import com.example.util.AppContext;
import com.example.util.BusinessException;
import com.example.util.RequestUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebServlet(name = "LoginServlet", urlPatterns = {"/login"})
public class LoginServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(LoginServlet.class);

    private UserService userService;
    private OperationLogService logService;

    @Override
    public void init() throws ServletException {
        userService = AppContext.get().getUserService();
        logService = AppContext.get().getOperationLogService();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User loginUser = RequestUtil.getLoginUser(req);
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

        if (username == null || username.trim().isEmpty() ||
            password == null || password.trim().isEmpty()) {
            req.setAttribute("error", "用户名和密码不能为空");
            req.getRequestDispatcher("/WEB-INF/jsp/login.jsp").forward(req, resp);
            return;
        }

        String ip = RequestUtil.getClientIp(req);
        User user;
        try {
            user = userService.login(username.trim(), password, ip);
        } catch (BusinessException e) {
            req.setAttribute("error", e.getMessage());
            req.getRequestDispatcher("/WEB-INF/jsp/login.jsp").forward(req, resp);
            return;
        }

        HttpSession existingSession = req.getSession(false);
        if (existingSession != null) {
            User existingUser = (User) existingSession.getAttribute(AppConstants.SESSION_USER);
            if (existingUser != null && !existingUser.getId().equals(user.getId())) {
                existingSession.invalidate();
            }
        }

        HttpSession session = req.getSession(true);
        try {
            req.changeSessionId();
        } catch (IllegalStateException ignored) {
        }

        user.clearSensitiveFields();
        session.setAttribute(AppConstants.SESSION_USER, user);
        session.setAttribute(AppConstants.SESSION_USER_ID, user.getId());
        session.setMaxInactiveInterval(30 * 60);

        if ("on".equals(rememberMe)) {
            Cookie rememberCookie = new Cookie("rememberUser", username);
            rememberCookie.setMaxAge(7 * 24 * 60 * 60);
            rememberCookie.setPath(req.getContextPath().isEmpty() ? "/" : req.getContextPath());
            rememberCookie.setHttpOnly(true);
            rememberCookie.setSecure(req.isSecure());
            rememberCookie.setAttribute("SameSite", "Lax");
            resp.addCookie(rememberCookie);
        }

        if (user.isAdmin()) {
            resp.sendRedirect(req.getContextPath() + "/admin/dashboard");
        } else {
            resp.sendRedirect(req.getContextPath() + "/user/profile");
        }
    }
}
