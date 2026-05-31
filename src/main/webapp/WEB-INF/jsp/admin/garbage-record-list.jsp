<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
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
                <form class="search-form" method="get" action="${pageContext.request.contextPath}/admin/garbage-record/list">
                    <input type="text" name="keyword" class="search-input" placeholder="搜索关键词" value="${param.keyword}">
                    <select name="status" class="search-input" style="width:150px;">
                        <option value="">全部状态</option>
                        <option value="PENDING" ${param.status == 'PENDING' ? 'selected' : ''}>待复核</option>
                        <option value="REVIEWED" ${param.status == 'REVIEWED' ? 'selected' : ''}>已复核</option>
                    </select>
                    <button type="submit" class="btn btn-primary">搜索</button>
                </form>
            </div>

            <table class="table">
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>用户ID</th>
                        <th>图片</th>
                        <th>识别摘要</th>
                        <th>推荐</th>
                        <th>选择</th>
                        <th>正确</th>
                        <th>状态</th>
                        <th>时间</th>
                        <th>操作</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach items="${pageResult.data}" var="r">
                        <tr>
                            <td><c:out value="${r.id}"/></td>
                            <td><c:out value="${r.userId}"/></td>
                            <td>
                                <c:if test="${not empty r.imageName}">
                                    <img data-src="${pageContext.request.contextPath}/image/input/${r.imageName}"
                                         alt="" style="width:60px;height:45px;object-fit:cover;border-radius:6px;background:rgba(255,255,255,0.06);"
                                         onerror="this.style.display='none';this.parentNode.innerHTML='<span class=text-muted>无图片</span>'">
                                </c:if>
                                <c:if test="${empty r.imageName}">
                                    <span class="text-muted">无图片</span>
                                </c:if>
                            </td>
                            <td><c:out value="${r.detectedSummary}"/></td>
                            <td><span class="badge ${r.recommendedCategory == '可回收物' ? 'category-recyclable' : r.recommendedCategory == '厨余垃圾' ? 'category-kitchen' : r.recommendedCategory == '有害垃圾' ? 'category-hazardous' : r.recommendedCategory == '其他垃圾' ? 'category-other' : 'category-mixed'}"><c:out value="${r.recommendedCategory}"/></span></td>
                            <td><span class="badge ${r.selectedCategory == '可回收物' ? 'category-recyclable' : r.selectedCategory == '厨余垃圾' ? 'category-kitchen' : r.selectedCategory == '有害垃圾' ? 'category-hazardous' : r.selectedCategory == '其他垃圾' ? 'category-other' : 'category-mixed'}"><c:out value="${r.selectedCategory}"/></span></td>
                            <td>${r.isCorrect == 1 ? '✓' : '✗'}</td>
                            <td><span class="badge ${r.status == 'PENDING' ? 'badge-pending' : 'badge-rectified'}">${r.status == 'PENDING' ? '待复核' : '已复核'}</span></td>
                            <td><fmt:formatDate value="${r.createTime}" pattern="MM-dd HH:mm"/></td>
                            <td class="actions"><a href="${pageContext.request.contextPath}/admin/garbage-record/detail?id=${r.id}" class="btn btn-small btn-primary">详情</a></td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty pageResult.data}"><tr><td colspan="10"><div class="empty-state"><div class="empty-state-icon">📋</div><div class="empty-state-title">暂无投放记录</div><div class="empty-state-desc">还没有任何垃圾投放记录数据</div></div></td></tr></c:if>
                </tbody>
            </table>

            <c:if test="${pageResult.totalPages > 1}">
                <div class="pagination">
                    <c:if test="${pageResult.page > 1}">
                        <a href="?page=${pageResult.page - 1}&keyword=${param.keyword}&status=${param.status}">上一页</a>
                    </c:if>
                    <span class="active">${pageResult.page} / ${pageResult.totalPages}</span>
                    <c:if test="${pageResult.page < pageResult.totalPages}">
                        <a href="?page=${pageResult.page + 1}&keyword=${param.keyword}&status=${param.status}">下一页</a>
                    </c:if>
                </div>
            </c:if>
        </div>
    </div>

<script>
window._pageConfig = {
    contextPath: '${fn:escapeXml(pageContext.request.contextPath)}',
    csrfToken: '${fn:escapeXml(sessionScope._csrfToken)}'
};
</script>
<script src="${pageContext.request.contextPath}/js/common.js"></script>
<script>
(function() {
    'use strict';
    // 异步搜索
    initLiveSearch({
        inputSelector: '.search-input[name="keyword"]',
        formSelector: '.search-form',
        containerSelector: '.card'
    });
    // 异步分页
    initAjaxPagination({ containerSelector: '.card', paginationSelector: '.pagination' });
    // 图片懒加载
    initLazyLoad();
})();
</script>
</body>
</html>
