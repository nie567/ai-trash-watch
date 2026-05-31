<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>编辑用户 - 垃圾分类监管系统</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
    <jsp:include page="/WEB-INF/jsp/nav-admin.jsp" />

    <div class="main-content">
        <div class="card" style="max-width:600px;">
            <h2 class="card-title">编辑用户</h2>

            <c:if test="${not empty error}">
                <div class="alert alert-error">${error}</div>
            </c:if>

            <form method="post" action="${pageContext.request.contextPath}/admin/user/edit">
                <input type="hidden" name="_csrf" value="${sessionScope._csrfToken}">
                <input type="hidden" name="id" value="${user.id}">

                <div class="form-group">
                    <label>用户名</label>
                    <input type="text" value="${user.username}" readonly class="search-input" style="width:100%;opacity:0.6;">
                </div>

                <div class="form-group">
                    <label for="password">密码</label>
                    <input type="password" id="password" name="password" placeholder="留空则不修改密码">
                    <p class="text-muted" style="margin-top:6px;font-size:14px;">至少6个字符，留空不修改</p>
                </div>

                <div class="form-group">
                    <label for="email">邮箱</label>
                    <input type="email" id="email" name="email" value="${user.email}" placeholder="请输入邮箱">
                </div>

                <div class="form-group">
                    <label for="phone">手机号</label>
                    <input type="tel" id="phone" name="phone" value="${user.phone}" placeholder="请输入手机号">
                </div>

                <div class="form-group">
                    <label for="role">角色</label>
                    <select id="role" name="role" class="search-input" style="width:100%;">
                        <option value="user" ${user.role == 'user' ? 'selected' : ''}>普通用户</option>
                        <option value="admin" ${user.role == 'admin' ? 'selected' : ''}>管理员</option>
                    </select>
                </div>

                <div class="form-group">
                    <label for="status">状态</label>
                    <select id="status" name="status" class="search-input" style="width:100%;">
                        <option value="1" ${user.status == 1 ? 'selected' : ''}>正常</option>
                        <option value="0" ${user.status == 0 ? 'selected' : ''}>禁用</option>
                    </select>
                </div>

                <div class="form-actions">
                    <a href="${pageContext.request.contextPath}/admin/users" class="btn btn-secondary">取消</a>
                    <button type="submit" class="btn btn-success">保存修改</button>
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
</body>
</html>
