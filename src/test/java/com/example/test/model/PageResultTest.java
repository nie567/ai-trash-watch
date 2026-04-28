package com.example.test.model;

import com.example.model.PageResult;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.*;

public class PageResultTest {

    @Test
    public void testConstructor() {
        PageResult<String> pr = new PageResult<>(Arrays.asList("a", "b", "c"), 30, 1, 10);
        assertEquals(3, pr.getData().size());
        assertEquals(30, pr.getTotal());
        assertEquals(1, pr.getPage());
        assertEquals(10, pr.getSize());
        assertEquals(3, pr.getTotalPages());
    }

    @Test
    public void testTotalPagesCeiling() {
        PageResult<String> pr = new PageResult<>(Collections.emptyList(), 25, 1, 10);
        assertEquals(3, pr.getTotalPages());

        PageResult<String> pr2 = new PageResult<>(Collections.emptyList(), 20, 1, 10);
        assertEquals(2, pr2.getTotalPages());

        PageResult<String> pr3 = new PageResult<>(Collections.emptyList(), 0, 1, 10);
        assertEquals(0, pr3.getTotalPages());
    }

    @Test
    public void testHasNextAndPrevious() {
        PageResult<String> pr = new PageResult<>(Collections.emptyList(), 30, 1, 10);
        assertTrue("第1页应有下一页", pr.hasNext());
        assertFalse("第1页不应有上一页", pr.hasPrevious());

        PageResult<String> pr2 = new PageResult<>(Collections.emptyList(), 30, 3, 10);
        assertFalse("第3页不应有下一页", pr2.hasNext());
        assertTrue("第3页应有上一页", pr2.hasPrevious());

        PageResult<String> pr3 = new PageResult<>(Collections.emptyList(), 30, 2, 10);
        assertTrue("第2页应有下一页", pr3.hasNext());
        assertTrue("第2页应有上一页", pr3.hasPrevious());
    }

    @Test
    public void testDefaultConstructor() {
        PageResult<String> pr = new PageResult<>();
        assertNull(pr.getData());
        assertEquals(0, pr.getTotal());
        assertEquals(0, pr.getPage());
    }

    @Test
    public void testSetters() {
        PageResult<String> pr = new PageResult<>();
        pr.setData(Arrays.asList("x"));
        pr.setTotal(1);
        pr.setPage(2);
        pr.setSize(5);
        pr.setTotalPages(1);

        assertEquals(1, pr.getData().size());
        assertEquals(1, pr.getTotal());
        assertEquals(2, pr.getPage());
        assertEquals(5, pr.getSize());
        assertEquals(1, pr.getTotalPages());
    }
}
