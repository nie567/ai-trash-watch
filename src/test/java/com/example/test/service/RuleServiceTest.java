package com.example.test.service;

import com.example.model.GarbageRule;
import com.example.service.RuleService;
import com.example.test.BaseTest;
import org.junit.Test;

import java.sql.SQLException;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * RuleService 测试类
 * 测试分类规则业务服务
 */
public class RuleServiceTest extends BaseTest {

    private RuleService ruleService = new RuleService();

    @Override
    protected void initTestData() throws SQLException {
        // 清空并初始化规则数据
        truncateTable("garbage_rule");
        executeSQL("INSERT INTO garbage_rule (class_name, mapped_category, description, status) VALUES " +
                "('METAL', '可回收物', '金属制品', 1), " +
                "('PLASTIC', '可回收物', '塑料制品', 1), " +
                "('PAPER', '可回收物', '纸张类', 1), " +
                "('GLASS', '可回收物', '玻璃制品', 1), " +
                "('CARDBOARD', '可回收物', '硬纸板', 1), " +
                "('BIODEGRADABLE', '厨余垃圾', '可生物降解垃圾', 1), " +
                "('DISABLED_RULE', '其他垃圾', '禁用规则', 0)");
    }

    /**
     * 测试类别映射
     */
    @Test
    public void testMapCategory() {
        // 测试金属映射
        String metalCategory = ruleService.mapCategory("METAL");
        assertEquals("METAL应映射到可回收物", "可回收物", metalCategory);
        
        // 测试塑料映射
        String plasticCategory = ruleService.mapCategory("PLASTIC");
        assertEquals("PLASTIC应映射到可回收物", "可回收物", plasticCategory);
        
        // 测试厨余垃圾映射
        String bioCategory = ruleService.mapCategory("BIODEGRADABLE");
        assertEquals("BIODEGRADABLE应映射到厨余垃圾", "厨余垃圾", bioCategory);
        
        // 测试禁用的规则
        String disabledCategory = ruleService.mapCategory("DISABLED_RULE");
        assertNull("禁用的规则应返回null", disabledCategory);
        
        // 测试不存在的规则
        String notExistCategory = ruleService.mapCategory("NOTEXIST");
        assertNull("不存在的规则应返回null", notExistCategory);
        
        // 测试null和空字符串
        String nullCategory = ruleService.mapCategory(null);
        assertNull("null应返回null", nullCategory);
        
        String emptyCategory = ruleService.mapCategory("");
        assertNull("空字符串应返回null", emptyCategory);
        
        // 测试前后空格
        String trimmedCategory = ruleService.mapCategory("  METAL  ");
        assertEquals("前后空格应被trim", "可回收物", trimmedCategory);
    }

    /**
     * 测试获取所有规则映射
     */
    @Test
    public void testGetAllRuleMap() {
        Map<String, String> ruleMap = ruleService.getAllRuleMap();
        
        assertNotNull("规则映射不应为null", ruleMap);
        assertEquals("应有6条启用的规则", 6, ruleMap.size());
        
        // 验证映射内容
        assertEquals("METAL应映射到可回收物", "可回收物", ruleMap.get("METAL"));
        assertEquals("PLASTIC应映射到可回收物", "可回收物", ruleMap.get("PLASTIC"));
        assertEquals("BIODEGRADABLE应映射到厨余垃圾", "厨余垃圾", ruleMap.get("BIODEGRADABLE"));
        
        // 验证禁用的规则不在映射中
        assertFalse("禁用的规则不应在映射中", ruleMap.containsKey("DISABLED_RULE"));
    }

    /**
     * 测试查询所有规则
     */
    @Test
    public void testListRules() {
        java.util.List<GarbageRule> rules = ruleService.listRules();
        
        assertNotNull("规则列表不应为null", rules);
        assertEquals("应有7条规则（包括禁用的）", 7, rules.size());
        
        // 验证规则内容
        boolean hasMetal = false;
        boolean hasDisabled = false;
        
        for (GarbageRule rule : rules) {
            if ("METAL".equals(rule.getClassName())) {
                hasMetal = true;
                assertEquals("METAL应映射到可回收物", "可回收物", rule.getMappedCategory());
                assertEquals("METAL应为启用状态", 1, rule.getStatus().intValue());
            }
            if ("DISABLED_RULE".equals(rule.getClassName())) {
                hasDisabled = true;
                assertEquals("DISABLED_RULE应为禁用状态", 0, rule.getStatus().intValue());
            }
        }
        
        assertTrue("应包含METAL规则", hasMetal);
        assertTrue("应包含DISABLED_RULE规则", hasDisabled);
    }

    /**
     * 测试新增规则
     */
    @Test
    public void testSaveRuleInsert() {
        GarbageRule newRule = new GarbageRule();
        newRule.setClassName("NEW_CLASS");
        newRule.setMappedCategory("有害垃圾");
        newRule.setDescription("新增的测试规则");
        newRule.setStatus(1);
        
        // 新增规则
        ruleService.saveRule(newRule);
        
        // 验证新增结果
        String category = ruleService.mapCategory("NEW_CLASS");
        assertEquals("新增的规则应能查询到", "有害垃圾", category);
    }

    /**
     * 测试更新规则
     */
    @Test
    public void testSaveRuleUpdate() {
        // 先查询METAL规则
        java.util.List<GarbageRule> rules = ruleService.listRules();
        GarbageRule metalRule = rules.stream()
                .filter(r -> "METAL".equals(r.getClassName()))
                .findFirst()
                .orElse(null);
        
        assertNotNull("METAL规则应存在", metalRule);
        
        // 修改规则
        metalRule.setMappedCategory("有害垃圾");
        metalRule.setDescription("修改后的描述");
        
        // 更新规则
        ruleService.saveRule(metalRule);
        
        // 验证更新结果
        String category = ruleService.mapCategory("METAL");
        assertEquals("METAL应映射到有害垃圾", "有害垃圾", category);
    }

    /**
     * 测试新增重复规则
     */
    @Test(expected = Exception.class)
    public void testSaveRuleDuplicate() {
        GarbageRule duplicateRule = new GarbageRule();
        duplicateRule.setClassName("METAL"); // 已存在
        duplicateRule.setMappedCategory("其他垃圾");
        duplicateRule.setStatus(1);
        
        ruleService.saveRule(duplicateRule);
        // 应抛出异常
    }

    /**
     * 测试新增规则时className为空
     */
    @Test(expected = Exception.class)
    public void testSaveRuleEmptyClassName() {
        GarbageRule rule = new GarbageRule();
        rule.setClassName("");
        rule.setMappedCategory("其他垃圾");
        
        ruleService.saveRule(rule);
        // 应抛出异常
    }

    /**
     * 测试新增规则时mappedCategory为空
     */
    @Test(expected = Exception.class)
    public void testSaveRuleEmptyMappedCategory() {
        GarbageRule rule = new GarbageRule();
        rule.setClassName("NEW_CLASS");
        rule.setMappedCategory("");
        
        ruleService.saveRule(rule);
        // 应抛出异常
    }
}
