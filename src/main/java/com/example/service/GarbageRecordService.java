package com.example.service;

import com.example.dao.DetectionResultDAO;
import com.example.dao.GarbageRecordDAO;
import com.example.dao.RectificationTaskDAO;
import com.example.dao.ViolationRecordDAO;
import com.example.model.*;
import com.example.util.AppConstants;
import com.example.util.BusinessException;

import java.util.ArrayList;
import java.util.List;

/**
 * 投放记录核心业务服务
 */
public class GarbageRecordService {

    private final GarbageRecordDAO recordDAO;
    private final DetectionResultDAO detectionDAO;
    private final ViolationService violationService;
    private final ViolationRecordDAO violationDAO;
    private final RectificationTaskDAO rectTaskDAO;

    public GarbageRecordService() {
        this.recordDAO = new GarbageRecordDAO();
        this.detectionDAO = new DetectionResultDAO();
        this.violationService = new ViolationService();
        this.violationDAO = new ViolationRecordDAO();
        this.rectTaskDAO = new RectificationTaskDAO();
    }

    /**
     * 保存投放记录及检测明细
     * 判定isCorrect，若错误则调用ViolationService生成违规记录
     */
    public Long saveRecord(Long userId, GarbageRecordSubmitDTO dto) {
        if (userId == null) {
            throw new BusinessException(400, "用户ID不能为空");
        }
        if (dto == null) {
            throw new BusinessException(400, "投放数据不能为空");
        }
        if (dto.getSelectedCategory() == null || dto.getSelectedCategory().trim().isEmpty()) {
            throw new BusinessException(400, "请选择投放类别");
        }

        // 判定isCorrect
        int isCorrect = 0;
        if (dto.getRecommendedCategory() != null
                && dto.getRecommendedCategory().equals(dto.getSelectedCategory())) {
            isCorrect = 1;
        }

        // 构建投放记录
        GarbageRecord record = new GarbageRecord();
        record.setUserId(userId);
        record.setImageName(dto.getImageName());
        record.setImagePath(dto.getImagePath());
        record.setResultImagePath(dto.getResultImagePath());
        record.setDetectedSummary(dto.getDetectedSummary());
        record.setRecommendedCategory(dto.getRecommendedCategory());
        record.setSelectedCategory(dto.getSelectedCategory());
        record.setIsMixed(dto.getIsMixed() != null ? dto.getIsMixed() : 0);
        record.setIsCorrect(isCorrect);
        record.setStatus(AppConstants.RECORD_STATUS_PENDING);
        record.setRemark(dto.getRemark());

        // 保存投放记录
        Long recordId = recordDAO.insert(record);
        if (recordId == null) {
            throw new BusinessException(500, "保存投放记录失败");
        }
        record.setId(recordId);

        // 保存检测明细
        if (dto.getDetections() != null && !dto.getDetections().isEmpty()) {
            List<DetectionResult> detectionResults = new ArrayList<>();
            for (DetectionResultDTO d : dto.getDetections()) {
                DetectionResult dr = new DetectionResult();
                dr.setRecordId(recordId);
                dr.setClassName(d.getClassName());
                dr.setConfidence(d.getConfidence());
                dr.setXMin(d.getXMin());
                dr.setYMin(d.getYMin());
                dr.setXMax(d.getXMax());
                dr.setYMax(d.getYMax());
                dr.setMappedCategory(d.getMappedCategory());
                detectionResults.add(dr);
            }
            detectionDAO.batchInsert(detectionResults);
        }

        // 如果投放错误，自动生成违规记录
        if (isCorrect == 0) {
            List<DetectionResult> details = detectionDAO.findByRecordId(recordId);
            violationService.createViolationIfNeeded(record, details);
        }

        return recordId;
    }

    /**
     * 用户投放记录分页
     */
    public PageResult<GarbageRecord> getUserRecords(Long userId, int page, int pageSize) {
        if (page < 1) page = AppConstants.DEFAULT_PAGE_NUM;
        if (pageSize < 1) pageSize = AppConstants.DEFAULT_PAGE_SIZE;
        if (pageSize > AppConstants.MAX_PAGE_SIZE) pageSize = AppConstants.MAX_PAGE_SIZE;

        int offset = (page - 1) * pageSize;
        List<GarbageRecord> list = recordDAO.findByUserId(userId, offset, pageSize);
        int total = recordDAO.countByUserId(userId);
        return new PageResult<>(list, total, page, pageSize);
    }

    /**
     * 管理员投放记录分页
     */
    public PageResult<GarbageRecord> getAllRecords(int page, int pageSize, String keyword, String status) {
        if (page < 1) page = AppConstants.DEFAULT_PAGE_NUM;
        if (pageSize < 1) pageSize = AppConstants.DEFAULT_PAGE_SIZE;
        if (pageSize > AppConstants.MAX_PAGE_SIZE) pageSize = AppConstants.MAX_PAGE_SIZE;

        int offset = (page - 1) * pageSize;
        List<GarbageRecord> list = recordDAO.findAll(offset, pageSize, keyword, status);
        int total = recordDAO.countAll(keyword, status);
        return new PageResult<>(list, total, page, pageSize);
    }

    /**
     * 投放记录详情（含检测明细、违规信息、整改信息）
     */
    public GarbageRecordDetailVO getRecordDetail(Long recordId) {
        if (recordId == null) {
            throw new BusinessException(400, "记录ID不能为空");
        }

        GarbageRecord record = recordDAO.findById(recordId);
        if (record == null) {
            throw new BusinessException(404, "投放记录不存在");
        }

        GarbageRecordDetailVO vo = new GarbageRecordDetailVO();
        vo.setRecord(record);

        // 查询检测明细
        List<DetectionResult> detections = detectionDAO.findByRecordId(recordId);
        vo.setDetections(detections);

        // 查询关联违规记录
        ViolationRecord violation = violationDAO.findByRecordId(recordId);
        vo.setViolation(violation);

        // 查询关联整改任务
        if (violation != null) {
            RectificationTask rectification = rectTaskDAO.findByViolationId(violation.getId());
            vo.setRectification(rectification);
        }

        return vo;
    }

    /**
     * 管理员人工复核
     * 复核逻辑：
     * 1. finalCategory是管理员确认的正确类别
     * 2. 用户选择的selectedCategory与finalCategory对比判断用户是否正确
     * 3. 更新违规记录状态
     */
    public void reviewRecord(Long recordId, String finalCategory, String reviewComment) {
        if (recordId == null) {
            throw new BusinessException(400, "记录ID不能为空");
        }

        GarbageRecord record = recordDAO.findById(recordId);
        if (record == null) {
            throw new BusinessException(404, "投放记录不存在");
        }

        // 根据用户选择的类别与管理员确认的正确类别对比，判断用户是否正确
        int isCorrect = 0;
        if (finalCategory != null && record.getSelectedCategory() != null
                && finalCategory.equals(record.getSelectedCategory())) {
            isCorrect = 1;
        }

        // 更新投放记录
        recordDAO.updateReviewResult(recordId, finalCategory, isCorrect,
                AppConstants.RECORD_STATUS_REVIEWED, reviewComment);

        // 处理关联的违规记录
        ViolationRecord violation = violationDAO.findByRecordId(recordId);
        if (violation != null) {
            if (isCorrect == 1) {
                // 复核确认用户正确，忽略违规记录（误判）
                violationDAO.updateStatus(violation.getId(), AppConstants.VIOLATION_STATUS_IGNORED);
            } else {
                // 复核确认用户错误，保持违规待处理状态
                // 如果违规记录状态是IGNORED，恢复为PENDING
                if (AppConstants.VIOLATION_STATUS_IGNORED.equals(violation.getStatus())) {
                    violationDAO.updateStatus(violation.getId(), AppConstants.VIOLATION_STATUS_PENDING);
                }
            }
        } else {
            // 没有违规记录，但复核确认用户错误，需要创建违规记录
            if (isCorrect == 0) {
                record.setIsCorrect(0);
                record.setId(recordId);
                List<DetectionResult> details = detectionDAO.findByRecordId(recordId);
                violationService.createViolationIfNeeded(record, details);
            }
        }
    }

    /**
     * 删除投放记录（级联删除关联数据）
     * 删除顺序：整改任务 -> 违规记录 -> 检测明细 -> 投放记录
     */
    public void deleteRecord(Long recordId) {
        if (recordId == null) {
            throw new BusinessException(400, "记录ID不能为空");
        }

        GarbageRecord record = recordDAO.findById(recordId);
        if (record == null) {
            throw new BusinessException(404, "投放记录不存在");
        }

        // 1. 查找关联的违规记录
        ViolationRecord violation = violationDAO.findByRecordId(recordId);
        
        if (violation != null) {
            // 2. 查找并删除关联的整改任务
            RectificationTask rectTask = rectTaskDAO.findByViolationId(violation.getId());
            if (rectTask != null) {
                // 删除整改任务（需要先在DAO中添加delete方法）
                rectTaskDAO.deleteById(rectTask.getId());
            }
            
            // 3. 删除违规记录
            violationDAO.deleteById(violation.getId());
        }

        // 4. 删除检测明细
        detectionDAO.deleteByRecordId(recordId);

        // 5. 删除投放记录
        recordDAO.deleteById(recordId);
    }
}
