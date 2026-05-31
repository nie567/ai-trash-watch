package com.example.test.util;

import com.example.util.Result;
import org.junit.Test;

import static org.junit.Assert.*;

public class ResultTest {

    @Test
    public void testSuccess_defaultMessage() {
        Result<?> result = Result.success();
        assertEquals(200, result.getCode());
        assertEquals("success", result.getMessage());
    }

    @Test
    public void testSuccess_withData() {
        String data = "hello";
        Result<String> result = Result.success(data);
        assertEquals(200, result.getCode());
        assertEquals("success", result.getMessage());
        assertEquals("hello", result.getData());
    }

    @Test
    public void testSuccess_withCodeAndMessage() {
        Result<?> result = Result.success(201, "created");
        assertEquals(201, result.getCode());
        assertEquals("created", result.getMessage());
    }

    @Test
    public void testError_default() {
        Result<?> result = Result.error("something went wrong");
        assertEquals(500, result.getCode());
        assertEquals("something went wrong", result.getMessage());
    }

    @Test
    public void testError_withCode() {
        Result<?> result = Result.error(400, "bad request");
        assertEquals(400, result.getCode());
        assertEquals("bad request", result.getMessage());
    }

    @Test
    public void testToJson_validJson() {
        Result<String> result = Result.success("testdata");
        String json = result.toJson();
        assertNotNull(json);
        assertTrue("JSON应包含code字段", json.contains("\"code\""));
        assertTrue("JSON应包含message字段", json.contains("\"message\""));
        assertTrue("JSON应包含data字段", json.contains("\"data\""));
        assertTrue("JSON应包含200", json.contains("200"));
        assertTrue("JSON应包含testdata", json.contains("testdata"));
    }
}
