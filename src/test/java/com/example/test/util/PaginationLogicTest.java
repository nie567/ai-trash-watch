package com.example.test.util;

import com.example.service.ViolationService;
import com.example.util.AppConstants;
import org.junit.Test;

import static org.junit.Assert.*;

public class PaginationLogicTest {

    @Test
    public void testPageLessThan1() {
        ViolationService service = new ViolationService();

        com.example.model.PageResult<?> result0 = service.getUserViolations(1L, 0, 10);
        assertEquals("page=0应默认为1", AppConstants.DEFAULT_PAGE_NUM, result0.getPage());

        com.example.model.PageResult<?> resultNeg = service.getUserViolations(1L, -1, 10);
        assertEquals("page=-1应默认为1", AppConstants.DEFAULT_PAGE_NUM, resultNeg.getPage());
    }

    @Test
    public void testPageSizeLessThan1() {
        ViolationService service = new ViolationService();

        com.example.model.PageResult<?> result0 = service.getUserViolations(1L, 1, 0);
        assertEquals("pageSize=0应默认为" + AppConstants.DEFAULT_PAGE_SIZE,
                AppConstants.DEFAULT_PAGE_SIZE, result0.getSize());

        com.example.model.PageResult<?> resultNeg = service.getUserViolations(1L, 1, -5);
        assertEquals("pageSize=-5应默认为" + AppConstants.DEFAULT_PAGE_SIZE,
                AppConstants.DEFAULT_PAGE_SIZE, resultNeg.getSize());
    }

    @Test
    public void testPageSizeExceedsMax() {
        ViolationService service = new ViolationService();

        com.example.model.PageResult<?> result = service.getUserViolations(1L, 1, 200);
        assertEquals("pageSize=200应被限制为" + AppConstants.MAX_PAGE_SIZE,
                AppConstants.MAX_PAGE_SIZE, result.getSize());
    }
}
