<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>用户列表 - 用户管理系统</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
    <div class="container">
        <header class="header">
            <h1>用户管理系统</h1>
        </header>

        <div class="toolbar">
            <a href="${pageContext.request.contextPath}/user/add" class="btn btn-primary">新增用户</a>
            <form action="${pageContext.request.contextPath}/user/search" method="get" class="search-form">
                <input type="text" name="keyword" placeholder="搜索用户名..." 
                       value="${keyword}" class="search-input">
                <button type="submit" class="btn btn-secondary">搜索</button>
                <c:if test="${not empty keyword}">
                    <a href="${pageContext.request.contextPath}/user/list" class="btn">重置</a>
                </c:if>
            </form>
        </div>

        <c:if test="${not empty error}">
            <div class="alert alert-error">${error}</div>
        </c:if>

        <c:if test="${not empty success}">
            <div class="alert alert-success">${success}</div>
        </c:if>

        <table class="table">
            <thead>
                <tr>
                    <th>ID</th>
                    <th>用户名</th>
                    <th>邮箱</th>
                    <th>电话</th>
                    <th>创建时间</th>
                    <th>操作</th>
                </tr>
            </thead>
            <tbody>
                <c:choose>
                    <c:when test="${not empty users}">
                        <c:forEach var="user" items="${users}">
                            <tr>
                                <td>${user.id}</td>
                                <td>${user.username}</td>
                                <td>${user.email}</td>
                                <td>${user.phone}</td>
                                <td>${user.createdAt}</td>
                                <td class="actions">
                                    <a href="${pageContext.request.contextPath}/user/edit?id=${user.id}" 
                                       class="btn btn-small">编辑</a>
                                    <a href="${pageContext.request.contextPath}/user/delete?id=${user.id}" 
                                       class="btn btn-small btn-danger"
                                       onclick="return confirm('确定要删除用户 ${user.username} 吗？')">删除</a>
                                </td>
                            </tr>
                        </c:forEach>
                    </c:when>
                    <c:otherwise>
                        <tr>
                            <td colspan="6" class="text-center">暂无数据</td>
                        </tr>
                    </c:otherwise>
                </c:choose>
            </tbody>
        </table>
    </div>
</body>
</html>