package com.example.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * CSRF Token 工具（同步令牌模式 Synchronizer Token Pattern）。
 * <p>
 * Token 绑定到 HttpSession，每个 session 一枚；登录/登出会随 session 一起重置。
 */
public final class CsrfTokenUtil {

    public static final String SESSION_ATTR = "_csrfToken";
    public static final String PARAM_NAME = "_csrf";
    public static final String HEADER_NAME = "X-CSRF-Token";

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int TOKEN_BYTES = 32;

    private CsrfTokenUtil() {}

    /** 获取当前 session 的 token；不存在则生成。 */
    public static String getOrCreateToken(HttpServletRequest req) {
        HttpSession session = req.getSession(true);
        Object cached = session.getAttribute(SESSION_ATTR);
        if (cached instanceof String && !((String) cached).isEmpty()) {
            return (String) cached;
        }
        String token = generate();
        session.setAttribute(SESSION_ATTR, token);
        return token;
    }

    /** 校验请求中的 token 是否与 session 中的一致。 */
    public static boolean validate(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null) {
            return false;
        }
        Object expected = session.getAttribute(SESSION_ATTR);
        if (!(expected instanceof String) || ((String) expected).isEmpty()) {
            return false;
        }
        String actual = req.getHeader(HEADER_NAME);
        if (actual == null || actual.isEmpty()) {
            actual = req.getParameter(PARAM_NAME);
        }
        if (actual == null || actual.isEmpty()) {
            return false;
        }
        return constantTimeEquals((String) expected, actual);
    }

    private static String generate() {
        byte[] buf = new byte[TOKEN_BYTES];
        RANDOM.nextBytes(buf);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(buf);
    }

    private static boolean constantTimeEquals(String a, String b) {
        if (a.length() != b.length()) {
            return false;
        }
        int diff = 0;
        for (int i = 0; i < a.length(); i++) {
            diff |= a.charAt(i) ^ b.charAt(i);
        }
        return diff == 0;
    }
}
