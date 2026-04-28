package com.example.model;

import java.sql.Timestamp;

/**
 * 知识库实体类
 */
public class KnowledgeBase {
    private Long id;
    private String title;
    private String garbageType;
    private String content;
    private String imagePath;
    private Timestamp createTime;

    public KnowledgeBase() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getGarbageType() { return garbageType; }
    public void setGarbageType(String garbageType) { this.garbageType = garbageType; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    public Timestamp getCreateTime() { return createTime; }
    public void setCreateTime(Timestamp createTime) { this.createTime = createTime; }
}
