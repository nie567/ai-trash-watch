package com.example.test.util;

import com.example.util.BCryptUtil;
import org.junit.Test;

import static org.junit.Assert.*;

public class PasswordStrengthFullTest {

    @Test
    public void testTooLong() {
        String pwd65 = "a".repeat(65);
        assertEquals("密码长度不得超过 64 位", BCryptUtil.checkStrength(pwd65));
    }

    @Test
    public void testExactly64() {
        String base = "Ab1@xK9#mP2$qWnR";
        String pwd64 = base.repeat(4);
        assertEquals(64, pwd64.length());
        assertNull(BCryptUtil.checkStrength(pwd64));
    }

    @Test
    public void testWeakPassword() {
        assertEquals("密码过于常见，请更换", BCryptUtil.checkStrength("12345678"));
    }

    @Test
    public void testWeakPassword2() {
        assertEquals("密码过于常见，请更换", BCryptUtil.checkStrength("password"));
    }

    @Test
    public void testRepeatedChar() {
        assertEquals("密码不能全部为同一字符", BCryptUtil.checkStrength("aaaaaaaa"));
    }

    @Test
    public void testPureDigits() {
        assertEquals("密码需至少包含字母、数字、符号中的两类", BCryptUtil.checkStrength("2468135799"));
    }

    @Test
    public void testPureSymbols() {
        assertEquals("密码需至少包含字母、数字、符号中的两类", BCryptUtil.checkStrength("!@#$%^&*"));
    }

    @Test
    public void testLetterAndDigit() {
        assertEquals("密码过于常见，请更换", BCryptUtil.checkStrength("abc12345"));
        assertNull(BCryptUtil.checkStrength("abx12345"));
    }

    @Test
    public void testIsPasswordStrongEnough_true() {
        assertTrue(BCryptUtil.isPasswordStrongEnough("Str0ng!Pass"));
    }

    @Test
    public void testIsPasswordStrongEnough_false() {
        assertFalse(BCryptUtil.isPasswordStrongEnough("12345678"));
    }
}
