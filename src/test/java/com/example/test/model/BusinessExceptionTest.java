package com.example.test.model;

import com.example.util.BusinessException;
import org.junit.Test;

import static org.junit.Assert.*;

public class BusinessExceptionTest {

    @Test
    public void testDefaultConstructor() {
        BusinessException e = new BusinessException();
        assertEquals(500, e.getCode());
        assertNull(e.getMessage());
    }

    @Test
    public void testMessageConstructor() {
        BusinessException e = new BusinessException("错误消息");
        assertEquals(500, e.getCode());
        assertEquals("错误消息", e.getMessage());
    }

    @Test
    public void testCodeAndMessageConstructor() {
        BusinessException e = new BusinessException(400, "参数错误");
        assertEquals(400, e.getCode());
        assertEquals("参数错误", e.getMessage());
    }

    @Test
    public void testMessageAndCauseConstructor() {
        Throwable cause = new RuntimeException("root cause");
        BusinessException e = new BusinessException("包装错误", cause);
        assertEquals(500, e.getCode());
        assertEquals("包装错误", e.getMessage());
        assertEquals(cause, e.getCause());
    }

    @Test
    public void testCodeMessageAndCauseConstructor() {
        Throwable cause = new RuntimeException("root");
        BusinessException e = new BusinessException(404, "未找到", cause);
        assertEquals(404, e.getCode());
        assertEquals("未找到", e.getMessage());
        assertEquals(cause, e.getCause());
    }

    @Test
    public void testSetCodeIsPrivate() {
        BusinessException e = new BusinessException();
        assertEquals(500, e.getCode());
        BusinessException e2 = new BusinessException(403, "forbidden");
        assertEquals(403, e2.getCode());
    }
}
