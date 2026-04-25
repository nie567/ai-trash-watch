<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>用户列表 - 用户管理系统</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body {
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
            background: #f5f6fa;
            min-height: 100vh;
        }
        .header {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            padding: 0 30px;
            height: 60px;
            display: flex;
            align-items: center;
            justify-content: space-between;
        }
        .header h1 { font-size: 20px; font-weight: 500; }
        .header-right { display: flex; align-items: center; gap: 20px; }
        .header-right a { color: white; text-decoration: none; font-size: 14px; }
        .header-right a:hover { text-decoration: underline; }
        .container { max-width: 1200px; margin: 0 auto; padding: 30px; }
        .card {
            background: white;
            border-radius: 12px;
            box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
            padding: 24px;
            margin-bottom: 24px;
        }
        .card-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 20px;
        }
        .card-header h2 { color: #333; font-size: 18px; }
        .btn {
            display: inline-block;
            padding: 10px 20px;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            text-decoration: none;
            border-radius: 8px;
            font-size: 14px;
            font-weight: 500;
            border: none;
            cursor: pointer;
            transition: transform 0.2s;
        }
        .btn:hover { transform: translateY(-1px); }
        .btn-sm { padding: 6px 12px; font-size: 12px; }
        .btn-danger { background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%); }
        .btn-success { background: linear-gradient(135deg, #10b981 0%, #059669 100%); }
        .search-form {
            display: flex;
            gap: 12px;
            margin-bottom: 20px;
        }
        .search-form input {
            flex: 1;
            padding: 10px 16px;
            border: 2px solid #e1e1e1;
            border-radius: 8px;
            font-size: 14px;
        }
        .search-form input:focus {
            outline: none;
            border-color: #667eea;
        }
        .search-form button {
            padding: 10px 24px;
            background: #333;
            color: white;
            border: none;
            border-radius: 8px;
            font-size: 14px;
            cursor: pointer;
        }
        table { width: 100%; border-collapse: collapse; }
        th, td { padding: 12px 16px; text-align: left; border-bottom: 1px solid #eee; }
        th { background: #f9fafb; color: #666; font-weight: 500; font-size: 14px; }
        td { color: #333; font-size: 14px; }
        tr:hover { background: #f9fafb; }
        .status {
            display: inline-block;
            padding: 4px 10px;
            border-radius: 20px;
            font-size: 12px;
            font-weight: 500;
        }
        .status-active { background: #dcfce7; color: #166534; }
        .status-disabled { background: #fee2e2; color: #991b1b; }
        .role-badge {
            display: inline-block;
            padding: 4px 10px;
            border-radius: 20px;
            font-size: 12px;
            font-weight: 500;
        }
        .role-admin { background: #fef3c7; color: #92400e; }
        .role-user { background: #dbeafe; color: #1e40af; }
        .actions { display: flex; gap: 8px; }
        .pagination {
            display: flex;
            justify-content: center;
            gap: 8px;
            margin-top: 20px;
        }
        .pagination a {
            padding: 8px 14px;
            border: 1px solid #e1e1e1;
            border-radius: 6px;
            color: #333;
            text-decoration: none;
            font-size: 14px;
        }
        .pagination a:hover { background: #f5f6fa; }
        .pagination .active {
            background: #667eea;
            color: white;
            border-color: #667eea;
        }
        .pagination .disabled {
            color: #ccc;
            pointer-events: none;
        }
        .alert {
            padding: 12px 16px;
            border-radius: 8px;
            margin-bottom: 16px;
            font-size: 14px;
        }
        .alert-success {
            background: #dcfce7;
            color: #166534;
            border: 1px solid #bbf7d0;
        }
        .empty-state {
            text-align: center;
            padding: 60px 20px;
            color: #999;
        }
    </style>
</head>
<body>
    <div class="header">
        <h1>用户管理系统 - 用户列表</h1>
        <div class="header-right">
            <a href="${pageContext.request.contextPath}/admin/dashboard">仪表盘</a>
            <span>欢迎，${sessionScope.loginUser.username}</span>
            <a href="${pageContext.request.contextPath}/logout">退出登录</a>
        </div>
    </div>
    
    <div class="container">
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
            <div class="card-header">
                <h2>用户列表</h2>
                <a href="${pageContext.request.contextPath}/admin/users/create" class="btn">添加用户</a>
            </div>
            
            <form method="get" action="${pageContext.request.contextPath}/admin/users" class="search-form">
                <input type="text" name="keyword" placeholder="搜索用户名..." value="${keyword}">
                <button type="submit">搜索</button>
                <c:if test="${not empty keyword}">
                    <a href="${pageContext.request.contextPath}/admin/users" class="btn btn-sm">清除筛选</a>
                </c:if>
            </form>
            
            <c:choose>
                <c:when test="${pageResult.total == 0}">
                    <div class="empty-state">
                        <p>暂无用户数据</p>
                    </div>
                </c:when>
                <c:otherwise>
                    <table>
                        <thead>
                            <tr>
                                <th>ID</th>
                                <th>用户名</th>
                                <th>邮箱</th>
                                <th>手机号</th>
                                <th>角色</th>
                                <th>状态</th>
                                <th>创建时间</th>
                                <th>操作</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach items="${pageResult.data}" var="user">
                                <tr>
                                    <td>${user.id}</td>
                                    <td>${user.username}</td>
                                    <td>${user.email}</td>
                                    <td>${user.phone}</td>
                                    <td>
                                        <span class="role-badge ${user.role == 'admin' ? 'role-admin' : 'role-user'}">
                                            ${user.role == 'admin' ? '管理员' : '普通用户'}
                                        </span>
                                    </td>
                                    <td>
                                        <span class="status ${user.status == 1 ? 'status-active' : 'status-disabled'}">
</span>
                                    </td>
                                    <td>
                                        <fmt:formatDate value="${user.createTime}" pattern="yyyy-MM-dd HH:mm" />
                                    </td>
<td>
                                            <a href="${pageContext.request.contextPath}/admin/users/edit?id=${user.id}" class="btn btn-sm">编辑</a>
                                            <c:if test="${user.id != sessionScope.loginUser.id}">
                                                <a href="${pageContext.request.contextPath}/admin/users/${user.id}/status?status=${user.status == 1 ? 0 : 1}" 
                                                   class="btn btn-sm ${user.status == 1 ? 'btn-danger' : 'btn-success'}">
                                                    ${user.status == 1 ? '禁用' : '启用'}
                                                </a>
                                                <a href="${pageContext.request.contextPath}/admin/users/${user.id}" 
                                                   class="btn btn-sm btn-danger"
                                                   onclick="return confirm('确定要删除该用户吗？')">删除</a>
                                            </c:if>
                                        </div>
                                    </td>
                            </c:forEach>
                        </tbody>
                    </table>
                    
                    <c:if test="${pageResult.totalPages > 1}">
                        <div class="pagination">
                            <c:if test="${pageResult.hasPrev}">
                                <a href="?page=${pageResult.page - 1}&size=${pageResult.pageSize}&keyword=${keyword}">上一页</a>
                            </c:if>
                            <c:if test="${!pageResult.hasPrev}">
                                <a class="disabled">上一页</a>
                            </c:if>
                            
                            <c:forEach begin="1" end="${pageResult.totalPages}" var="i">
                                <c:choose>
                                    <c:when test="${i == pageResult.page}">
                                        <a class="active">${i}</a>
                                    </c:when>
                                    <c:otherwise>
                                        <a href="?page=${i}&size=${pageResult.pageSize}&keyword=${keyword}">${i}</a>
                                    </c:otherwise>
                                </c:choose>
                            </c:forEach>
                            
                            <c:if test="${pageResult.hasNext}">
                                <a href="?page=${pageResult.page + 1}&size=${pageResult.pageSize}&keyword=${keyword}">下一页</a>
                            </c:if>
                            <c:if test="${!pageResult.hasNext}">
                                <a class="disabled">下一页</a>
                            </c:if>
                        </div>
                    </c:if>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</body>
</html>