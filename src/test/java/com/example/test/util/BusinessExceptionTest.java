package com.example.test.util;

import com.example.util.BusinessException;
import org.junit.Test;

import static org.junit.Assert.*;

public class BusinessExceptionTest {

    @Test
    public void testDefaultCode() {
        BusinessException ex = new BusinessException("error occurred");
        assertEquals(500, ex.getCode());
        assertEquals("error occurred", ex.getMessage());
    }

    @Test
    public void testCustomCode() {
        BusinessException ex = new BusinessException(400, "bad request");
        assertEquals(400, ex.getCode());
        assertEquals("bad request", ex.getMessage());
    }

    @Test
    public void testGetCode() {
        BusinessException ex1 = new BusinessException(404, "not found");
        assertEquals(404, ex1.getCode());

        BusinessException ex2 = new BusinessException(403, "forbidden");
        assertEquals(403, ex2.getCode());
    }

    @Test
    public void testWithCause() {
        RuntimeException cause = new RuntimeException("root cause");
        BusinessException ex = new BusinessException(500, "wrapped error", cause);
        assertEquals(500, ex.getCode());
        assertEquals("wrapped error", ex.getMessage());
        assertSame(cause, ex.getCause());

        BusinessException ex2 = new BusinessException("msg with cause", cause);
        assertEquals(500, ex2.getCode());
        assertSame(cause, ex2.getCause());
    }
}
