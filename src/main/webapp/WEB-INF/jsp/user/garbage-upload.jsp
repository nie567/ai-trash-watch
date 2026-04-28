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
                <div class="upload-icon">&#128247;</div>
                <p>点击选择图片或拖拽图片到此处</p>
                <p style="font-size:12px;color:#aaa;">支持 jpg/jpeg/png/gif 格式，最大10MB</p>
                <input type="file" id="fileSelect" accept=".jpg,.jpeg,.png,.gif" style="display:none" onchange="onFileSelected(this)">
            </div>

            <div id="imagePreview" style="display:none;margin-bottom:20px;text-align:center;">
                <img id="previewImg" style="max-width:100%;max-height:400px;border-radius:4px;">
            </div>

            <div style="margin-bottom:20px;">
                <label style="font-weight:500;margin-bottom:8px;display:block;">或从已有图片中选择：</label>
                <select id="imageSelect" class="search-input" style="width:100%;" onchange="onImageSelected(this)">
                    <option value="">-- 请选择 --</option>
                    <c:forEach items="${imageFiles}" var="file">
                        <option value="${file}">${file}</option>
                    </c:forEach>
                </select>
            </div>

            <button class="btn btn-success" id="detectBtn" onclick="doDetect()" disabled>开始检测</button>
            <span id="detectStatus" style="margin-left:10px;color:#7f8c8d;"></span>
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
                <h4 style="margin-bottom:8px;">检测到的目标：</h4>
                <table class="table" id="detectionTable">
                    <thead>
                        <tr><th>类别</th><th>置信度</th><th>映射类别</th></tr>
                    </thead>
                    <tbody id="detectionBody"></tbody>
                </table>
            </div>

            <div style="margin-bottom:16px;">
                <strong>推荐投放类别：</strong>
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

            <button class="btn btn-success" onclick="doSubmit()">提交投放</button>
            <span id="submitStatus" style="margin-left:10px;"></span>
        </div>
    </div>

<script>
var contextPath = '${pageContext.request.contextPath}';
var currentFileName = '';
var currentFile = null;  // 用户上传的文件对象
var detectResult = null;

function onFileSelected(input) {
    if (input.files && input.files[0]) {
        var file = input.files[0];
        if (file.size > 10 * 1024 * 1024) {
            alert('文件大小超过10MB限制');
            return;
        }
        currentFile = file;
        currentFileName = file.name;
        var reader = new FileReader();
        reader.onload = function(e) {
            document.getElementById('previewImg').src = e.target.result;
            document.getElementById('imagePreview').style.display = 'block';
            document.getElementById('detectBtn').disabled = false;
        };
        reader.readAsDataURL(file);
    }
}

function onImageSelected(sel) {
    currentFileName = sel.value;
    currentFile = null;  // 清除上传文件，使用本地文件
    document.getElementById('detectBtn').disabled = !currentFileName;
    if (currentFileName) {
        // 显示input目录中的图片预览
        document.getElementById('previewImg').src = contextPath + '/image/input/' + currentFileName;
        document.getElementById('imagePreview').style.display = 'block';
    } else {
        document.getElementById('imagePreview').style.display = 'none';
    }
}

function doDetect() {
    if (!currentFileName && !currentFile) { alert('请先选择图片'); return; }
    document.getElementById('detectStatus').textContent = '检测中...';
    document.getElementById('detectBtn').disabled = true;

    var formData = new FormData();
    if (currentFile) {
        // 用户上传了文件
        formData.append('file', currentFile);
    } else {
        // 使用本地已有文件
        formData.append('fileName', currentFileName);
    }

    fetch(contextPath + '/inference/detect', {
        method: 'POST',
        body: formData
    })
    .then(r => r.json())
    .then(data => {
        document.getElementById('detectStatus').textContent = '';
        document.getElementById('detectBtn').disabled = false;
        if (data.code === 200 && data.data) {
            detectResult = data.data;
            showResult(data.data);
        } else {
            alert(data.message || '检测失败');
        }
    })
    .catch(err => {
        document.getElementById('detectStatus').textContent = '';
        document.getElementById('detectBtn').disabled = false;
        alert('检测服务暂不可用，请稍后重试');
    });
}

function showResult(data) {
    document.getElementById('resultArea').style.display = 'block';
    if (data.outputImageName) {
        // outputImageName现在只包含文件名
        document.getElementById('resultImg').src = contextPath + '/image/output/' + data.outputImageName;
    }

    var tbody = document.getElementById('detectionBody');
    tbody.innerHTML = '';
    if (data.detectedObjects) {
        data.detectedObjects.forEach(function(obj) {
            var tr = document.createElement('tr');
            tr.innerHTML = '<td>' + obj.className + '</td>' +
                '<td>' + (obj.confidence * 100).toFixed(1) + '%</td>' +
                '<td><span class="badge ' + getCategoryClass(obj.mappedCategory) + '">' + (obj.mappedCategory || '-') + '</span></td>';
            tbody.appendChild(tr);
        });
    }

    var recCat = document.getElementById('recommendedCategory');
    recCat.textContent = data.recommendedCategory || '未知';
    recCat.className = 'badge ' + getCategoryClass(data.recommendedCategory);
    recCat.style.fontSize = '14px';
    recCat.style.padding = '6px 14px';

    if (data.recommendedCategory) {
        document.getElementById('selectedCategory').value = data.recommendedCategory;
    }
}

function getCategoryClass(cat) {
    if (!cat) return '';
    var map = {'可回收物':'category-recyclable','厨余垃圾':'category-kitchen',
        '有害垃圾':'category-hazardous','其他垃圾':'category-other','混合待分拣':'category-mixed'};
    return map[cat] || '';
}

function doSubmit() {
    var selCat = document.getElementById('selectedCategory').value;
    if (!selCat) { alert('请选择投放类别'); return; }
    if (!detectResult) { alert('请先进行检测'); return; }

    var params = new URLSearchParams();
    params.append('imageName', detectResult.imageName || currentFileName);
    params.append('imagePath', detectResult.imageName || currentFileName);
    params.append('resultImagePath', detectResult.outputImageName || '');
    var summary = (detectResult.detectedObjects || []).map(function(o){return o.className}).join(', ');
    params.append('detectedSummary', summary);
    params.append('recommendedCategory', detectResult.recommendedCategory || '');
    params.append('selectedCategory', selCat);
    params.append('isMixed', detectResult.isMixed || 0);
    params.append('remark', document.getElementById('remark').value);

    fetch(contextPath + '/user/garbage-record', {
        method: 'POST',
        headers: {'Content-Type': 'application/x-www-form-urlencoded'},
        body: params.toString()
    })
    .then(r => r.json())
    .then(data => {
        var status = document.getElementById('submitStatus');
        if (data.code === 200) {
            var isCorrect = detectResult.recommendedCategory === selCat;
            status.innerHTML = isCorrect
                ? '<span class="text-success">投放正确！记录已保存</span>'
                : '<span class="text-danger">投放错误！已自动生成违规记录</span>';
        } else {
            status.innerHTML = '<span class="text-danger">' + (data.message || '提交失败') + '</span>';
        }
    })
    .catch(err => {
        document.getElementById('submitStatus').innerHTML = '<span class="text-danger">提交失败</span>';
    });
}
</script>
</body>
</html>
