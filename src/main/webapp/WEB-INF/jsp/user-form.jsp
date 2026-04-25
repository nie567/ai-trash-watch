<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${isEdit ? '编辑用户' : '新增用户'} - 用户管理系统</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
    <div class="container">
        <header class="header">
            <h1>${isEdit ? '编辑用户' : '新增用户'}</h1>
        </header>

        <div class="form-wrapper">
            <c:if test="${not empty error}">
                <div class="alert alert-error">${error}</div>
            </c:if>

            <form action="${pageContext.request.contextPath}/user/${isEdit ? 'edit' : 'add'}" 
                  method="post" class="form">
                
                <c:if test="${isEdit}">
                    <input type="hidden" name="id" value="${user.id}">
                </c:if>

                <div class="form-group">
                    <label for="username">用户名 <span class="required">*</span></label>
                    <input type="text" id="username" name="username" 
                           value="${user.username}" required maxlength="50">
                </div>

                <div class="form-group">
                    <label for="password">密码 <c:if test="${!isEdit}"><span class="required">*</span></c:if></label>
                    <input type="password" id="password" name="password" 
                           ${isEdit ? '' : 'required'} maxlength="100"
                           placeholder="${isEdit ? '留空则不修改密码' : ''}">
                </div>

                <div class="form-group">
                    <label for="email">邮箱</label>
                    <input type="email" id="email" name="email" 
                           value="${user.email}" maxlength="100">
                </div>

                <div class="form-group">
                    <label for="phone">电话</label>
                    <input type="text" id="phone" name="phone" 
                           value="${user.phone}" maxlength="20">
                </div>

                <div class="form-actions">
                    <button type="submit" class="btn btn-primary">
                        ${isEdit ? '保存' : '提交'}
                    </button>
                    <a href="${pageContext.request.contextPath}/user/list" class="btn">取消</a>
                </div>
            </form>
        </div>
    </div>
</body>
</html>