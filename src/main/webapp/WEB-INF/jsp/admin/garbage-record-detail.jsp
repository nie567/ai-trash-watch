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
    <jsp:include page="/WEB-INF/jsp/nav-admin.jsp" />

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
                <div class="info-item"><span class="info-label">ID：</span><span class="info-value">${r.id}</span></div>
                <div class="info-item"><span class="info-label">用户ID：</span><span class="info-value">${r.userId}</span></div>
                <div class="info-item"><span class="info-label">图片名称：</span><span class="info-value">${r.imageName}</span></div>
                <div class="info-item"><span class="info-label">识别摘要：</span><span class="info-value">${r.detectedSummary}</span></div>
                <div class="info-item"><span class="info-label">推荐类别：</span><span class="info-value">${r.recommendedCategory}</span></div>
                <div class="info-item"><span class="info-label">选择类别：</span><span class="info-value">${r.selectedCategory}</span></div>
                <div class="info-item"><span class="info-label">最终类别：</span><span class="info-value">${empty r.finalCategory ? '-' : r.finalCategory}</span></div>
                <div class="info-item"><span class="info-label">是否正确：</span><span class="info-value">${r.isCorrect == 1 ? '正确' : '错误'}</span></div>
                <div class="info-item"><span class="info-label">是否混投：</span><span class="info-value">${r.isMixed == 1 ? '是' : '否'}</span></div>
                <div class="info-item"><span class="info-label">状态：</span><span class="info-value">${r.status}</span></div>
                <div class="info-item"><span class="info-label">复核意见：</span><span class="info-value">${empty r.reviewComment ? '-' : r.reviewComment}</span></div>
                <div class="info-item"><span class="info-label">创建时间：</span><span class="info-value"><fmt:formatDate value="${r.createTime}" pattern="yyyy-MM-dd HH:mm:ss"/></span></div>
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
                            <tr><td>${d.className}</td><td>${d.confidence}%</td><td>${d.mappedCategory}</td></tr>
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
// 设置结果图片路径
(function() {
    var resultImg = document.getElementById('resultImg');
    if (resultImg) {
        var resultPath = '${r.resultImagePath}';
        if (resultPath && resultPath.trim()) {
            var fileName = resultPath.replace(/\\/g, '/');
            fileName = fileName.substring(fileName.lastIndexOf('/') + 1);
            resultImg.src = '${pageContext.request.contextPath}/image/output/' + fileName;
        }
    }
})();

function submitReview() {
    var params = 'id=${r.id}&finalCategory=' + encodeURIComponent(document.getElementById('finalCategory').value)
        + '&reviewComment=' + encodeURIComponent(document.getElementById('reviewComment').value);
    fetch('${pageContext.request.contextPath}/admin/garbage-record/review', {
        method: 'POST',
        headers: {'Content-Type': 'application/x-www-form-urlencoded'},
        body: params
    }).then(r => r.json()).then(data => {
        if (data.code === 200) {
            document.getElementById('reviewStatus').innerHTML = '<span class="text-success">复核成功</span>';
            setTimeout(function(){ location.reload(); }, 1000);
        } else {
            document.getElementById('reviewStatus').innerHTML = '<span class="text-danger">' + (data.message || '复核失败') + '</span>';
        }
    }).catch(err => {
        document.getElementById('reviewStatus').innerHTML = '<span class="text-danger">操作失败</span>';
    });
}
</script>
</body>
</html>
