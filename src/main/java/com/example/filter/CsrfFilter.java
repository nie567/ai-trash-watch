package com.example.filter;

import com.example.util.CsrfTokenUtil;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CSRF 同步令牌校验过滤器。
 * <p>
 * 安全读方法（GET/HEAD/OPTIONS）放行并确保 session 中存在 token；
 * 非安全方法（POST/PUT/DELETE/PATCH）必须携带正确 token，否则返回 403。
 * <p>
 * 豁免路径：登录（尚无 session）、推理上传（multipart 表单在 filter 中解析会与业务 Servlet 冲突）。
 */
@WebFilter(filterName = "CsrfFilter", urlPatterns = "/*")
public class CsrfFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(CsrfFilter.class);

    private static final Set<String> SAFE_METHODS = new HashSet<>(Arrays.asList("GET", "HEAD", "OPTIONS", "TRACE"));

    /** 无法/不应校验 CSRF 的路径前缀（相对 contextPath）。 */
    private static final String[] EXEMPT_PREFIXES = {
            "/login",         // 登录请求无 session
            "/register",      // 注册请求与登录共享同一页面
            "/inference",     // multipart 上传，表单不由 JSP 渲染
            "/upload",        // multipart 上传，表单不由 JSP 渲染
            "/image/",        // 静态图片流
            "/css/",
            "/js/",
            "/images/"
    };

    @Override
    public void init(FilterConfig filterConfig) {}

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        String method = req.getMethod();
        String path = req.getRequestURI().substring(req.getContextPath().length());

        // 安全方法：保证 session 有 token 供页面渲染使用，然后放行
        if (SAFE_METHODS.contains(method)) {
            CsrfTokenUtil.getOrCreateToken(req);
            chain.doFilter(request, response);
            return;
        }

        // 豁免清单
        if (isExempt(path)) {
            chain.doFilter(request, response);
            return;
        }

        if (!CsrfTokenUtil.validate(req)) {
            logger.warn("CSRF token invalid, method={}, path={}, ip={}", method, path, req.getRemoteAddr());
            // AJAX 请求返回 JSON，传统请求返回纯文本
            if ("XMLHttpRequest".equals(req.getHeader("X-Requested-With"))) {
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                resp.setContentType("application/json;charset=UTF-8");
                resp.getWriter().write("{\"code\":403,\"message\":\"会话已过期或安全校验失败，请刷新页面后重试\"}");
            } else {
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                resp.setContentType("text/plain;charset=UTF-8");
                resp.getWriter().write("CSRF token 校验失败，请刷新页面后重试");
            }
            return;
        }

        chain.doFilter(request, response);
    }

    private boolean isExempt(String path) {
        for (String prefix : EXEMPT_PREFIXES) {
            if (path.equals(prefix) || path.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void destroy() {}
}
