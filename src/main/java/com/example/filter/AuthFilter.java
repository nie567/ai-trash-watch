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
 * 认证过滤器
 * 确保用户已登录才能访问受保护资源
 */
@WebFilter(filterName = "AuthFilter", urlPatterns = {"/admin/*", "/user/*"})
public class AuthFilter implements Filter {
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        HttpSession session = req.getSession(false);
        
        User loginUser = session != null ? (User) session.getAttribute(AppConstants.SESSION_USER) : null;
        
        if (loginUser == null) {
            // 未登录，重定向到登录页
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        
        // 检查账号是否被禁用
        if (!loginUser.isActive()) {
            session.invalidate();
            req.setAttribute("error", "账号已被禁用，请联系管理员");
            req.getRequestDispatcher("/WEB-INF/jsp/login.jsp").forward(req, resp);
            return;
        }
        
        chain.doFilter(request, response);
    }
    
    @Override
    public void destroy() {}
}