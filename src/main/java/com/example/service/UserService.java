package com.example.service;

import com.example.dao.OperationLogDAO;
import com.example.dao.UserDAO;
import com.example.model.OperationLog;
import com.example.model.PageResult;
import com.example.model.User;
import com.example.util.AppConstants;
import com.example.util.BusinessException;
import com.example.util.BCryptUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户服务层 - 处理所有用户相关业务逻辑
 */
public class UserService {
    
    private final UserDAO userDAO;
    private final OperationLogDAO logDAO;
    
    public UserService() {
        this.userDAO = new UserDAO();
        this.logDAO = new OperationLogDAO();
    }
    
    /**
     * 用户登录验证
     */
    public User login(String username, String password, String ip) {
        if (username == null || username.trim().isEmpty()) {
            throw new BusinessException(400, "用户名不能为空");
        }
        if (password == null || password.isEmpty()) {
            throw new BusinessException(400, "密码不能为空");
        }
        
        User user = userDAO.findByUsername(username.trim());
        
        if (user == null) {
            throw new BusinessException(401, "账号不存在");
        }
        
        if (!user.isActive()) {
            throw new BusinessException(401, "账号已被禁用");
        }
        
        // 验证密码
        String passwordHash = user.getPasswordHash();
        if (passwordHash == null || passwordHash.isEmpty()) {
            // 兼容旧数据：如果没有hash，尝试用明文密码
            if (!password.equals(user.getPassword())) {
                throw new BusinessException(401, "密码错误");
            }
        } else {
            if (!BCryptUtil.checkPassword(password, passwordHash)) {
                throw new BusinessException(401, "密码错误");
            }
        }
        
        // 记录登录日志
        logLogin(user, ip);
        
        return user;
    }
    
    private void logLogin(User user, String ip) {
        try {
            OperationLog log = new OperationLog(
                user.getId(),
                user.getUsername(),
                OperationLog.ACTION_LOGIN,
                "SYSTEM",
                "用户登录",
                ip
            );
            logDAO.insert(log);
        } catch (Exception e) {
            // 日志记录失败不影响主流程
        }
    }
    
    /**
     * 用户登出
     */
    public void logout(User user, String ip) {
        if (user == null) return;
        try {
            OperationLog log = new OperationLog(
                user.getId(),
                user.getUsername(),
                OperationLog.ACTION_LOGOUT,
                "SYSTEM",
                "用户退出登录",
                ip
            );
            logDAO.insert(log);
        } catch (Exception e) {
            // 日志记录失败不影响主流程
        }
    }
    
    /**
     * 获取用户列表（分页）
     */
    public PageResult<User> getUserPage(int page, int size, String keyword) {
        // 校验分页参数
        if (page < 1) page = AppConstants.DEFAULT_PAGE_NUM;
        if (size < 1) size = AppConstants.DEFAULT_PAGE_SIZE;
        if (size > AppConstants.MAX_PAGE_SIZE) size = AppConstants.MAX_PAGE_SIZE;
        
        List<User> users;
        long total;
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            users = userDAO.findByUsernamePrefix(keyword.trim(), page, size);
            total = userDAO.countByUsernamePrefix(keyword.trim());
        } else {
            users = userDAO.findAll(page, size);
            total = userDAO.countAll();
        }
        
        return new PageResult<>(users, total, page, size);
    }
    
    /**
     * 根据ID获取用户
     */
    public User getUserById(Integer id) {
        if (id == null) {
            throw new BusinessException(400, "用户ID不能为空");
        }
        User user = userDAO.findById(id);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        return user;
    }
    
    /**
     * 创建用户（管理员）
     */
    public User createUser(User user, User operator) {
        validateUserInput(user);
        
        // 检查用户名是否已存在
        User existing = userDAO.findByUsername(user.getUsername());
        if (existing != null) {
            throw new BusinessException(400, "用户名已存在");
        }
        
        // 加密密码
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPasswordHash(BCryptUtil.hashPassword(user.getPassword()));
        }
        
        // 设置默认值
        if (user.getRole() == null || user.getRole().isEmpty()) {
            user.setRole(AppConstants.ROLE_USER);
        }
        if (user.getStatus() == null) {
            user.setStatus(AppConstants.STATUS_ACTIVE);
        }
        
        if (!userDAO.create(user)) {
            throw new BusinessException(500, "创建用户失败");
        }
        
        // 记录操作日志
        logOperation(operator, OperationLog.ACTION_CREATE, "user:" + user.getId(), 
            "创建用户: " + user.getUsername());
        
        return user;
    }
    
    /**
     * 更新用户（管理员）
     */
    public void updateUser(User user, User operator) {
        if (user.getId() == null) {
            throw new BusinessException(400, "用户ID不能为空");
        }
        
        User existing = getUserById(user.getId());
        validateUserInput(user);
        
        // 不能修改自己的角色
        if (operator != null && user.getId().equals(operator.getId())) {
            user.setRole(existing.getRole());
        }
        
        if (!userDAO.updateProfile(user)) {
            throw new BusinessException(500, "更新用户失败");
        }
        
        // 记录操作日志
        logOperation(operator, OperationLog.ACTION_UPDATE, "user:" + user.getId(),
            "更新用户: " + user.getUsername());
    }
    
    /**
     * 删除用户（管理员）
     */
    public void deleteUser(Integer id, User operator) {
        if (id == null) {
            throw new BusinessException(400, "用户ID不能为空");
        }
        
        User user = getUserById(id);
        
        // 不能删除自己
        if (operator != null && id.equals(operator.getId())) {
            throw new BusinessException(400, "不能删除自己的账号");
        }
        
        if (!userDAO.delete(id)) {
            throw new BusinessException(500, "删除用户失败");
        }
        
        // 记录操作日志
        logOperation(operator, OperationLog.ACTION_DELETE, "user:" + id,
            "删除用户: " + user.getUsername());
    }
    
    /**
     * 启用/禁用用户
     */
    public void updateUserStatus(Integer id, int status, User operator) {
        if (id == null) {
            throw new BusinessException(400, "用户ID不能为空");
        }
        
        User user = getUserById(id);
        
        // 不能禁用/启用自己
        if (operator != null && id.equals(operator.getId())) {
            throw new BusinessException(400, "不能修改自己的状态");
        }
        
        if (!userDAO.updateStatus(id, status)) {
            throw new BusinessException(500, "更新用户状态失败");
        }
        
        // 记录操作日志
        String action = status == AppConstants.STATUS_ACTIVE 
            ? OperationLog.ACTION_ENABLE 
            : OperationLog.ACTION_DISABLE;
        logOperation(operator, action, "user:" + id,
            (status == AppConstants.STATUS_ACTIVE ? "启用" : "禁用") + "用户: " + user.getUsername());
    }
    
    /**
     * 修改密码
     */
    public void changePassword(Integer userId, String oldPassword, String newPassword) {
        if (userId == null) {
            throw new BusinessException(400, "用户ID不能为空");
        }
        
        User user = userDAO.findById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        
        // 验证旧密码
        String passwordHash = user.getPasswordHash();
        if (passwordHash != null && !passwordHash.isEmpty()) {
            if (!BCryptUtil.checkPassword(oldPassword, passwordHash)) {
                throw new BusinessException(400, "旧密码错误");
            }
        } else {
            // 兼容旧数据
            if (!oldPassword.equals(user.getPassword())) {
                throw new BusinessException(400, "旧密码错误");
            }
        }
        
        // 加密并保存新密码
        String newHash = BCryptUtil.hashPassword(newPassword);
        if (!userDAO.changePassword(userId, newHash)) {
            throw new BusinessException(500, "修改密码失败");
        }
    }
    
    /**
     * 更新个人资料
     */
    public void updateProfile(Integer userId, String email, String phone) {
        if (userId == null) {
            throw new BusinessException(400, "用户ID不能为空");
        }
        
        User user = userDAO.findById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        
        user.setEmail(email);
        user.setPhone(phone);
        
        if (!userDAO.updateProfile(user)) {
            throw new BusinessException(500, "更新个人资料失败");
        }
    }
    
    /**
     * 获取仪表盘统计数据
     */
    public Map<String, Object> getDashboardStats() {
        int totalUsers = userDAO.countAll();
        int todayNew = userDAO.countTodayNew();
        int adminCount = userDAO.countByRole(AppConstants.ROLE_ADMIN);
        int userCount = userDAO.countByRole(AppConstants.ROLE_USER);
        int activeCount = userDAO.countByStatus(AppConstants.STATUS_ACTIVE);
        int disabledCount = userDAO.countByStatus(AppConstants.STATUS_DISABLED);
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", totalUsers);
        stats.put("todayNew", todayNew);
        stats.put("adminCount", adminCount);
        stats.put("userCount", userCount);
        stats.put("activeCount", activeCount);
        stats.put("disabledCount", disabledCount);
        return stats;
    }
    
    /**
     * 验证用户输入
     */
    private void validateUserInput(User user) {
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            throw new BusinessException(400, "用户名不能为空");
        }
        if (user.getUsername().length() > 50) {
            throw new BusinessException(400, "用户名长度不能超过50个字符");
        }
        if (user.getEmail() != null && user.getEmail().length() > 100) {
            throw new BusinessException(400, "邮箱长度不能超过100个字符");
        }
        if (user.getPhone() != null && user.getPhone().length() > 20) {
            throw new BusinessException(400, "手机号长度不能超过20个字符");
        }
    }
    
    /**
     * 记录操作日志
     */
    private void logOperation(User operator, String action, String target, String detail) {
        if (operator == null) return;
        try {
            OperationLog log = new OperationLog(
                operator.getId(),
                operator.getUsername(),
                action,
                target,
                detail,
                null
            );
            logDAO.insert(log);
        } catch (Exception e) {
            // 日志记录失败不影响主流程
        }
    }
}