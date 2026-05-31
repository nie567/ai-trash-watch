/**
 * 整改任务管理页（管理员）
 * 增强: 异步搜索/分页 + Toast替代alert
 */
(function() {
    'use strict';

    var config = window._pageConfig;
    var contextPath = config.contextPath;

    // 初始化异步搜索和分页
    initLiveSearch({
        inputSelector: '.search-input',
        formSelector: '.search-form',
        containerSelector: '.card'
    });

    initAjaxPagination({
        containerSelector: '.card',
        paginationSelector: '.pagination'
    });

    window.showReviewModal = function(taskId) {
        document.getElementById('reviewTaskId').value = taskId;
        document.getElementById('reviewModal').classList.add('show');
    };

    window.closeReviewModal = function() {
        document.getElementById('reviewModal').classList.remove('show');
    };

    window.submitReview = function() {
        var taskId = document.getElementById('reviewTaskId').value;
        var status = document.getElementById('reviewStatusSelect') ? document.getElementById('reviewStatusSelect').value : document.getElementById('reviewResult').value;
        var comment = document.getElementById('reviewComment') ? document.getElementById('reviewComment').value : '';

        var params = 'id=' + taskId
            + '&reviewResult=' + encodeURIComponent(status)
            + '&reviewComment=' + encodeURIComponent(comment);

        apiFetch(contextPath + '/admin/rectification/review', {
            method: 'POST',
            headers: {'Content-Type': 'application/x-www-form-urlencoded'},
            body: params
        })
        .then(function(r) { return r.json(); })
        .then(function(data) {
            if (data.code === 200) {
                closeReviewModal();
                showToast('复核完成', 'success');
                setTimeout(function() { location.reload(); }, 800);
            } else {
                showToast(data.message || '操作失败', 'error');
            }
        })
        .catch(function(err) { showToast('操作失败', 'error'); });
    };
})();
