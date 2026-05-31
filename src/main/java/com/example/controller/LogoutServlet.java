package com.example.controller;

import com.example.model.User;
import com.example.service.OperationLogService;
import com.example.util.AppConstants;
import com.example.util.AppContext;
import com.example.util.RequestUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebServlet(name = "LogoutServlet", urlPatterns = {"/logout"})
public class LogoutServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(LogoutServlet.class);

    private OperationLogService logService;

    @Override
    public void init() throws ServletException {
        logService = AppContext.get().getOperationLogService();
    }

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
                logService.log(user.getId(), user.getUsername(),
                        AppConstants.ACTION_LOGOUT, "USER", "用户登出", RequestUtil.getClientIp(req));
            }
            session.invalidate();
        }
        Cookie[] cookies = req.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("rememberUser".equals(cookie.getName())) {
                    cookie.setMaxAge(0);
                    cookie.setPath(req.getContextPath().isEmpty() ? "/" : req.getContextPath());
                    cookie.setHttpOnly(true);
                    cookie.setSecure(req.isSecure());
                    cookie.setAttribute("SameSite", "Lax");
                    resp.addCookie(cookie);
                    break;
                }
            }
        }
        resp.sendRedirect(req.getContextPath() + "/login");
    }
}
