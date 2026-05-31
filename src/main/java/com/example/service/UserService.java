package com.example.service;

import com.example.dao.GarbageRecordDAO;
import com.example.dao.OperationLogDAO;
import com.example.dao.RectificationTaskDAO;
import com.example.dao.UserDAO;
import com.example.dao.ViolationRecordDAO;
import com.example.util.DBUtil;

import java.sql.Connection;
import java.sql.SQLException;
import com.example.model.OperationLog;
import com.example.model.PageResult;
import com.example.model.User;
import com.example.util.AppConstants;
import com.example.util.BusinessException;
import com.example.util.BCryptUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户服务层 - 处理所有用户相关业务逻辑
 */
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserDAO userDAO;
    private final OperationLogDAO logDAO;
    private final GarbageRecordDAO garbageRecordDAO;
    private final ViolationRecordDAO violationRecordDAO;
    private final RectificationTaskDAO rectificationTaskDAO;
    
    public UserService() {
        this.userDAO = new UserDAO();
        this.logDAO = new OperationLogDAO();
        this.garbageRecordDAO = new GarbageRecordDAO();
        this.violationRecordDAO = new ViolationRecordDAO();
        this.rectificationTaskDAO = new RectificationTaskDAO();
    }

    public UserService(UserDAO userDAO, OperationLogDAO logDAO) {
        this.userDAO = userDAO;
        this.logDAO = logDAO;
        this.garbageRecordDAO = new GarbageRecordDAO();
        this.violationRecordDAO = new ViolationRecordDAO();
        this.rectificationTaskDAO = new RectificationTaskDAO();
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
            // 统一"用户名或密码错误"避免用户名枚举
            throw new BusinessException(401, "用户名或密码错误");
        }

        if (!user.isActive()) {
            throw new BusinessException(401, "账号已被禁用");
        }

        // 验证密码
        String passwordHash = user.getPasswordHash();
        if (passwordHash == null || passwordHash.isEmpty()) {
            // TODO [迁移] 明文密码兼容 — 全部迁移为BCrypt后移除此分支 (目标: 2025-Q4)
            logger.warn("用户 {} 使用明文密码登录，建议尽快迁移", username);
            if (!password.equals(user.getPassword())) {
                throw new BusinessException(401, "用户名或密码错误");
            }
            // 自动迁移：明文登录成功后升级为BCrypt
            try {
                String newHash = BCryptUtil.hashPassword(password);
                userDAO.updatePassword(user.getId(), newHash);
                logger.info("用户 {} 明文密码已自动迁移为BCrypt", username);
            } catch (Exception e) {
                logger.error("自动迁移BCrypt失败，用户: {}", username, e);
            }
        } else {
            if (!BCryptUtil.checkPassword(password, passwordHash)) {
                throw new BusinessException(401, "用户名或密码错误");
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
            logger.warn("记录操作日志失败", e);
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
            logger.warn("记录操作日志失败", e);
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
    public User createUser(User user, User operator, String ip) {
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
            "创建用户: " + user.getUsername(), ip);
        
        return user;
    }
    
    /**
     * 更新用户（管理员）
     */
    public void updateUser(User user, User operator, String ip) {
        if (user.getId() == null) {
            throw new BusinessException(400, "用户ID不能为空");
        }
        
        User existing = getUserById(user.getId());
        validateUserInput(user);
        
        // 不能修改自己的角色
        if (operator != null && user.getId().equals(operator.getId())) {
            user.setRole(existing.getRole());
        }
        
        if (!userDAO.update(user)) {
            throw new BusinessException(500, "更新用户失败");
        }
        
        // 记录操作日志
        logOperation(operator, OperationLog.ACTION_UPDATE, "user:" + user.getId(),
            "更新用户: " + user.getUsername(), ip);
    }
    
    /**
     * 删除用户（管理员）- 事务内级联删除关联数据
     */
    public void deleteUser(Integer id, User operator, String ip) {
        if (id == null) {
            throw new BusinessException(400, "用户ID不能为空");
        }
        
        User user = getUserById(id);
        
        // 不能删除自己
        if (operator != null && id.equals(operator.getId())) {
            throw new BusinessException(400, "不能删除自己的账号");
        }
        
        // 事务内级联删除：先删除关联数据，再删除用户
        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false);
            
            long userId = id.longValue();
            
            // 1. 删除整改任务
            int tasks = rectificationTaskDAO.deleteByUserId(userId, conn);
            logger.info("删除用户 {} 的整改任务 {} 条", id, tasks);
            
            // 2. 删除违规记录
            int violations = violationRecordDAO.deleteByUserId(userId, conn);
            logger.info("删除用户 {} 的违规记录 {} 条", id, violations);
            
            // 3. 删除投放记录
            int records = garbageRecordDAO.deleteByUserId(userId, conn);
            logger.info("删除用户 {} 的投放记录 {} 条", id, records);
            
            // 4. 删除操作日志
            int logs = logDAO.deleteByUserId(userId, conn);
            logger.info("删除用户 {} 的操作日志 {} 条", id, logs);
            
            // 5. 删除用户本身
            if (!userDAO.delete(id, conn)) {
                throw new BusinessException(500, "删除用户失败");
            }
            
            conn.commit();
            logger.info("用户 {} ({}) 及其关联数据已级联删除", id, user.getUsername());
            
        } catch (BusinessException e) {
            if (conn != null) try { conn.rollback(); } catch (SQLException ignored) {}
            throw e;
        } catch (Exception e) {
            if (conn != null) try { conn.rollback(); } catch (SQLException ignored) {}
            logger.error("级联删除用户失败, id={}", id, e);
            throw new BusinessException(500, "删除用户失败: " + e.getMessage());
        } finally {
            if (conn != null) try { conn.close(); } catch (SQLException ignored) {}
        }
        
        // 记录操作日志（在事务外，因为用户已删除，用管理员身份记录）
        logOperation(operator, OperationLog.ACTION_DELETE, "user:" + id,
            "删除用户: " + user.getUsername(), ip);
    }
    
    /**
     * 启用/禁用用户
     */
    public void updateUserStatus(Integer id, int status, User operator, String ip) {
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
            (status == AppConstants.STATUS_ACTIVE ? "启用" : "禁用") + "用户: " + user.getUsername(), ip);
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
            // TODO [迁移] 明文密码兼容 — 全部迁移为BCrypt后移除此分支 (目标: 2025-Q4)
            if (!oldPassword.equals(user.getPassword())) {
                throw new BusinessException(400, "旧密码错误");
            }
        }

        // 校验新密码强度
        String strengthError = BCryptUtil.checkStrength(newPassword);
        if (strengthError != null) {
            throw new BusinessException(400, strengthError);
        }

        // 加密并保存新密码
        String newHash = BCryptUtil.hashPassword(newPassword);
        if (!userDAO.updatePassword(userId, newHash)) {
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
        
        if (!userDAO.update(user)) {
            throw new BusinessException(500, "更新个人资料失败");
        }
    }
    
    /**
     * 获取仪表盘统计数据
     */
    public Map<String, Object> getDashboardStats() {
        Map<String, Integer> counts = userDAO.countDashboardStats();
        int todayNew = userDAO.countTodayNew();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", counts.getOrDefault("totalUsers", 0));
        stats.put("todayNew", todayNew);
        stats.put("adminCount", counts.getOrDefault("adminCount", 0));
        stats.put("userCount", counts.getOrDefault("userCount", 0));
        stats.put("activeUsers", counts.getOrDefault("activeUsers", 0));
        stats.put("disabledUsers", counts.getOrDefault("disabledUsers", 0));
        return stats;
    }
    
    /**
     * 验证用户输入
     */
    public List<User> findAll(int page, int pageSize) {
        return userDAO.findAll(page, pageSize);
    }

    public int countAll() {
        return userDAO.countAll();
    }

    public List<User> search(String keyword, int page, int pageSize) {
        return userDAO.search(keyword, page, pageSize);
    }

    public int countSearch(String keyword) {
        return userDAO.countSearch(keyword);
    }

    /**
     * 用户自主注册（不需要操作员）
     */
    public User register(String username, String password, String email, String phone) {
        if (username == null || username.trim().isEmpty()) {
            throw new BusinessException(400, "用户名不能为空");
        }
        username = username.trim();

        // 检查用户名是否已存在
        if (userDAO.existsByUsername(username)) {
            throw new BusinessException(400, "用户名已存在");
        }

        // 检查邮箱是否已被使用
        if (email != null && !email.trim().isEmpty() && userDAO.existsByEmail(email.trim())) {
            throw new BusinessException(400, "该邮箱已被注册");
        }

        // 密码强度校验
        String strengthError = BCryptUtil.checkStrength(password);
        if (strengthError != null) {
            throw new BusinessException(400, strengthError);
        }

        // 构建用户对象
        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(BCryptUtil.hashPassword(password));
        user.setEmail(email != null && !email.trim().isEmpty() ? email.trim() : null);
        user.setPhone(phone != null && !phone.trim().isEmpty() ? phone.trim() : null);
        user.setRole(AppConstants.ROLE_USER);
        user.setStatus(AppConstants.STATUS_ACTIVE);

        if (!userDAO.create(user)) {
            throw new BusinessException(500, "注册失败，请稍后重试");
        }

        logger.info("新用户注册: {}", username);
        return user;
    }

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
    private void logOperation(User operator, String action, String target, String detail, String ip) {
        if (operator == null) return;
        try {
            OperationLog log = new OperationLog(
                operator.getId(),
                operator.getUsername(),
                action,
                target,
                detail,
                ip
            );
            logDAO.insert(log);
        } catch (Exception e) {
            // 日志记录失败不影响主流程
            logger.warn("记录操作日志失败", e);
        }
    }
}