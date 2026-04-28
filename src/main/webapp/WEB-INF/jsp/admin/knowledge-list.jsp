<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>知识库管理 - 垃圾分类监管系统</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
    <jsp:include page="/WEB-INF/jsp/nav-admin.jsp" />

    <div class="main-content">
        <div class="card">
            <h2 class="card-title">知识库管理</h2>

            <div style="margin-bottom:16px;">
                <button class="btn btn-success" onclick="showKbModal()">新增知识条目</button>
            </div>

            <table class="table">
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>标题</th>
                        <th>垃圾类型</th>
                        <th>内容</th>
                        <th>操作</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach items="${knowledgeList}" var="kb">
                        <tr>
                            <td>${kb.id}</td>
                            <td>${kb.title}</td>
                            <td><span class="badge ${kb.garbageType == '可回收物' ? 'category-recyclable' : kb.garbageType == '厨余垃圾' ? 'category-kitchen' : kb.garbageType == '有害垃圾' ? 'category-hazardous' : 'category-other'}">${kb.garbageType}</span></td>
                            <td>${kb.content.length() > 50 ? kb.content.substring(0, 50).concat('...') : kb.content}</td>
                            <td>
                                <button class="btn btn-small btn-primary" onclick='editKb(${kb.id},"${kb.title}","${kb.garbageType}","${kb.content}")'>编辑</button>
                                <button class="btn btn-small btn-danger" onclick="deleteKb(${kb.id})">删除</button>
                            </td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty knowledgeList}">
                        <tr><td colspan="5" class="text-center text-muted">暂无知识条目</td></tr>
                    </c:if>
                </tbody>
            </table>
        </div>
    </div>

    <!-- 知识条目编辑模态框 -->
    <div class="modal-overlay" id="kbModal">
        <div class="modal">
            <h3 id="kbModalTitle">新增知识条目</h3>
            <input type="hidden" id="kbId">
            <div class="form-group">
                <label>标题 <span class="required">*</span></label>
                <input type="text" id="kbTitle" placeholder="知识条目标题">
            </div>
            <div class="form-group">
                <label>垃圾类型 <span class="required">*</span></label>
                <select id="kbGarbageType">
                    <option value="可回收物">可回收物</option>
                    <option value="厨余垃圾">厨余垃圾</option>
                    <option value="有害垃圾">有害垃圾</option>
                    <option value="其他垃圾">其他垃圾</option>
                </select>
            </div>
            <div class="form-group">
                <label>内容</label>
                <textarea id="kbContent" rows="5" placeholder="知识内容"></textarea>
            </div>
            <div class="form-group">
                <label>配图路径</label>
                <input type="text" id="kbImagePath" placeholder="可选">
            </div>
            <div class="modal-actions">
                <button class="btn btn-secondary" onclick="closeKbModal()">取消</button>
                <button class="btn btn-success" onclick="saveKb()">保存</button>
            </div>
        </div>
    </div>

<script>
var contextPath = '${pageContext.request.contextPath}';

function showKbModal() {
    document.getElementById('kbModalTitle').textContent = '新增知识条目';
    document.getElementById('kbId').value = '';
    document.getElementById('kbTitle').value = '';
    document.getElementById('kbGarbageType').value = '可回收物';
    document.getElementById('kbContent').value = '';
    document.getElementById('kbImagePath').value = '';
    document.getElementById('kbModal').classList.add('show');
}

function editKb(id, title, garbageType, content) {
    document.getElementById('kbModalTitle').textContent = '编辑知识条目';
    document.getElementById('kbId').value = id;
    document.getElementById('kbTitle').value = title;
    document.getElementById('kbGarbageType').value = garbageType;
    document.getElementById('kbContent').value = content;
    document.getElementById('kbModal').classList.add('show');
}

function closeKbModal() {
    document.getElementById('kbModal').classList.remove('show');
}

function saveKb() {
    var id = document.getElementById('kbId').value;
    var title = document.getElementById('kbTitle').value;
    var garbageType = document.getElementById('kbGarbageType').value;
    var content = document.getElementById('kbContent').value;
    var imagePath = document.getElementById('kbImagePath').value;
    if (!title.trim()) { alert('请输入标题'); return; }

    var params = 'title=' + encodeURIComponent(title) + '&garbageType=' + encodeURIComponent(garbageType) + '&content=' + encodeURIComponent(content) + '&imagePath=' + encodeURIComponent(imagePath);
    if (id) params += '&id=' + id;

    fetch(contextPath + '/admin/knowledge/save', {
        method: 'POST',
        headers: {'Content-Type': 'application/x-www-form-urlencoded'},
        body: params
    }).then(r => r.json()).then(data => {
        if (data.code === 200) { closeKbModal(); location.reload(); }
        else { alert(data.message || '保存失败'); }
    }).catch(err => { alert('操作失败'); });
}

function deleteKb(id) {
    if (!confirm('确定删除该知识条目？')) return;
    fetch(contextPath + '/admin/knowledge/delete', {
        method: 'POST',
        headers: {'Content-Type': 'application/x-www-form-urlencoded'},
        body: 'id=' + id
    }).then(r => r.json()).then(data => {
        if (data.code === 200) { location.reload(); }
        else { alert(data.message || '删除失败'); }
    }).catch(err => { alert('操作失败'); });
}
</script>
</body>
</html>
