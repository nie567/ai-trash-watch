package com.example.test.dao;

import com.example.dao.DetectionResultDAO;
import com.example.model.DetectionResult;
import com.example.test.BaseTest;
import org.junit.Test;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class DetectionResultDAOTest extends BaseTest {

    private DetectionResultDAO detectionDAO = new DetectionResultDAO();

    @Override
    protected void initTestData() throws SQLException {
        truncateTable("detection_result");
        executeSQL("INSERT INTO detection_result (record_id, class_name, confidence, x_min, y_min, x_max, y_max, mapped_category, create_time) VALUES " +
                "(1, 'METAL', 0.95, 10, 20, 100, 200, '可回收物', NOW()), " +
                "(1, 'PLASTIC', 0.85, 30, 40, 150, 250, '可回收物', NOW()), " +
                "(2, 'BIODEGRADABLE', 0.90, 50, 60, 170, 270, '厨余垃圾', NOW())");
    }

    @Test
    public void testBatchInsert() {
        List<DetectionResult> list = new ArrayList<>();
        DetectionResult dr1 = new DetectionResult();
        dr1.setRecordId(3L);
        dr1.setClassName("GLASS");
        dr1.setConfidence(0.88);
        dr1.setXMin(10);
        dr1.setYMin(20);
        dr1.setXMax(100);
        dr1.setYMax(200);
        dr1.setMappedCategory("可回收物");
        list.add(dr1);

        DetectionResult dr2 = new DetectionResult();
        dr2.setRecordId(3L);
        dr2.setClassName("PAPER");
        dr2.setConfidence(0.75);
        dr2.setXMin(30);
        dr2.setYMin(40);
        dr2.setXMax(120);
        dr2.setYMax(220);
        dr2.setMappedCategory("可回收物");
        list.add(dr2);

        detectionDAO.batchInsert(list);

        List<DetectionResult> results = detectionDAO.findByRecordId(3L);
        assertEquals("应有2条检测明细", 2, results.size());
    }

    @Test
    public void testBatchInsertEmpty() {
        detectionDAO.batchInsert(null);
        detectionDAO.batchInsert(new ArrayList<>());
    }

    @Test
    public void testFindByRecordId() {
        List<DetectionResult> record1 = detectionDAO.findByRecordId(1L);
        assertEquals("recordId=1应有2条", 2, record1.size());

        List<DetectionResult> record2 = detectionDAO.findByRecordId(2L);
        assertEquals("recordId=2应有1条", 1, record2.size());
        assertEquals("BIODEGRADABLE", record2.get(0).getClassName());

        List<DetectionResult> record99 = detectionDAO.findByRecordId(99L);
        assertTrue("不存在的recordId应返回空列表", record99.isEmpty());
    }

    @Test
    public void testDeleteByRecordId() {
        List<DetectionResult> before = detectionDAO.findByRecordId(1L);
        assertEquals(2, before.size());

        assertTrue("删除应成功", detectionDAO.deleteByRecordId(1L));

        List<DetectionResult> after = detectionDAO.findByRecordId(1L);
        assertTrue("删除后应为空", after.isEmpty());
    }

    @Test
    public void testBatchInsertWithNullValues() {
        List<DetectionResult> list = new ArrayList<>();
        DetectionResult dr = new DetectionResult();
        dr.setRecordId(4L);
        dr.setClassName("UNKNOWN");
        dr.setMappedCategory("其他垃圾");
        list.add(dr);

        detectionDAO.batchInsert(list);

        List<DetectionResult> results = detectionDAO.findByRecordId(4L);
        assertEquals(1, results.size());
        assertEquals("UNKNOWN", results.get(0).getClassName());
    }
}
