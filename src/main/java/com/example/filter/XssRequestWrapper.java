package com.example.filter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.util.regex.Pattern;

/**
 * XSS 请求包装器
 * 对所有 GET/POST 参数进行 HTML 转义
 */
public class XssRequestWrapper extends HttpServletRequestWrapper {
    
    private static final Pattern SCRIPT_PATTERN = Pattern.compile(
            "<script[^>]*?>.*?</script>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern JAVASCRIPT_PATTERN = Pattern.compile(
            "javascript:", Pattern.CASE_INSENSITIVE);
    private static final Pattern ONERROR_PATTERN = Pattern.compile(
            "onerror\\s*=", Pattern.CASE_INSENSITIVE);
    private static final Pattern ONCLICK_PATTERN = Pattern.compile(
            "onclick\\s*=", Pattern.CASE_INSENSITIVE);
    
    public XssRequestWrapper(HttpServletRequest request) {
        super(request);
    }
    
    @Override
    public String[] getParameterValues(String name) {
        String[] values = super.getParameterValues(name);
        if (values == null) return null;
        String[] encoded = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            encoded[i] = sanitize(values[i]);
        }
        return encoded;
    }
    
    @Override
    public String getParameter(String name) {
        String value = super.getParameter(name);
        return sanitize(value);
    }
    
    private String sanitize(String value) {
        if (value == null) return null;
        // HTML 转义
        value = value.replace("&", "&amp;")
                     .replace("<", "&lt;")
                     .replace(">", "&gt;")
                     .replace("\"", "&quot;")
                     .replace("'", "&#x27;");
        return value;
    }
}