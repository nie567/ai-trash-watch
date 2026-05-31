package com.example.controller;

import com.example.model.User;
import com.example.service.UserService;
import com.example.util.AppContext;
import com.example.util.BCryptUtil;
import com.example.util.BusinessException;
import com.example.util.RequestUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebServlet(name = "RegisterServlet", urlPatterns = {"/register"})
public class RegisterServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(RegisterServlet.class);

    private UserService userService;

    @Override
    public void init() throws ServletException {
        userService = AppContext.get().getUserService();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // 已登录用户直接跳转
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
        req.setCharacterEncoding("UTF-8");

        String username = req.getParameter("username");
        String password = req.getParameter("password");
        String confirmPassword = req.getParameter("confirmPassword");
        String email = req.getParameter("email");
        String phone = req.getParameter("phone");

        // 基本校验
        if (username == null || username.trim().isEmpty()) {
            forwardWithError(req, resp, "用户名不能为空", username, email, phone);
            return;
        }
        username = username.trim();
        if (username.length() < 3 || username.length() > 20) {
            forwardWithError(req, resp, "用户名长度需在3-20个字符之间", username, email, phone);
            return;
        }
        if (!username.matches("^[a-zA-Z0-9_]+$")) {
            forwardWithError(req, resp, "用户名只能包含字母、数字和下划线", username, email, phone);
            return;
        }
        if (password == null || password.isEmpty()) {
            forwardWithError(req, resp, "密码不能为空", username, email, phone);
            return;
        }
        // 密码强度校验
        String strengthError = BCryptUtil.checkStrength(password);
        if (strengthError != null) {
            forwardWithError(req, resp, strengthError, username, email, phone);
            return;
        }
        if (!password.equals(confirmPassword)) {
            forwardWithError(req, resp, "两次输入的密码不一致", username, email, phone);
            return;
        }

        try {
            User user = userService.register(username, password, email, phone);
            logger.info("用户注册成功: {}", username);
            // 注册成功，跳转到登录页并提示
            req.setAttribute("success", "注册成功，请登录");
            req.getRequestDispatcher("/WEB-INF/jsp/login.jsp").forward(req, resp);
        } catch (BusinessException e) {
            forwardWithError(req, resp, e.getMessage(), username, email, phone);
        }
    }

    private void forwardWithError(HttpServletRequest req, HttpServletResponse resp,
                                  String error, String username, String email, String phone)
            throws ServletException, IOException {
        req.setAttribute("regError", error);
        req.setAttribute("regUsername", username);
        req.setAttribute("regEmail", email);
        req.setAttribute("regPhone", phone);
        req.setAttribute("showRegister", true);
        req.getRequestDispatcher("/WEB-INF/jsp/login.jsp").forward(req, resp);
    }
}
