package com.example.test.dao;

import com.example.dao.KnowledgeBaseDAO;
import com.example.model.KnowledgeBase;
import com.example.test.BaseTest;
import org.junit.Test;

import java.sql.SQLException;
import java.util.List;

import static org.junit.Assert.*;

public class KnowledgeBaseDAOTest extends BaseTest {

    private KnowledgeBaseDAO knowledgeDAO = new KnowledgeBaseDAO();

    @Override
    protected void initTestData() throws SQLException {
        truncateTable("knowledge_base");
        executeSQL("INSERT INTO knowledge_base (title, garbage_type, content, image_path, create_time) VALUES " +
                "('塑料分类指南', '可回收物', '塑料瓶属于可回收物', '/img/plastic.jpg', NOW()), " +
                "('金属分类指南', '可回收物', '金属罐属于可回收物', '/img/metal.jpg', NOW()), " +
                "('剩菜剩饭处理', '厨余垃圾', '剩菜属于厨余垃圾', '/img/food.jpg', NOW()), " +
                "('电池处理', '有害垃圾', '废电池属于有害垃圾', '/img/battery.jpg', NOW())");
    }

    @Test
    public void testFindAll() {
        List<KnowledgeBase> all = knowledgeDAO.findAll();
        assertNotNull(all);
        assertEquals(4, all.size());
    }

    @Test
    public void testFindByType() {
        List<KnowledgeBase> recyclable = knowledgeDAO.findByType("可回收物");
        assertEquals(2, recyclable.size());

        List<KnowledgeBase> kitchen = knowledgeDAO.findByType("厨余垃圾");
        assertEquals(1, kitchen.size());

        List<KnowledgeBase> hazardous = knowledgeDAO.findByType("有害垃圾");
        assertEquals(1, hazardous.size());

        List<KnowledgeBase> other = knowledgeDAO.findByType("其他垃圾");
        assertTrue("不存在的类型应返回空", other.isEmpty());
    }

    @Test
    public void testInsert() {
        KnowledgeBase kb = new KnowledgeBase();
        kb.setTitle("纸张分类");
        kb.setGarbageType("可回收物");
        kb.setContent("纸箱属于可回收物");
        kb.setImagePath("/img/paper.jpg");

        assertTrue("插入应成功", knowledgeDAO.insert(kb));

        List<KnowledgeBase> all = knowledgeDAO.findAll();
        assertEquals(5, all.size());
    }

    @Test
    public void testUpdate() {
        List<KnowledgeBase> all = knowledgeDAO.findAll();
        KnowledgeBase first = all.get(0);

        first.setTitle("更新后的标题");
        first.setContent("更新后的内容");

        assertTrue("更新应成功", knowledgeDAO.update(first));

        List<KnowledgeBase> updated = knowledgeDAO.findAll();
        KnowledgeBase found = updated.stream()
                .filter(k -> k.getId().equals(first.getId()))
                .findFirst().orElse(null);
        assertNotNull(found);
        assertEquals("更新后的标题", found.getTitle());
        assertEquals("更新后的内容", found.getContent());
    }

    @Test
    public void testDelete() {
        List<KnowledgeBase> all = knowledgeDAO.findAll();
        Long id = all.get(0).getId();

        assertTrue("删除应成功", knowledgeDAO.delete(id));

        List<KnowledgeBase> after = knowledgeDAO.findAll();
        assertEquals(3, after.size());

        assertFalse("重复删除应返回false", knowledgeDAO.delete(id));
    }

    @Test
    public void testDeleteNonExistent() {
        assertFalse("删除不存在的ID应返回false", knowledgeDAO.delete(99999L));
    }
}
