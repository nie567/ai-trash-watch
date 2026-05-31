/**
 * 分类规则管理页
 * 增强: Toast替代alert
 */
(function() {
    'use strict';

    var config = window._pageConfig;
    var contextPath = config.contextPath;

    window.showRuleModal = function() {
        document.getElementById('ruleModal').classList.add('show');
        document.getElementById('ruleForm').reset();
        document.getElementById('ruleId').value = '';
        document.getElementById('ruleModalTitle').textContent = '新增规则';
    };

    window.closeRuleModal = function() {
        document.getElementById('ruleModal').classList.remove('show');
    };

    window.editRule = function(id, className, mappedCategory, description) {
        document.getElementById('ruleModal').classList.add('show');
        document.getElementById('ruleId').value = id;
        document.getElementById('ruleClassName').value = className;
        document.getElementById('ruleMappedCategory').value = mappedCategory;
        document.getElementById('ruleDescription').value = description;
        document.getElementById('ruleModalTitle').textContent = '编辑规则';
    };

    window.saveRule = function() {
        var id = document.getElementById('ruleId').value;
        var className = document.getElementById('ruleClassName').value.trim();
        var mappedCategory = document.getElementById('ruleMappedCategory').value;
        var description = document.getElementById('ruleDescription').value.trim();
        if (!className) { showToast('请输入检测类别', 'warning'); return; }

        var params = 'className=' + encodeURIComponent(className)
            + '&mappedCategory=' + encodeURIComponent(mappedCategory)
            + '&description=' + encodeURIComponent(description);
        if (id) params += '&id=' + id;

        apiFetch(contextPath + '/admin/rule/save', {
            method: 'POST',
            headers: {'Content-Type': 'application/x-www-form-urlencoded'},
            body: params
        })
        .then(function(r) { return r.json(); })
        .then(function(data) {
            if (data.code === 200) {
                closeRuleModal();
                showToast('保存成功', 'success');
                setTimeout(function() { location.reload(); }, 800);
            } else {
                showToast(data.message || '保存失败', 'error');
            }
        })
        .catch(function(err) { showToast('操作失败', 'error'); });
    };

    window.deleteRule = function(id) {
        if (!confirm('确定删除该规则？')) return;
        apiFetch(contextPath + '/admin/rule/delete', {
            method: 'POST',
            headers: {'Content-Type': 'application/x-www-form-urlencoded'},
            body: 'id=' + id
        })
        .then(function(r) { return r.json(); })
        .then(function(data) {
            if (data.code === 200) {
                showToast('删除成功', 'success');
                setTimeout(function() { location.reload(); }, 800);
            }
            else { showToast(data.message || '删除失败', 'error'); }
        })
        .catch(function(err) { showToast('操作失败', 'error'); });
    };

    window.toggleStatus = function(id, currentStatus) {
        var newStatus = currentStatus == 1 ? 0 : 1;
        var params = 'id=' + id + '&className=&mappedCategory=可回收物&description=&status=' + newStatus;
        apiFetch(contextPath + '/admin/rule/save', {
            method: 'POST',
            headers: {'Content-Type': 'application/x-www-form-urlencoded'},
            body: params
        })
        .then(function(r) { return r.json(); })
        .then(function(data) {
            if (data.code === 200) {
                showToast('状态已更新', 'success');
                setTimeout(function() { location.reload(); }, 800);
            }
            else { showToast(data.message || '操作失败', 'error'); }
        })
        .catch(function(err) { showToast('操作失败', 'error'); });
    };
})();
