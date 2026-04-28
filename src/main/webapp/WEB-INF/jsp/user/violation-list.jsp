<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>我的违规 - 垃圾分类监管系统</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
    <jsp:include page="/WEB-INF/jsp/nav-user.jsp" />

    <div class="main-content">
        <div class="card">
            <h2 class="card-title">我的违规记录</h2>

            <table class="table">
                <thead>
                    <tr>
                        <th>时间</th>
                        <th>关联投放记录</th>
                        <th>违规类型</th>
                        <th>违规级别</th>
                        <th>描述</th>
                        <th>状态</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach items="${pageResult.data}" var="v">
                        <tr>
                            <td><fmt:formatDate value="${v.createTime}" pattern="MM-dd HH:mm"/></td>
                            <td><a href="${pageContext.request.contextPath}/user/garbage-record/detail?id=${v.recordId}">${v.recordId}</a></td>
                            <td>${v.violationType}</td>
                            <td><span class="badge ${v.level == 'LOW' ? 'badge-pending' : v.level == 'MEDIUM' ? 'badge-submitted' : 'badge-rejected'}">${v.level}</span></td>
                            <td>${v.description}</td>
                            <td><span class="badge ${v.status == 'PENDING' ? 'badge-pending' : v.status == 'RECTIFIED' ? 'badge-rectified' : 'badge-ignored'}">${v.status == 'PENDING' ? '待处理' : v.status == 'RECTIFIED' ? '已整改' : '已忽略'}</span></td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty pageResult.data}">
                        <tr><td colspan="6" class="text-center text-muted">暂无违规记录</td></tr>
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
