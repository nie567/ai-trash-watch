package com.example.test.util;

import com.example.util.AppContext;
import org.junit.After;
import org.junit.Test;

import static org.junit.Assert.*;

public class AppContextTest {

    @After
    public void cleanup() {
        AppContext.destroy();
    }

    @Test(expected = IllegalStateException.class)
    public void testGetBeforeInitThrows() {
        AppContext.destroy();
        AppContext.get();
    }

    @Test
    public void testInitCreatesInstance() {
        AppContext.init();
        AppContext ctx = AppContext.get();
        assertNotNull(ctx);
    }

    @Test
    public void testGetServicesNonNull() {
        AppContext.init();
        AppContext ctx = AppContext.get();

        assertNotNull(ctx.getUserService());
        assertNotNull(ctx.getOperationLogService());
        assertNotNull(ctx.getRuleService());
        assertNotNull(ctx.getStatisticsService());
        assertNotNull(ctx.getKnowledgeService());
        assertNotNull(ctx.getViolationService());
        assertNotNull(ctx.getRectificationService());
        assertNotNull(ctx.getGarbageRecordService());
        assertNotNull(ctx.getOperationLogDAO());
    }

    @Test(expected = IllegalStateException.class)
    public void testDestroyClearsInstance() {
        AppContext.init();
        AppContext.destroy();
        AppContext.get();
    }

    @Test
    public void testDoubleInitSafe() {
        AppContext.init();
        AppContext.init();
        AppContext ctx = AppContext.get();
        assertNotNull(ctx);
    }
}
