package com.example.controller;

import com.example.model.PageResult;
import com.example.model.User;
import com.example.model.ViolationRecord;
import com.example.service.RectificationService;
import com.example.service.UserService;
import com.example.service.ViolationService;
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
 * 管理员侧违规管理控制器
 */
@WebServlet("/admin/violation/*")
public class AdminViolationServlet extends HttpServlet {

    private ViolationService violationService;
    private RectificationService rectificationService;
    private UserService userService;

    @Override
    public void init() throws ServletException {
        violationService = AppContext.get().getViolationService();
        rectificationService = AppContext.get().getRectificationService();
        userService = AppContext.get().getUserService();
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
        if (pathInfo == null) pathInfo = "/";

        // 发起整改任务
        if ("/create-rectification".equals(pathInfo)) {
            try {
                Long violationId = Long.parseLong(req.getParameter("violationId"));
                String requirement = req.getParameter("requirement");
                String deadline = req.getParameter("deadline");

                ViolationRecord violation = violationService.getById(violationId);
                if (violation == null) {
                    out.write(Result.error("违规记录不存在").toJson());
                    return;
                }

                // 检查目标用户不是管理员
                // 通过 UserService 检查目标用户角色
                User targetUser = userService.getUserById(violation.getUserId().intValue());
                if (targetUser != null && targetUser.isAdmin()) {
                    out.write(Result.error("不能给管理员创建整改任务").toJson());
                    return;
                }

                Long taskId = rectificationService.createTask(violationId, violation.getUserId(), requirement, deadline);
                out.write(Result.success(taskId).toJson());
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
        int page = 1;
        int pageSize = AppConstants.DEFAULT_PAGE_SIZE;
        try { page = Integer.parseInt(req.getParameter("page")); } catch (Exception ignored) {}
        try { pageSize = Integer.parseInt(req.getParameter("pageSize")); } catch (Exception ignored) {}

        String status = req.getParameter("status");

        PageResult<ViolationRecord> pageResult = violationService.getAllViolations(page, pageSize, status);
        req.setAttribute("pageResult", pageResult);
        req.setAttribute("status", status);
        req.getRequestDispatcher("/WEB-INF/jsp/admin/violation-list.jsp").forward(req, resp);
    }

    private void showDetail(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Long id = null;
        try { id = Long.parseLong(req.getParameter("id")); } catch (Exception ignored) {}

        if (id == null) {
            resp.sendRedirect(req.getContextPath() + "/admin/violation/list");
            return;
        }

        ViolationRecord violation = violationService.getById(id);
        req.setAttribute("violation", violation);
        req.getRequestDispatcher("/WEB-INF/jsp/admin/violation-list.jsp").forward(req, resp);
    }
}
