<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>投放记录管理 - 垃圾分类监管系统</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
    <jsp:include page="/WEB-INF/jsp/nav-admin.jsp" />

    <div class="main-content">
        <div class="card">
            <h2 class="card-title">投放记录管理</h2>

            <div class="toolbar">
                <form class="search-form" method="get">
                    <input type="text" name="keyword" class="search-input" placeholder="搜索关键词" value="${keyword}">
                    <select name="status" class="search-input" style="width:150px;">
                        <option value="">全部状态</option>
                        <option value="PENDING" ${status == 'PENDING' ? 'selected' : ''}>待复核</option>
                        <option value="REVIEWED" ${status == 'REVIEWED' ? 'selected' : ''}>已复核</option>
                    </select>
                    <button type="submit" class="btn btn-primary">搜索</button>
                </form>
            </div>

            <table class="table">
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>用户ID</th>
                        <th>时间</th>
                        <th>图片</th>
                        <th>推荐类别</th>
                        <th>选择类别</th>
                        <th>是否正确</th>
                        <th>状态</th>
                        <th>操作</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach items="${pageResult.data}" var="record">
                        <tr>
                            <td>${record.id}</td>
                            <td>${record.userId}</td>
                            <td><fmt:formatDate value="${record.createTime}" pattern="MM-dd HH:mm"/></td>
                            <td>${record.imageName}</td>
                            <td><span class="badge ${record.recommendedCategory == '可回收物' ? 'category-recyclable' : record.recommendedCategory == '厨余垃圾' ? 'category-kitchen' : record.recommendedCategory == '有害垃圾' ? 'category-hazardous' : record.recommendedCategory == '其他垃圾' ? 'category-other' : 'category-mixed'}">${record.recommendedCategory}</span></td>
                            <td><span class="badge ${record.selectedCategory == '可回收物' ? 'category-recyclable' : record.selectedCategory == '厨余垃圾' ? 'category-kitchen' : record.selectedCategory == '有害垃圾' ? 'category-hazardous' : record.selectedCategory == '其他垃圾' ? 'category-other' : 'category-mixed'}">${record.selectedCategory}</span></td>
                            <td>${record.isCorrect == 1 ? '<span class=text-success>正确</span>' : '<span class=text-danger>错误</span>'}</td>
                            <td><span class="badge ${record.status == 'PENDING' ? 'badge-pending' : 'badge-reviewed'}">${record.status == 'PENDING' ? '待复核' : '已复核'}</span></td>
                            <td><a href="${pageContext.request.contextPath}/admin/garbage-record/detail?id=${record.id}" class="btn btn-small btn-primary">详情</a></td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty pageResult.data}">
                        <tr><td colspan="9" class="text-center text-muted">暂无数据</td></tr>
                    </c:if>
                </tbody>
            </table>

            <c:if test="${pageResult.totalPages > 1}">
                <div class="pagination">
                    <c:if test="${pageResult.page > 1}">
                        <a href="?page=${pageResult.page - 1}&keyword=${keyword}&status=${status}">上一页</a>
                    </c:if>
                    <span class="active">${pageResult.page} / ${pageResult.totalPages}</span>
                    <c:if test="${pageResult.page < pageResult.totalPages}">
                        <a href="?page=${pageResult.page + 1}&keyword=${keyword}&status=${status}">下一页</a>
                    </c:if>
                </div>
            </c:if>
        </div>
    </div>
</body>
</html>
