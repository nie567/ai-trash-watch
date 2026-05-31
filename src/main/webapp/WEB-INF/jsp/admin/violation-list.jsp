<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
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
                <form class="search-form" method="get" action="${pageContext.request.contextPath}/admin/violation/list">
                    <select name="status" class="search-input" style="width:150px;">
                        <option value="">全部状态</option>
                        <option value="PENDING" ${param.status == 'PENDING' ? 'selected' : ''}>待处理</option>
                        <option value="RECTIFIED" ${param.status == 'RECTIFIED' ? 'selected' : ''}>已整改</option>
                        <option value="IGNORED" ${param.status == 'IGNORED' ? 'selected' : ''}>已忽略</option>
                    </select>
                    <button type="submit" class="btn btn-primary">筛选</button>
                </form>
            </div>

            <table class="table">
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>用户ID</th>
                        <th>投放记录</th>
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
                            <td><c:out value="${v.id}"/></td>
                            <td><c:out value="${v.userId}"/></td>
                            <td><a href="${pageContext.request.contextPath}/admin/garbage-record/detail?id=${v.recordId}" target="_blank"><c:out value="${v.recordId}"/></a></td>
                            <td><fmt:formatDate value="${v.createTime}" pattern="MM-dd HH:mm"/></td>
                            <td><c:out value="${v.violationType}"/></td>
                            <td><span class="badge ${v.level == 'LOW' ? 'badge-pending' : v.level == 'MEDIUM' ? 'badge-submitted' : 'badge-rejected'}"><c:out value="${v.level}"/></span></td>
                            <td><c:out value="${v.description}"/></td>
                            <td><span class="badge ${v.status == 'PENDING' ? 'badge-pending' : v.status == 'RECTIFIED' ? 'badge-rectified' : 'badge-ignored'}">${v.status == 'PENDING' ? '待处理' : v.status == 'RECTIFIED' ? '已整改' : '已忽略'}</span></td>
                            <td class="actions">
                                <c:if test="${v.status == 'PENDING'}">
                                    <button class="btn btn-small btn-warning" onclick="showRectModal(${v.id}, ${v.userId})">发起整改</button>
                                </c:if>
                                <c:if test="${v.status != 'PENDING'}">
                                    <span class="text-muted">-</span>
                                </c:if>
                            </td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty pageResult.data}">
                        <tr>
                            <td colspan="9">
                                <div class="empty-state">
                                    <div class="empty-state-icon">⚠️</div>
                                    <div class="empty-state-title">暂无违规记录</div>
                                    <div class="empty-state-desc">还没有任何违规记录</div>
                                </div>
                            </td>
                        </tr>
                    </c:if>
                </tbody>
            </table>

            <c:if test="${pageResult.totalPages > 1}">
                <div class="pagination">
                    <c:if test="${pageResult.page > 1}"><a href="?page=${pageResult.page - 1}&status=${param.status}">上一页</a></c:if>
                    <span class="active">${pageResult.page} / ${pageResult.totalPages}</span>
                    <c:if test="${pageResult.page < pageResult.totalPages}"><a href="?page=${pageResult.page + 1}&status=${param.status}">下一页</a></c:if>
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
window._pageConfig = {
    contextPath: '${fn:escapeXml(pageContext.request.contextPath)}',
    csrfToken: '${fn:escapeXml(sessionScope._csrfToken)}'
};
</script>
<script src="${pageContext.request.contextPath}/js/common.js"></script>
<script src="${pageContext.request.contextPath}/js/violation-list.js"></script>
</body>
</html>
