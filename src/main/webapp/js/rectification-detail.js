/**
 * 用户-整改详情页
 * 增强版：图片上传 + 预览
 */
(function() {
    'use strict';

    function escapeHtml(s) { var d = document.createElement('div'); d.textContent = s; return d.innerHTML; }

    var config = window._pageConfig;
    var contextPath = config.contextPath;
    var uploadedImagePath = ''; // 上传成功后的图片路径

    /**
     * 选择文件后：预览 + 上传
     */
    window.onRectFileSelected = function(input) {
        var file = input.files && input.files[0];
        if (!file) return;

        // 校验文件大小（10MB）
        if (file.size > 10 * 1024 * 1024) {
            showToast('图片大小不能超过10MB', 'error');
            input.value = '';
            return;
        }

        // 本地预览
        var reader = new FileReader();
        reader.onload = function(e) {
            var preview = document.getElementById('rectPreviewImg');
            var placeholder = document.getElementById('rectUploadPlaceholder');
            preview.src = e.target.result;
            preview.style.display = 'block';
            placeholder.style.display = 'none';
        };
        reader.readAsDataURL(file);

        // 上传到服务器
        var formData = new FormData();
        formData.append('file', file);

        var statusEl = document.getElementById('submitStatus');
        statusEl.innerHTML = '<span class="text-muted">图片上传中...</span>';

        fetch(contextPath + '/upload/rectification', {
            method: 'POST',
            body: formData,
            credentials: 'same-origin'
        })
        .then(function(r) { return r.json(); })
        .then(function(data) {
            if (data.code === 200 && data.data) {
                uploadedImagePath = data.data;
                statusEl.innerHTML = '<span class="text-success">图片上传成功 &#10003;</span>';
            } else {
                statusEl.innerHTML = '<span class="text-danger">图片上传失败：' + escapeHtml(data.message || '未知错误') + '</span>';
                uploadedImagePath = '';
            }
        })
        .catch(function(err) {
            statusEl.innerHTML = '<span class="text-danger">图片上传失败</span>';
            uploadedImagePath = '';
        });
    };

    /**
     * 提交整改
     */
    window.doSubmit = function() {
        var desc = document.getElementById('submitDesc').value;
        if (!desc.trim()) { alert('请输入整改说明'); return; }

        var statusEl = document.getElementById('submitStatus');
        statusEl.innerHTML = '';

        var params = 'id=' + config.taskId + '&submitDesc=' + encodeURIComponent(desc);
        if (uploadedImagePath) {
            params += '&submitImagePath=' + encodeURIComponent(uploadedImagePath);
        }

        apiFetch(contextPath + '/user/rectification/submit', {
            method: 'POST',
            headers: {'Content-Type': 'application/x-www-form-urlencoded'},
            body: params
        })
        .then(function(r) { return r.json(); })
        .then(function(data) {
            if (data.code === 200) {
                statusEl.innerHTML = '<span class="text-success">提交成功</span>';
                setTimeout(function() { location.reload(); }, 1000);
            } else {
                statusEl.innerHTML = '<span class="text-danger">' + escapeHtml(data.message || '提交失败') + '</span>';
            }
        })
        .catch(function(err) {
            statusEl.innerHTML = '<span class="text-danger">提交失败</span>';
        });
    };
})();
