package com.example.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * 应用配置读取工具类
 * 优先级：系统属性 > 环境变量 > classpath:app.properties
 */
public class AppConfig {

    private static final Properties FILE_PROPS;

    static {
        FILE_PROPS = new Properties();
        try (InputStream in = AppConfig.class.getClassLoader().getResourceAsStream("app.properties")) {
            if (in != null) {
                FILE_PROPS.load(in);
            }
        } catch (IOException ignored) {
        }
    }

    public static String getDjlInputDir() {
        return resolve("djl.input.dir", "DJL_INPUT_DIR", "./data/input");
    }

    public static String getDjlOutputDir() {
        return resolve("djl.output.dir", "DJL_OUTPUT_DIR", "./data/output");
    }

    public static String getDjlInferenceUrl() {
        return resolve("djl.inference.url", "DJL_INFERENCE_URL", "http://localhost:8080");
    }

    public static String getUploadDir() {
        return resolve("upload.dir", "UPLOAD_DIR", "./data/upload");
    }

    private static String resolve(String sysKey, String envKey, String defaultValue) {
        String v = System.getProperty(sysKey);
        if (v != null && !v.isEmpty()) return v;
        v = System.getenv(envKey);
        if (v != null && !v.isEmpty()) return v;
        v = FILE_PROPS.getProperty(sysKey);
        if (v != null && !v.isEmpty()) return v;
        return defaultValue;
    }

    private AppConfig() {}
}
