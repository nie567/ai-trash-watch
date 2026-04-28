package com.example.model;

/**
 * 趋势统计视图对象
 */
public class TrendVO {
    private String date;
    private int count;

    public TrendVO() {}

    public TrendVO(String date, int count) {
        this.date = date;
        this.count = count;
    }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public int getCount() { return count; }
    public void setCount(int count) { this.count = count; }
}
