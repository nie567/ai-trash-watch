<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>垃圾分类推理 - 垃圾分类监管系统</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <style>
        .grid { display:grid; grid-template-columns:1fr 1fr; gap:20px; }
        @media (max-width:768px) { .grid { grid-template-columns:1fr; } }
        .image-list { list-style:none; }
        .image-list li {
            padding:10px 12px; cursor:pointer; border-radius:8px; margin-bottom:5px;
            transition:background 0.2s; display:flex; align-items:center; gap:8px;
            color:var(--text-primary);
        }
        .image-list li:hover { background:rgba(255,255,255,0.06); }
        .image-list li.selected {
            background:rgba(102,126,234,0.15); border:1px solid var(--accent-purple);
        }
        .result-area .detected {
            padding:8px 12px; background:rgba(34,197,94,0.1); border:1px solid rgba(34,197,94,0.3);
            border-radius:6px; margin-bottom:5px; font-size:14px;
        }
        .result-area .error {
            padding:8px 12px; background:rgba(239,68,68,0.1); border:1px solid rgba(239,68,68,0.3);
            border-radius:6px; margin-bottom:5px; font-size:14px; color:var(--accent-red);
        }
        .result-area .loading { color:var(--text-muted); font-size:14px; }
        .result-image { max-width:100%; border-radius:8px; margin-top:10px; border:1px solid rgba(255,255,255,0.1); }
        .empty-state { color:var(--text-muted); text-align:center; padding:30px; font-size:14px; }
    </style>
</head>
<body>
    <jsp:include page="/WEB-INF/jsp/nav-user.jsp" />

    <div class="main-content">
        <h2 class="card-title" style="margin-bottom:20px;">♻️ 垃圾分类推理</h2>
        <div class="grid">
            <!-- 左侧：图片列表 -->
            <div class="card">
                <h3 class="card-title">📁 图片列表</h3>
                <c:choose>
                    <c:when test="${empty imageFiles}">
                        <div class="empty-state">
                            <p>暂无图片文件</p>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <ul class="image-list" id="imageList">
                            <c:forEach var="file" items="${imageFiles}">
                                <li onclick="selectFile(this, '${file}')">
                                    <span>🖼️</span>
                                    <span>${file}</span>
                                </li>
                            </c:forEach>
                        </ul>
                    </c:otherwise>
                </c:choose>
                <button class="btn btn-primary" id="detectBtn" onclick="doDetect()" disabled style="width:100%;margin-top:15px;">
                    🔍 开始推理
                </button>
            </div>

            <!-- 右侧：推理结果 -->
            <div class="card">
                <h3 class="card-title">📊 推理结果</h3>
                <div id="resultArea" class="result-area">
                    <div class="empty-state">
                        <p>请选择左侧图片开始推理</p>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <script>
    window._pageConfig = {
        contextPath: '${pageContext.request.contextPath}',
        csrfToken: '${sessionScope._csrfToken}'
    };
    </script>
    <script src="${pageContext.request.contextPath}/js/common.js"></script>
    <script src="${pageContext.request.contextPath}/js/garbage-inference.js"></script>
</body>
</html>
