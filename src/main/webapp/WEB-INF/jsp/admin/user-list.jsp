<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>用户管理 - 垃圾分类监管系统</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
    <jsp:include page="/WEB-INF/jsp/nav-admin.jsp" />
    
    <div class="main-content">
        <!-- 提示消息 -->
        <c:if test="${param.success == 'created'}">
            <div class="alert alert-success" style="margin-bottom:20px;padding:12px 16px;background:#d4edda;color:#155724;border-radius:4px;">用户创建成功</div>
        </c:if>
        <c:if test="${param.success == 'updated'}">
            <div class="alert alert-success" style="margin-bottom:20px;padding:12px 16px;background:#d4edda;color:#155724;border-radius:4px;">用户更新成功</div>
        </c:if>
        <c:if test="${param.success == 'deleted'}">
            <div class="alert alert-success" style="margin-bottom:20px;padding:12px 16px;background:#d4edda;color:#155724;border-radius:4px;">用户删除成功</div>
        </c:if>
        <c:if test="${param.success == 'statusUpdated'}">
            <div class="alert alert-success" style="margin-bottom:20px;padding:12px 16px;background:#d4edda;color:#155724;border-radius:4px;">状态更新成功</div>
        </c:if>
        
        <div class="card">
            <!-- 标题和操作按钮 -->
            <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:24px;">
                <h2 class="card-title" style="margin:0;border:none;padding:0;">用户列表</h2>
                <a href="${pageContext.request.contextPath}/admin/users/create" class="btn btn-success">+ 添加用户</a>
            </div>
            
            <!-- 搜索区域 -->
            <div style="background:#f8f9fa;padding:16px;border-radius:6px;margin-bottom:24px;">
                <form method="get" action="${pageContext.request.contextPath}/admin/users" style="display:flex;gap:12px;align-items:center;">
                    <input type="text" name="keyword" placeholder="搜索用户名..." value="${keyword}" 
                           style="flex:1;padding:10px 14px;border:1px solid #ddd;border-radius:4px;font-size:14px;">
                    <button type="submit" class="btn btn-primary">搜索</button>
                    <c:if test="${not empty keyword}">
                        <a href="${pageContext.request.contextPath}/admin/users" class="btn btn-secondary">清除</a>
                    </c:if>
                </form>
            </div>
            
            <!-- 用户表格 -->
            <c:choose>
                <c:when test="${empty users}">
                    <div style="text-align:center;padding:60px 20px;color:#7f8c8d;">
                        <p style="font-size:16px;margin-bottom:8px;">暂无用户数据</p>
                        <p style="font-size:14px;">点击上方"添加用户"创建新用户</p>
                    </div>
                </c:when>
                <c:otherwise>
                    <table class="table">
                        <thead>
                            <tr>
                                <th style="width:60px;">ID</th>
                                <th style="width:120px;">用户名</th>
                                <th>邮箱</th>
                                <th style="width:130px;">手机号</th>
                                <th style="width:100px;">角色</th>
                                <th style="width:80px;">状态</th>
                                <th style="width:140px;">创建时间</th>
                                <th style="width:200px;">操作</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach items="${users}" var="user">
                                <tr>
                                    <td>${user.id}</td>
                                    <td><strong>${user.username}</strong></td>
                                    <td>${user.email}</td>
                                    <td>${user.phone}</td>
                                    <td>
                                        <span class="badge ${user.role == 'admin' ? 'badge-submitted' : 'badge-pending'}">
                                            ${user.role == 'admin' ? '管理员' : '用户'}
                                        </span>
                                    </td>
                                    <td>
                                        <span class="badge ${user.status == 1 ? 'badge-approved' : 'badge-rejected'}">
                                            ${user.status == 1 ? '正常' : '禁用'}
                                        </span>
                                    </td>
                                    <td><fmt:formatDate value="${user.createTime}" pattern="yyyy-MM-dd HH:mm" /></td>
                                    <td>
                                        <div style="display:flex;gap:8px;flex-wrap:wrap;">
                                            <a href="${pageContext.request.contextPath}/admin/users/edit?id=${user.id}" 
                                               class="btn btn-small btn-primary">编辑</a>
                                            <c:if test="${user.id != sessionScope.loginUser.id}">
                                                <button onclick="updateStatus(${user.id}, ${user.status == 1 ? 0 : 1})" 
                                                        class="btn btn-small ${user.status == 1 ? 'btn-warning' : 'btn-success'}">
                                                    ${user.status == 1 ? '禁用' : '启用'}
                                                </button>
                                                <button onclick="deleteUser(${user.id})" 
                                                        class="btn btn-small btn-danger">删除</button>
                                            </c:if>
                                        </div>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                    
                    <!-- 分页 -->
                    <c:if test="${totalPages > 1}">
                        <div class="pagination">
                            <c:if test="${currentPage > 1}">
                                <a href="?page=${currentPage - 1}&keyword=${keyword}">上一页</a>
                            </c:if>
                            <c:if test="${currentPage == 1}">
                                <span class="disabled">上一页</span>
                            </c:if>
                            
                            <c:forEach begin="1" end="${totalPages}" var="i">
                                <c:choose>
                                    <c:when test="${i == currentPage}">
                                        <span class="active">${i}</span>
                                    </c:when>
                                    <c:otherwise>
                                        <a href="?page=${i}&keyword=${keyword}">${i}</a>
                                    </c:otherwise>
                                </c:choose>
                            </c:forEach>
                            
                            <c:if test="${currentPage < totalPages}">
                                <a href="?page=${currentPage + 1}&keyword=${keyword}">下一页</a>
                            </c:if>
                            <c:if test="${currentPage >= totalPages}">
                                <span class="disabled">下一页</span>
                            </c:if>
                        </div>
                    </c:if>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
    
<script>
var contextPath = '${pageContext.request.contextPath}';

function updateStatus(userId, newStatus) {
    var msg = newStatus == 1 ? '确定要启用该用户吗？' : '确定要禁用该用户吗？';
    if (!confirm(msg)) return;
    
    fetch(contextPath + '/admin/users/' + userId + '/status?status=' + newStatus, { method: 'PUT' })
    .then(r => r.json())
    .then(data => {
        if (data.code === 200) {
            alert('状态更新成功');
            location.reload();
        } else {
            alert(data.message || '状态更新失败');
        }
    })
    .catch(err => alert('操作失败，请稍后重试'));
}

function deleteUser(userId) {
    if (!confirm('确定要删除该用户吗？此操作不可恢复！')) return;
    
    fetch(contextPath + '/admin/users/' + userId, { method: 'DELETE' })
    .then(r => r.json())
    .then(data => {
        if (data.code === 200) {
            alert('删除成功');
            location.reload();
        } else {
            alert(data.message || '删除失败');
        }
    })
    .catch(err => alert('操作失败，请稍后重试'));
}
</script>
</body>
</html>
