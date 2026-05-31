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
 * 认证过滤器
 * 确保用户已登录才能访问受保护资源
 */
@WebFilter(filterName = "AuthFilter", urlPatterns = {"/admin/*", "/user/*", "/inference", "/inference/*"})
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
        boolean isAjax = "XMLHttpRequest".equals(req.getHeader("X-Requested-With"));

        if (loginUser == null) {
            if (isAjax) {
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                resp.setContentType("application/json;charset=UTF-8");
                resp.getWriter().write("{\"code\":401,\"message\":\"未登录或会话已过期\"}");
            } else {
                // 未登录，重定向到登录页
                resp.sendRedirect(req.getContextPath() + "/login");
            }
            return;
        }

        // 检查账号是否被禁用
        if (!loginUser.isActive()) {
            session.invalidate();
            if (isAjax) {
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                resp.setContentType("application/json;charset=UTF-8");
                resp.getWriter().write("{\"code\":403,\"message\":\"账号已被禁用，请联系管理员\"}");
            } else {
                req.setAttribute("error", "账号已被禁用，请联系管理员");
                req.getRequestDispatcher("/WEB-INF/jsp/login.jsp").forward(req, resp);
            }
            return;
        }
        
        chain.doFilter(request, response);
    }
    
    @Override
    public void destroy() {}
}