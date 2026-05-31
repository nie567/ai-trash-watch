package com.example.controller;

import com.example.model.PageResult;
import com.example.model.RectificationTask;
import com.example.service.RectificationService;
import com.example.util.AppConstants;
import com.example.util.AppContext;
import com.example.util.Result;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 管理员侧整改任务控制器
 */
@WebServlet("/admin/rectification/*")
public class AdminRectificationServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(AdminRectificationServlet.class);


    private RectificationService rectService;

    @Override
    public void init() throws ServletException {
        rectService = AppContext.get().getRectificationService();
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

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");
        PrintWriter out = resp.getWriter();

        String pathInfo = req.getPathInfo();

        // 复核整改
        if ("/review".equals(pathInfo)) {
            try {
                Long id = Long.parseLong(req.getParameter("id"));
                String reviewResult = req.getParameter("reviewResult");
                String reviewComment = req.getParameter("reviewComment");

                rectService.reviewTask(id, reviewResult, reviewComment);
                out.write(Result.success().toJson());
            } catch (Exception e) {
                logger.error("unexpected error", e);
                out.write(Result.error(e.getMessage()).toJson());
            }
        } else {
            out.write(Result.error("未知路径").toJson());
        }
    }

    private void showList(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        int page = 1;
        int pageSize = AppConstants.DEFAULT_PAGE_SIZE;
        try { page = Integer.parseInt(req.getParameter("page")); } catch (Exception ignored) {}
        try { pageSize = Integer.parseInt(req.getParameter("pageSize")); } catch (Exception ignored) {}

        String status = req.getParameter("status");

        PageResult<RectificationTask> pageResult = rectService.getAllTasks(page, pageSize, status);
        req.setAttribute("pageResult", pageResult);
        req.setAttribute("status", status);
        req.getRequestDispatcher("/WEB-INF/jsp/admin/rectification-list.jsp").forward(req, resp);
    }
}
