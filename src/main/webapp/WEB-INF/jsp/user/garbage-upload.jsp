<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>垃圾投放 - 垃圾分类监管系统</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
    <jsp:include page="/WEB-INF/jsp/nav-user.jsp" />

    <div class="main-content">
        <div class="card">
            <h2 class="card-title">垃圾投放识别</h2>

            <div class="upload-area" id="uploadArea" onclick="document.getElementById('fileSelect').click()">
                <div class="upload-icon"><svg viewBox="0 0 24 24"><path d="M23 19a2 2 0 0 1-2 2H3a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h4l2-3h6l2 3h4a2 2 0 0 1 2 2z"/><circle cx="12" cy="13" r="4"/></svg></div>
                <p>点击选择图片或拖拽图片到此处</p>
                <p class="text-muted" style="font-size:14px;">支持 jpg/jpeg/png/gif 格式，最大10MB</p>
                <input type="file" id="fileSelect" accept=".jpg,.jpeg,.png,.gif" style="display:none" onchange="onFileSelected(this)">
            </div>

            <div id="imagePreview" style="display:none;margin-bottom:20px;text-align:center;">
                <div class="image-box" style="display:inline-block;max-width:100%;padding:12px;">
                    <img id="previewImg" style="max-width:100%;max-height:400px;border-radius:8px;">
                    <p>预览图片</p>
                </div>
            </div>

            <div class="form-group">
                <label>或从已有图片中选择：</label>
                <select id="imageSelect" class="search-input" style="width:100%;" onchange="onImageSelected(this)">
                    <option value="">-- 请选择 --</option>
                    <c:forEach items="${imageFiles}" var="file">
                        <option value="${file}">${file}</option>
                    </c:forEach>
                </select>
            </div>

            <div class="form-actions">
                <button class="btn btn-success" id="detectBtn" onclick="doDetect()" disabled>开始检测</button>
                <span id="detectStatus" class="text-muted"></span>
            </div>
        </div>

        <!-- 检测结果区域 -->
        <div class="card" id="resultArea" style="display:none;">
            <h2 class="card-title">检测结果</h2>

            <div class="image-pair" id="resultImages">
                <div class="image-box">
                    <img id="resultImg" src="" alt="检测结果图">
                    <p>检测结果图</p>
                </div>
            </div>

            <div style="margin-bottom:16px;">
                <h4 style="margin-bottom:8px;color:var(--text-primary);font-size:14px;">检测到的目标：</h4>
                <table class="table" id="detectionTable">
                    <thead>
                        <tr><th>类别</th><th>置信度</th><th>映射类别</th></tr>
                    </thead>
                    <tbody id="detectionBody"></tbody>
                </table>
            </div>

            <div style="margin-bottom:16px;">
                <strong style="color:var(--text-primary);">推荐投放类别：</strong>
                <span id="recommendedCategory" class="badge" style="font-size:14px;padding:6px 14px;"></span>
            </div>

            <div class="form-group">
                <label>请选择实际投放类别 <span class="required">*</span></label>
                <select id="selectedCategory">
                    <option value="">-- 请选择 --</option>
                    <option value="可回收物">可回收物</option>
                    <option value="厨余垃圾">厨余垃圾</option>
                    <option value="有害垃圾">有害垃圾</option>
                    <option value="其他垃圾">其他垃圾</option>
                    <option value="混合待分拣">混合待分拣</option>
                </select>
            </div>

            <div class="form-group">
                <label>备注</label>
                <input type="text" id="remark" placeholder="可选备注">
            </div>

            <div class="form-actions">
                <button class="btn btn-success" onclick="doSubmit()">提交投放</button>
                <span id="submitStatus"></span>
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
<script src="${pageContext.request.contextPath}/js/garbage-upload.js"></script>
</body>
</html>
