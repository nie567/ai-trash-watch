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
                <div style="margin-top:20px;padding-top:16px;border-top:1px solid #eee;">
                    <h4 style="margin-bottom:12px;">整改提交</h4>
                    <div class="info-grid">
                        <div class="info-item"><span class="info-label">整改说明：</span><span class="info-value">${task.submitDesc}</span></div>
                        <div class="info-item"><span class="info-label">整改图片：</span><span class="info-value">${empty task.submitImagePath ? '无' : task.submitImagePath}</span></div>
                    </div>
                </div>
            </c:if>

            <!-- 复核结果 -->
            <c:if test="${not empty task.reviewResult}">
                <div style="margin-top:20px;padding-top:16px;border-top:1px solid #eee;">
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
                    <label>整改图片路径（可选）</label>
                    <input type="text" id="submitImagePath" placeholder="整改图片文件路径">
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
function doSubmit() {
    var desc = document.getElementById('submitDesc').value;
    if (!desc.trim()) { alert('请输入整改说明'); return; }
    var imagePath = document.getElementById('submitImagePath').value;
    var params = 'id=${task.id}&submitDesc=' + encodeURIComponent(desc) + '&submitImagePath=' + encodeURIComponent(imagePath);
    fetch('${pageContext.request.contextPath}/user/rectification/submit', {
        method: 'POST',
        headers: {'Content-Type': 'application/x-www-form-urlencoded'},
        body: params
    }).then(r => r.json()).then(data => {
        if (data.code === 200) {
            document.getElementById('submitStatus').innerHTML = '<span class="text-success">提交成功</span>';
            setTimeout(function(){ location.reload(); }, 1000);
        } else {
            document.getElementById('submitStatus').innerHTML = '<span class="text-danger">' + (data.message || '提交失败') + '</span>';
        }
    }).catch(err => {
        document.getElementById('submitStatus').innerHTML = '<span class="text-danger">提交失败</span>';
    });
}
</script>
</body>
</html>
