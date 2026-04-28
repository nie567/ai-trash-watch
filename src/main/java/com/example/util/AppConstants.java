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

    // DJL 推理服务
    public static final String DJL_INFERENCE_URL = "http://localhost:8080";

    // 图片路径
    public static final String DJL_INPUT_DIR = "D:\\ny\\data_set\\input";
    public static final String DJL_OUTPUT_DIR = "D:\\ny\\data_set\\output";

    // 垃圾类别
    public static final String CATEGORY_RECYCLABLE = "可回收物";
    public static final String CATEGORY_KITCHEN = "厨余垃圾";
    public static final String CATEGORY_HAZARDOUS = "有害垃圾";
    public static final String CATEGORY_OTHER = "其他垃圾";
    public static final String CATEGORY_MIXED = "混合待分拣";

    // 投放记录状态
    public static final String RECORD_STATUS_PENDING = "PENDING";
    public static final String RECORD_STATUS_REVIEWED = "REVIEWED";

    // 违规状态
    public static final String VIOLATION_STATUS_PENDING = "PENDING";
    public static final String VIOLATION_STATUS_RECTIFIED = "RECTIFIED";
    public static final String VIOLATION_STATUS_IGNORED = "IGNORED";

    // 违规级别
    public static final String VIOLATION_LEVEL_LOW = "LOW";
    public static final String VIOLATION_LEVEL_MEDIUM = "MEDIUM";
    public static final String VIOLATION_LEVEL_HIGH = "HIGH";

    // 整改状态
    public static final String RECT_STATUS_PENDING = "PENDING";
    public static final String RECT_STATUS_SUBMITTED = "SUBMITTED";
    public static final String RECT_STATUS_APPROVED = "APPROVED";
    public static final String RECT_STATUS_REJECTED = "REJECTED";

    // 图片上传
    public static final long MAX_IMAGE_SIZE = 10 * 1024 * 1024; // 10MB
}