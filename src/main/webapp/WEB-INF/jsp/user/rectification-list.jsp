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
                                    <c:when test="${not empty t.deadline}">${t.deadline}</c:when>
                                    <c:otherwise>无期限</c:otherwise>
                                </c:choose>
                            </td>
                            <td><span class="badge badge-${t.status eq 'PENDING' ? 'pending' : t.status eq 'SUBMITTED' ? 'submitted' : t.status eq 'APPROVED' ? 'approved' : 'rejected'}">${t.status}</span></td>
                            <td><a href="${pageContext.request.contextPath}/user/rectification/detail?id=${t.id}" class="btn btn-small btn-primary">详情</a></td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty pageResult.data}">
                        <tr><td colspan="5" class="text-center text-muted">暂无整改任务</td></tr>
                    </c:if>
                </tbody>
            </table>

            <c:if test="${pageResult.totalPages > 1}">
                <div class="pagination">
                    <c:if test="${pageResult.page > 1}"><a href="?page=${pageResult.page - 1}">上一页</a></c:if>
                    <span class="active">${pageResult.page} / ${pageResult.totalPages}</span>
                    <c:if test="${pageResult.page < pageResult.totalPages}"><a href="?page=${pageResult.page + 1}">下一页</a></c:if>
                </div>
            </c:if>
        </div>
    </div>
</body>
</html>
