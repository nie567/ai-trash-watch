package com.example.test.integration;

import com.example.dao.GarbageRecordDAO;
import com.example.dao.GarbageRuleDAO;
import com.example.dao.ViolationRecordDAO;
import com.example.model.GarbageRecord;
import com.example.model.GarbageRule;
import com.example.model.ViolationRecord;
import com.example.service.RuleService;
import com.example.test.BaseTest;
import com.example.util.AppConstants;
import org.junit.Test;

import java.sql.SQLException;
import java.util.List;

import static org.junit.Assert.*;

/**
 * 垃圾投放流程集成测试
 * 测试完整的投放、检测、违规判定流程
 */
public class GarbageDisposalIntegrationTest extends BaseTest {

    private GarbageRuleDAO ruleDAO = new GarbageRuleDAO();
    private GarbageRecordDAO recordDAO = new GarbageRecordDAO();
    private ViolationRecordDAO violationDAO = new ViolationRecordDAO();
    private RuleService ruleService = new RuleService();

    @Override
    protected void initTestData() throws SQLException {
        // 清空所有表
        truncateTable("garbage_rule");
        truncateTable("garbage_record");
        truncateTable("violation_record");
        truncateTable("detection_result");
        
        // 初始化用户
        executeSQL("INSERT IGNORE INTO user (id, username, password_hash, role, status) VALUES " +
                "(1, 'testuser', 'hash', 'user', 1)");
        
        // 初始化分类规则
        executeSQL("INSERT INTO garbage_rule (class_name, mapped_category, description, status) VALUES " +
                "('METAL', '可回收物', '金属制品', 1), " +
                "('PLASTIC', '可回收物', '塑料制品', 1), " +
                "('BIODEGRADABLE', '厨余垃圾', '可生物降解垃圾', 1)");
    }

    /**
     * 测试完整的正确投放流程
     */
    @Test
    public void testCorrectDisposalFlow() {
        // 1. 用户上传图片，系统检测到METAL
        String detectedClass = "METAL";
        String recommendedCategory = ruleService.mapCategory(detectedClass);
        assertEquals("METAL应推荐为可回收物", "可回收物", recommendedCategory);
        
        // 2. 用户选择正确的投放类别
        String selectedCategory = "可回收物";
        
        // 3. 创建投放记录
        GarbageRecord record = new GarbageRecord();
        record.setUserId(1L);
        record.setImageName("metal.jpg");
        record.setImagePath("/input/metal.jpg");
        record.setDetectedSummary(detectedClass);
        record.setRecommendedCategory(recommendedCategory);
        record.setSelectedCategory(selectedCategory);
        record.setFinalCategory(selectedCategory);
        record.setIsMixed(0);
        record.setIsCorrect(recommendedCategory.equals(selectedCategory) ? 1 : 0);
        record.setStatus("PENDING");
        
        Long recordId = recordDAO.insert(record);
        assertNotNull("投放记录应创建成功", recordId);
        
        // 4. 验证投放记录
        GarbageRecord savedRecord = recordDAO.findById(recordId);
        assertNotNull("投放记录应存在", savedRecord);
        assertEquals("投放应判定为正确", Integer.valueOf(1), savedRecord.getIsCorrect());
        
        // 5. 验证没有生成违规记录
        List<ViolationRecord> violations = violationDAO.findByUserId(1L, 0, 10);
        assertEquals("正确投放不应生成违规记录", 0, violations.size());
    }

    /**
     * 测试完整的错误投放流程（应生成违规记录）
     */
    @Test
    public void testIncorrectDisposalFlow() {
        // 1. 用户上传图片，系统检测到BIODEGRADABLE
        String detectedClass = "BIODEGRADABLE";
        String recommendedCategory = ruleService.mapCategory(detectedClass);
        assertEquals("BIODEGRADABLE应推荐为厨余垃圾", "厨余垃圾", recommendedCategory);
        
        // 2. 用户选择错误的投放类别
        String selectedCategory = "其他垃圾";
        
        // 3. 创建投放记录
        GarbageRecord record = new GarbageRecord();
        record.setUserId(1L);
        record.setImageName("bio.jpg");
        record.setImagePath("/input/bio.jpg");
        record.setDetectedSummary(detectedClass);
        record.setRecommendedCategory(recommendedCategory);
        record.setSelectedCategory(selectedCategory);
        record.setFinalCategory(selectedCategory);
        record.setIsMixed(0);
        record.setIsCorrect(recommendedCategory.equals(selectedCategory) ? 1 : 0);
        record.setStatus("PENDING");
        
        Long recordId = recordDAO.insert(record);
        assertNotNull("投放记录应创建成功", recordId);
        
        // 4. 验证投放记录
        GarbageRecord savedRecord = recordDAO.findById(recordId);
        assertNotNull("投放记录应存在", savedRecord);
        assertEquals("投放应判定为错误", Integer.valueOf(0), savedRecord.getIsCorrect());
        
        // 5. 自动生成违规记录
        ViolationRecord violation = new ViolationRecord();
        violation.setRecordId(recordId);
        violation.setUserId(1L);
        violation.setViolationType("错误投放");
        violation.setDescription("推荐类别: " + recommendedCategory + ", 实际投放: " + selectedCategory);
        violation.setLevel("MEDIUM");
        violation.setStatus("PENDING");
        
        Long violationId = violationDAO.insert(violation);
        assertNotNull("违规记录应创建成功", violationId);
        
        // 6. 验证违规记录
        List<ViolationRecord> violations = violationDAO.findByUserId(1L, 0, 10);
        assertEquals("应有1条违规记录", 1, violations.size());
        
        ViolationRecord savedViolation = violations.get(0);
        assertEquals("违规类型应为错误投放", "错误投放", savedViolation.getViolationType());
        assertEquals("违规状态应为PENDING", "PENDING", savedViolation.getStatus());
    }

    /**
     * 测试混合投放流程
     */
    @Test
    public void testMixedDisposalFlow() {
        // 1. 用户上传图片，系统检测到多种类别
        String[] detectedClasses = {"METAL", "PLASTIC", "BIODEGRADABLE"};
        
        // 2. 计算推荐类别
        java.util.Set<String> categories = new java.util.LinkedHashSet<>();
        for (String className : detectedClasses) {
            String category = ruleService.mapCategory(className);
            if (category != null) {
                categories.add(category);
            }
        }
        
        String recommendedCategory;
        int isMixed;
        if (categories.size() > 1) {
            recommendedCategory = AppConstants.CATEGORY_MIXED;
            isMixed = 1;
        } else {
            recommendedCategory = categories.iterator().next();
            isMixed = 0;
        }
        
        assertEquals("应推荐为混合待分拣", "混合待分拣", recommendedCategory);
        assertEquals("应标记为混合投放", 1, isMixed);
        
        // 3. 创建投放记录
        GarbageRecord record = new GarbageRecord();
        record.setUserId(1L);
        record.setImageName("mixed.jpg");
        record.setImagePath("/input/mixed.jpg");
        record.setDetectedSummary(String.join(", ", detectedClasses));
        record.setRecommendedCategory(recommendedCategory);
        record.setSelectedCategory("混合待分拣");
        record.setFinalCategory("混合待分拣");
        record.setIsMixed(isMixed);
        record.setIsCorrect(1);
        record.setStatus("PENDING");
        
        Long recordId = recordDAO.insert(record);
        assertNotNull("投放记录应创建成功", recordId);
        
        // 4. 验证投放记录
        GarbageRecord savedRecord = recordDAO.findById(recordId);
        assertNotNull("投放记录应存在", savedRecord);
        assertEquals("应标记为混合投放", Integer.valueOf(1), savedRecord.getIsMixed());
    }

    /**
     * 测试规则变更对历史记录的影响
     */
    @Test
    public void testRuleChangeImpact() {
        // 1. 创建投放记录（METAL -> 可回收物）
        GarbageRecord record = new GarbageRecord();
        record.setUserId(1L);
        record.setImageName("metal.jpg");
        record.setImagePath("/input/metal.jpg");
        record.setDetectedSummary("METAL");
        record.setRecommendedCategory("可回收物");
        record.setSelectedCategory("可回收物");
        record.setFinalCategory("可回收物");
        record.setIsMixed(0);
        record.setIsCorrect(1);
        record.setStatus("REVIEWED");
        
        Long recordId = recordDAO.insert(record);
        assertNotNull("投放记录应创建成功", recordId);
        
        // 2. 修改规则（METAL -> 有害垃圾）
        List<GarbageRule> rules = ruleDAO.findAll();
        GarbageRule metalRule = rules.stream()
                .filter(r -> "METAL".equals(r.getClassName()))
                .findFirst()
                .orElse(null);
        
        assertNotNull("METAL规则应存在", metalRule);
        metalRule.setMappedCategory("有害垃圾");
        ruleDAO.update(metalRule);
        
        // 3. 验证规则已更新
        String newCategory = ruleService.mapCategory("METAL");
        assertEquals("METAL应映射到有害垃圾", "有害垃圾", newCategory);
        
        // 4. 验证历史记录不受影响
        GarbageRecord savedRecord = recordDAO.findById(recordId);
        assertNotNull("历史记录应存在", savedRecord);
        assertEquals("历史记录的推荐类别应不变", "可回收物", savedRecord.getRecommendedCategory());
        assertEquals("历史记录的最终类别应不变", "可回收物", savedRecord.getFinalCategory());
    }
}
