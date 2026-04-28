<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>统计分析 - 垃圾分类监管系统</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <script src="https://cdn.jsdelivr.net/npm/echarts@5/dist/echarts.min.js"></script>
</head>
<body>
    <jsp:include page="/WEB-INF/jsp/nav-admin.jsp" />

    <div class="main-content">
        <!-- 概览卡片 -->
        <div class="stats-grid">
            <div class="stat-card green">
                <div class="stat-value">${correctCount + wrongCount}</div>
                <div class="stat-label">总投放数</div>
            </div>
            <div class="stat-card blue">
                <div class="stat-value">${correctCount}</div>
                <div class="stat-label">正确投放</div>
            </div>
            <div class="stat-card red">
                <div class="stat-value">${wrongCount}</div>
                <div class="stat-label">错误投放</div>
            </div>
            <div class="stat-card orange">
                <div class="stat-value">${correctCount + wrongCount > 0 ? (correctCount * 100 / (correctCount + wrongCount)) : 0}%</div>
                <div class="stat-label">正确率</div>
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

        <div class="card">
            <h3 class="card-title">近7天投放趋势</h3>
            <div class="chart-container" id="lineChart"></div>
        </div>

        <!-- 违规排名 -->
        <div class="card">
            <h3 class="card-title">用户违规次数排名</h3>
            <table class="table">
                <thead><tr><th>排名</th><th>用户ID</th><th>用户名</th><th>违规次数</th></tr></thead>
                <tbody>
                    <c:forEach items="${violationRank}" var="u" varStatus="idx">
                        <tr>
                            <td>${idx.index + 1}</td>
                            <td>${u.userId}</td>
                            <td>${u.username}</td>
                            <td>${u.violationCount}</td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty violationRank}">
                        <tr><td colspan="4" class="text-center text-muted">暂无数据</td></tr>
                    </c:if>
                </tbody>
            </table>
        </div>
    </div>

<script>
// 饼图 - 垃圾类别分布
var pieChart = echarts.init(document.getElementById('pieChart'));
var pieData = [
    <c:forEach items="${typeCounts}" var="tc" varStatus="idx">
    <c:if test="${idx.index > 0}">,</c:if>{name:'${tc.type}',value:${tc.count}}
    </c:forEach>
];
pieChart.setOption({
    tooltip: {trigger: 'item'},
    legend: {bottom: 0},
    series: [{type: 'pie', radius: ['40%','70%'], data: pieData, label: {formatter: '{b}: {c}'}}]
});

// 柱状图 - 正确/错误对比
var barChart = echarts.init(document.getElementById('barChart'));
barChart.setOption({
    tooltip: {trigger: 'axis'},
    xAxis: {type: 'category', data: ['正确投放', '错误投放']},
    yAxis: {type: 'value'},
    series: [{type: 'bar', data: [${correctCount}, ${wrongCount}],
        itemStyle: {color: function(params){return params.dataIndex===0?'#27ae60':'#e74c3c';}}}]
});

// 折线图 - 趋势
var lineChart = echarts.init(document.getElementById('lineChart'));
var trendDates = [<c:forEach items="${trends}" var="t" varStatus="idx"><c:if test="${idx.index > 0}">,</c:if>'${t.date}'</c:forEach>];
var trendCounts = [<c:forEach items="${trends}" var="t" varStatus="idx"><c:if test="${idx.index > 0}">,</c:if>${t.count}</c:forEach>];
lineChart.setOption({
    tooltip: {trigger: 'axis'},
    xAxis: {type: 'category', data: trendDates},
    yAxis: {type: 'value'},
    series: [{type: 'line', data: trendCounts, smooth: true, areaStyle: {opacity: 0.3}, itemStyle: {color: '#27ae60'}}]
});

window.addEventListener('resize', function(){
    pieChart.resize(); barChart.resize(); lineChart.resize();
});
</script>
</body>
</html>
