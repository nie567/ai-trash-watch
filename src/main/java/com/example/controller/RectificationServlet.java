package com.example.controller;

import com.example.model.PageResult;
import com.example.model.RectificationTask;
import com.example.model.User;
import com.example.service.RectificationService;
import com.example.util.AppConstants;
import com.example.util.Result;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * 用户侧整改任务控制器
 */
@WebServlet("/user/rectification/*")
public class RectificationServlet extends HttpServlet {

    private RectificationService rectService;

    @Override
    public void init() throws ServletException {
        rectService = new RectificationService();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo == null) pathInfo = "/list";

        if ("/list".equals(pathInfo)) {
            showList(req, resp);
        } else if ("/detail".equals(pathInfo)) {
            showDetail(req, resp);
        } else {
            showList(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");
        PrintWriter out = resp.getWriter();

        String pathInfo = req.getPathInfo();

        // 提交整改
        if ("/submit".equals(pathInfo)) {
            try {
                Long id = Long.parseLong(req.getParameter("id"));
                String submitDesc = req.getParameter("submitDesc");
                String submitImagePath = req.getParameter("submitImagePath");

                rectService.submitRectification(id, submitDesc, submitImagePath);
                out.write(Result.success().toJson());
            } catch (Exception e) {
                e.printStackTrace();
                out.write(Result.error(e.getMessage()).toJson());
            }
        } else {
            out.write(Result.error("未知路径").toJson());
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

        PageResult<RectificationTask> pageResult = rectService.getUserTasks(loginUser.getId().longValue(), page, pageSize);
        req.setAttribute("pageResult", pageResult);
        req.getRequestDispatcher("/WEB-INF/jsp/user/rectification-list.jsp").forward(req, resp);
    }

    private void showDetail(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Long id = null;
        try { id = Long.parseLong(req.getParameter("id")); } catch (Exception ignored) {}

        if (id == null) {
            resp.sendRedirect(req.getContextPath() + "/user/rectification/list");
            return;
        }

        RectificationTask task = rectService.getById(id);
        req.setAttribute("task", task);
        req.getRequestDispatcher("/WEB-INF/jsp/user/rectification-detail.jsp").forward(req, resp);
    }
}
