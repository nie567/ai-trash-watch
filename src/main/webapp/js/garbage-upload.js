/**
 * 垃圾投放识别页
 * 增强: Toast替代alert + 拖拽上传
 */
(function() {
    'use strict';

    var config = window._pageConfig;
    var contextPath = config.contextPath;
    var currentFileName = '';
    var currentFile = null;
    var detectResult = null;

    // 文件选择
    window.onFileSelected = function(input) {
        if (input.files && input.files[0]) {
            var file = input.files[0];
            if (file.size > 10 * 1024 * 1024) {
                showToast('文件大小超过10MB限制', 'warning');
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
    };

    // 拖拽上传支持
    var uploadArea = document.getElementById('uploadArea');
    if (uploadArea) {
        uploadArea.addEventListener('dragover', function(e) {
            e.preventDefault();
            e.stopPropagation();
            uploadArea.classList.add('dragover');
        });
        uploadArea.addEventListener('dragleave', function(e) {
            e.preventDefault();
            uploadArea.classList.remove('dragover');
        });
        uploadArea.addEventListener('drop', function(e) {
            e.preventDefault();
            uploadArea.classList.remove('dragover');
            var files = e.dataTransfer.files;
            if (files.length > 0) {
                var fileInput = document.getElementById('fileSelect');
                fileInput.files = files;
                onFileSelected(fileInput);
            }
        });
    }

    // 从已有图片选择
    window.onImageSelected = function(sel) {
        currentFileName = sel.value;
        currentFile = null;
        document.getElementById('detectBtn').disabled = !currentFileName;
        if (currentFileName) {
            document.getElementById('previewImg').src = contextPath + '/image/input/' + currentFileName;
            document.getElementById('imagePreview').style.display = 'block';
        } else {
            document.getElementById('imagePreview').style.display = 'none';
        }
    };

    // 开始检测
    window.doDetect = function() {
        if (!currentFileName) { showToast('请先选择图片', 'warning'); return; }

        var detectBtn = document.getElementById('detectBtn');
        var detectStatus = document.getElementById('detectStatus');
        detectBtn.disabled = true;
        detectStatus.textContent = '检测中...';

        var formData = new FormData();
        if (currentFile) {
            formData.append('file', currentFile);
            formData.append('fileName', currentFileName);
        } else {
            formData.append('fileName', currentFileName);
        }

        apiFetch(contextPath + '/inference/detect', {
            method: 'POST',
            body: formData
        })
        .then(function(r) {
            var ct = r.headers.get('content-type') || '';
            if (ct.indexOf('json') === -1) {
                // 非JSON响应（可能是重定向到登录页）
                return r.text().then(function(text) {
                    throw new Error('服务器返回非JSON响应(状态:' + r.status + ')，可能登录已过期，请刷新页面重试');
                });
            }
            return r.json();
        })
        .then(function(data) {
            detectBtn.disabled = false;
            detectStatus.textContent = '';
            if (data.code === 200) {
                detectResult = data.data;
                showResult(data.data);
                showToast('检测完成', 'success');
            } else {
                showToast(data.message || '检测失败', 'error');
            }
        })
        .catch(function(err) {
            detectBtn.disabled = false;
            detectStatus.textContent = '';
            showToast(err.message || '检测失败，请稍后重试', 'error');
        });
    };

    // 显示检测结果
    function showResult(result) {
        var resultArea = document.getElementById('resultArea');
        if (!resultArea) return;
        resultArea.style.display = 'block';

        // 更新检测结果图（后端返回 outputImageName）
        var resultImg = document.getElementById('resultImg');
        if (resultImg && result.outputImageName) {
            resultImg.src = contextPath + '/image/output/' + result.outputImageName;
        } else if (resultImg && currentFileName) {
            resultImg.src = contextPath + '/image/input/' + currentFileName;
        }

        // 更新检测表格
        var tbody = document.getElementById('detectionBody');
        if (tbody) {
            var html = '';
            if (result.detectedObjects && result.detectedObjects.length > 0) {
                for (var i = 0; i < result.detectedObjects.length; i++) {
                    var obj = result.detectedObjects[i];
                    html += '<tr><td>' + obj.className + '</td><td>' + (obj.confidence != null ? (obj.confidence * 100).toFixed(1) + '%' : '-') + '</td><td>' + (obj.mappedCategory || '-') + '</td></tr>';
                }
            } else {
                html = '<tr><td colspan="3">未检测到目标</td></tr>';
            }
            tbody.innerHTML = html;
        }

        // 更新推荐类别
        var recCat = document.getElementById('recommendedCategory');
        if (recCat) {
            recCat.textContent = result.recommendedCategory || '未知';
        }

        // 预选投放类别
        var selCat = document.getElementById('selectedCategory');
        if (selCat && result.recommendedCategory) {
            selCat.value = result.recommendedCategory;
        }
    }

    // 提交投放记录
    window.doSubmit = function() {
        if (!detectResult) { showToast('请先进行检测', 'warning'); return; }

        var selCat = document.getElementById('selectedCategory');
        if (!selCat) return;
        selCat = selCat.value;

        var params = new URLSearchParams();
        // imageName: 使用后端返回的唯一文件名（含时间戳前缀），而非原始文件名
        var savedImageName = detectResult.imageName || currentFileName;
        params.append('imageName', savedImageName);
        // imagePath: 输入图片路径
        params.append('imagePath', 'input/' + savedImageName);
        if (detectResult.outputImageName) {
            params.append('resultImagePath', detectResult.outputImageName);
        }
        var summary = (detectResult.detectedObjects || []).map(function(o) { return o.className; }).join(', ');
        params.append('detectedSummary', summary);
        params.append('recommendedCategory', detectResult.recommendedCategory || '');
        params.append('selectedCategory', selCat);
        params.append('isMixed', String(detectResult.isMixed || 0));
        params.append('remark', document.getElementById('remark').value);

        apiFetch(contextPath + '/user/garbage-record', {
            method: 'POST',
            headers: {'Content-Type': 'application/x-www-form-urlencoded'},
            body: params.toString()
        })
        .then(function(r) {
            var ct = r.headers.get('content-type') || '';
            if (ct.indexOf('json') === -1) {
                return r.text().then(function(text) {
                    throw new Error('登录可能已过期，请刷新页面重试');
                });
            }
            return r.json();
        })
        .then(function(data) {
            var status = document.getElementById('submitStatus');
            if (data.code === 200) {
                var isCorrect = detectResult.recommendedCategory === selCat;
                if (isCorrect) {
                    showToast('投放正确！记录已保存', 'success');
                } else {
                    showToast('投放错误！已自动生成违规记录', 'warning');
                }
                status.innerHTML = isCorrect
                    ? '<span class="text-success">投放正确！记录已保存</span>'
                    : '<span class="text-danger">投放错误！已自动生成违规记录</span>';
            } else {
                showToast(data.message || '提交失败', 'error');
                status.innerHTML = '<span class="text-danger">' + (data.message || '提交失败') + '</span>';
            }
        })
        .catch(function(err) {
            showToast('提交失败', 'error');
            document.getElementById('submitStatus').innerHTML = '<span class="text-danger">提交失败</span>';
        });
    };
})();
