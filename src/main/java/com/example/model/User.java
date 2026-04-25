package com.example.model;

import java.sql.Timestamp;

/**
 * 用户实体类
 */
public class User {
    private Integer id;
    private String username;
    private String password;       // 原始密码（仅用于兼容旧数据）
    private String passwordHash;   // BCrypt加密密码
    private String email;
    private String phone;
    private String role;           // admin/user
    private Integer status;        // 1:正常 0:禁用
    private Timestamp createTime;
    private Timestamp updateTime;
    
    public User() {}
    
    public User(Integer id, String username, String role, Integer status) {
        this.id = id;
        this.username = username;
        this.role = role;
        this.status = status;
    }
    
    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    
    public Timestamp getCreateTime() { return createTime; }
    public void setCreateTime(Timestamp createTime) { this.createTime = createTime; }
    
    public Timestamp getUpdateTime() { return updateTime; }
    public void setUpdateTime(Timestamp updateTime) { this.updateTime = updateTime; }
    
    /**
     * 判断是否为管理员
     */
    public boolean isAdmin() {
        return "admin".equals(this.role);
    }
    
    /**
     * 判断账号是否正常
     */
    public boolean isActive() {
        return this.status != null && this.status == 1;
    }
}