package com.example.model;

import java.sql.Timestamp;

/**
 * 投放记录实体类
 */
public class GarbageRecord {
    private Long id;
    private Long userId;
    private String imageName;
    private String imagePath;
    private String resultImagePath;
    private String detectedSummary;
    private String recommendedCategory;
    private String selectedCategory;
    private String finalCategory;
    private Integer isMixed;
    private Integer isCorrect;
    private String status;
    private String reviewComment;
    private String remark;
    private Timestamp createTime;

    public GarbageRecord() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getImageName() { return imageName; }
    public void setImageName(String imageName) { this.imageName = imageName; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    public String getResultImagePath() { return resultImagePath; }
    public void setResultImagePath(String resultImagePath) { this.resultImagePath = resultImagePath; }

    public String getDetectedSummary() { return detectedSummary; }
    public void setDetectedSummary(String detectedSummary) { this.detectedSummary = detectedSummary; }

    public String getRecommendedCategory() { return recommendedCategory; }
    public void setRecommendedCategory(String recommendedCategory) { this.recommendedCategory = recommendedCategory; }

    public String getSelectedCategory() { return selectedCategory; }
    public void setSelectedCategory(String selectedCategory) { this.selectedCategory = selectedCategory; }

    public String getFinalCategory() { return finalCategory; }
    public void setFinalCategory(String finalCategory) { this.finalCategory = finalCategory; }

    public Integer getIsMixed() { return isMixed; }
    public void setIsMixed(Integer isMixed) { this.isMixed = isMixed; }

    public Integer getIsCorrect() { return isCorrect; }
    public void setIsCorrect(Integer isCorrect) { this.isCorrect = isCorrect; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getReviewComment() { return reviewComment; }
    public void setReviewComment(String reviewComment) { this.reviewComment = reviewComment; }

    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }

    public Timestamp getCreateTime() { return createTime; }
    public void setCreateTime(Timestamp createTime) { this.createTime = createTime; }
}
