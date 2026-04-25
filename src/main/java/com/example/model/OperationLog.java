package com.example.model;

import java.sql.Timestamp;

/**
 * 操作日志实体类
 */
public class OperationLog {
    // 操作类型常量（与 AppConstants 保持一致）
    public static final String ACTION_LOGIN = "LOGIN";
    public static final String ACTION_LOGOUT = "LOGOUT";
    public static final String ACTION_CREATE = "CREATE";
    public static final String ACTION_UPDATE = "UPDATE";
    public static final String ACTION_DELETE = "DELETE";
    public static final String ACTION_ENABLE = "ENABLE";
    public static final String ACTION_DISABLE = "DISABLE";
    public static final String ACTION_PASSWORD_CHANGE = "PASSWORD_CHANGE";
    
    private Integer id;
    private Integer userId;
    private String username;
    private String action;
    private String target;
    private String detail;
    private String ip;
    private Timestamp createTime;
    
    public OperationLog() {}
    
    public OperationLog(Integer userId, String username, String action, String target, String detail, String ip) {
        this.userId = userId;
        this.username = username;
        this.action = action;
        this.target = target;
        this.detail = detail;
        this.ip = ip;
    }
    
    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    
    public String getTarget() { return target; }
    public void setTarget(String target) { this.target = target; }
    
    public String getDetail() { return detail; }
    public void setDetail(String detail) { this.detail = detail; }
    
    public String getIp() { return ip; }
    public void setIp(String ip) { this.ip = ip; }
    
    public Timestamp getCreateTime() { return createTime; }
    public void setCreateTime(Timestamp createTime) { this.createTime = createTime; }
}