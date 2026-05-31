<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>我的整改 - 垃圾分类监管系统</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
    <jsp:include page="/WEB-INF/jsp/nav-user.jsp" />

    <div class="main-content">
        <div class="card">
            <h2 class="card-title">我的整改任务</h2>

            <div class="toolbar">
                <form class="search-form" method="get" action="${pageContext.request.contextPath}/user/rectification/list">
                    <select name="status" class="search-input" style="width:150px;">
                        <option value="">全部状态</option>
                        <option value="PENDING" ${param.status == 'PENDING' ? 'selected' : ''}>待提交</option>
                        <option value="SUBMITTED" ${param.status == 'SUBMITTED' ? 'selected' : ''}>已提交</option>
                        <option value="APPROVED" ${param.status == 'APPROVED' ? 'selected' : ''}>已通过</option>
                        <option value="REJECTED" ${param.status == 'REJECTED' ? 'selected' : ''}>已驳回</option>
                    </select>
                    <button type="submit" class="btn btn-primary">筛选</button>
                </form>
            </div>

            <table class="table">
                <thead>
                    <tr>
                        <th>违规类型</th>
                        <th>整改要求</th>
                        <th>期限</th>
                        <th>状态</th>
                        <th>操作</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach items="${pageResult.data}" var="t">
                        <tr>
                            <td>违规#${t.violationId}</td>
                            <td>${t.requirement}</td>
                            <td>
                                <c:choose>
                                    <c:when test="${not empty t.deadline}">
                                        <c:choose>
                                            <c:when test="${t.status eq 'PENDING' and t.deadline < now}"><span class="text-danger">${t.deadline} (已逾期)</span></c:when>
                                            <c:otherwise>${t.deadline}</c:otherwise>
                                        </c:choose>
                                    </c:when>
                                    <c:otherwise><span class="text-muted">无期限</span></c:otherwise>
                                </c:choose>
                            </td>
                            <td><span class="badge badge-${t.status eq 'PENDING' ? 'pending' : t.status eq 'SUBMITTED' ? 'submitted' : t.status eq 'APPROVED' ? 'approved' : 'rejected'}">${t.status eq 'PENDING' ? '待提交' : t.status eq 'SUBMITTED' ? '已提交' : t.status eq 'APPROVED' ? '已通过' : '已驳回'}</span></td>
                            <td><a href="${pageContext.request.contextPath}/user/rectification/detail?id=${t.id}" class="btn btn-small btn-primary">详情</a></td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty pageResult.data}">
                        <tr><td colspan="5">
                            <div class="empty-state">
                                <div class="empty-state-icon">📋</div>
                                <div class="empty-state-title">暂无整改任务</div>
                                <div class="empty-state-desc">当前没有待整改的任务</div>
                            </div>
                        </td></tr>
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
</body>
</html>
