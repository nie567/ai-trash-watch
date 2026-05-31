/**
 * 用户管理列表页
 * 增强: 异步搜索/分页 + Toast替代alert
 */
(function() {
    'use strict';

    var config = window._pageConfig;
    var contextPath = config.contextPath;
    var csrfToken = config.csrfToken;

    // 初始化异步搜索和分页
    initLiveSearch({
        inputSelector: 'input[name="keyword"]',
        formSelector: 'form[action$="/admin/users"]',
        containerSelector: '.card'
    });

    initAjaxPagination({
        containerSelector: '.card',
        paginationSelector: '.pagination'
    });

    // 切换用户状态
    window.toggleStatus = function(userId, currentStatus) {
        var newStatus = currentStatus == 1 ? 0 : 1;
        var msg = newStatus == 1 ? '确定要启用该用户吗？' : '确定要禁用该用户吗？';
        if (!confirm(msg)) return;

        apiFetch(contextPath + '/admin/users/' + userId + '/status?status=' + newStatus, {
            method: 'PUT',
            headers: { 'X-CSRF-Token': csrfToken }
        })
        .then(function(r) { return r.json(); })
        .then(function(data) {
            if (data.code === 200) {
                showToast('状态更新成功', 'success');
                setTimeout(function() { location.reload(); }, 800);
            } else {
                showToast(data.message || '操作失败', 'error');
            }
        })
        .catch(function(err) { showToast('操作失败，请稍后重试', 'error'); });
    };

    // 删除用户
    window.deleteUser = function(userId) {
        if (!confirm('确定要删除该用户吗？此操作不可恢复。')) return;

        apiFetch(contextPath + '/admin/users/' + userId, {
            method: 'DELETE',
            headers: { 'X-CSRF-Token': csrfToken }
        })
        .then(function(r) { return r.json(); })
        .then(function(data) {
            if (data.code === 200) {
                showToast('删除成功', 'success');
                setTimeout(function() { location.reload(); }, 800);
            } else {
                showToast(data.message || '删除失败', 'error');
            }
        })
        .catch(function(err) { showToast('操作失败，请稍后重试', 'error'); });
    };
})();
