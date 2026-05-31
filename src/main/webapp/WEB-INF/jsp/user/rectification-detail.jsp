<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>整改详情 - 垃圾分类监管系统</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
    <jsp:include page="/WEB-INF/jsp/nav-user.jsp" />

    <div class="main-content">
        <div class="card">
            <h2 class="card-title">整改任务详情</h2>

            <div class="info-grid">
                <div class="info-item"><span class="info-label">任务ID：</span><span class="info-value">${task.id}</span></div>
                <div class="info-item"><span class="info-label">关联违规：</span><span class="info-value">${task.violationId}</span></div>
                <div class="info-item"><span class="info-label">整改要求：</span><span class="info-value">${task.requirement}</span></div>
                <div class="info-item"><span class="info-label">整改期限：</span><span class="info-value">${empty task.deadline ? '无期限' : task.deadline}</span></div>
                <div class="info-item"><span class="info-label">状态：</span><span class="info-value"><span class="badge badge-${task.status eq 'PENDING' ? 'pending' : task.status eq 'SUBMITTED' ? 'submitted' : task.status eq 'APPROVED' ? 'approved' : 'rejected'}">${task.status}</span></span></div>
                <div class="info-item"><span class="info-label">创建时间：</span><span class="info-value"><fmt:formatDate value="${task.createTime}" pattern="yyyy-MM-dd HH:mm"/></span></div>
            </div>

            <!-- 已提交的整改信息 -->
            <c:if test="${not empty task.submitDesc}">
                <div style="margin-top:20px;padding-top:16px;border-top:1px solid rgba(255,255,255,0.1);">
                    <h4 style="margin-bottom:12px;">整改提交</h4>
                    <div class="info-grid">
                        <div class="info-item"><span class="info-label">整改说明：</span><span class="info-value">${task.submitDesc}</span></div>
                        <div class="info-item"><span class="info-label">整改图片：</span><span class="info-value"><c:choose><c:when test="${not empty task.submitImagePath}"><img src="${pageContext.request.contextPath}/image/${task.submitImagePath}" alt="整改图片" style="max-width:300px;max-height:200px;border-radius:6px;cursor:pointer;" onclick="window.open(this.src)"></c:when><c:otherwise>无</c:otherwise></c:choose></span></div>
                    </div>
                </div>
            </c:if>

            <!-- 复核结果 -->
            <c:if test="${not empty task.reviewResult}">
                <div style="margin-top:20px;padding-top:16px;border-top:1px solid rgba(255,255,255,0.1);">
                    <h4 style="margin-bottom:12px;">复核结果</h4>
                    <div class="info-grid">
                        <div class="info-item"><span class="info-label">复核结果：</span><span class="info-value"><span class="badge badge-${task.reviewResult eq 'APPROVED' ? 'approved' : 'rejected'}">${task.reviewResult == 'APPROVED' ? '通过' : '驳回'}</span></span></div>
                        <div class="info-item"><span class="info-label">复核意见：</span><span class="info-value">${empty task.reviewComment ? '无' : task.reviewComment}</span></div>
                    </div>
                </div>
            </c:if>
        </div>

        <!-- 提交整改表单（仅PENDING状态显示） -->
        <c:if test="${task.status == 'PENDING'}">
            <div class="card">
                <h2 class="card-title">提交整改</h2>
                <div class="form-group">
                    <label>整改说明 <span class="required">*</span></label>
                    <textarea id="submitDesc" rows="4" placeholder="请详细描述您的整改情况"></textarea>
                </div>
                <div class="form-group">
                    <label>整改图片（可选）</label>
                    <div class="upload-area" id="rectUploadArea" onclick="document.getElementById('rectFileInput').click()" style="padding:20px;text-align:center;cursor:pointer;border:2px dashed rgba(255,255,255,0.2);border-radius:8px;min-height:80px;position:relative;">
                        <div id="rectUploadPlaceholder">
                            <div style="font-size:28px;">&#128247;</div>
                            <p style="margin:4px 0;">点击选择整改图片</p>
                            <p class="text-muted" style="font-size:14px;">支持 jpg/png/gif 格式，最大10MB</p>
                        </div>
                        <img id="rectPreviewImg" src="" alt="" style="max-width:100%;max-height:200px;display:none;border-radius:6px;">
                        <input type="file" id="rectFileInput" accept=".jpg,.jpeg,.png,.gif" style="display:none" onchange="onRectFileSelected(this)">
                    </div>
                </div>
                <button class="btn btn-success" onclick="doSubmit()">提交整改</button>
                <span id="submitStatus" style="margin-left:10px;"></span>
            </div>
        </c:if>

        <div style="margin-top:20px;">
            <a href="${pageContext.request.contextPath}/user/rectification/list" class="btn btn-secondary">返回列表</a>
        </div>
    </div>

<script>
window._pageConfig = {
    contextPath: '${pageContext.request.contextPath}',
    csrfToken: '${sessionScope._csrfToken}',
    taskId: parseInt('${task.id}') || 0
};
</script>
<script src="${pageContext.request.contextPath}/js/common.js"></script>
<script src="${pageContext.request.contextPath}/js/rectification-detail.js"></script>
</body>
</html>
