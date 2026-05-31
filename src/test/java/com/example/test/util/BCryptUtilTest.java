package com.example.test.util;

import com.example.util.BCryptUtil;
import org.junit.Test;

import static org.junit.Assert.*;

public class BCryptUtilTest {

    @Test
    public void testHashAndVerify() {
        String plain = "MyStr0ng@Pass";
        String hashed = BCryptUtil.hashPassword(plain);
        assertNotNull(hashed);
        assertTrue("hash后应能验证通过", BCryptUtil.verifyPassword(plain, hashed));
    }

    @Test
    public void testVerifyWrongPassword() {
        String hashed = BCryptUtil.hashPassword("Correct@123");
        assertFalse("错误密码不应验证通过", BCryptUtil.verifyPassword("Wrong@456", hashed));
    }

    @Test
    public void testCheckStrength_null() {
        String result = BCryptUtil.checkStrength(null);
        assertNotNull("null密码应返回错误", result);
        assertTrue(result.contains("不能为空"));
    }

    @Test
    public void testCheckStrength_short() {
        String result = BCryptUtil.checkStrength("abc");
        assertNotNull("短密码应返回错误", result);
        assertTrue(result.contains("长度至少"));
    }

    @Test
    public void testCheckStrength_noDigit() {
        String result = BCryptUtil.checkStrength("abcdefgh");
        assertNotNull("纯字母密码应返回错误(缺少数字或符号)", result);
    }

    @Test
    public void testCheckStrength_strong() {
        String result = BCryptUtil.checkStrength("Str0ng!Pass");
        assertNull("强密码应返回null", result);
    }

    @Test
    public void testCheckPassword_matches_verifyPassword() {
        String plain = "Ch3ck@Test";
        String hashed = BCryptUtil.hashPassword(plain);
        assertEquals("checkPassword应与verifyPassword结果一致",
                BCryptUtil.verifyPassword(plain, hashed),
                BCryptUtil.checkPassword(plain, hashed));
    }
}
