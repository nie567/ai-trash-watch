package com.example.model;

import java.util.List;

/**
 * 投放记录详情视图对象
 */
public class GarbageRecordDetailVO {
    private GarbageRecord record;
    private List<DetectionResult> detections;
    private ViolationRecord violation;
    private RectificationTask rectification;

    public GarbageRecordDetailVO() {}

    public GarbageRecord getRecord() { return record; }
    public void setRecord(GarbageRecord record) { this.record = record; }

    public List<DetectionResult> getDetections() { return detections; }
    public void setDetections(List<DetectionResult> detections) { this.detections = detections; }

    public ViolationRecord getViolation() { return violation; }
    public void setViolation(ViolationRecord violation) { this.violation = violation; }

    public RectificationTask getRectification() { return rectification; }
    public void setRectification(RectificationTask rectification) { this.rectification = rectification; }
}
