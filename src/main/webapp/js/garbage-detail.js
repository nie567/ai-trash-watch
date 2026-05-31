/**
 * 用户-投放记录详情页
 */
(function() {
    'use strict';

    var config = window._pageConfig;
    var contextPath = config.contextPath;

    // 设置结果图片路径
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
