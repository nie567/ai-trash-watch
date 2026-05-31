package com.example.controller;

import com.example.model.GarbageRule;
import com.example.service.RuleService;
import com.example.util.AppContext;
import com.example.util.Result;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 分类规则管理控制器
 */
@WebServlet("/admin/rule/*")
public class RuleServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(RuleServlet.class);


    private RuleService ruleService;

    @Override
    public void init() throws ServletException {
        ruleService = AppContext.get().getRuleService();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo == null) pathInfo = "/list";

        if ("/list".equals(pathInfo)) {
            List<GarbageRule> rules = ruleService.listRules();
            req.setAttribute("rules", rules);
            req.getRequestDispatcher("/WEB-INF/jsp/admin/rule-list.jsp").forward(req, resp);
        } else {
            List<GarbageRule> rules = ruleService.listRules();
            req.setAttribute("rules", rules);
            req.getRequestDispatcher("/WEB-INF/jsp/admin/rule-list.jsp").forward(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");
        PrintWriter out = resp.getWriter();

        String pathInfo = req.getPathInfo();

        // 保存规则
        if ("/save".equals(pathInfo)) {
            try {
                GarbageRule rule = new GarbageRule();
                String idStr = req.getParameter("id");
                if (idStr != null && !idStr.trim().isEmpty()) {
                    rule.setId(Long.parseLong(idStr));
                }
                rule.setClassName(req.getParameter("className"));
                rule.setMappedCategory(req.getParameter("mappedCategory"));
                rule.setDescription(req.getParameter("description"));
                String statusStr = req.getParameter("status");
                rule.setStatus(statusStr != null && !statusStr.isEmpty() ? Integer.parseInt(statusStr) : 1);

                ruleService.saveRule(rule);
                out.write(Result.success().toJson());
            } catch (Exception e) {
                logger.error("unexpected error", e);
                out.write(Result.error(e.getMessage()).toJson());
            }
        } else {
            out.write(Result.error("未知路径").toJson());
        }
    }
}
