package com.example.test.model;

import com.example.model.GarbageRecord;
import com.example.model.ViolationRecord;
import com.example.model.RectificationTask;
import com.example.model.DetectionResult;
import com.example.model.KnowledgeBase;
import com.example.model.GarbageRule;
import com.example.model.User;
import org.junit.Test;

import static org.junit.Assert.*;

public class ModelTest {

    @Test
    public void testGarbageRecordGettersSetters() {
        GarbageRecord r = new GarbageRecord();
        r.setId(1L);
        r.setUserId(2L);
        r.setImageName("test.jpg");
        r.setImagePath("/input/test.jpg");
        r.setResultImagePath("/output/test.jpg");
        r.setDetectedSummary("METAL, PLASTIC");
        r.setRecommendedCategory("可回收物");
        r.setSelectedCategory("可回收物");
        r.setFinalCategory("可回收物");
        r.setIsMixed(0);
        r.setIsCorrect(1);
        r.setStatus("PENDING");
        r.setReviewComment("通过");
        r.setRemark("备注");

        assertEquals(Long.valueOf(1L), r.getId());
        assertEquals(Long.valueOf(2L), r.getUserId());
        assertEquals("test.jpg", r.getImageName());
        assertEquals("/input/test.jpg", r.getImagePath());
        assertEquals("/output/test.jpg", r.getResultImagePath());
        assertEquals("METAL, PLASTIC", r.getDetectedSummary());
        assertEquals("可回收物", r.getRecommendedCategory());
        assertEquals("可回收物", r.getSelectedCategory());
        assertEquals("可回收物", r.getFinalCategory());
        assertEquals(Integer.valueOf(0), r.getIsMixed());
        assertEquals(Integer.valueOf(1), r.getIsCorrect());
        assertEquals("PENDING", r.getStatus());
        assertEquals("通过", r.getReviewComment());
        assertEquals("备注", r.getRemark());
    }

    @Test
    public void testViolationRecordGettersSetters() {
        ViolationRecord v = new ViolationRecord();
        v.setId(1L);
        v.setRecordId(2L);
        v.setUserId(3L);
        v.setViolationType("分类错误");
        v.setDescription("描述");
        v.setLevel("LOW");
        v.setStatus("PENDING");

        assertEquals(Long.valueOf(1L), v.getId());
        assertEquals(Long.valueOf(2L), v.getRecordId());
        assertEquals(Long.valueOf(3L), v.getUserId());
        assertEquals("分类错误", v.getViolationType());
        assertEquals("描述", v.getDescription());
        assertEquals("LOW", v.getLevel());
        assertEquals("PENDING", v.getStatus());
    }

    @Test
    public void testRectificationTaskGettersSetters() {
        RectificationTask t = new RectificationTask();
        t.setId(1L);
        t.setViolationId(2L);
        t.setUserId(3L);
        t.setRequirement("整改要求");
        t.setStatus("PENDING");
        t.setSubmitDesc("提交说明");
        t.setSubmitImagePath("/path");
        t.setReviewResult("APPROVED");
        t.setReviewComment("通过");

        assertEquals(Long.valueOf(1L), t.getId());
        assertEquals(Long.valueOf(2L), t.getViolationId());
        assertEquals(Long.valueOf(3L), t.getUserId());
        assertEquals("整改要求", t.getRequirement());
        assertEquals("PENDING", t.getStatus());
        assertEquals("提交说明", t.getSubmitDesc());
        assertEquals("/path", t.getSubmitImagePath());
        assertEquals("APPROVED", t.getReviewResult());
        assertEquals("通过", t.getReviewComment());
    }

    @Test
    public void testDetectionResultGettersSetters() {
        DetectionResult d = new DetectionResult();
        d.setId(1L);
        d.setRecordId(2L);
        d.setClassName("METAL");
        d.setConfidence(0.95);
        d.setXMin(10);
        d.setYMin(20);
        d.setXMax(100);
        d.setYMax(200);
        d.setMappedCategory("可回收物");

        assertEquals(Long.valueOf(1L), d.getId());
        assertEquals(Long.valueOf(2L), d.getRecordId());
        assertEquals("METAL", d.getClassName());
        assertEquals(Double.valueOf(0.95), d.getConfidence());
        assertEquals(Integer.valueOf(10), d.getXMin());
        assertEquals(Integer.valueOf(20), d.getYMin());
        assertEquals(Integer.valueOf(100), d.getXMax());
        assertEquals(Integer.valueOf(200), d.getYMax());
        assertEquals("可回收物", d.getMappedCategory());
    }

    @Test
    public void testKnowledgeBaseGettersSetters() {
        KnowledgeBase kb = new KnowledgeBase();
        kb.setId(1L);
        kb.setTitle("标题");
        kb.setGarbageType("可回收物");
        kb.setContent("内容");
        kb.setImagePath("/img.jpg");

        assertEquals(Long.valueOf(1L), kb.getId());
        assertEquals("标题", kb.getTitle());
        assertEquals("可回收物", kb.getGarbageType());
        assertEquals("内容", kb.getContent());
        assertEquals("/img.jpg", kb.getImagePath());
    }

    @Test
    public void testGarbageRuleGettersSetters() {
        GarbageRule rule = new GarbageRule();
        rule.setId(1L);
        rule.setClassName("METAL");
        rule.setMappedCategory("可回收物");
        rule.setDescription("金属");
        rule.setStatus(1);

        assertEquals(Long.valueOf(1L), rule.getId());
        assertEquals("METAL", rule.getClassName());
        assertEquals("可回收物", rule.getMappedCategory());
        assertEquals("金属", rule.getDescription());
        assertEquals(Integer.valueOf(1), rule.getStatus());
    }

    @Test
    public void testUserGettersSetters() {
        User u = new User();
        u.setId(1);
        u.setUsername("test");
        u.setEmail("test@test.com");
        u.setPhone("13800000000");
        u.setRole("user");
        u.setStatus(1);

        assertEquals(1, (long) u.getId());
        assertEquals("test", u.getUsername());
        assertEquals("test@test.com", u.getEmail());
        assertEquals("13800000000", u.getPhone());
        assertEquals("user", u.getRole());
        assertEquals(1, (long) u.getStatus());
    }
}
