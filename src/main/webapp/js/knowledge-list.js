/**
 * 知识库管理页
 * 增强: Toast替代alert
 */
(function() {
    'use strict';

    var config = window._pageConfig;
    var contextPath = config.contextPath;

    window.showKbModal = function() {
        document.getElementById('kbModal').classList.add('show');
        document.getElementById('kbForm').reset();
        document.getElementById('kbId').value = '';
        document.getElementById('kbModalTitle').textContent = '新增知识条目';
    };

    window.closeKbModal = function() {
        document.getElementById('kbModal').classList.remove('show');
    };

    window.editKb = function(id, title, garbageType, content) {
        document.getElementById('kbModal').classList.add('show');
        document.getElementById('kbId').value = id;
        document.getElementById('kbTitle').value = title;
        document.getElementById('kbGarbageType').value = garbageType;
        document.getElementById('kbContent').value = content;
        document.getElementById('kbModalTitle').textContent = '编辑知识条目';
    };

    window.saveKb = function() {
        var id = document.getElementById('kbId').value;
        var title = document.getElementById('kbTitle').value.trim();
        var garbageType = document.getElementById('kbGarbageType').value;
        var content = document.getElementById('kbContent').value.trim();
        if (!title) { showToast('请输入标题', 'warning'); return; }

        var params = 'title=' + encodeURIComponent(title)
            + '&garbageType=' + encodeURIComponent(garbageType)
            + '&content=' + encodeURIComponent(content);
        if (id) params += '&id=' + id;

        apiFetch(contextPath + '/admin/knowledge/save', {
            method: 'POST',
            headers: {'Content-Type': 'application/x-www-form-urlencoded'},
            body: params
        })
        .then(function(r) { return r.json(); })
        .then(function(data) {
            if (data.code === 200) {
                closeKbModal();
                showToast('保存成功', 'success');
                setTimeout(function() { location.reload(); }, 800);
            } else {
                showToast(data.message || '保存失败', 'error');
            }
        })
        .catch(function(err) { showToast('操作失败', 'error'); });
    };

    window.deleteKb = function(id) {
        if (!confirm('确定删除该知识条目？')) return;
        apiFetch(contextPath + '/admin/knowledge/delete', {
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
})();
