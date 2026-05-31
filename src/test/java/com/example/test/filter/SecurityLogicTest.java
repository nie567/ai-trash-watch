package com.example.test.filter;

import com.example.filter.XssRequestWrapper;
import com.example.util.CsrfTokenUtil;
import com.example.util.RequestUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.junit.Test;
import org.mockito.Mockito;

import java.lang.reflect.Method;

import static org.junit.Assert.*;

public class SecurityLogicTest {

    @Test
    public void testCsrfConstantTimeEquals() throws Exception {
        Method method = CsrfTokenUtil.class.getDeclaredMethod("constantTimeEquals", String.class, String.class);
        method.setAccessible(true);

        assertTrue("相同字符串应返回true", (boolean) method.invoke(null, "abc", "abc"));
        assertTrue("空字符串应返回true", (boolean) method.invoke(null, "", ""));
        assertFalse("不同字符串应返回false", (boolean) method.invoke(null, "abc", "abd"));
        assertFalse("不同长度应返回false", (boolean) method.invoke(null, "abc", "ab"));
        assertFalse("一个空一个非空应返回false", (boolean) method.invoke(null, "abc", ""));
        assertFalse("完全不同应返回false", (boolean) method.invoke(null, "aaa", "bbb"));
    }

    @Test
    public void testCsrfTokenGeneration() {
        HttpServletRequest mockReq1 = Mockito.mock(HttpServletRequest.class);
        HttpSession mockSession1 = Mockito.mock(HttpSession.class);
        Mockito.when(mockReq1.getSession(Mockito.anyBoolean())).thenReturn(mockSession1);
        Mockito.when(mockReq1.getSession(true)).thenReturn(mockSession1);
        Mockito.when(mockSession1.getAttribute(CsrfTokenUtil.SESSION_ATTR)).thenReturn(null);

        String token1 = CsrfTokenUtil.getOrCreateToken(mockReq1);
        assertNotNull("token不应为null", token1);
        assertFalse("token不应为空", token1.isEmpty());

        HttpServletRequest mockReq2 = Mockito.mock(HttpServletRequest.class);
        HttpSession mockSession2 = Mockito.mock(HttpSession.class);
        Mockito.when(mockReq2.getSession(Mockito.anyBoolean())).thenReturn(mockSession2);
        Mockito.when(mockReq2.getSession(true)).thenReturn(mockSession2);
        Mockito.when(mockSession2.getAttribute(CsrfTokenUtil.SESSION_ATTR)).thenReturn(null);

        String token2 = CsrfTokenUtil.getOrCreateToken(mockReq2);
        assertNotNull("第二个token不应为null", token2);
        assertFalse("两个不同调用应产生不同token", token1.equals(token2));
    }

    @Test
    public void testXssSkipParams() {
        HttpServletRequest mockReq = Mockito.mock(HttpServletRequest.class);
        String testValue = "P@ss<w>ord&test";
        Mockito.when(mockReq.getParameter("password")).thenReturn(testValue);

        XssRequestWrapper wrapper = new XssRequestWrapper(mockReq);
        String result = wrapper.getParameter("password");

        assertEquals("password参数不应被XSS过滤修改", testValue, result);
    }

    @Test
    public void testRequestUtilXRealIp() {
        HttpServletRequest mockReq = Mockito.mock(HttpServletRequest.class);

        Mockito.when(mockReq.getHeader("X-Forwarded-For")).thenReturn(null);
        Mockito.when(mockReq.getHeader("X-Real-IP")).thenReturn("10.0.0.1");
        Mockito.when(mockReq.getRemoteAddr()).thenReturn("192.168.1.1");

        String ip = RequestUtil.getClientIp(mockReq);
        assertEquals("X-Real-IP应优先于remoteAddr", "10.0.0.1", ip);

        HttpServletRequest mockReq2 = Mockito.mock(HttpServletRequest.class);
        Mockito.when(mockReq2.getHeader("X-Forwarded-For")).thenReturn("172.16.0.1");
        Mockito.when(mockReq2.getHeader("X-Real-IP")).thenReturn("10.0.0.2");
        Mockito.when(mockReq2.getRemoteAddr()).thenReturn("192.168.1.2");

        String ip2 = RequestUtil.getClientIp(mockReq2);
        assertEquals("X-Forwarded-For应优先于X-Real-IP", "172.16.0.1", ip2);
    }
}
