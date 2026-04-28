package com.example.service;

import com.example.dao.GarbageRuleDAO;
import com.example.model.GarbageRule;
import com.example.util.BusinessException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 分类规则业务服务
 */
public class RuleService {

    private final GarbageRuleDAO ruleDAO;

    public RuleService() {
        this.ruleDAO = new GarbageRuleDAO();
    }

    /**
     * 根据className查询规则映射，返回mappedCategory
     * 若规则不存在或禁用，返回null
     */
    public String mapCategory(String className) {
        if (className == null || className.trim().isEmpty()) {
            return null;
        }
        GarbageRule rule = ruleDAO.findByClassName(className.trim());
        if (rule == null || rule.getStatus() == 0) {
            return null;
        }
        return rule.getMappedCategory();
    }

    /**
     * 获取所有启用规则的className→mappedCategory映射
     */
    public Map<String, String> getAllRuleMap() {
        List<GarbageRule> rules = ruleDAO.findAllEnabled();
        Map<String, String> map = new HashMap<>();
        for (GarbageRule rule : rules) {
            map.put(rule.getClassName(), rule.getMappedCategory());
        }
        return map;
    }

    /**
     * 查询所有规则列表
     */
    public List<GarbageRule> listRules() {
        return ruleDAO.findAll();
    }

    /**
     * 新增或更新规则
     * id为空则insert，否则update
     */
    public void saveRule(GarbageRule rule) {
        if (rule.getClassName() == null || rule.getClassName().trim().isEmpty()) {
            throw new BusinessException(400, "检测类别不能为空");
        }
        if (rule.getMappedCategory() == null || rule.getMappedCategory().trim().isEmpty()) {
            throw new BusinessException(400, "映射类别不能为空");
        }

        if (rule.getId() == null) {
            // 新增：检查className唯一性
            GarbageRule existing = ruleDAO.findByClassName(rule.getClassName().trim());
            if (existing != null) {
                throw new BusinessException(400, "该检测类别的规则已存在");
            }
            rule.setClassName(rule.getClassName().trim());
            rule.setMappedCategory(rule.getMappedCategory().trim());
            if (!ruleDAO.insert(rule)) {
                throw new BusinessException(500, "新增规则失败");
            }
        } else {
            // 更新
            rule.setClassName(rule.getClassName().trim());
            rule.setMappedCategory(rule.getMappedCategory().trim());
            if (!ruleDAO.update(rule)) {
                throw new BusinessException(500, "更新规则失败");
            }
        }
    }
}
