package com.example.service;

import com.example.dao.KnowledgeBaseDAO;
import com.example.model.KnowledgeBase;
import com.example.util.BusinessException;

import java.util.List;

/**
 * 知识库业务服务
 */
public class KnowledgeService {

    private final KnowledgeBaseDAO knowledgeDAO;

    public KnowledgeService() {
        this.knowledgeDAO = new KnowledgeBaseDAO();
    }

    /**
     * 查询所有知识条目
     */
    public List<KnowledgeBase> listAll() {
        return knowledgeDAO.findAll();
    }

    /**
     * 按垃圾类型查询
     */
    public List<KnowledgeBase> listByType(String garbageType) {
        if (garbageType == null || garbageType.trim().isEmpty()) {
            return listAll();
        }
        return knowledgeDAO.findByType(garbageType.trim());
    }

    /**
     * 新增或更新知识条目
     */
    public void save(KnowledgeBase kb) {
        if (kb.getTitle() == null || kb.getTitle().trim().isEmpty()) {
            throw new BusinessException(400, "标题不能为空");
        }
        if (kb.getGarbageType() == null || kb.getGarbageType().trim().isEmpty()) {
            throw new BusinessException(400, "垃圾类型不能为空");
        }

        if (kb.getId() == null) {
            if (!knowledgeDAO.insert(kb)) {
                throw new BusinessException(500, "新增知识条目失败");
            }
        } else {
            if (!knowledgeDAO.update(kb)) {
                throw new BusinessException(500, "更新知识条目失败");
            }
        }
    }

    /**
     * 删除知识条目
     */
    public void delete(Long id) {
        if (id == null) {
            throw new BusinessException(400, "ID不能为空");
        }
        if (!knowledgeDAO.delete(id)) {
            throw new BusinessException(500, "删除知识条目失败");
        }
    }
}
