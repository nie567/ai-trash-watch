package com.example.filter;

import com.example.model.User;
import com.example.util.AppConstants;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
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
        boolean isAjax = "XMLHttpRequest".equals(req.getHeader("X-Requested-With"));

        if (loginUser == null || !loginUser.isAdmin()) {
            if (isAjax) {
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                resp.setContentType("application/json;charset=UTF-8");
                resp.getWriter().write("{\"code\":403,\"message\":\"无管理员权限\"}");
            } else {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            }
            return;
        }
        
        chain.doFilter(request, response);
    }
    
    @Override
    public void destroy() {}
}