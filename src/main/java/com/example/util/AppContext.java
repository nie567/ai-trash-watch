package com.example.util;

import com.example.dao.*;
import com.example.service.*;

public class AppContext {

    private final UserDAO userDAO;
    private final OperationLogDAO operationLogDAO;
    private final GarbageRecordDAO garbageRecordDAO;
    private final DetectionResultDAO detectionResultDAO;
    private final GarbageRuleDAO garbageRuleDAO;
    private final ViolationRecordDAO violationRecordDAO;
    private final RectificationTaskDAO rectificationTaskDAO;
    private final StatisticsDAO statisticsDAO;
    private final KnowledgeBaseDAO knowledgeBaseDAO;

    private final UserService userService;
    private final OperationLogService operationLogService;
    private final RuleService ruleService;
    private final StatisticsService statisticsService;
    private final KnowledgeService knowledgeService;
    private final ViolationService violationService;
    private final RectificationService rectificationService;
    private final GarbageRecordService garbageRecordService;

    private static volatile AppContext instance;

    private AppContext() {
        userDAO = new UserDAO();
        operationLogDAO = new OperationLogDAO();
        garbageRecordDAO = new GarbageRecordDAO();
        detectionResultDAO = new DetectionResultDAO();
        garbageRuleDAO = new GarbageRuleDAO();
        violationRecordDAO = new ViolationRecordDAO();
        rectificationTaskDAO = new RectificationTaskDAO();
        statisticsDAO = new StatisticsDAO();
        knowledgeBaseDAO = new KnowledgeBaseDAO();

        operationLogService = new OperationLogService(operationLogDAO);
        userService = new UserService(userDAO, operationLogDAO);
        ruleService = new RuleService(garbageRuleDAO);
        statisticsService = new StatisticsService(statisticsDAO);
        knowledgeService = new KnowledgeService(knowledgeBaseDAO);
        violationService = new ViolationService(violationRecordDAO, rectificationTaskDAO);
        rectificationService = new RectificationService(rectificationTaskDAO, violationRecordDAO);
        garbageRecordService = new GarbageRecordService(garbageRecordDAO, detectionResultDAO,
                violationRecordDAO, rectificationTaskDAO, violationService);
    }

    public static void init() {
        if (instance == null) {
            instance = new AppContext();
        }
    }

    public static AppContext get() {
        if (instance == null) {
            throw new IllegalStateException("AppContext not initialized");
        }
        return instance;
    }

    public static void destroy() {
        instance = null;
    }

    public UserService getUserService() { return userService; }
    public OperationLogService getOperationLogService() { return operationLogService; }
    public RuleService getRuleService() { return ruleService; }
    public StatisticsService getStatisticsService() { return statisticsService; }
    public KnowledgeService getKnowledgeService() { return knowledgeService; }
    public ViolationService getViolationService() { return violationService; }
    public RectificationService getRectificationService() { return rectificationService; }
    public GarbageRecordService getGarbageRecordService() { return garbageRecordService; }
    public OperationLogDAO getOperationLogDAO() { return operationLogDAO; }
}
