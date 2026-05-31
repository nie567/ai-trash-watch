/**
 * 管理员-投放记录详情页
 */
(function() {
    'use strict';

    function escapeHtml(s) { var d = document.createElement('div'); d.textContent = s; return d.innerHTML; }

    var config = window._pageConfig;
    var contextPath = config.contextPath;

    // 设置结果图片路径
    (function() {
        var resultImg = document.getElementById('resultImg');
        if (resultImg) {
            var resultPath = config.resultImagePath;
            if (resultPath && resultPath.trim()) {
                var fileName = resultPath.replace(/\\/g, '/');
                fileName = fileName.substring(fileName.lastIndexOf('/') + 1);
                resultImg.src = contextPath + '/image/output/' + fileName;
            }
        }
    })();

    // 提交复核
    window.submitReview = function() {
        var params = 'id=' + config.recordId
            + '&finalCategory=' + encodeURIComponent(document.getElementById('finalCategory').value)
            + '&reviewComment=' + encodeURIComponent(document.getElementById('reviewComment').value);

        apiFetch(contextPath + '/admin/garbage-record/review', {
            method: 'POST',
            headers: {'Content-Type': 'application/x-www-form-urlencoded'},
            body: params
        })
        .then(function(r) { return r.json(); })
        .then(function(data) {
            if (data.code === 200) {
                document.getElementById('reviewStatus').innerHTML = '<span class="text-success">复核成功</span>';
                setTimeout(function() { location.reload(); }, 1000);
            } else {
                document.getElementById('reviewStatus').innerHTML = '<span class="text-danger">' + escapeHtml(data.message || '复核失败') + '</span>';
            }
        })
        .catch(function(err) {
            document.getElementById('reviewStatus').innerHTML = '<span class="text-danger">操作失败</span>';
        });
    };
})();
