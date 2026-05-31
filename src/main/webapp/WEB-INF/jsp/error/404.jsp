<%@ page contentType="text/html;charset=UTF-8" language="java" isErrorPage="true" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>页面未找到 - 垃圾分类监管系统</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <style>
        body { background: var(--bg-deep); display:flex; align-items:center; justify-content:center; min-height:100vh; }
        .error-container { text-align:center; padding:40px; }
        .error-code { font-size:120px; font-weight:700; color:var(--accent-orange); line-height:1; }
        .error-title { font-size:24px; color:var(--text-primary); margin:20px 0; }
        .error-message { font-size:14px; color:var(--text-secondary); margin-bottom:30px; }
    </style>
</head>
<body>
    <div class="error-container">
        <div class="error-code">404</div>
        <h1 class="error-title">页面未找到</h1>
        <p class="error-message">您访问的页面不存在或已被移除</p>
        <a href="${pageContext.request.contextPath}/" class="btn btn-primary">返回首页</a>
    </div>
</body>
</html>
