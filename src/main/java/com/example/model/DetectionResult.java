package com.example.model;

import java.sql.Timestamp;

/**
 * 检测明细实体类
 */
public class DetectionResult {
    private Long id;
    private Long recordId;
    private String className;
    private Double confidence;
    private Integer xMin;
    private Integer yMin;
    private Integer xMax;
    private Integer yMax;
    private String mappedCategory;
    private Timestamp createTime;

    public DetectionResult() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getRecordId() { return recordId; }
    public void setRecordId(Long recordId) { this.recordId = recordId; }

    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }

    public Double getConfidence() { return confidence; }
    public void setConfidence(Double confidence) { this.confidence = confidence; }

    public Integer getXMin() { return xMin; }
    public void setXMin(Integer xMin) { this.xMin = xMin; }

    public Integer getYMin() { return yMin; }
    public void setYMin(Integer yMin) { this.yMin = yMin; }

    public Integer getXMax() { return xMax; }
    public void setXMax(Integer xMax) { this.xMax = xMax; }

    public Integer getYMax() { return yMax; }
    public void setYMax(Integer yMax) { this.yMax = yMax; }

    public String getMappedCategory() { return mappedCategory; }
    public void setMappedCategory(String mappedCategory) { this.mappedCategory = mappedCategory; }

    public Timestamp getCreateTime() { return createTime; }
    public void setCreateTime(Timestamp createTime) { this.createTime = createTime; }
}
