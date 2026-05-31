package com.example.test.util;

import com.example.model.User;
import com.example.util.AppConstants;
import com.example.util.RequestUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.*;

public class RequestUtilTest {

    @Test
    public void testGetLoginUser_present() {
        HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
        HttpSession session = Mockito.mock(HttpSession.class);
        User user = new User(1, "testuser", "user", 1);

        Mockito.when(req.getSession(false)).thenReturn(session);
        Mockito.when(session.getAttribute(AppConstants.SESSION_USER)).thenReturn(user);

        User result = RequestUtil.getLoginUser(req);
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
    }

    @Test
    public void testGetLoginUser_absent() {
        HttpServletRequest req1 = Mockito.mock(HttpServletRequest.class);
        Mockito.when(req1.getSession(false)).thenReturn(null);
        assertNull("session为null应返回null", RequestUtil.getLoginUser(req1));

        HttpServletRequest req2 = Mockito.mock(HttpServletRequest.class);
        HttpSession session = Mockito.mock(HttpSession.class);
        Mockito.when(req2.getSession(false)).thenReturn(session);
        Mockito.when(session.getAttribute(AppConstants.SESSION_USER)).thenReturn(null);
        assertNull("session中无user应返回null", RequestUtil.getLoginUser(req2));
    }

    @Test
    public void testGetClientIp_xForwardedFor() {
        HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
        Mockito.when(req.getHeader("X-Forwarded-For")).thenReturn("192.168.1.100");
        Mockito.when(req.getRemoteAddr()).thenReturn("10.0.0.1");

        String ip = RequestUtil.getClientIp(req);
        assertEquals("应优先使用X-Forwarded-For", "192.168.1.100", ip);
    }

    @Test
    public void testGetClientIp_remoteAddr() {
        HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
        Mockito.when(req.getHeader("X-Forwarded-For")).thenReturn(null);
        Mockito.when(req.getHeader("X-Real-IP")).thenReturn(null);
        Mockito.when(req.getRemoteAddr()).thenReturn("10.0.0.1");

        String ip = RequestUtil.getClientIp(req);
        assertEquals("无代理头应使用remoteAddr", "10.0.0.1", ip);
    }
}
