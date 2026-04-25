package com.example.filter;

import com.example.model.User;
import com.example.util.AppConstants;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * 管理员权限过滤器
 * 确保只有 admin 角色才能访问 /admin/* 路径
 */
@WebFilter(filterName = "AdminFilter", urlPatterns = {"/admin/*"})
public class AdminFilter implements Filter {
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        HttpSession session = req.getSession(false);
        
        User loginUser = session != null ? (User) session.getAttribute(AppConstants.SESSION_USER) : null;
        
        if (loginUser == null || !loginUser.isAdmin()) {
            // 不是管理员，返回 403 禁止访问
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            req.setAttribute("error", "您没有权限访问此页面");
            req.getRequestDispatcher("/WEB-INF/jsp/error/403.jsp").forward(req, resp);
            return;
        }
        
        chain.doFilter(request, response);
    }
    
    @Override
    public void destroy() {}
}