package com.example.test.config;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.LoggingEvent;
import com.example.config.MaskingPatternLayout;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.*;

/**
 * MaskingPatternLayout 脱敏规则单元测试。
 * <p>
 * 直接构造 LoggingEvent 并调用 layout.doLayout()，
 * 验证输出中敏感字段已被替换。
 */
public class MaskingPatternLayoutTest {

    private MaskingPatternLayout layout;
    private LoggerContext context;
    private Logger logger;

    @Before
    public void setUp() {
        context = (LoggerContext) LoggerFactory.getILoggerFactory();
        logger = context.getLogger("test");
        layout = new MaskingPatternLayout();
        layout.setContext(context);
        layout.setPattern("%msg%n");
        layout.start();
    }

    @After
    public void tearDown() {
        layout.stop();
    }

    /** 构造一条日志事件，经过 layout 格式化后返回输出文本 */
    private String doLayout(String message) {
        LoggingEvent event = new LoggingEvent(
                "com.example.test.config.MaskingPatternLayoutTest",
                logger, Level.INFO, message, null, null);
        return layout.doLayout(event);
    }

    // ========== URL / SQL 参数风格: key=value ==========

    @Test
    public void testMaskPasswordInUrlParam() {
        String out = doLayout("Login failed: password=abc123&username=admin");
        assertTrue("URL password 应被脱敏", out.contains("password=***"));
        assertFalse("不应泄漏 password 明文", out.contains("password=abc123"));
    }

    @Test
    public void testMaskTokenInUrlParam() {
        String out = doLayout("token=eyJhbGciOiJIUzI1NiJ9.xyz");
        assertTrue("URL token 应被脱敏", out.contains("token=***"));
        assertFalse("不应泄漏 token 明文", out.contains("eyJhbGci"));
    }

    @Test
    public void testMaskSecretInUrlParam() {
        String out = doLayout("secret=myKey123");
        assertTrue("URL secret 应被脱敏", out.contains("secret=***"));
        assertFalse("不应泄漏 secret 明文", out.contains("myKey123"));
    }

    @Test
    public void testMaskPwdInUrlParam() {
        String out = doLayout("pwd=hunter2");
        assertTrue("URL pwd 应被脱敏", out.contains("pwd=***"));
    }

    @Test
    public void testMaskPasswordHashInUrlParam() {
        String out = doLayout("password_hash=$2a$10$abcdefg");
        assertTrue("URL password_hash 应被脱敏", out.contains("password_hash=***"));
        assertFalse("不应泄漏 hash", out.contains("$2a$10$"));
    }

    @Test
    public void testMaskPasswordHashCamelCaseInUrlParam() {
        String out = doLayout("passwordHash=$2a$10$abcdefg");
        assertTrue("驼峰 passwordHash 应被脱敏", out.contains("passwordHash=***"));
    }

    // ========== JSON 风格: "key":"value" ==========

    @Test
    public void testMaskJsonPassword() {
        String out = doLayout("{\"password\":\"123456\"}");
        assertTrue("JSON password 应被脱敏", out.contains("\"password\":\"***\""));
        assertFalse("不应泄漏 JSON 明文", out.contains("\"password\":\"123456\""));
    }

    @Test
    public void testMaskJsonToken() {
        String out = doLayout("{\"token\":\"eyJhbGci.xxxx\"}");
        assertTrue("JSON token 应被脱敏", out.contains("\"token\":\"***\""));
    }

    @Test
    public void testMaskJsonPwd() {
        String out = doLayout("{\"pwd\":\"secret\"}");
        assertTrue("JSON pwd 应被脱敏", out.contains("\"pwd\":\"***\""));
    }

    // ========== YAML / 配置风格: key: value ==========

    @Test
    public void testMaskColonPassword() {
        String out = doLayout("Config: password: 123456");
        assertTrue("冒号分隔 password 应被脱敏", out.contains("password:***"));
        assertFalse("不应泄漏冒号分隔明文", out.contains("password: 123456"));
    }

    @Test
    public void testMaskColonToken() {
        String out = doLayout("db.token: sk-xxxxx");
        assertTrue("冒号分隔 token 应被脱敏", out.contains("token:***"));
    }

    // ========== Authorization 请求头 ==========

    @Test
    public void testMaskBearerToken() {
        String out = doLayout("Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.token");
        assertTrue("Bearer 应被覆盖", out.contains("Bearer ***"));
        assertFalse("不应泄漏 Bearer token", out.contains("eyJhbGciOiJIUzI1NiJ9.token"));
    }

    @Test
    public void testMaskBasicAuth() {
        String out = doLayout("Auth: Basic dXNlcjpwYXNz");
        assertTrue("Basic 应被覆盖", out.contains("Basic ***"));
        assertFalse("不应泄漏 Basic 参数", out.contains("dXNlcjpwYXNz"));
    }

    // ========== 边界情况 ==========

    @Test
    public void testNormalMessageNotAffected() {
        String msg = "User login success: admin";
        assertEquals(msg + System.lineSeparator(), doLayout(msg));
    }

    @Test
    public void testEmptyMessage() {
        assertNotNull("空消息不应崩溃", doLayout(""));
    }

    @Test
    public void testMessageWithPasswordLikeButNotSensitive() {
        // "pass" 不在关键词列表中，不应误杀
        String msg = "Please pass the message to admin";
        assertEquals(msg + System.lineSeparator(), doLayout(msg));
    }

    @Test
    public void testMultipleSensitiveFieldsInOneMessage() {
        String out = doLayout("password=abc&token=xyz&secret=123");
        assertTrue("多个字段全部脱敏", out.contains("password=***"));
        assertTrue("多个字段全部脱敏", out.contains("token=***"));
        assertTrue("多个字段全部脱敏", out.contains("secret=***"));
    }

    @Test
    public void testCaseInsensitiveMasking() {
        String out = doLayout("PASSWORD=admin123");
        assertTrue("大写 PASSWORD 应脱敏", out.contains("PASSWORD=***"));
    }

    @Test
    public void testMessageWithoutNewline() {
        // 消息结尾不应有额外空行以外的问题
        String out = doLayout("password=secret123");
        assertTrue(out.contains("password=***"));
        assertTrue("输出应包含换行", out.endsWith(System.lineSeparator()));
    }
}
