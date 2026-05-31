package com.example.test.filter;

import com.example.filter.XssRequestWrapper;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.*;

public class XssRequestWrapperTest {

    private XssRequestWrapper wrap(String paramName, String paramValue) {
        HttpServletRequest mockReq = Mockito.mock(HttpServletRequest.class);
        Mockito.when(mockReq.getParameter(paramName)).thenReturn(paramValue);
        return new XssRequestWrapper(mockReq);
    }

    private XssRequestWrapper wrapValues(String paramName, String[] values) {
        HttpServletRequest mockReq = Mockito.mock(HttpServletRequest.class);
        Mockito.when(mockReq.getParameterValues(paramName)).thenReturn(values);
        Mockito.when(mockReq.getParameter(paramName)).thenReturn(values != null && values.length > 0 ? values[0] : null);
        return new XssRequestWrapper(mockReq);
    }

    @Test
    public void testSanitize_scriptTag() {
        XssRequestWrapper wrapper = wrap("input", "<script>alert(1)</script>");
        String result = wrapper.getParameter("input");
        assertFalse("script标签应被清除", result.contains("<script>"));
        assertFalse("script标签应被清除", result.contains("</script>"));
    }

    @Test
    public void testSanitize_javascriptProtocol() {
        XssRequestWrapper wrapper = wrap("href", "javascript:alert(1)");
        String result = wrapper.getParameter("href");
        assertFalse("javascript:协议应被清除", result.toLowerCase().contains("javascript:"));
    }

    @Test
    public void testSanitize_eventHandler() {
        XssRequestWrapper wrapper = wrap("attr", "onerror=alert(1)");
        String result = wrapper.getParameter("attr");
        assertFalse("事件处理器应被清除", result.toLowerCase().contains("onerror="));
    }

    @Test
    public void testSanitize_normalText() {
        XssRequestWrapper wrapper = wrap("text", "hello world");
        assertEquals("正常文本应保留", "hello world", wrapper.getParameter("text"));
    }

    @Test
    public void testSanitize_nestedScript() {
        XssRequestWrapper wrapper = wrap("evil", "<scr<script>ipt>alert(1)</scr</script>ipt>");
        String result = wrapper.getParameter("evil");
        assertFalse("嵌套script应被清除", result.contains("<script>"));
        assertFalse("最终结果不应包含script标签", result.toLowerCase().contains("script>"));
    }

    @Test
    public void testGetParameter_null() {
        HttpServletRequest mockReq = Mockito.mock(HttpServletRequest.class);
        Mockito.when(mockReq.getParameter("missing")).thenReturn(null);
        XssRequestWrapper wrapper = new XssRequestWrapper(mockReq);
        assertNull("null应返回null", wrapper.getParameter("missing"));
    }

    @Test
    public void testGetParameterValues_cleaned() {
        String[] values = {"<script>bad</script>good", "normal"};
        XssRequestWrapper wrapper = wrapValues("multi", values);
        String[] cleaned = wrapper.getParameterValues("multi");
        assertNotNull(cleaned);
        assertEquals(2, cleaned.length);
        assertFalse(cleaned[0].contains("<script>"));
        assertTrue(cleaned[0].contains("good"));
        assertEquals("normal", cleaned[1]);
    }
}
