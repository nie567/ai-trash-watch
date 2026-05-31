package com.example.test.util;

import com.example.util.AppConfig;
import org.junit.After;
import org.junit.Test;

import static org.junit.Assert.*;

public class AppConfigTest {

    private static final String KEY_URL = "djl.inference.url";
    private static final String KEY_INPUT = "djl.input.dir";
    private static final String KEY_OUTPUT = "djl.output.dir";

    @After
    public void clearOverrides() {
        System.clearProperty(KEY_URL);
        System.clearProperty(KEY_INPUT);
        System.clearProperty(KEY_OUTPUT);
    }

    @Test
    public void testGetDjlInferenceUrl_notNull() {
        String url = AppConfig.getDjlInferenceUrl();
        assertNotNull(url);
        assertFalse(url.isEmpty());
    }

    @Test
    public void testGetDjlInputDir_notNull() {
        String dir = AppConfig.getDjlInputDir();
        assertNotNull(dir);
        assertFalse(dir.isEmpty());
    }

    @Test
    public void testGetDjlOutputDir_notNull() {
        String dir = AppConfig.getDjlOutputDir();
        assertNotNull(dir);
        assertFalse(dir.isEmpty());
    }

    @Test
    public void testSystemPropertyOverride() {
        String overrideUrl = "http://override-host:9999";
        System.setProperty(KEY_URL, overrideUrl);
        assertEquals("系统属性应优先于默认值", overrideUrl, AppConfig.getDjlInferenceUrl());

        String overrideInput = "/tmp/override-input";
        System.setProperty(KEY_INPUT, overrideInput);
        assertEquals(overrideInput, AppConfig.getDjlInputDir());

        String overrideOutput = "/tmp/override-output";
        System.setProperty(KEY_OUTPUT, overrideOutput);
        assertEquals(overrideOutput, AppConfig.getDjlOutputDir());
    }
}
