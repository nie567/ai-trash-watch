<%@ page contentType="text/html;charset=UTF-8" language="java" isErrorPage="true" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>访问禁止 - 垃圾分类监管系统</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <style>
        body { background: var(--bg-deep); display:flex; align-items:center; justify-content:center; min-height:100vh; }
        .error-container { text-align:center; padding:40px; }
        .error-code { font-size:120px; font-weight:700; color:var(--accent-red); line-height:1; }
        .error-title { font-size:24px; color:var(--text-primary); margin:20px 0; }
        .error-message { font-size:14px; color:var(--text-secondary); margin-bottom:30px; }
    </style>
</head>
<body>
    <div class="error-container">
        <div class="error-code">403</div>
        <h1 class="error-title">访问被禁止</h1>
        <p class="error-message">您没有权限访问此页面，请确认是否已登录或联系管理员</p>
        <a href="${pageContext.request.contextPath}/logout" class="btn btn-primary">重新登录</a>
    </div>
</body>
</html>
