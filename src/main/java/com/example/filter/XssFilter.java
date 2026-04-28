package com.example.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * XSS 防护过滤器
 * 对请求参数进行 HTML 转义，防止跨站脚本攻击
 */
@WebFilter(filterName = "XssFilter", urlPatterns = {"/*"})
public class XssFilter implements Filter {
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        chain.doFilter(new XssRequestWrapper((HttpServletRequest) request), response);
    }
    
    @Override
    public void destroy() {}
}