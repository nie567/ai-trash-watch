package com.example.test.service;

import com.example.dao.KnowledgeBaseDAO;
import com.example.model.KnowledgeBase;
import com.example.service.KnowledgeService;
import com.example.test.BaseTest;
import com.example.util.BusinessException;
import org.junit.Test;

import java.sql.SQLException;
import java.util.List;

import static org.junit.Assert.*;

public class KnowledgeServiceTest extends BaseTest {

    private KnowledgeService knowledgeService = new KnowledgeService();
    private KnowledgeBaseDAO knowledgeDAO = new KnowledgeBaseDAO();

    @Override
    protected void initTestData() throws SQLException {
        truncateTable("knowledge_base");
        executeSQL("INSERT INTO knowledge_base (title, garbage_type, content, image_path, create_time) VALUES " +
                "('塑料分类', '可回收物', '塑料瓶属于可回收物', '/img/p.jpg', NOW()), " +
                "('电池处理', '有害垃圾', '废电池属于有害垃圾', '/img/b.jpg', NOW())");
    }

    @Test
    public void testListAll() {
        List<KnowledgeBase> all = knowledgeService.listAll();
        assertNotNull(all);
        assertEquals(2, all.size());
    }

    @Test
    public void testListByType() {
        List<KnowledgeBase> recyclable = knowledgeService.listByType("可回收物");
        assertEquals(1, recyclable.size());

        List<KnowledgeBase> hazardous = knowledgeService.listByType("有害垃圾");
        assertEquals(1, hazardous.size());
    }

    @Test
    public void testListByTypeNullReturnsAll() {
        List<KnowledgeBase> all = knowledgeService.listByType(null);
        assertEquals(2, all.size());

        List<KnowledgeBase> allEmpty = knowledgeService.listByType("");
        assertEquals(2, allEmpty.size());
    }

    @Test
    public void testSaveNew() {
        KnowledgeBase kb = new KnowledgeBase();
        kb.setTitle("纸张分类");
        kb.setGarbageType("可回收物");
        kb.setContent("纸箱属于可回收物");

        knowledgeService.save(kb);

        List<KnowledgeBase> all = knowledgeService.listAll();
        assertEquals(3, all.size());
    }

    @Test
    public void testSaveUpdate() {
        List<KnowledgeBase> all = knowledgeService.listAll();
        KnowledgeBase first = all.get(0);

        first.setTitle("更新标题");
        first.setContent("更新内容");

        knowledgeService.save(first);

        KnowledgeBase updated = knowledgeDAO.findAll().stream()
                .filter(k -> k.getId().equals(first.getId()))
                .findFirst().orElse(null);
        assertNotNull(updated);
        assertEquals("更新标题", updated.getTitle());
    }

    @Test
    public void testSaveEmptyTitle() {
        try {
            KnowledgeBase kb = new KnowledgeBase();
            kb.setTitle("");
            kb.setGarbageType("可回收物");
            knowledgeService.save(kb);
            fail("应抛出BusinessException");
        } catch (BusinessException e) {
            assertEquals(400, e.getCode());
        }
    }

    @Test
    public void testSaveEmptyGarbageType() {
        try {
            KnowledgeBase kb = new KnowledgeBase();
            kb.setTitle("测试");
            kb.setGarbageType("");
            knowledgeService.save(kb);
            fail("应抛出BusinessException");
        } catch (BusinessException e) {
            assertEquals(400, e.getCode());
        }
    }

    @Test
    public void testDelete() {
        List<KnowledgeBase> all = knowledgeService.listAll();
        Long id = all.get(0).getId();

        knowledgeService.delete(id);

        List<KnowledgeBase> after = knowledgeService.listAll();
        assertEquals(1, after.size());
    }

    @Test
    public void testDeleteNullId() {
        try {
            knowledgeService.delete(null);
            fail("应抛出BusinessException");
        } catch (BusinessException e) {
            assertEquals(400, e.getCode());
        }
    }
}
