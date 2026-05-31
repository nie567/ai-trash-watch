<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>编辑资料 - 垃圾分类监管系统</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
    <jsp:include page="/WEB-INF/jsp/nav-user.jsp" />

    <div class="main-content">
        <div class="card">
            <h2 class="card-title">编辑个人资料</h2>

            <c:if test="${not empty error}">
                <div class="alert alert-error">${error}</div>
            </c:if>

            <form id="profileForm" method="post" action="${pageContext.request.contextPath}/user/profile/edit">
                <input type="hidden" name="_csrf" value="${sessionScope._csrfToken}">
                <div class="form-group">
                    <label for="realName">真实姓名</label>
                    <input type="text" id="realName" name="realName" value="${user.realName}" placeholder="请输入真实姓名">
                </div>

                <div class="form-group">
                    <label for="email">邮箱</label>
                    <input type="email" id="email" name="email" value="${user.email}" placeholder="请输入邮箱">
                </div>

                <div class="form-group">
                    <label for="phone">手机号</label>
                    <input type="tel" id="phone" name="phone" value="${user.phone}" placeholder="请输入手机号">
                </div>

                <div class="form-actions">
                    <a href="${pageContext.request.contextPath}/user/profile" class="btn btn-secondary">取消</a>
                    <button type="submit" class="btn btn-primary">保存</button>
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
    var form = document.getElementById('profileForm');
    form.addEventListener('submit', function(e) {
        e.preventDefault();
        ajaxSubmit(form, { successMsg: '资料已更新' });
    });
})();
</script>
</body>
</html>
