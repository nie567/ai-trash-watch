package com.example.controller;

import com.example.model.GarbageRecord;
import com.example.model.GarbageRecordDetailVO;
import com.example.model.PageResult;
import com.example.service.GarbageRecordService;
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
 * 管理员侧投放记录控制器
 */
@WebServlet("/admin/garbage-record/*")
public class AdminGarbageRecordServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(AdminGarbageRecordServlet.class);


    private GarbageRecordService recordService;

    @Override
    public void init() throws ServletException {
        recordService = AppContext.get().getGarbageRecordService();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo == null) pathInfo = "/";

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
        if (pathInfo == null) pathInfo = "/";

        // 人工复核
        if ("/review".equals(pathInfo)) {
            try {
                Long id = Long.parseLong(req.getParameter("id"));
                String finalCategory = req.getParameter("finalCategory");
                String reviewComment = req.getParameter("reviewComment");
                recordService.reviewRecord(id, finalCategory, reviewComment);
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

        String keyword = req.getParameter("keyword");
        String status = req.getParameter("status");

        PageResult<GarbageRecord> pageResult = recordService.getAllRecords(page, pageSize, keyword, status);
        req.setAttribute("pageResult", pageResult);
        req.setAttribute("keyword", keyword);
        req.setAttribute("status", status);
        req.getRequestDispatcher("/WEB-INF/jsp/admin/garbage-record-list.jsp").forward(req, resp);
    }

    private void showDetail(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Long id = null;
        try { id = Long.parseLong(req.getParameter("id")); } catch (Exception ignored) {}

        if (id == null) {
            resp.sendRedirect(req.getContextPath() + "/admin/garbage-record/list");
            return;
        }

        try {
            GarbageRecordDetailVO detail = recordService.getRecordDetail(id);
            req.setAttribute("detail", detail);
            req.getRequestDispatcher("/WEB-INF/jsp/admin/garbage-record-detail.jsp").forward(req, resp);
        } catch (Exception e) {
            req.setAttribute("error", e.getMessage());
            resp.sendRedirect(req.getContextPath() + "/admin/garbage-record/list");
        }
    }
}
