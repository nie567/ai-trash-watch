<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>仪表盘 - 垃圾分类监管系统</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
    <jsp:include page="/WEB-INF/jsp/nav-admin.jsp" />
    
    <div class="main-content">
        <!-- 用户统计 -->
        <div class="stats-grid">
            <div class="stat-card blue">
                <div class="stat-value">${stats.totalUsers}</div>
                <div class="stat-label">用户总数</div>
            </div>
            <div class="stat-card green">
                <div class="stat-value">${stats.todayNew}</div>
                <div class="stat-label">今日新增</div>
            </div>
            <div class="stat-card orange">
                <div class="stat-value">${stats.adminCount}</div>
                <div class="stat-label">管理员数量</div>
            </div>
            <div class="stat-card">
                <div class="stat-value">${stats.userCount}</div>
                <div class="stat-label">普通用户数量</div>
            </div>
        </div>

        <!-- 垃圾分类统计概览 -->
        <div class="stats-grid">
            <div class="stat-card green">
                <div class="stat-value">${stats.totalRecords}</div>
                <div class="stat-label">总投放数</div>
            </div>
            <div class="stat-card blue">
                <div class="stat-value">${stats.correctCount}</div>
                <div class="stat-label">正确投放</div>
            </div>
            <div class="stat-card red">
                <div class="stat-value">${stats.wrongCount}</div>
                <div class="stat-label">错误投放</div>
            </div>
            <div class="stat-card orange">
                <div class="stat-value">${stats.totalRecords > 0 ? (stats.correctCount * 100 / stats.totalRecords) : 0}%</div>
                <div class="stat-label">正确率</div>
            </div>
        </div>

        <!-- 快捷操作 -->
        <div class="card">
            <h2 class="card-title">快捷操作</h2>
            <div style="display:flex;gap:12px;flex-wrap:wrap;">
                <a href="${pageContext.request.contextPath}/admin/users" class="btn btn-primary">用户管理</a>
                <a href="${pageContext.request.contextPath}/admin/garbage-record/list" class="btn btn-success">投放记录</a>
                <a href="${pageContext.request.contextPath}/admin/violation/list" class="btn btn-warning">违规管理</a>
                <a href="${pageContext.request.contextPath}/admin/statistics" class="btn btn-primary">统计分析</a>
                <a href="${pageContext.request.contextPath}/admin/rule/list" class="btn btn-secondary">分类规则</a>
                <a href="${pageContext.request.contextPath}/admin/knowledge/list" class="btn btn-secondary">知识库管理</a>
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
                                <td>${log.username}</td>
                                <td>${log.action}</td>
                                <td>${log.target}</td>
                                <td>${log.detail}</td>
                                <td><fmt:formatDate value="${log.createTime}" pattern="MM-dd HH:mm"/></td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </div>
        </c:if>
    </div>
</body>
</html>
