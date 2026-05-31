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

@WebServlet(name = "PasswordServlet", urlPatterns = {"/user/change-password"})
public class PasswordServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(PasswordServlet.class);

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
        req.getRequestDispatcher("/WEB-INF/jsp/user/password.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User loginUser = RequestUtil.getLoginUser(req);

        String oldPassword = req.getParameter("oldPassword");
        String newPassword = req.getParameter("newPassword");
        String confirmPassword = req.getParameter("confirmPassword");

        if (oldPassword == null || newPassword == null || confirmPassword == null ||
            oldPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            req.setAttribute("error", "所有字段都不能为空");
            req.getRequestDispatcher("/WEB-INF/jsp/user/password.jsp").forward(req, resp);
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            req.setAttribute("error", "两次输入的新密码不一致");
            req.getRequestDispatcher("/WEB-INF/jsp/user/password.jsp").forward(req, resp);
            return;
        }

        try {
            userService.changePassword(loginUser.getId(), oldPassword, newPassword);
            logService.log(loginUser.getId(), loginUser.getUsername(),
                    AppConstants.ACTION_PASSWORD_CHANGE, "USER", "修改密码", RequestUtil.getClientIp(req));
            resp.sendRedirect(req.getContextPath() + "/user/password?success=changed");
        } catch (BusinessException e) {
            req.setAttribute("error", e.getMessage());
            req.getRequestDispatcher("/WEB-INF/jsp/user/password.jsp").forward(req, resp);
        }
    }
}
