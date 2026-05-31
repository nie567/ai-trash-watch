package com.example.service;

import com.example.dao.OperationLogDAO;
import com.example.model.OperationLog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OperationLogService {

    private static final Logger logger = LoggerFactory.getLogger(OperationLogService.class);

    private final OperationLogDAO logDAO;

    public OperationLogService() {
        this.logDAO = new OperationLogDAO();
    }

    public OperationLogService(OperationLogDAO logDAO) {
        this.logDAO = logDAO;
    }

    public void log(Integer userId, String username, String action, String target, String detail, String ip) {
        try {
            OperationLog log = new OperationLog(userId, username, action, target, detail, ip);
            logDAO.insert(log);
        } catch (Exception e) {
            logger.warn("记录操作日志失败", e);
        }
    }
}
