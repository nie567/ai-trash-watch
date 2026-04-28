package com.example.model;

import java.sql.Timestamp;

/**
 * 整改任务实体类
 */
public class RectificationTask {
    private Long id;
    private Long violationId;
    private Long userId;
    private String requirement;
    private Timestamp deadline;
    private String status;
    private String submitDesc;
    private String submitImagePath;
    private String reviewResult;
    private String reviewComment;
    private Timestamp createTime;
    private Timestamp updateTime;

    public RectificationTask() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getViolationId() { return violationId; }
    public void setViolationId(Long violationId) { this.violationId = violationId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getRequirement() { return requirement; }
    public void setRequirement(String requirement) { this.requirement = requirement; }

    public Timestamp getDeadline() { return deadline; }
    public void setDeadline(Timestamp deadline) { this.deadline = deadline; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getSubmitDesc() { return submitDesc; }
    public void setSubmitDesc(String submitDesc) { this.submitDesc = submitDesc; }

    public String getSubmitImagePath() { return submitImagePath; }
    public void setSubmitImagePath(String submitImagePath) { this.submitImagePath = submitImagePath; }

    public String getReviewResult() { return reviewResult; }
    public void setReviewResult(String reviewResult) { this.reviewResult = reviewResult; }

    public String getReviewComment() { return reviewComment; }
    public void setReviewComment(String reviewComment) { this.reviewComment = reviewComment; }

    public Timestamp getCreateTime() { return createTime; }
    public void setCreateTime(Timestamp createTime) { this.createTime = createTime; }

    public Timestamp getUpdateTime() { return updateTime; }
    public void setUpdateTime(Timestamp updateTime) { this.updateTime = updateTime; }
}
