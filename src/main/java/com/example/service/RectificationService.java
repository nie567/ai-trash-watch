package com.example.service;

import com.example.dao.RectificationTaskDAO;
import com.example.dao.ViolationRecordDAO;
import com.example.model.PageResult;
import com.example.model.RectificationTask;
import com.example.model.ViolationRecord;
import com.example.util.AppConstants;
import com.example.util.BusinessException;

import java.sql.Timestamp;
import java.util.List;

/**
 * 整改任务业务服务
 */
public class RectificationService {

    private final RectificationTaskDAO taskDAO;
    private final ViolationRecordDAO violationDAO;

    public RectificationService() {
        this.taskDAO = new RectificationTaskDAO();
        this.violationDAO = new ViolationRecordDAO();
    }

    /**
     * 创建整改任务
     * 创建前检查：该违规记录是否已有未完成整改任务
     */
    public Long createTask(Long violationId, Long userId, String requirement, String deadline) {
        if (violationId == null) {
            throw new BusinessException(400, "违规记录ID不能为空");
        }
        if (requirement == null || requirement.trim().isEmpty()) {
            throw new BusinessException(400, "整改要求不能为空");
        }

        // 检查违规记录是否存在
        ViolationRecord violation = violationDAO.findById(violationId);
        if (violation == null) {
            throw new BusinessException(404, "违规记录不存在");
        }

        // 检查是否已有未完成的整改任务
        RectificationTask existingTask = taskDAO.findByViolationId(violationId);
        if (existingTask != null && !isFinished(existingTask)) {
            throw new BusinessException(400, "该违规记录已有进行中的整改任务");
        }

        RectificationTask task = new RectificationTask();
        task.setViolationId(violationId);
        task.setUserId(userId != null ? userId : violation.getUserId());
        task.setRequirement(requirement.trim());
        if (deadline != null && !deadline.trim().isEmpty()) {
            try {
                task.setDeadline(Timestamp.valueOf(deadline.trim() + " 00:00:00"));
            } catch (Exception e) {
                // 如果格式不对，尝试直接解析
                try {
                    task.setDeadline(Timestamp.valueOf(deadline.trim()));
                } catch (Exception ex) {
                    // 忽略格式错误
                }
            }
        }
        task.setStatus(AppConstants.RECT_STATUS_PENDING);

        Long taskId = taskDAO.insert(task);
        if (taskId == null) {
            throw new BusinessException(500, "创建整改任务失败");
        }

        // 更新违规记录状态
        violationDAO.updateStatus(violationId, AppConstants.VIOLATION_STATUS_PENDING);

        return taskId;
    }

    /**
     * 判断整改任务是否已结束
     */
    private boolean isFinished(RectificationTask task) {
        return AppConstants.RECT_STATUS_APPROVED.equals(task.getStatus())
                || AppConstants.RECT_STATUS_REJECTED.equals(task.getStatus());
    }

    /**
     * 用户整改任务分页
     */
    public PageResult<RectificationTask> getUserTasks(Long userId, int page, int pageSize) {
        if (page < 1) page = AppConstants.DEFAULT_PAGE_NUM;
        if (pageSize < 1) pageSize = AppConstants.DEFAULT_PAGE_SIZE;
        if (pageSize > AppConstants.MAX_PAGE_SIZE) pageSize = AppConstants.MAX_PAGE_SIZE;

        int offset = (page - 1) * pageSize;
        List<RectificationTask> list = taskDAO.findByUserId(userId, offset, pageSize);
        int total = taskDAO.countByUserId(userId);
        return new PageResult<>(list, total, page, pageSize);
    }

    /**
     * 管理员整改任务分页
     */
    public PageResult<RectificationTask> getAllTasks(int page, int pageSize, String status) {
        if (page < 1) page = AppConstants.DEFAULT_PAGE_NUM;
        if (pageSize < 1) pageSize = AppConstants.DEFAULT_PAGE_SIZE;
        if (pageSize > AppConstants.MAX_PAGE_SIZE) pageSize = AppConstants.MAX_PAGE_SIZE;

        int offset = (page - 1) * pageSize;
        List<RectificationTask> list = taskDAO.findAll(offset, pageSize, status);
        int total = taskDAO.countAll(status);
        return new PageResult<>(list, total, page, pageSize);
    }

    /**
     * 按ID查询
     */
    public RectificationTask getById(Long id) {
        if (id == null) {
            return null;
        }
        return taskDAO.findById(id);
    }

    /**
     * 用户提交整改
     */
    public void submitRectification(Long taskId, String submitDesc, String submitImagePath) {
        if (taskId == null) {
            throw new BusinessException(400, "整改任务ID不能为空");
        }
        if (submitDesc == null || submitDesc.trim().isEmpty()) {
            throw new BusinessException(400, "整改说明不能为空");
        }

        RectificationTask task = taskDAO.findById(taskId);
        if (task == null) {
            throw new BusinessException(404, "整改任务不存在");
        }

        // 检查状态：只有PENDING状态可以提交
        if (!AppConstants.RECT_STATUS_PENDING.equals(task.getStatus())) {
            throw new BusinessException(400, "当前任务状态不允许提交");
        }

        taskDAO.submit(taskId, submitDesc.trim(), submitImagePath, AppConstants.RECT_STATUS_SUBMITTED);
    }

    /**
     * 管理员复核整改
     */
    public void reviewTask(Long taskId, String reviewResult, String reviewComment) {
        if (taskId == null) {
            throw new BusinessException(400, "整改任务ID不能为空");
        }
        if (reviewResult == null || reviewResult.trim().isEmpty()) {
            throw new BusinessException(400, "复核结果不能为空");
        }

        RectificationTask task = taskDAO.findById(taskId);
        if (task == null) {
            throw new BusinessException(404, "整改任务不存在");
        }

        // 检查状态：只有SUBMITTED状态可以复核
        if (!AppConstants.RECT_STATUS_SUBMITTED.equals(task.getStatus())) {
            throw new BusinessException(400, "当前任务状态不允许复核");
        }

        String newStatus;
        if (AppConstants.RECT_STATUS_APPROVED.equals(reviewResult)) {
            newStatus = AppConstants.RECT_STATUS_APPROVED;
        } else {
            newStatus = AppConstants.RECT_STATUS_REJECTED;
        }

        taskDAO.review(taskId, reviewResult, reviewComment, newStatus);

        // 如果复核通过，更新违规记录状态为已整改
        if (AppConstants.RECT_STATUS_APPROVED.equals(newStatus)) {
            violationDAO.updateStatus(task.getViolationId(), AppConstants.VIOLATION_STATUS_RECTIFIED);
        }
    }
}
