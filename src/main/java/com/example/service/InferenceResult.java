package com.example.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * DJL 推理结果对象
 * 匹配微服务返回格式
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class InferenceResult {
    private boolean success = true; // 默認成功
    private String message;
    private String imageName;

    @JsonProperty("outputReference")
    private String outputImageName;

    @JsonProperty("inferredObjects")
    private List<DetectedObject> detectedObjects;

    public boolean isSuccess() {
        // 如果有 detectedObjects 或 outputImageName，認為成功
        return success || detectedObjects != null || outputImageName != null;
    }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getImageName() { return imageName; }
    public void setImageName(String imageName) { this.imageName = imageName; }

    public String getOutputImageName() { return outputImageName; }
    public void setOutputImageName(String outputImageName) { this.outputImageName = outputImageName; }

    public List<DetectedObject> getDetectedObjects() { return detectedObjects; }
    public void setDetectedObjects(List<DetectedObject> detectedObjects) { this.detectedObjects = detectedObjects; }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DetectedObject {
        @JsonProperty("objectClass")
        private String className;

        @JsonProperty("probability")
        private double confidence;

        public String getClassName() { return className; }
        public void setClassName(String className) { this.className = className; }

        public double getConfidence() { return confidence; }
        public void setConfidence(double confidence) { this.confidence = confidence; }
    }
}