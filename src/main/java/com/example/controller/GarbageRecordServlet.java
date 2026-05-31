package com.example.controller;

import com.example.model.GarbageRecord;
import com.example.model.GarbageRecordDetailVO;
import com.example.model.GarbageRecordSubmitDTO;
import com.example.model.PageResult;
import com.example.model.User;
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

/**
 * 用户侧投放记录控制器
 */
@WebServlet("/user/garbage-record/*")
public class GarbageRecordServlet extends HttpServlet {

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
            showHistory(req, resp);
        } else if ("/detail".equals(pathInfo)) {
            showDetail(req, resp);
        } else {
            // 默认跳转到上传页
            req.getRequestDispatcher("/inference").forward(req, resp);
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

        User loginUser = (User) req.getSession().getAttribute(AppConstants.SESSION_USER);
        if (loginUser == null) {
            out.write(Result.error(401, "未登录").toJson());
            return;
        }

        // 管理员不允许提交投放记录
        if (loginUser.isAdmin()) {
            out.write(Result.error(403, "管理员不允许投放垃圾").toJson());
            return;
        }

        // 提交投放记录
        if ("/".equals(pathInfo) || "".equals(pathInfo)) {
            try {
                GarbageRecordSubmitDTO dto = new GarbageRecordSubmitDTO();
                dto.setImageName(req.getParameter("imageName"));
                dto.setImagePath(req.getParameter("imagePath"));
                dto.setResultImagePath(req.getParameter("resultImagePath"));
                dto.setDetectedSummary(req.getParameter("detectedSummary"));
                dto.setRecommendedCategory(req.getParameter("recommendedCategory"));
                dto.setSelectedCategory(req.getParameter("selectedCategory"));
                String isMixedStr = req.getParameter("isMixed");
                dto.setIsMixed(isMixedStr != null ? Integer.parseInt(isMixedStr) : 0);
                dto.setRemark(req.getParameter("remark"));

                Long recordId = recordService.saveRecord(loginUser.getId().longValue(), dto);
                out.write(Result.success(recordId).toJson());
            } catch (Exception e) {
                e.printStackTrace();
                out.write(Result.error(e.getMessage()).toJson());
            }
        } else {
            out.write(Result.error("未知路径").toJson());
        }
    }

    private void showHistory(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User loginUser = (User) req.getSession().getAttribute(AppConstants.SESSION_USER);
        if (loginUser == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        int page = 1;
        int pageSize = AppConstants.DEFAULT_PAGE_SIZE;
        try {
            page = Integer.parseInt(req.getParameter("page"));
        } catch (Exception ignored) {}
        try {
            pageSize = Integer.parseInt(req.getParameter("pageSize"));
        } catch (Exception ignored) {}
        String status = req.getParameter("status");

        PageResult<GarbageRecord> pageResult = recordService.getUserRecords(loginUser.getId().longValue(), page, pageSize, status);
        req.setAttribute("pageResult", pageResult);
        req.getRequestDispatcher("/WEB-INF/jsp/user/garbage-history.jsp").forward(req, resp);
    }

    private void showDetail(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Long id = null;
        try {
            id = Long.parseLong(req.getParameter("id"));
        } catch (Exception ignored) {}

        if (id == null) {
            resp.sendRedirect(req.getContextPath() + "/user/garbage-record/list");
            return;
        }

        try {
            GarbageRecordDetailVO detail = recordService.getRecordDetail(id);
            req.setAttribute("detail", detail);
            req.getRequestDispatcher("/WEB-INF/jsp/user/garbage-detail.jsp").forward(req, resp);
        } catch (Exception e) {
            req.setAttribute("error", e.getMessage());
            req.getRequestDispatcher("/WEB-INF/jsp/user/garbage-history.jsp").forward(req, resp);
        }
    }
}
