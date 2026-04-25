package com.example.util;

/**
 * 应用常量
 */
public class AppConstants {
    // 角色
    public static final String ROLE_ADMIN = "admin";
    public static final String ROLE_USER = "user";
    
    // 用户状态
    public static final int STATUS_ACTIVE = 1;
    public static final int STATUS_DISABLED = 0;
    
    // Session 键
    public static final String SESSION_USER = "loginUser";
    public static final String SESSION_LOGIN_USER = "loginUser";
    public static final String SESSION_USER_ID = "loginUserId";
    public static final String REQUEST_ERROR = "error";
    public static final String REQUEST_SUCCESS = "success";
    
    // 操作类型
    public static final String ACTION_LOGIN = "LOGIN";
    public static final String ACTION_LOGOUT = "LOGOUT";
    public static final String ACTION_CREATE = "CREATE";
    public static final String ACTION_UPDATE = "UPDATE";
    public static final String ACTION_DELETE = "DELETE";
    public static final String ACTION_ENABLE = "ENABLE";
    public static final String ACTION_DISABLE = "DISABLE";
    public static final String ACTION_PASSWORD_CHANGE = "PASSWORD_CHANGE";
    
    // 分页
    public static final int DEFAULT_PAGE_SIZE = 10;
    public static final int MAX_PAGE_SIZE = 100;
    public static final int DEFAULT_PAGE_NUM = 1;
}