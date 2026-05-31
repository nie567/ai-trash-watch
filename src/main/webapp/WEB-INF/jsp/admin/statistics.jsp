<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>统计分析 - 垃圾分类监管系统</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <script src="${pageContext.request.contextPath}/js/lib/echarts.min.js"></script>
</head>
<body>
    <jsp:include page="/WEB-INF/jsp/nav-admin.jsp" />

    <div class="main-content">
        <!-- 概览卡片 -->
        <div class="stats-grid">
            <div class="stat-card blue">
                <div class="stat-icon"><svg viewBox="0 0 24 24"><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/><polyline points="3.27 6.96 12 12.01 20.73 6.96"/><line x1="12" y1="22.08" x2="12" y2="12"/></svg></div>
                <div class="stat-info"><div class="stat-value"><c:out value="${correctCount + wrongCount}"/></div><div class="stat-label">总投放数</div></div>
            </div>
            <div class="stat-card green">
                <div class="stat-icon"><svg viewBox="0 0 24 24"><polyline points="20 6 9 17 4 12"/></svg></div>
                <div class="stat-info"><div class="stat-value"><c:out value="${correctCount}"/></div><div class="stat-label">正确投放</div></div>
            </div>
            <div class="stat-card red">
                <div class="stat-icon"><svg viewBox="0 0 24 24"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg></div>
                <div class="stat-info"><div class="stat-value"><c:out value="${wrongCount}"/></div><div class="stat-label">错误投放</div></div>
            </div>
            <div class="stat-card green">
                <div class="stat-icon"><svg viewBox="0 0 24 24"><line x1="18" y1="20" x2="18" y2="10"/><line x1="12" y1="20" x2="12" y2="4"/><line x1="6" y1="20" x2="6" y2="14"/></svg></div>
                <div class="stat-info"><div class="stat-value"><fmt:formatNumber value="${correctCount + wrongCount > 0 ? (correctCount * 100.0 / (correctCount + wrongCount)) : 0}" pattern="#0.0" />%</div><div class="stat-label">正确率</div></div>
            </div>
        </div>

        <!-- 图表区域 -->
        <div style="display:grid;grid-template-columns:1fr 1fr;gap:20px;margin-bottom:20px;">
            <div class="card">
                <h3 class="card-title">垃圾类别分布</h3>
                <div class="chart-container" id="pieChart"></div>
            </div>
            <div class="card">
                <h3 class="card-title">正确/错误投放对比</h3>
                <div class="chart-container" id="barChart"></div>
            </div>
        </div>

        <div style="display:grid;grid-template-columns:1fr 1fr;gap:20px;margin-bottom:20px;">
            <div class="card">
                <h3 class="card-title">违规类型分布</h3>
                <div class="chart-container" id="violationTypeChart"></div>
            </div>
            <div class="card">
                <h3 class="card-title">违规等级分布</h3>
                <div class="chart-container" id="violationLevelChart"></div>
            </div>
        </div>

        <!-- 违规排名 -->
        <div class="card">
            <h3 class="card-title">用户违规次数排名</h3>
            <table class="table">
                <thead>
                    <tr><th>排名</th><th>用户名</th><th>违规次数</th></tr>
                </thead>
                <tbody>
                    <c:forEach items="${violationRank}" var="rank" varStatus="idx">
                        <tr>
                            <td>${idx.index + 1}</td>
                            <td>${fn:escapeXml(rank.username)}</td>
                            <td><c:out value="${rank.violationCount}"/></td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty violationRank}">
                        <tr><td colspan="3" class="text-center" style="color:var(--text-muted)">暂无数据</td></tr>
                    </c:if>
                </tbody>
            </table>
        </div>
    </div>

<script>
window._pageConfig = {
    contextPath: '${fn:escapeXml(pageContext.request.contextPath)}',
    csrfToken: '${fn:escapeXml(sessionScope._csrfToken)}',
    correctCount: ${correctCount},
    wrongCount: ${wrongCount},
    pieData: [<c:forEach items="${typeCounts}" var="tc" varStatus="idx"><c:if test="${idx.index > 0}">,</c:if>{name:'${fn:escapeXml(tc.type)}',value:${tc.count}}</c:forEach>],
    violationTypeData: [<c:forEach items="${violationTypeCounts}" var="vt" varStatus="idx"><c:if test="${idx.index > 0}">,</c:if>{name:'${fn:escapeXml(vt.type)}',value:${vt.count}}</c:forEach>],
    violationLevelData: [<c:forEach items="${violationLevelCounts}" var="vl" varStatus="idx"><c:if test="${idx.index > 0}">,</c:if>{name:'${fn:escapeXml(vl.type)}',value:${vl.count}}</c:forEach>]
};
</script>
<script src="${pageContext.request.contextPath}/js/common.js"></script>
<script src="${pageContext.request.contextPath}/js/statistics.js"></script>
</body>
</html>
