<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>修改密码 - 用户管理系统</title>
    
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
    <jsp:include page="/WEB-INF/jsp/nav-user.jsp" />
    
    <div class="main-content">
        <div class="card">
            <h2>修改密码</h2>
            
            <c:if test="${param.success == 'changed'}">
                <div class="alert">密码修改成功</div>
            </c:if>
            
            <c:if test="${not empty error}">
                <div class="error-message">${error}</div>
            </c:if>
            
            <form method="post" action="${pageContext.request.contextPath}/user/password">
                <div class="form-group">
                    <label for="oldPassword">旧密码 <span class="required">*</span></label>
                    <input type="password" id="oldPassword" name="oldPassword" 
                           placeholder="请输入旧密码" required>
                </div>
                
                <div class="form-group">
                    <label for="newPassword">新密码 <span class="required">*</span></label>
                    <input type="password" id="newPassword" name="newPassword" 
                           placeholder="请输入新密码" required>
                    <div class="hint">至少6个字符</div>
                </div>
                
                <div class="form-group">
                    <label for="confirmPassword">确认新密码 <span class="required">*</span></label>
                    <input type="password" id="confirmPassword" name="confirmPassword" 
                           placeholder="请再次输入新密码" required>
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