<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>个人中心 - 垃圾分类监管系统</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
    <jsp:include page="/WEB-INF/jsp/nav-user.jsp" />
    
    <div class="main-content">
        <c:if test="${param.success == 'updated'}">
            <div class="alert alert-success">个人信息更新成功</div>
        </c:if>
        
        <div class="card">
            <h2 class="card-title">基本信息</h2>
            <div class="info-grid">
                <div class="info-item">
                    <span class="info-label">用户名</span>
                    <span class="info-value">${user.username}</span>
                </div>
                <div class="info-item">
                    <span class="info-label">角色</span>
                    <span class="info-value">${user.role == 'admin' ? '管理员' : '普通用户'}</span>
                </div>
                <div class="info-item">
                    <span class="info-label">邮箱</span>
                    <span class="info-value">${empty user.email ? '-' : user.email}</span>
                </div>
                <div class="info-item">
                    <span class="info-label">手机号</span>
                    <span class="info-value">${empty user.phone ? '-' : user.phone}</span>
                </div>
                <div class="info-item">
                    <span class="info-label">账号状态</span>
                    <span class="info-value">${user.status == 1 ? '<span class=text-success>正常</span>' : '<span class=text-danger>禁用</span>'}</span>
                </div>
                <div class="info-item">
                    <span class="info-label">创建时间</span>
                    <span class="info-value"><fmt:formatDate value="${user.createTime}" pattern="yyyy-MM-dd HH:mm" /></span>
                </div>
            </div>
            
            <div class="form-actions" style="margin-top:16px;">
                <a href="${pageContext.request.contextPath}/user/profile/edit" class="btn btn-primary">编辑资料</a>
                <a href="${pageContext.request.contextPath}/user/password" class="btn btn-secondary">修改密码</a>
            </div>
        </div>
    </div>
</body>
</html>
