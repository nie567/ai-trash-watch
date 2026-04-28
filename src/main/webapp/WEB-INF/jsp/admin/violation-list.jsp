<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>违规管理 - 垃圾分类监管系统</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
    <jsp:include page="/WEB-INF/jsp/nav-admin.jsp" />

    <div class="main-content">
        <div class="card">
            <h2 class="card-title">违规记录管理</h2>

            <div class="toolbar">
                <form class="search-form" method="get">
                    <select name="status" class="search-input" style="width:150px;">
                        <option value="">全部状态</option>
                        <option value="PENDING" ${status == 'PENDING' ? 'selected' : ''}>待处理</option>
                        <option value="RECTIFIED" ${status == 'RECTIFIED' ? 'selected' : ''}>已整改</option>
                        <option value="IGNORED" ${status == 'IGNORED' ? 'selected' : ''}>已忽略</option>
                    </select>
                    <button type="submit" class="btn btn-primary">筛选</button>
                </form>
            </div>

            <table class="table">
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>用户ID</th>
                        <th>时间</th>
                        <th>违规类型</th>
                        <th>级别</th>
                        <th>描述</th>
                        <th>状态</th>
                        <th>操作</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach items="${pageResult.data}" var="v">
                        <tr>
                            <td>${v.id}</td>
                            <td>${v.userId}</td>
                            <td><fmt:formatDate value="${v.createTime}" pattern="MM-dd HH:mm"/></td>
                            <td>${v.violationType}</td>
                            <td><span class="badge ${v.level == 'LOW' ? 'badge-pending' : v.level == 'MEDIUM' ? 'badge-submitted' : 'badge-rejected'}">${v.level}</span></td>
                            <td>${v.description}</td>
                            <td><span class="badge ${v.status == 'PENDING' ? 'badge-pending' : v.status == 'RECTIFIED' ? 'badge-rectified' : 'badge-ignored'}">${v.status == 'PENDING' ? '待处理' : v.status == 'RECTIFIED' ? '已整改' : '已忽略'}</span></td>
                            <td>
                                <c:if test="${v.status == 'PENDING'}">
                                    <button class="btn btn-small btn-warning" onclick="showRectModal(${v.id}, ${v.userId})">发起整改</button>
                                </c:if>
                            </td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty pageResult.data}">
                        <tr><td colspan="8" class="text-center text-muted">暂无数据</td></tr>
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

    <!-- 发起整改模态框 -->
    <div class="modal-overlay" id="rectModal">
        <div class="modal">
            <h3>发起整改任务</h3>
            <input type="hidden" id="rectViolationId">
            <input type="hidden" id="rectUserId">
            <div class="form-group">
                <label>整改要求 <span class="required">*</span></label>
                <textarea id="rectRequirement" rows="3" placeholder="请输入整改要求"></textarea>
            </div>
            <div class="form-group">
                <label>整改期限</label>
                <input type="date" id="rectDeadline">
            </div>
            <div class="modal-actions">
                <button class="btn btn-secondary" onclick="closeRectModal()">取消</button>
                <button class="btn btn-success" onclick="submitRectification()">确认发起</button>
            </div>
        </div>
    </div>

<script>
var contextPath = '${pageContext.request.contextPath}';

function showRectModal(violationId, userId) {
    document.getElementById('rectViolationId').value = violationId;
    document.getElementById('rectUserId').value = userId;
    document.getElementById('rectRequirement').value = '';
    document.getElementById('rectDeadline').value = '';
    document.getElementById('rectModal').classList.add('show');
}

function closeRectModal() {
    document.getElementById('rectModal').classList.remove('show');
}

function submitRectification() {
    var violationId = document.getElementById('rectViolationId').value;
    var requirement = document.getElementById('rectRequirement').value;
    var deadline = document.getElementById('rectDeadline').value;
    if (!requirement.trim()) { alert('请输入整改要求'); return; }

    var params = 'violationId=' + violationId + '&requirement=' + encodeURIComponent(requirement) + '&deadline=' + encodeURIComponent(deadline);
    fetch(contextPath + '/admin/violation/create-rectification', {
        method: 'POST',
        headers: {'Content-Type': 'application/x-www-form-urlencoded'},
        body: params
    }).then(r => r.json()).then(data => {
        if (data.code === 200) {
            closeRectModal();
            location.reload();
        } else {
            alert(data.message || '操作失败');
        }
    }).catch(err => { alert('操作失败'); });
}
</script>
</body>
</html>