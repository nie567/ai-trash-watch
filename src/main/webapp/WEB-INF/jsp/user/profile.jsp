<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>个人中心 - 用户管理系统</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
    <jsp:include page="/WEB-INF/jsp/nav-user.jsp" />
    
    <div class="main-content">
        <c:if test="${param.success == 'updated'}">
            <div class="alert alert-success">个人信息更新成功</div>
        </c:if>
        
        <div class="card">
            <h2>基本信息</h2>
            <div class="info-grid">
                <div class="info-item">
                    <label>用户名</label>
                    <div class="value">${user.username}</div>
                </div>
                <div class="info-item">
                    <label>角色</label>
                    <div class="value">${user.role == 'admin' ? '管理员' : '普通用户'}</div>
                </div>
                <div class="info-item">
                    <label>邮箱</label>
                    <div class="value">${empty user.email ? '-' : user.email}</div>
                </div>
                <div class="info-item">
                    <label>手机号</label>
                    <div class="value">${empty user.phone ? '-' : user.phone}</div>
                </div>
                <div class="info-item">
                    <label>账号状态</label>
                    <div class="value">${user.status == 1 ? '正常' : '禁用'}</div>
                </div>
                <div class="info-item">
                    <label>创建时间</label>
                    <div class="value">
                        <fmt:formatDate value="${user.createTime}" pattern="yyyy-MM-dd HH:mm" />
                    </div>
                </div>
            </div>
            
            <div class="btn-group">
                <a href="${pageContext.request.contextPath}/user/profile/edit" class="btn btn-primary">编辑资料</a>
                <a href="${pageContext.request.contextPath}/user/password" class="btn btn-outline">修改密码</a>
            </div>
        </div>
    </div>
</body>
</html>