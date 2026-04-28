package com.example.model;

/**
 * 分类规则实体类
 */
public class GarbageRule {
    private Long id;
    private String className;
    private String mappedCategory;
    private String description;
    private Integer status;

    public GarbageRule() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }

    public String getMappedCategory() { return mappedCategory; }
    public void setMappedCategory(String mappedCategory) { this.mappedCategory = mappedCategory; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
}
