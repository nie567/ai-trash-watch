<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>整改任务管理 - 垃圾分类监管系统</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
    <jsp:include page="/WEB-INF/jsp/nav-admin.jsp" />

    <div class="main-content">
        <div class="card">
            <h2 class="card-title">整改任务管理</h2>

            <div class="toolbar">
                <form class="search-form" method="get">
                    <select name="status" class="search-input" style="width:150px;">
                        <option value="">全部状态</option>
                        <option value="PENDING" ${status == 'PENDING' ? 'selected' : ''}>待提交</option>
                        <option value="SUBMITTED" ${status == 'SUBMITTED' ? 'selected' : ''}>已提交</option>
                        <option value="APPROVED" ${status == 'APPROVED' ? 'selected' : ''}>已通过</option>
                        <option value="REJECTED" ${status == 'REJECTED' ? 'selected' : ''}>已驳回</option>
                    </select>
                    <button type="submit" class="btn btn-primary">筛选</button>
                </form>
            </div>

            <table class="table">
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>用户ID</th>
                        <th>违规ID</th>
                        <th>整改要求</th>
                        <th>提交说明</th>
                        <th>状态</th>
                        <th>操作</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach items="${pageResult.data}" var="t">
                        <tr>
                            <td>${t.id}</td>
                            <td>${t.userId}</td>
                            <td>${t.violationId}</td>
                            <td>${t.requirement}</td>
                            <td>${empty t.submitDesc ? '-' : t.submitDesc}</td>
                            <td><span class="badge badge-${t.status eq 'PENDING' ? 'pending' : t.status eq 'SUBMITTED' ? 'submitted' : t.status eq 'APPROVED' ? 'approved' : 'rejected'}">${t.status}</span></td>
                            <td>
                                <c:if test="${t.status == 'SUBMITTED'}">
                                    <button class="btn btn-small btn-success" onclick="showReviewModal(${t.id})">复核</button>
                                </c:if>
                            </td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty pageResult.data}">
                        <tr><td colspan="7" class="text-center text-muted">暂无数据</td></tr>
                    </c:if>
                </tbody>
            </table>

            <c:if test="${pageResult.totalPages > 1}">
                <div class="pagination">
                    <c:if test="${pageResult.page > 1}"><a href="?page=${pageResult.page - 1}&status=${status}">上一页</a></c:if>
                    <span class="active">${pageResult.page} / ${pageResult.totalPages}</span>
                    <c:if test="${pageResult.page < pageResult.totalPages}"><a href="?page=${pageResult.page + 1}&status=${status}">下一页</a></c:if>
                </div>
            </c:if>
        </div>
    </div>

    <!-- 复核模态框 -->
    <div class="modal-overlay" id="reviewModal">
        <div class="modal">
            <h3>复核整改</h3>
            <input type="hidden" id="reviewTaskId">
            <div class="form-group">
                <label>复核结果 <span class="required">*</span></label>
                <select id="reviewResult">
                    <option value="APPROVED">通过</option>
                    <option value="REJECTED">驳回</option>
                </select>
            </div>
            <div class="form-group">
                <label>复核意见</label>
                <textarea id="reviewComment" rows="3" placeholder="请输入复核意见"></textarea>
            </div>
            <div class="modal-actions">
                <button class="btn btn-secondary" onclick="closeReviewModal()">取消</button>
                <button class="btn btn-success" onclick="submitReview()">确认复核</button>
            </div>
        </div>
    </div>

<script>
var contextPath = '${pageContext.request.contextPath}';

function showReviewModal(taskId) {
    document.getElementById('reviewTaskId').value = taskId;
    document.getElementById('reviewResult').value = 'APPROVED';
    document.getElementById('reviewComment').value = '';
    document.getElementById('reviewModal').classList.add('show');
}

function closeReviewModal() {
    document.getElementById('reviewModal').classList.remove('show');
}

function submitReview() {
    var taskId = document.getElementById('reviewTaskId').value;
    var result = document.getElementById('reviewResult').value;
    var comment = document.getElementById('reviewComment').value;
    var params = 'id=' + taskId + '&reviewResult=' + result + '&reviewComment=' + encodeURIComponent(comment);
    fetch(contextPath + '/admin/rectification/review', {
        method: 'POST',
        headers: {'Content-Type': 'application/x-www-form-urlencoded'},
        body: params
    }).then(r => r.json()).then(data => {
        if (data.code === 200) {
            closeReviewModal();
            location.reload();
        } else {
            alert(data.message || '操作失败');
        }
    }).catch(err => { alert('操作失败'); });
}
</script>
</body>
</html>
