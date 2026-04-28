<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>分类知识 - 垃圾分类监管系统</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
    <jsp:include page="/WEB-INF/jsp/nav-user.jsp" />

    <div class="main-content">
        <div class="card">
            <h2 class="card-title">垃圾分类知识</h2>

            <div class="filter-tabs">
                <a href="${pageContext.request.contextPath}/user/knowledge/list" class="filter-tab ${empty currentType ? 'active' : ''}">全部</a>
                <a href="${pageContext.request.contextPath}/user/knowledge/list?garbageType=可回收物" class="filter-tab ${currentType == '可回收物' ? 'active' : ''}">可回收物</a>
                <a href="${pageContext.request.contextPath}/user/knowledge/list?garbageType=厨余垃圾" class="filter-tab ${currentType == '厨余垃圾' ? 'active' : ''}">厨余垃圾</a>
                <a href="${pageContext.request.contextPath}/user/knowledge/list?garbageType=有害垃圾" class="filter-tab ${currentType == '有害垃圾' ? 'active' : ''}">有害垃圾</a>
                <a href="${pageContext.request.contextPath}/user/knowledge/list?garbageType=其他垃圾" class="filter-tab ${currentType == '其他垃圾' ? 'active' : ''}">其他垃圾</a>
            </div>

            <div class="knowledge-grid">
                <c:forEach items="${knowledgeList}" var="kb">
                    <div class="knowledge-card">
                        <h4>${kb.title}</h4>
                        <p><span class="badge ${kb.garbageType == '可回收物' ? 'category-recyclable' : kb.garbageType == '厨余垃圾' ? 'category-kitchen' : kb.garbageType == '有害垃圾' ? 'category-hazardous' : 'category-other'}">${kb.garbageType}</span></p>
                        <p style="margin-top:8px;">${kb.content}</p>
                    </div>
                </c:forEach>
                <c:if test="${empty knowledgeList}">
                    <p class="text-muted" style="text-align:center;padding:40px;">暂无知识条目</p>
                </c:if>
            </div>
        </div>
    </div>
</body>
</html>
