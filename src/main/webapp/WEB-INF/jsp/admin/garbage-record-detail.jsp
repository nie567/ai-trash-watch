<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>投放详情 - 垃圾分类监管系统</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
    <jsp:include page="/WEB-INF/jsp/nav-admin.jsp" />

    <div class="main-content">
        <div class="card">
            <h2 class="card-title">投放记录详情</h2>
            <c:set var="r" value="${detail.record}" />

            <div class="image-pair">
                <div class="image-box">
                    <c:if test="${not empty r.imageName}">
                        <img src="${pageContext.request.contextPath}/image/input/${r.imageName}" alt="原图"
                             onerror="this.style.display='none';this.parentNode.innerHTML+='<p class=text-muted>图片文件不存在</p>'">
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
                <div class="info-item"><span class="info-label">ID</span><span class="info-value"><c:out value="${r.id}"/></span></div>
                <div class="info-item"><span class="info-label">用户ID</span><span class="info-value"><c:out value="${r.userId}"/></span></div>
                <div class="info-item"><span class="info-label">图片名称</span><span class="info-value"><c:out value="${r.imageName}"/></span></div>
                <div class="info-item"><span class="info-label">识别摘要</span><span class="info-value"><c:out value="${r.detectedSummary}"/></span></div>
                <div class="info-item"><span class="info-label">推荐类别</span><span class="info-value"><span class="badge ${r.recommendedCategory == '可回收物' ? 'category-recyclable' : r.recommendedCategory == '厨余垃圾' ? 'category-kitchen' : r.recommendedCategory == '有害垃圾' ? 'category-hazardous' : r.recommendedCategory == '其他垃圾' ? 'category-other' : 'category-mixed'}"><c:out value="${r.recommendedCategory}"/></span></span></div>
                <div class="info-item"><span class="info-label">选择类别</span><span class="info-value"><span class="badge ${r.selectedCategory == '可回收物' ? 'category-recyclable' : r.selectedCategory == '厨余垃圾' ? 'category-kitchen' : r.selectedCategory == '有害垃圾' ? 'category-hazardous' : r.selectedCategory == '其他垃圾' ? 'category-other' : 'category-mixed'}"><c:out value="${r.selectedCategory}"/></span></span></div>
                <div class="info-item"><span class="info-label">最终类别</span><span class="info-value"><span class="badge ${r.finalCategory == '可回收物' ? 'category-recyclable' : r.finalCategory == '厨余垃圾' ? 'category-kitchen' : r.finalCategory == '有害垃圾' ? 'category-hazardous' : r.finalCategory == '其他垃圾' ? 'category-other' : 'category-mixed'}"><c:out value="${empty r.finalCategory ? '-' : r.finalCategory}"/></span></span></div>
                <div class="info-item"><span class="info-label">是否正确</span><span class="info-value"><c:choose><c:when test="${r.isCorrect == 1}"><span class="text-success">✓ 正确</span></c:when><c:otherwise><span class="text-danger">✗ 错误</span></c:otherwise></c:choose></span></div>
                <div class="info-item"><span class="info-label">是否混投</span><span class="info-value"><c:choose><c:when test="${r.isMixed == 1}"><span class="text-warning">是</span></c:when><c:otherwise>否</c:otherwise></c:choose></span></div>
                <div class="info-item"><span class="info-label">状态</span><span class="info-value"><span class="badge ${r.status == 'PENDING' ? 'badge-pending' : 'badge-reviewed'}">${r.status == 'PENDING' ? '待复核' : '已复核'}</span></span></div>
                <div class="info-item"><span class="info-label">复核意见</span><span class="info-value"><c:out value="${empty r.reviewComment ? '-' : r.reviewComment}"/></span></div>
                <div class="info-item"><span class="info-label">创建时间</span><span class="info-value"><fmt:formatDate value="${r.createTime}" pattern="yyyy-MM-dd HH:mm:ss"/></span></div>
            </div>

        <!-- 检测明细 -->
        <c:if test="${not empty detail.detections}">
            <div class="card">
                <h2 class="card-title">检测明细</h2>
                <table class="table">
                    <thead><tr><th>类别名称</th><th>置信度</th><th>映射类别</th></tr></thead>
                    <tbody>
                        <c:forEach items="${detail.detections}" var="d">
                            <tr><td><c:out value="${d.className}"/></td><td><c:out value="${d.confidence}%"/></td><td><c:out value="${d.mappedCategory}"/></td></tr>
                        </c:forEach>
                    </tbody>
                </table>
            </div>
        </c:if>

        <!-- 复核表单 -->
        <c:if test="${r.status == 'PENDING'}">
            <div class="card">
                <h2 class="card-title">人工复核</h2>
                <form id="reviewForm">
                    <div class="form-group">
                        <label>最终类别 <span class="required">*</span></label>
                        <select id="finalCategory" name="finalCategory">
                            <option value="可回收物">可回收物</option>
                            <option value="厨余垃圾">厨余垃圾</option>
                            <option value="有害垃圾">有害垃圾</option>
                            <option value="其他垃圾">其他垃圾</option>
                            <option value="混合待分拣">混合待分拣</option>
                        </select>
                    </div>
                    <div class="form-group">
                        <label>复核意见</label>
                        <textarea id="reviewComment" name="reviewComment" rows="3" placeholder="请输入复核意见"></textarea>
                    </div>
                    <button type="button" class="btn btn-success" onclick="submitReview()">提交复核</button>
                    <span id="reviewStatus" style="margin-left:10px;"></span>
                </form>
            </div>
        </c:if>

        <div style="margin-top:20px;">
            <a href="${pageContext.request.contextPath}/admin/garbage-record/list" class="btn btn-secondary">返回列表</a>
        </div>
    </div>

<script>
window._pageConfig = {
    contextPath: '${fn:escapeXml(pageContext.request.contextPath)}',
    csrfToken: '${fn:escapeXml(sessionScope._csrfToken)}',
    recordId: '${fn:escapeXml(empty r.id ? "" : r.id)}',
    resultImagePath: '${fn:escapeXml(r.resultImagePath)}'
};
</script>
<script src="${pageContext.request.contextPath}/js/common.js"></script>
<script src="${pageContext.request.contextPath}/js/garbage-record-detail.js"></script>
</body>
</html>
