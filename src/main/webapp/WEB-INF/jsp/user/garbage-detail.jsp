<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>投放详情 - 垃圾分类监管系统</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
    <jsp:include page="/WEB-INF/jsp/nav-user.jsp" />

    <div class="main-content">
        <div class="card">
            <h2 class="card-title">投放记录详情</h2>

            <c:set var="r" value="${detail.record}" />

            <div class="image-pair">
                <div class="image-box">
                    <c:if test="${not empty r.imagePath}">
                        <img src="${pageContext.request.contextPath}/image/input/${r.imageName}" alt="原图">
                    </c:if>
                    <p>原图</p>
                </div>
                <div class="image-box">
                    <c:if test="${not empty r.resultImagePath}">
                        <img id="resultImg" alt="结果图">
                    </c:if>
                    <p>检测结果图</p>
                </div>
            </div>

            <div class="info-grid">
                <div class="info-item"><span class="info-label">图片名称</span><span class="info-value">${r.imageName}</span></div>
                <div class="info-item"><span class="info-label">识别摘要</span><span class="info-value">${r.detectedSummary}</span></div>
                <div class="info-item"><span class="info-label">推荐类别</span><span class="info-value"><span class="badge ${r.recommendedCategory == '可回收物' ? 'category-recyclable' : r.recommendedCategory == '厨余垃圾' ? 'category-kitchen' : r.recommendedCategory == '有害垃圾' ? 'category-hazardous' : r.recommendedCategory == '其他垃圾' ? 'category-other' : 'category-mixed'}">${r.recommendedCategory}</span></span></div>
                <div class="info-item"><span class="info-label">选择类别</span><span class="info-value"><span class="badge ${r.selectedCategory == '可回收物' ? 'category-recyclable' : r.selectedCategory == '厨余垃圾' ? 'category-kitchen' : r.selectedCategory == '有害垃圾' ? 'category-hazardous' : r.selectedCategory == '其他垃圾' ? 'category-other' : 'category-mixed'}">${r.selectedCategory}</span></span></div>
                <div class="info-item"><span class="info-label">最终类别</span><span class="info-value"><span class="badge ${r.finalCategory == '可回收物' ? 'category-recyclable' : r.finalCategory == '厨余垃圾' ? 'category-kitchen' : r.finalCategory == '有害垃圾' ? 'category-hazardous' : r.finalCategory == '其他垃圾' ? 'category-other' : 'category-mixed'}">${empty r.finalCategory ? '-' : r.finalCategory}</span></span></div>
                <div class="info-item"><span class="info-label">是否正确</span><span class="info-value">${r.isCorrect == 1 ? '<span class=text-success>✓ 正确</span>' : '<span class=text-danger>✗ 错误</span>'}</span></div>
                <div class="info-item"><span class="info-label">是否混投</span><span class="info-value">${r.isMixed == 1 ? '<span class=text-warning>是</span>' : '否'}</span></div>
                <div class="info-item"><span class="info-label">状态</span><span class="info-value"><span class="badge ${r.status == 'PENDING' ? 'badge-pending' : 'badge-reviewed'}">${r.status == 'PENDING' ? '待复核' : '已复核'}</span></span></div>
                <div class="info-item"><span class="info-label">创建时间</span><span class="info-value"><fmt:formatDate value="${r.createTime}" pattern="yyyy-MM-dd HH:mm:ss"/></span></div>
                <div class="info-item"><span class="info-label">备注</span><span class="info-value">${empty r.remark ? '-' : r.remark}</span></div>
            </div>
        </div>

        <!-- 检测明细 -->
        <c:if test="${not empty detail.detections}">
            <div class="card">
                <h2 class="card-title">检测明细</h2>
                <table class="table">
                    <thead><tr><th>类别名称</th><th>置信度</th><th>映射类别</th></tr></thead>
                    <tbody>
                        <c:forEach items="${detail.detections}" var="d">
                            <tr><td>${d.className}</td><td>${d.confidence}%</td><td><span class="badge ${d.mappedCategory == '可回收物' ? 'category-recyclable' : d.mappedCategory == '厨余垃圾' ? 'category-kitchen' : d.mappedCategory == '有害垃圾' ? 'category-hazardous' : d.mappedCategory == '其他垃圾' ? 'category-other' : 'category-mixed'}">${d.mappedCategory}</span></td></tr>
                        </c:forEach>
                    </tbody>
                </table>
            </div>
        </c:if>

        <!-- 整改信息 -->
        <c:if test="${not empty detail.rectification}">
            <div class="card">
                <h2 class="card-title">整改任务</h2>
                <div class="info-grid">
                    <div class="info-item"><span class="info-label">整改要求</span><span class="info-value">${detail.rectification.requirement}</span></div>
                    <div class="info-item"><span class="info-label">整改期限</span><span class="info-value">${empty detail.rectification.deadline ? '无期限' : detail.rectification.deadline}</span></div>
                    <div class="info-item"><span class="info-label">状态</span><span class="info-value"><span class="badge badge-${detail.rectification.status eq 'PENDING' ? 'pending' : detail.rectification.status eq 'SUBMITTED' ? 'submitted' : detail.rectification.status eq 'APPROVED' ? 'approved' : 'rejected'}">${detail.rectification.status eq 'PENDING' ? '待提交' : detail.rectification.status eq 'SUBMITTED' ? '已提交' : detail.rectification.status eq 'APPROVED' ? '已通过' : '已驳回'}</span></span></div>
                </div>
                <div style="margin-top:12px;">
                    <a href="${pageContext.request.contextPath}/user/rectification/detail?id=${detail.rectification.id}" class="btn btn-small btn-primary">查看整改详情</a>
                </div>
            </div>
        </c:if>

        <div style="margin-top:20px;">
            <a href="${pageContext.request.contextPath}/user/garbage-record/list" class="btn btn-secondary">返回列表</a>
        </div>
    </div>

<script>
window._pageConfig = {
    contextPath: '${pageContext.request.contextPath}',
    csrfToken: '${sessionScope._csrfToken}',
    resultImagePath: '${r.resultImagePath}'
};
</script>
<script src="${pageContext.request.contextPath}/js/common.js"></script>
<script src="${pageContext.request.contextPath}/js/garbage-detail.js"></script>
</body>
</html>
