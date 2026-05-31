package com.example.filter;

import com.example.util.AppContext;
import com.example.util.DBUtil;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebListener
public class AppContextListener implements ServletContextListener {

    private static final Logger logger = LoggerFactory.getLogger(AppContextListener.class);

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        long t0 = System.currentTimeMillis();
        logger.info("Initializing AppContext...");
        AppContext.init();
        long t1 = System.currentTimeMillis();
        logger.info("AppContext initialized successfully ({}ms)", t1 - t0);

        // 预热数据库连接池，避免首次请求卡顿
        try {
            logger.info("Pre-warming HikariCP connection pool...");
            long t2 = System.currentTimeMillis();
            DBUtil.warmUp();
            long t3 = System.currentTimeMillis();
            logger.info("HikariCP connection pool ready ({}ms)", t3 - t2);
        } catch (Exception e) {
            logger.warn("HikariCP warm-up failed (will initialize on first request): {}", e.getMessage());
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        logger.info("Destroying AppContext...");
        AppContext.destroy();
        logger.info("AppContext destroyed");
    }
}
