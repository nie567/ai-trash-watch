<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>编辑资料 - 用户管理系统</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
    <jsp:include page="/WEB-INF/jsp/nav-user.jsp" />

    <div class="main-content">
        <div class="card">
            <h2>编辑个人资料</h2>

            <c:if test="${not empty error}">
                <div class="error-message">${error}</div>
            </c:if>

            <form method="post" action="${pageContext.request.contextPath}/user/profile/edit">
                <div class="form-group">
                    <label for="email">邮箱</label>
                    <input type="email" id="email" name="email" value="${user.email}" placeholder="请输入邮箱">
                    <div class="hint">用于接收通知和找回密码</div>
                </div>

                <div class="form-group">
                    <label for="phone">手机号</label>
                    <input type="tel" id="phone" name="phone" value="${user.phone}" placeholder="请输入手机号">
                    <div class="hint">11位手机号码</div>
                </div>

                <div class="btn-group">
                    <a href="${pageContext.request.contextPath}/user/profile" class="btn btn-secondary">取消</a>
                    <button type="submit" class="btn btn-primary">保存</button>
                </div>
            </form>
        </div>
    </div>
</body>
</html>
