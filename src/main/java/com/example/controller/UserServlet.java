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

@WebServlet(name = "UserServlet", urlPatterns = {"/user/info", "/user/info/*"})
public class UserServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(UserServlet.class);

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

    private void showProfile(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User loginUser = RequestUtil.getLoginUser(req);
        User user = userService.getUserById(loginUser.getId());
        req.setAttribute("user", user);
        req.getRequestDispatcher("/WEB-INF/jsp/user/profile.jsp").forward(req, resp);
    }

    private void showEditProfile(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User loginUser = RequestUtil.getLoginUser(req);
        User user = userService.getUserById(loginUser.getId());
        req.setAttribute("user", user);
        req.getRequestDispatcher("/WEB-INF/jsp/user/profile-edit.jsp").forward(req, resp);
    }

    private void updateProfile(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User loginUser = RequestUtil.getLoginUser(req);

        String email = req.getParameter("email");
        String phone = req.getParameter("phone");

        if (email != null && !email.isEmpty() && !email.matches("^[\\w.-]+@[\\w.-]+\\.\\w+$")) {
            req.setAttribute("error", "邮箱格式不正确");
            req.setAttribute("user", userService.getUserById(loginUser.getId()));
            req.getRequestDispatcher("/WEB-INF/jsp/user/profile-edit.jsp").forward(req, resp);
            return;
        }

        if (phone != null && !phone.isEmpty() && !phone.matches("^1[3-9]\\d{9}$")) {
            req.setAttribute("error", "手机号格式不正确");
            req.setAttribute("user", userService.getUserById(loginUser.getId()));
            req.getRequestDispatcher("/WEB-INF/jsp/user/profile-edit.jsp").forward(req, resp);
            return;
        }

        try {
            userService.updateProfile(loginUser.getId(), email, phone);
            User updatedUser = userService.getUserById(loginUser.getId());
            updatedUser.clearSensitiveFields();
            HttpSession session = req.getSession();
            session.setAttribute(AppConstants.SESSION_USER, updatedUser);

            logService.log(loginUser.getId(), loginUser.getUsername(),
                    AppConstants.ACTION_UPDATE, "PROFILE", "更新个人资料", RequestUtil.getClientIp(req));

            resp.sendRedirect(req.getContextPath() + "/user/profile?success=updated");
        } catch (BusinessException e) {
            req.setAttribute("error", e.getMessage());
            req.setAttribute("user", userService.getUserById(loginUser.getId()));
            req.getRequestDispatcher("/WEB-INF/jsp/user/profile-edit.jsp").forward(req, resp);
        }
    }
}
