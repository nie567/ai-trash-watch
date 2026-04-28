<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>分类规则管理 - 垃圾分类监管系统</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
    <jsp:include page="/WEB-INF/jsp/nav-admin.jsp" />

    <div class="main-content">
        <div class="card">
            <h2 class="card-title">分类规则管理</h2>

            <div style="margin-bottom:16px;">
                <button class="btn btn-success" onclick="showRuleModal()">新增规则</button>
            </div>

            <table class="table">
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>检测类别(className)</th>
                        <th>映射类别(mappedCategory)</th>
                        <th>描述</th>
                        <th>状态</th>
                        <th>操作</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach items="${rules}" var="rule">
                        <tr>
                            <td>${rule.id}</td>
                            <td>${rule.className}</td>
                            <td><span class="badge ${rule.mappedCategory == '可回收物' ? 'category-recyclable' : rule.mappedCategory == '厨余垃圾' ? 'category-kitchen' : rule.mappedCategory == '有害垃圾' ? 'category-hazardous' : rule.mappedCategory == '其他垃圾' ? 'category-other' : 'category-mixed'}">${rule.mappedCategory}</span></td>
                            <td>${rule.description}</td>
                            <td><span class="badge ${rule.status == 1 ? 'badge-approved' : 'badge-ignored'}">${rule.status == 1 ? '启用' : '禁用'}</span></td>
                            <td>
                                <button class="btn btn-small btn-primary" onclick='editRule(${rule.id},"${rule.className}","${rule.mappedCategory}","${rule.description}",${rule.status})'>编辑</button>
                                <button class="btn btn-small ${rule.status == 1 ? 'btn-warning' : 'btn-success'}" onclick="toggleStatus(${rule.id},${rule.status})">${rule.status == 1 ? '禁用' : '启用'}</button>
                            </td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
        </div>
    </div>

    <!-- 规则编辑模态框 -->
    <div class="modal-overlay" id="ruleModal">
        <div class="modal">
            <h3 id="ruleModalTitle">新增规则</h3>
            <input type="hidden" id="ruleId">
            <div class="form-group">
                <label>检测类别(className) <span class="required">*</span></label>
                <input type="text" id="ruleClassName" placeholder="如 PAPER, PLASTIC">
            </div>
            <div class="form-group">
                <label>映射类别(mappedCategory) <span class="required">*</span></label>
                <select id="ruleMappedCategory">
                    <option value="可回收物">可回收物</option>
                    <option value="厨余垃圾">厨余垃圾</option>
                    <option value="有害垃圾">有害垃圾</option>
                    <option value="其他垃圾">其他垃圾</option>
                </select>
            </div>
            <div class="form-group">
                <label>描述</label>
                <input type="text" id="ruleDescription" placeholder="规则描述">
            </div>
            <div class="modal-actions">
                <button class="btn btn-secondary" onclick="closeRuleModal()">取消</button>
                <button class="btn btn-success" onclick="saveRule()">保存</button>
            </div>
        </div>
    </div>

<script>
var contextPath = '${pageContext.request.contextPath}';

function showRuleModal() {
    document.getElementById('ruleModalTitle').textContent = '新增规则';
    document.getElementById('ruleId').value = '';
    document.getElementById('ruleClassName').value = '';
    document.getElementById('ruleMappedCategory').value = '可回收物';
    document.getElementById('ruleDescription').value = '';
    document.getElementById('ruleModal').classList.add('show');
}

function editRule(id, className, mappedCategory, description, status) {
    document.getElementById('ruleModalTitle').textContent = '编辑规则';
    document.getElementById('ruleId').value = id;
    document.getElementById('ruleClassName').value = className;
    document.getElementById('ruleMappedCategory').value = mappedCategory;
    document.getElementById('ruleDescription').value = description;
    document.getElementById('ruleModal').classList.add('show');
}

function closeRuleModal() {
    document.getElementById('ruleModal').classList.remove('show');
}

function saveRule() {
    var id = document.getElementById('ruleId').value;
    var className = document.getElementById('ruleClassName').value;
    var mappedCategory = document.getElementById('ruleMappedCategory').value;
    var description = document.getElementById('ruleDescription').value;
    if (!className.trim()) { alert('请输入检测类别'); return; }

    var params = 'className=' + encodeURIComponent(className) + '&mappedCategory=' + encodeURIComponent(mappedCategory) + '&description=' + encodeURIComponent(description) + '&status=1';
    if (id) params += '&id=' + id;

    fetch(contextPath + '/admin/rule/save', {
        method: 'POST',
        headers: {'Content-Type': 'application/x-www-form-urlencoded'},
        body: params
    }).then(r => r.json()).then(data => {
        if (data.code === 200) { closeRuleModal(); location.reload(); }
        else { alert(data.message || '保存失败'); }
    }).catch(err => { alert('操作失败'); });
}

function toggleStatus(id, currentStatus) {
    var newStatus = currentStatus == 1 ? 0 : 1;
    var params = 'id=' + id + '&className=&mappedCategory=可回收物&description=&status=' + newStatus;
    fetch(contextPath + '/admin/rule/save', {
        method: 'POST',
        headers: {'Content-Type': 'application/x-www-form-urlencoded'},
        body: params
    }).then(r => r.json()).then(data => {
        if (data.code === 200) { location.reload(); }
        else { alert(data.message || '操作失败'); }
    }).catch(err => { alert('操作失败'); });
}
</script>
</body>
</html>
