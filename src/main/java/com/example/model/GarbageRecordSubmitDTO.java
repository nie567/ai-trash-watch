package com.example.model;

import java.util.List;

/**
 * 用户提交投放记录的请求DTO
 */
public class GarbageRecordSubmitDTO {
    private String imageName;
    private String imagePath;
    private String resultImagePath;
    private String detectedSummary;
    private String recommendedCategory;
    private String selectedCategory;
    private Integer isMixed;
    private String remark;
    private List<DetectionResultDTO> detections;

    public GarbageRecordSubmitDTO() {}

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

    public Integer getIsMixed() { return isMixed; }
    public void setIsMixed(Integer isMixed) { this.isMixed = isMixed; }

    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }

    public List<DetectionResultDTO> getDetections() { return detections; }
    public void setDetections(List<DetectionResultDTO> detections) { this.detections = detections; }
}
