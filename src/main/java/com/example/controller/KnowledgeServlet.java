package com.example.controller;

import com.example.model.KnowledgeBase;
import com.example.service.KnowledgeService;
import com.example.util.Result;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * 知识库控制器
 * 用户浏览 + 管理员管理
 */
@WebServlet({"/user/knowledge/*", "/admin/knowledge/*"})
public class KnowledgeServlet extends HttpServlet {

    private KnowledgeService knowledgeService;

    @Override
    public void init() throws ServletException {
        knowledgeService = new KnowledgeService();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String uri = req.getRequestURI();
        String pathInfo = req.getPathInfo();
        if (pathInfo == null) pathInfo = "/list";

        if (uri.contains("/user/knowledge")) {
            // 用户端浏览
            String garbageType = req.getParameter("garbageType");
            List<KnowledgeBase> list = knowledgeService.listByType(garbageType);
            req.setAttribute("knowledgeList", list);
            req.setAttribute("currentType", garbageType);
            req.getRequestDispatcher("/WEB-INF/jsp/user/knowledge-list.jsp").forward(req, resp);
        } else if (uri.contains("/admin/knowledge")) {
            // 管理员管理
            List<KnowledgeBase> list = knowledgeService.listAll();
            req.setAttribute("knowledgeList", list);
            req.getRequestDispatcher("/WEB-INF/jsp/admin/knowledge-list.jsp").forward(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");
        PrintWriter out = resp.getWriter();

        String pathInfo = req.getPathInfo();

        // 保存知识条目
        if ("/save".equals(pathInfo)) {
            try {
                KnowledgeBase kb = new KnowledgeBase();
                String idStr = req.getParameter("id");
                if (idStr != null && !idStr.trim().isEmpty()) {
                    kb.setId(Long.parseLong(idStr));
                }
                kb.setTitle(req.getParameter("title"));
                kb.setGarbageType(req.getParameter("garbageType"));
                kb.setContent(req.getParameter("content"));
                kb.setImagePath(req.getParameter("imagePath"));

                knowledgeService.save(kb);
                out.write(Result.success().toJson());
            } catch (Exception e) {
                e.printStackTrace();
                out.write(Result.error(e.getMessage()).toJson());
            }
        } else if ("/delete".equals(pathInfo)) {
            try {
                Long id = Long.parseLong(req.getParameter("id"));
                knowledgeService.delete(id);
                out.write(Result.success().toJson());
            } catch (Exception e) {
                e.printStackTrace();
                out.write(Result.error(e.getMessage()).toJson());
            }
        } else {
            out.write(Result.error("未知路径").toJson());
        }
    }
}
