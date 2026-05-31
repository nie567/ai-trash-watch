/**
 * 垃圾分类推理页（独立推理页面）
 */
(function() {
    'use strict';

    function escapeHtml(s) { var d = document.createElement('div'); d.textContent = s; return d.innerHTML; }

    var config = window._pageConfig;
    var contextPath = config.contextPath;
    var selectedFile = null;

    window.selectFile = function(el, fileName) {
        // 清除其他选中状态
        document.querySelectorAll('#imageList li').forEach(function(li) {
            li.classList.remove('selected');
        });
        el.classList.add('selected');
        selectedFile = fileName;
        document.getElementById('detectBtn').disabled = false;
        // 清空之前的结果
        document.getElementById('resultArea').innerHTML =
            '<div class="empty-state"><p>已选择: ' + escapeHtml(fileName) + '</p></div>';
    };

    window.doDetect = function() {
        if (!selectedFile) {
            alert('请先选择一张图片');
            return;
        }

        var resultArea = document.getElementById('resultArea');
        resultArea.innerHTML = '<div class="loading">⏳ 正在推理中，请稍候...</div>';
        document.getElementById('detectBtn').disabled = true;

        apiFetch(contextPath + '/inference', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8' },
            body: 'fileName=' + encodeURIComponent(selectedFile)
        })
        .then(function(response) { return response.json(); })
        .then(function(data) {
            document.getElementById('detectBtn').disabled = false;
            if (data.code === 200 && data.data) {
                var result = data.data;
                var html = '<div class="chart-fallback-msg" style="margin-bottom:10px;">';
                html += '<p>✅ 推理完成</p>';
                html += '<p style="font-size:13px; color:var(--text-muted,#6B778C); margin-top:4px;">图片: ' + escapeHtml(result.imageName) + '</p>';
                html += '</div>';

                if (result.detectedObjects && result.detectedObjects.length > 0) {
                    html += '<h3 style="font-size:14px; margin-bottom:8px; color:var(--text-primary,#F1F5F9);">检测到的垃圾:</h3>';
                    result.detectedObjects.forEach(function(obj) {
                        html += '<div class="detected">' +
                                '🗑️ <strong>' + escapeHtml(obj.className) + '</strong> ' +
                                '(置信度: ' + (obj.confidence * 100).toFixed(1) + '%)' +
                                '</div>';
                    });
                } else {
                    html += '<div class="detected">未检测到垃圾对象</div>';
                }

                if (result.outputImageName) {
                    html += '<p style="margin-top:10px; font-size:14px; color:var(--text-muted,#6B778C);">' +
                            '输出图片: ' + escapeHtml(result.outputImageName) + '</p>';
                    // 显示结果图片 - 使用 ImageServlet 的 output 路径
                    var imageUrl = contextPath + '/image/output/' + encodeURIComponent(result.outputImageName);
                    html += '<img src="' + imageUrl + '" class="result-image" alt="检测结果" ' +
                            'onerror="this.style.display=\'none\'; this.nextElementSibling.style.display=\'block\';">';
                    html += '<p class="text-danger" style="display:none; font-size:14px;">图片加载失败，路径: ' + result.outputImageName + '</p>';
                }

                resultArea.innerHTML = html;
            } else {
                resultArea.innerHTML = '<div class="error">❌ ' + (data.message || '推理失败') + '</div>';
            }
        })
        .catch(function(error) {
            document.getElementById('detectBtn').disabled = false;
            resultArea.innerHTML = '<div class="error">❌ 请求失败: ' + error.message + '</div>';
        });
    };
})();
