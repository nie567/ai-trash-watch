package com.example.model;

/**
 * 类别统计视图对象
 */
public class TypeCountVO {
    private String type;
    private int count;

    public TypeCountVO() {}

    public TypeCountVO(String type, int count) {
        this.type = type;
        this.count = count;
    }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public int getCount() { return count; }
    public void setCount(int count) { this.count = count; }
}
