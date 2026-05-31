<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>创建用户 - 垃圾分类监管系统</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
    <jsp:include page="/WEB-INF/jsp/nav-admin.jsp" />
    
    <div class="main-content">
        <div class="card" style="max-width:600px;">
            <h2 class="card-title">创建新用户</h2>
            
            <c:if test="${not empty error}">
                <div class="alert alert-error">${error}</div>
            </c:if>
            
            <form id="createForm" method="post" action="${pageContext.request.contextPath}/admin/users/create">
                <input type="hidden" name="_csrf" value="${sessionScope._csrfToken}">
                <div class="form-group">
                    <label for="username">用户名 <span class="required">*</span></label>
                    <input type="text" id="username" name="username" placeholder="请输入用户名" required>
                    <p class="text-muted" style="margin-top:6px;font-size:14px;">用于登录系统，长度3-20个字符</p>
                </div>
                
                <div class="form-group">
                    <label for="password">密码 <span class="required">*</span></label>
                    <input type="password" id="password" name="password" placeholder="请输入密码" required>
                    <p class="text-muted" style="margin-top:6px;font-size:14px;">长度6-20个字符</p>
                </div>
                
                <div class="form-group">
                    <label for="role">角色</label>
                    <select id="role" name="role" class="search-input" style="width:100%;">
                        <option value="user">普通用户</option>
                        <option value="admin">管理员</option>
                    </select>
                </div>
                
                <div class="form-group">
                    <label for="realName">真实姓名</label>
                    <input type="text" id="realName" name="realName" placeholder="请输入真实姓名">
                </div>
                
                <div class="form-group">
                    <label for="email">邮箱</label>
                    <input type="email" id="email" name="email" placeholder="请输入邮箱">
                </div>
                
                <div class="form-group">
                    <label for="phone">手机号</label>
                    <input type="tel" id="phone" name="phone" placeholder="请输入手机号">
                </div>
                
                <div class="form-actions">
                    <a href="${pageContext.request.contextPath}/admin/users" class="btn btn-secondary">取消</a>
                    <button type="submit" class="btn btn-success">创建用户</button>
                </div>
            </form>
        </div>
    </div>

<script>
window._pageConfig = {
    contextPath: '${pageContext.request.contextPath}',
    csrfToken: '${sessionScope._csrfToken}'
};
</script>
<script src="${pageContext.request.contextPath}/js/common.js"></script>
<script>
(function() {
    'use strict';
    var form = document.getElementById('createForm');
    form.addEventListener('submit', function(e) {
        e.preventDefault();
        ajaxSubmit(form, { successMsg: '用户创建成功' });
    });
})();
</script>
</body>
</html>
