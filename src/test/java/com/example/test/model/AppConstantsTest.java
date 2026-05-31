package com.example.test.model;

import com.example.util.AppConfig;
import com.example.util.AppConstants;
import org.junit.Test;

import static org.junit.Assert.*;

public class AppConstantsTest {

    @Test
    public void testCategoryConstants() {
        assertEquals("可回收物", AppConstants.CATEGORY_RECYCLABLE);
        assertEquals("厨余垃圾", AppConstants.CATEGORY_KITCHEN);
        assertEquals("有害垃圾", AppConstants.CATEGORY_HAZARDOUS);
        assertEquals("其他垃圾", AppConstants.CATEGORY_OTHER);
        assertEquals("混合待分拣", AppConstants.CATEGORY_MIXED);
    }

    @Test
    public void testRecordStatusConstants() {
        assertEquals("PENDING", AppConstants.RECORD_STATUS_PENDING);
        assertEquals("REVIEWED", AppConstants.RECORD_STATUS_REVIEWED);
    }

    @Test
    public void testViolationStatusConstants() {
        assertEquals("PENDING", AppConstants.VIOLATION_STATUS_PENDING);
        assertEquals("RECTIFIED", AppConstants.VIOLATION_STATUS_RECTIFIED);
        assertEquals("IGNORED", AppConstants.VIOLATION_STATUS_IGNORED);
    }

    @Test
    public void testViolationLevelConstants() {
        assertEquals("LOW", AppConstants.VIOLATION_LEVEL_LOW);
        assertEquals("MEDIUM", AppConstants.VIOLATION_LEVEL_MEDIUM);
        assertEquals("HIGH", AppConstants.VIOLATION_LEVEL_HIGH);
    }

    @Test
    public void testRectificationStatusConstants() {
        assertEquals("PENDING", AppConstants.RECT_STATUS_PENDING);
        assertEquals("SUBMITTED", AppConstants.RECT_STATUS_SUBMITTED);
        assertEquals("APPROVED", AppConstants.RECT_STATUS_APPROVED);
        assertEquals("REJECTED", AppConstants.RECT_STATUS_REJECTED);
    }

    @Test
    public void testPaginationConstants() {
        assertEquals(10, AppConstants.DEFAULT_PAGE_SIZE);
        assertEquals(100, AppConstants.MAX_PAGE_SIZE);
        assertEquals(1, AppConstants.DEFAULT_PAGE_NUM);
    }

    @Test
    public void testDjlInferenceUrl() {
        assertTrue("DJL URL应指向8080端口", AppConfig.getDjlInferenceUrl().contains("8080"));
    }

    @Test
    public void testMaxImageSize() {
        assertEquals(10 * 1024 * 1024, AppConstants.MAX_IMAGE_SIZE);
    }
}
