package com.example.filter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

/**
 * XSS 请求包装器
 * 对所有 GET/POST 参数进行安全过滤：
 * - 移除危险的 script 标签、javascript: 协议、onXXX 事件属性
 * - 保留合法字符（& < > " '），由 JSP 输出层（EL/JSTL）负责 HTML 转义
 *
 * 设计原则：输入过滤只拦截危险模式，不做全量 HTML 转义，
 * 因为全量转义会破坏密码、邮箱等合法输入（如 P@ss&lt;word, alice&amp;bob@x.com）
 */
public class XssRequestWrapper extends HttpServletRequestWrapper {

    // 危险标签匹配：<script>...</script>
    private static final String SCRIPT_TAG_REGEX = "<\\s*/?script[^>]*>";
    // 危险协议：javascript:
    private static final String JS_PROTOCOL_REGEX = "(?i)javascript\\s*:";
    // 危险事件属性：onerror=, onclick=, onload= 等
    private static final String EVENT_ATTR_REGEX = "(?i)\\bon\\w+\\s*=";

    public XssRequestWrapper(HttpServletRequest request) {
        super(request);
    }

    @Override
    public String[] getParameterValues(String name) {
        String[] values = super.getParameterValues(name);
        if (values == null) return null;
        String[] cleaned = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            cleaned[i] = sanitize(values[i]);
        }
        return cleaned;
    }

    @Override
    public String getParameter(String name) {
        String value = super.getParameter(name);
        return sanitize(value);
    }

    /**
     * 移除 XSS 攻击向量，但保留合法的 HTML 特殊字符。
     * 不做全量 HTML 转义 — 输出转义由 JSP EL/JSTL 负责。
     * 多轮清理防止嵌套绕过（如 <scr<script>ipt>）。
     */
    private String sanitize(String value) {
        if (value == null) return null;
        String prev;
        do {
            prev = value;
            value = value.replaceAll(SCRIPT_TAG_REGEX, "");
            value = value.replaceAll(JS_PROTOCOL_REGEX, "");
            value = value.replaceAll(EVENT_ATTR_REGEX, "");
        } while (!value.equals(prev));
        return value;
    }
}
