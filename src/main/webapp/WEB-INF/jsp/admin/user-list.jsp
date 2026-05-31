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
            <div class="alert alert-success">用户创建成功</div>
        </c:if>
        <c:if test="${param.success == 'updated'}">
            <div class="alert alert-success">用户更新成功</div>
        </c:if>
        <c:if test="${param.success == 'deleted'}">
            <div class="alert alert-success">用户删除成功</div>
        </c:if>
        <c:if test="${param.success == 'statusUpdated'}">
            <div class="alert alert-success">状态更新成功</div>
        </c:if>
        
        <div class="card">
            <!-- 标题和操作按钮 -->
            <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:24px;">
                <h2 class="card-title" style="margin:0;border:none;padding:0;">用户列表</h2>
                <a href="${pageContext.request.contextPath}/admin/users/create" class="btn btn-success">+ 添加用户</a>
            </div>
            
            <!-- 搜索区域 -->
            <div style="background:rgba(255,255,255,0.03);padding:16px;border-radius:8px;margin-bottom:24px;border:1px solid rgba(255,255,255,0.06);">
                <form method="get" action="${pageContext.request.contextPath}/admin/users" style="display:flex;gap:12px;align-items:center;">
                    <input type="text" name="keyword" placeholder="搜索用户名..." value="${keyword}" 
                           class="search-input" style="flex:1;">
                    <button type="submit" class="btn btn-primary">搜索</button>
                    <c:if test="${not empty keyword}">
                        <a href="${pageContext.request.contextPath}/admin/users" class="btn btn-secondary">清除</a>
                    </c:if>
                </form>
            </div>
            
            <!-- 用户表格 -->
            <c:choose>
                <c:when test="${empty users}">
                    <div class="text-center" style="padding:60px 20px;">
                        <p class="text-muted" style="font-size:16px;margin-bottom:8px;">暂无用户数据</p>
                        <p class="text-muted" style="font-size:14px;">点击上方"添加用户"创建新用户</p>
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
                                    <td><strong style="color:var(--accent-purple-end)">${user.username}</strong></td>
                                    <td>${user.email}</td>
                                    <td>${user.phone}</td>
                                    <td><span class="badge ${user.role == 'admin' ? 'badge-submitted' : 'badge-pending'}">${user.role == 'admin' ? '管理员' : '用户'}</span></td>
                                    <td><span class="badge ${user.status == 1 ? 'badge-rectified' : 'badge-rejected'}">${user.status == 1 ? '正常' : '禁用'}</span></td>
                                    <td><fmt:formatDate value="${user.createTime}" pattern="yyyy-MM-dd HH:mm"/></td>
                                    <td class="actions">
                                        <a href="${pageContext.request.contextPath}/admin/users/edit?id=${user.id}" class="btn btn-small btn-secondary">编辑</a>
                                        <button class="btn btn-small ${user.status == 1 ? 'btn-warning' : 'btn-success'}" onclick="toggleStatus(${user.id}, ${user.status})">${user.status == 1 ? '禁用' : '启用'}</button>
                                        <button class="btn btn-small btn-danger" onclick="deleteUser(${user.id}, '${user.username}')">删除</button>
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
window._pageConfig = {
    contextPath: '${pageContext.request.contextPath}',
    csrfToken: '${sessionScope._csrfToken}'
};
</script>
<script src="${pageContext.request.contextPath}/js/common.js"></script>
<script src="${pageContext.request.contextPath}/js/user-list.js"></script>
</body>
</html>
