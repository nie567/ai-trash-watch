package com.example.controller;

import com.example.model.PageResult;
import com.example.model.User;
import com.example.model.ViolationRecord;
import com.example.service.ViolationService;
import com.example.util.AppConstants;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 用户侧违规记录控制器
 */
@WebServlet("/user/violation/*")
public class ViolationServlet extends HttpServlet {

    private ViolationService violationService;

    @Override
    public void init() throws ServletException {
        violationService = new ViolationService();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo == null) pathInfo = "/list";

        if ("/list".equals(pathInfo)) {
            showList(req, resp);
        } else {
            showList(req, resp);
        }
    }

    private void showList(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User loginUser = (User) req.getSession().getAttribute(AppConstants.SESSION_LOGIN_USER);
        if (loginUser == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        int page = 1;
        int pageSize = AppConstants.DEFAULT_PAGE_SIZE;
        try { page = Integer.parseInt(req.getParameter("page")); } catch (Exception ignored) {}
        try { pageSize = Integer.parseInt(req.getParameter("pageSize")); } catch (Exception ignored) {}

        PageResult<ViolationRecord> pageResult = violationService.getUserViolations(loginUser.getId().longValue(), page, pageSize);
        req.setAttribute("pageResult", pageResult);
        req.getRequestDispatcher("/WEB-INF/jsp/user/violation-list.jsp").forward(req, resp);
    }
}
