package com.example.model;

/**
 * 用户违规排名视图对象
 */
public class UserRankVO {
    private Long userId;
    private String username;
    private int violationCount;

    public UserRankVO() {}

    public UserRankVO(Long userId, String username, int violationCount) {
        this.userId = userId;
        this.username = username;
        this.violationCount = violationCount;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public int getViolationCount() { return violationCount; }
    public void setViolationCount(int violationCount) { this.violationCount = violationCount; }
}
