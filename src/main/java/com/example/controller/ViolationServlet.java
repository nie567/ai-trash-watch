package com.example.controller;

import com.example.model.PageResult;
import com.example.model.User;
import com.example.model.ViolationRecord;
import com.example.service.ViolationService;
import com.example.util.AppConstants;
import com.example.util.AppContext;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 用户侧违规记录控制器
 */
@WebServlet("/user/violation/*")
public class ViolationServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(ViolationServlet.class);

    private ViolationService violationService;

    @Override
    public void init() throws ServletException {
        violationService = AppContext.get().getViolationService();
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
        User loginUser = (User) req.getSession().getAttribute(AppConstants.SESSION_USER);
        if (loginUser == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        // 管理员无违规记录
        if (loginUser.isAdmin()) {
            req.setAttribute("pageResult", new PageResult<>(java.util.Collections.emptyList(), 0, 1, 10));
            req.getRequestDispatcher("/WEB-INF/jsp/user/violation-list.jsp").forward(req, resp);
            return;
        }

        int page = 1;
        int pageSize = AppConstants.DEFAULT_PAGE_SIZE;
        try { page = Integer.parseInt(req.getParameter("page")); } catch (Exception ignored) {}
        try { pageSize = Integer.parseInt(req.getParameter("pageSize")); } catch (Exception ignored) {}
        String status = req.getParameter("status");

        PageResult<ViolationRecord> pageResult = violationService.getUserViolations(loginUser.getId().longValue(), page, pageSize, status);
        req.setAttribute("pageResult", pageResult);
        req.getRequestDispatcher("/WEB-INF/jsp/user/violation-list.jsp").forward(req, resp);
    }
}
