package com.example.model;

/**
 * 检测结果传输DTO
 */
public class DetectionResultDTO {
    private String className;
    private Double confidence;
    private Integer xMin;
    private Integer yMin;
    private Integer xMax;
    private Integer yMax;
    private String mappedCategory;

    public DetectionResultDTO() {}

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
}
