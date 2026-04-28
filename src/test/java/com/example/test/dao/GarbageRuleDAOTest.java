package com.example.test.dao;

import com.example.dao.GarbageRuleDAO;
import com.example.model.GarbageRule;
import com.example.test.BaseTest;
import org.junit.Test;

import java.sql.SQLException;
import java.util.List;

import static org.junit.Assert.*;

/**
 * GarbageRuleDAO 测试类
 * 测试分类规则数据访问层
 */
public class GarbageRuleDAOTest extends BaseTest {

    private GarbageRuleDAO ruleDAO = new GarbageRuleDAO();

    @Override
    protected void initTestData() throws SQLException {
        // 清空规则表
        truncateTable("garbage_rule");
        
        // 插入测试数据
        executeSQL("INSERT INTO garbage_rule (class_name, mapped_category, description, status) VALUES " +
                "('METAL', '可回收物', '金属制品', 1), " +
                "('PLASTIC', '可回收物', '塑料制品', 1), " +
                "('PAPER', '可回收物', '纸张类', 1), " +
                "('GLASS', '可回收物', '玻璃制品', 0)"); // GLASS为禁用状态
    }

    /**
     * 测试查询所有规则
     */
    @Test
    public void testFindAll() {
        List<GarbageRule> rules = ruleDAO.findAll();
        
        assertNotNull("规则列表不应为null", rules);
        assertEquals("应该有4条规则", 4, rules.size());
        
        // 验证第一条规则
        GarbageRule firstRule = rules.get(0);
        assertEquals("第一条规则的className应为METAL", "METAL", firstRule.getClassName());
        assertEquals("第一条规则的mappedCategory应为可回收物", "可回收物", firstRule.getMappedCategory());
    }

    /**
     * 测试查询启用的规则
     */
    @Test
    public void testFindAllEnabled() {
        List<GarbageRule> rules = ruleDAO.findAllEnabled();
        
        assertNotNull("规则列表不应为null", rules);
        assertEquals("应该有3条启用的规则", 3, rules.size());
        
        // 验证所有规则都是启用状态
        for (GarbageRule rule : rules) {
            assertEquals("所有规则的status应为1", 1, rule.getStatus().intValue());
        }
    }

    /**
     * 测试按className查询规则
     */
    @Test
    public void testFindByClassName() {
        // 测试存在的启用规则
        GarbageRule metalRule = ruleDAO.findByClassName("METAL");
        assertNotNull("METAL规则应存在", metalRule);
        assertEquals("METAL规则应映射到可回收物", "可回收物", metalRule.getMappedCategory());
        assertEquals("METAL规则应为启用状态", 1, metalRule.getStatus().intValue());
        
        // 测试禁用的规则
        GarbageRule glassRule = ruleDAO.findByClassName("GLASS");
        assertNull("GLASS规则已禁用，应返回null", glassRule);
        
        // 测试不存在的规则
        GarbageRule notExistRule = ruleDAO.findByClassName("NOTEXIST");
        assertNull("不存在的规则应返回null", notExistRule);
    }

    /**
     * 测试新增规则
     */
    @Test
    public void testInsert() {
        GarbageRule newRule = new GarbageRule();
        newRule.setClassName("BIODEGRADABLE");
        newRule.setMappedCategory("厨余垃圾");
        newRule.setDescription("可生物降解垃圾");
        newRule.setStatus(1);
        
        boolean result = ruleDAO.insert(newRule);
        assertTrue("新增规则应成功", result);
        
        // 验证新增的规则
        GarbageRule insertedRule = ruleDAO.findByClassName("BIODEGRADABLE");
        assertNotNull("新增的规则应能查询到", insertedRule);
        assertEquals("新增规则的mappedCategory应正确", "厨余垃圾", insertedRule.getMappedCategory());
    }

    /**
     * 测试更新规则
     */
    @Test
    public void testUpdate() {
        // 先查询METAL规则
        List<GarbageRule> rules = ruleDAO.findAll();
        GarbageRule metalRule = rules.stream()
                .filter(r -> "METAL".equals(r.getClassName()))
                .findFirst()
                .orElse(null);
        
        assertNotNull("METAL规则应存在", metalRule);
        
        // 修改规则
        metalRule.setMappedCategory("有害垃圾");
        metalRule.setDescription("修改后的描述");
        metalRule.setStatus(0);
        
        boolean result = ruleDAO.update(metalRule);
        assertTrue("更新规则应成功", result);
        
        // 验证更新后的规则（status=0，findByClassName会返回null）
        GarbageRule updatedRule = ruleDAO.findByClassName("METAL");
        assertNull("METAL规则已禁用，应返回null", updatedRule);
        
        // 通过findAll验证
        List<GarbageRule> allRules = ruleDAO.findAll();
        GarbageRule metalInAll = allRules.stream()
                .filter(r -> "METAL".equals(r.getClassName()))
                .findFirst()
                .orElse(null);
        
        assertNotNull("METAL规则应存在", metalInAll);
        assertEquals("METAL规则的mappedCategory应已更新", "有害垃圾", metalInAll.getMappedCategory());
        assertEquals("METAL规则的status应已更新", 0, metalInAll.getStatus().intValue());
    }

    /**
     * 测试重复插入
     */
    @Test
    public void testInsertDuplicate() {
        // 尝试插入已存在的className
        GarbageRule duplicateRule = new GarbageRule();
        duplicateRule.setClassName("METAL"); // 已存在
        duplicateRule.setMappedCategory("其他垃圾");
        duplicateRule.setStatus(1);
        
        boolean result = ruleDAO.insert(duplicateRule);
        // 由于UNIQUE约束，插入应失败，返回false
        assertFalse("重复插入应失败", result);
        
        // 验证原数据未被修改
        GarbageRule originalRule = ruleDAO.findByClassName("METAL");
        assertNotNull("原METAL规则应仍存在", originalRule);
        assertEquals("原METAL规则的mappedCategory应未改变", "可回收物", originalRule.getMappedCategory());
    }
}
