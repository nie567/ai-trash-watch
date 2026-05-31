/**
 * 违规记录管理页
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

    window.showRectModal = function(violationId) {
        document.getElementById('rectViolationId').value = violationId;
        document.getElementById('rectModal').classList.add('show');
    };

    window.closeRectModal = function() {
        document.getElementById('rectModal').classList.remove('show');
    };

    window.submitRectification = function() {
        var violationId = document.getElementById('rectViolationId').value;
        var requirement = document.getElementById('rectRequirement').value;
        var deadline = document.getElementById('rectDeadline').value;
        var userId = document.getElementById('rectUserId') ? document.getElementById('rectUserId').value : '';

        if (!requirement.trim()) { showToast('请输入整改要求', 'warning'); return; }

        var params = 'violationId=' + violationId
            + '&requirement=' + encodeURIComponent(requirement)
            + '&deadline=' + encodeURIComponent(deadline);
        if (userId) params += '&userId=' + userId;

        apiFetch(contextPath + '/admin/violation/create-rectification', {
            method: 'POST',
            headers: {'Content-Type': 'application/x-www-form-urlencoded'},
            body: params
        })
        .then(function(r) { return r.json(); })
        .then(function(data) {
            if (data.code === 200) {
                closeRectModal();
                showToast('整改任务已发起', 'success');
                setTimeout(function() { location.reload(); }, 800);
            } else {
                showToast(data.message || '操作失败', 'error');
            }
        })
        .catch(function(err) { showToast('操作失败', 'error'); });
    };
})();
