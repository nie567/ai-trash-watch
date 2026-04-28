<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>垃圾分类推理</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body {
            font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
            background: #f5f7fa;
            padding: 20px;
        }
        .container {
            max-width: 1200px;
            margin: 0 auto;
        }
        h1 {
            font-size: 24px;
            color: #333;
            margin-bottom: 20px;
        }
        .grid {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 20px;
        }
        .card {
            background: #fff;
            border-radius: 12px;
            padding: 20px;
            box-shadow: 0 2px 8px rgba(0,0,0,0.1);
        }
        .card h2 {
            font-size: 18px;
            color: #333;
            margin-bottom: 15px;
            padding-bottom: 10px;
            border-bottom: 1px solid #eee;
        }
        .image-list {
            list-style: none;
        }
        .image-list li {
            padding: 10px 12px;
            cursor: pointer;
            border-radius: 8px;
            margin-bottom: 5px;
            transition: background 0.2s;
            display: flex;
            align-items: center;
            gap: 8px;
        }
        .image-list li:hover {
            background: #eef2ff;
        }
        .image-list li.selected {
            background: #dbeafe;
            border: 1px solid #93c5fd;
        }
        .image-list li .icon {
            font-size: 18px;
        }
        .btn-detect {
            display: block;
            width: 100%;
            padding: 12px;
            background: #4f46e5;
            color: #fff;
            border: none;
            border-radius: 8px;
            font-size: 16px;
            cursor: pointer;
            margin-top: 15px;
            transition: background 0.2s;
        }
        .btn-detect:hover { background: #4338ca; }
        .btn-detect:disabled {
            background: #a5b4fc;
            cursor: not-allowed;
        }
        .result-area {
            margin-top: 15px;
        }
        .result-area .detected {
            padding: 8px 12px;
            background: #f0fdf4;
            border: 1px solid #bbf7d0;
            border-radius: 6px;
            margin-bottom: 5px;
            font-size: 14px;
        }
        .result-area .error {
            padding: 8px 12px;
            background: #fef2f2;
            border: 1px solid #fecaca;
            border-radius: 6px;
            margin-bottom: 5px;
            font-size: 14px;
            color: #dc2626;
        }
        .result-area .loading {
            color: #6b7280;
            font-size: 14px;
        }
        .result-image {
            max-width: 100%;
            border-radius: 8px;
            margin-top: 10px;
            border: 1px solid #e5e7eb;
        }
        .empty-state {
            color: #9ca3af;
            text-align: center;
            padding: 30px;
            font-size: 14px;
        }
    </style>
</head>
<body>
    <div class="container">
        <h1>♻️ 垃圾分类推理</h1>
        <div class="grid">
            <!-- 左侧：图片列表 -->
            <div class="card">
                <h2>📁 图片列表 (D:\ny\data_set\input)</h2>
                <c:choose>
                    <c:when test="${empty imageFiles}">
                        <div class="empty-state">
                            <p>暂无图片文件</p>
                            <p style="margin-top:8px; font-size:12px;">请将图片放入 D:\ny\data_set\input 目录</p>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <ul class="image-list" id="imageList">
                            <c:forEach var="file" items="${imageFiles}">
                                <li onclick="selectFile(this, '${file}')">
                                    <span class="icon">🖼️</span>
                                    <span>${file}</span>
                                </li>
                            </c:forEach>
                        </ul>
                    </c:otherwise>
                </c:choose>
                <button class="btn-detect" id="detectBtn" onclick="startDetect()" disabled>
                    🔍 开始推理
                </button>
            </div>

            <!-- 右侧：推理结果 -->
            <div class="card">
                <h2>📊 推理结果</h2>
                <div id="resultArea" class="result-area">
                    <div class="empty-state">
                        <p>请选择左侧图片开始推理</p>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <script>
        let selectedFile = null;

        function selectFile(el, fileName) {
            // 清除其他选中状态
            document.querySelectorAll('#imageList li').forEach(li => li.classList.remove('selected'));
            el.classList.add('selected');
            selectedFile = fileName;
            document.getElementById('detectBtn').disabled = false;
            // 清空之前的结果
            document.getElementById('resultArea').innerHTML = '<div class="empty-state"><p>已选择: ' + fileName + '</p></div>';
        }

        function startDetect() {
            if (!selectedFile) {
                alert('请先选择一张图片');
                return;
            }

            const resultArea = document.getElementById('resultArea');
            resultArea.innerHTML = '<div class="loading">⏳ 正在推理中，请稍候...</div>';
            document.getElementById('detectBtn').disabled = true;

            fetch('${pageContext.request.contextPath}/inference', {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8' },
                body: 'fileName=' + encodeURIComponent(selectedFile)
            })
            .then(response => response.json())
            .then(data => {
                document.getElementById('detectBtn').disabled = false;
                if (data.code === 200 && data.data) {
                    const result = data.data;
                    let html = '<div style="margin-bottom:10px;">';
                    html += '<p>✅ 推理完成</p>';
                    html += '<p style="font-size:13px; color:#6b7280; margin-top:4px;">图片: ' + result.imageName + '</p>';
                    html += '</div>';

                    if (result.detectedObjects && result.detectedObjects.length > 0) {
                        html += '<h3 style="font-size:14px; margin-bottom:8px;">检测到的垃圾:</h3>';
                        result.detectedObjects.forEach(obj => {
                            html += '<div class="detected">' +
                                    '🗑️ <strong>' + obj.className + '</strong> ' +
                                    '(置信度: ' + (obj.confidence * 100).toFixed(1) + '%)' +
                                    '</div>';
                        });
                    } else {
                        html += '<div class="detected">未检测到垃圾对象</div>';
                    }

                    if (result.outputImageName) {
                        html += '<p style="margin-top:10px; font-size:12px; color:#6b7280;">' +
                                '输出图片: ' + result.outputImageName + '</p>';
                        // 显示结果图片 - 使用本地文件路径转换后的URL
                        var imageUrl = '${pageContext.request.contextPath}/inference/image?path=' + encodeURIComponent(result.outputImageName);
                        html += '<img src="' + imageUrl + '" class="result-image" alt="检测结果" ' +
                                'onerror="this.style.display=\'none\'; this.nextElementSibling.style.display=\'block\';">';
                        html += '<p style="display:none; color:#dc2626; font-size:12px;">图片加载失败，路径: ' + result.outputImageName + '</p>';
                    }

                    resultArea.innerHTML = html;
                } else {
                    resultArea.innerHTML = '<div class="error">❌ ' + (data.message || '推理失败') + '</div>';
                }
            })
            .catch(error => {
                document.getElementById('detectBtn').disabled = false;
                resultArea.innerHTML = '<div class="error">❌ 请求失败: ' + error.message + '</div>';
            });
        }
    </script>
</body>
</html>