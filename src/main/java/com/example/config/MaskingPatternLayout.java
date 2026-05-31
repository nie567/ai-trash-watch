package com.example.config;

import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;

import java.util.AbstractMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 自定义 PatternLayout —— 在输出日志前脱敏敏感字段。
 * <p>
 * 在 logback.xml 中引用：
 * <pre>{@code
 * <encoder class="ch.qos.logback.core.encoder.LayoutWrappingEncoder">
 *     <layout class="com.example.config.MaskingPatternLayout">
 *         <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
 *     </layout>
 * </encoder>
 * }</pre>
 */
public class MaskingPatternLayout extends PatternLayout {

    /** (正则, 替换模板) 规则列表，按优先级排列 */
    private static final Map.Entry<Pattern, String>[] MASK_RULES = new Map.Entry[]{
            // 1) URL 参数 / SQL: key=value
            newEntry(
                    "(?i)(password|password_hash|passwordHash|pwd|token|secret)\\s*=\\s*[^&,\\s;)]+",
                    "$1=***"),
            // 2) JSON 格式: "key":"value"
            newEntry(
                    "(?i)(\"(password|password_hash|pwd|token|secret)\"\\s*:\\s*)\"[^\"]*\"",
                    "$1\"***\""),
            // 3) 冒号分隔: key: value（YAML / 配置风格）
            newEntry(
                    "(?i)(password|password_hash|pwd|token|secret)\\s*:\\s*[^,\\s;)\"'\\s]+",
                    "$1:***"),
            // 4) Authorization 请求头: Bearer xxxx, Basic xxxx
            newEntry(
                    "(?i)(Basic|Bearer)\\s+[A-Za-z0-9+/=._\\-]+",
                    "$1 ***"),
    };

    @Override
    public String doLayout(ILoggingEvent event) {
        String message = super.doLayout(event);
        if (message == null) {
            return null;
        }
        for (Map.Entry<Pattern, String> rule : MASK_RULES) {
            message = rule.getKey().matcher(message).replaceAll(rule.getValue());
        }
        return message;
    }

    private static Map.Entry<Pattern, String> newEntry(String regex, String replacement) {
        return new AbstractMap.SimpleImmutableEntry<>(
                Pattern.compile(regex, Pattern.CASE_INSENSITIVE), replacement);
    }
}
