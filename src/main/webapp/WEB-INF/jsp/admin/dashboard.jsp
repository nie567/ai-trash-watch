<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>仪表盘 - 垃圾分类监管系统</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <script src="${pageContext.request.contextPath}/js/lib/echarts.min.js"></script>
    <script src="${pageContext.request.contextPath}/js/lib/dayjs.min.js"></script>
</head>
<body>
    <jsp:include page="/WEB-INF/jsp/nav-admin.jsp" />
    
    <div class="main-content">
        <!-- 用户统计 -->
        <div class="stats-grid">
            <div class="stat-card blue">
                <div class="stat-icon"><svg viewBox="0 0 24 24"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg></div>
                <div class="stat-info"><div class="stat-value" id="statTotalUsers"><c:out value="${stats.totalUsers}"/></div><div class="stat-label">用户总数</div></div>
            </div>
            <div class="stat-card orange">
                <div class="stat-icon"><svg viewBox="0 0 24 24"><path d="M12 2v4M12 18v4M4.93 4.93l2.83 2.83M16.24 16.24l2.83 2.83M2 12h4M18 12h4M4.93 19.07l2.83-2.83M16.24 7.76l2.83-2.83"/></svg></div>
                <div class="stat-info"><div class="stat-value" id="statTodayNew"><c:out value="${stats.todayNew}"/></div><div class="stat-label">今日新增</div></div>
            </div>
            <div class="stat-card blue">
                <div class="stat-icon"><svg viewBox="0 0 24 24"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/></svg></div>
                <div class="stat-info"><div class="stat-value" id="statAdminCount"><c:out value="${stats.adminCount}"/></div><div class="stat-label">管理员数量</div></div>
            </div>
            <div class="stat-card blue">
                <div class="stat-icon"><svg viewBox="0 0 24 24"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg></div>
                <div class="stat-info"><div class="stat-value" id="statUserCount"><c:out value="${stats.userCount}"/></div><div class="stat-label">普通用户数量</div></div>
            </div>
        </div>

        <!-- 垃圾分类统计概览 -->
        <div class="stats-grid">
            <div class="stat-card blue">
                <div class="stat-icon"><svg viewBox="0 0 24 24"><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/><polyline points="3.27 6.96 12 12.01 20.73 6.96"/><line x1="12" y1="22.08" x2="12" y2="12"/></svg></div>
                <div class="stat-info"><div class="stat-value" id="statTotalRecords"><c:out value="${stats.totalRecords}"/></div><div class="stat-label">总投放数</div></div>
            </div>
            <div class="stat-card green">
                <div class="stat-icon"><svg viewBox="0 0 24 24"><polyline points="20 6 9 17 4 12"/></svg></div>
                <div class="stat-info"><div class="stat-value" id="statCorrectCount"><c:out value="${stats.correctCount}"/></div><div class="stat-label">正确投放</div></div>
            </div>
            <div class="stat-card red">
                <div class="stat-icon"><svg viewBox="0 0 24 24"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg></div>
                <div class="stat-info"><div class="stat-value" id="statWrongCount"><c:out value="${stats.wrongCount}"/></div><div class="stat-label">错误投放</div></div>
            </div>
            <div class="stat-card green">
                <div class="stat-icon"><svg viewBox="0 0 24 24"><line x1="18" y1="20" x2="18" y2="10"/><line x1="12" y1="20" x2="12" y2="4"/><line x1="6" y1="20" x2="6" y2="14"/></svg></div>
                <div class="stat-info"><div class="stat-value" id="statCorrectRate"><fmt:formatNumber value="${stats.totalRecords > 0 ? (stats.correctCount * 100.0 / stats.totalRecords) : 0}" pattern="#0.0" />%</div><div class="stat-label">正确率</div></div>
            </div>
        </div>

        <!-- ECharts 图表区 -->
        <div style="display:grid;grid-template-columns:1fr 1fr;gap:20px;margin-bottom:20px;">
            <div class="card">
                <h2 class="card-title">投放正确率</h2>
                <div id="pieChart" style="height:300px;"></div>
            </div>
            <div class="card">
                <h2 class="card-title">近7日投放趋势</h2>
                <div id="trendChart" style="height:300px;"></div>
            </div>
        </div>

        <!-- 最近操作日志 -->
        <c:if test="${not empty recentLogs}">
            <div class="card">
                <h2 class="card-title">最近操作日志</h2>
                <table class="table">
                    <thead>
                        <tr><th>用户</th><th>操作</th><th>目标</th><th>详情</th><th>时间</th></tr>
                    </thead>
                    <tbody>
                        <c:forEach items="${recentLogs}" var="log">
                            <tr>
                                <td><c:out value="${log.username}"/></td>
                                <td><c:out value="${log.action}"/></td>
                                <td><c:out value="${log.target}"/></td>
                                <td><c:out value="${log.detail}"/></td>
                                <td><fmt:formatDate value="${log.createTime}" pattern="MM-dd HH:mm"/></td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </div>
        </c:if>
    </div>

<script>
window._pageConfig = {
    contextPath: '${fn:escapeXml(pageContext.request.contextPath)}',
    csrfToken: '${fn:escapeXml(sessionScope._csrfToken)}',
    correctCount: ${stats.correctCount},
    wrongCount: ${stats.wrongCount},
    totalRecords: ${stats.totalRecords},
    trendData: [<c:forEach items="${trendData}" var="td" varStatus="idx"><c:if test="${idx.index > 0}">,</c:if>${td}</c:forEach>]
};
</script>
<script src="${pageContext.request.contextPath}/js/common.js"></script>
<script src="${pageContext.request.contextPath}/js/dashboard.js"></script>
</body>
</html>
