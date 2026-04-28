<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>登录 - 垃圾分类监管系统</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body {
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            min-height: 100vh;
            display: flex;
            align-items: center;
            justify-content: center;
        }
        .login-container {
            width: 400px;
            padding: 40px;
            background: white;
            border-radius: 16px;
            box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
        }
        .login-header {
            text-align: center;
            margin-bottom: 30px;
        }
        .login-header h1 {
            font-size: 28px;
            font-weight: 600;
            color: #333;
            margin-bottom: 8px;
        }
        .login-header p {
            color: #666;
            font-size: 14px;
        }
        .form-group {
            margin-bottom: 20px;
        }
        .form-group label {
            display: block;
            color: #333;
            font-size: 14px;
            font-weight: 500;
            margin-bottom: 8px;
        }
        .form-group input {
            width: 100%;
            padding: 14px 16px;
            border: 2px solid #e1e1e1;
            border-radius: 8px;
            font-size: 14px;
            transition: border-color 0.3s;
        }
        .form-group input:focus {
            outline: none;
            border-color: #667eea;
        }
        .form-group.remember {
            display: flex;
            align-items: center;
        }
        .form-group.remember input {
            width: auto;
            margin-right: 8px;
        }
        .form-group.remember label {
            margin-bottom: 0;
            font-weight: normal;
        }
        .btn-login {
            width: 100%;
            padding: 14px;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            border: none;
            border-radius: 8px;
            font-size: 16px;
            font-weight: 500;
            cursor: pointer;
            transition: transform 0.2s, box-shadow 0.2s;
        }
        .btn-login:hover {
            transform: translateY(-2px);
            box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
        }
        .error-message {
            background: #fee;
            border: 1px solid #fcc;
            color: #c33;
            padding: 12px 16px;
            border-radius: 8px;
            margin-bottom: 20px;
            font-size: 14px;
        }
        .hint {
            text-align: center;
            margin-top: 20px;
            font-size: 13px;
            color: #999;
        }
        .hint span { color: #667eea; font-weight: 500; }
    </style>
</head>
<body>
    <div class="login-container">
        <div class="login-header">
            <h1>垃圾分类监管系统</h1>
            <p>请登录您的账号</p>
        </div>
        
        <c:if test="${not empty error}">
            <div class="error-message">${error}</div>
        </c:if>
        
        <form method="post" action="${pageContext.request.contextPath}/login">
            <div class="form-group">
                <label for="username">用户名</label>
                <input type="text" id="username" name="username" 
                       value="${cookie.rememberUser.value}" 
                       placeholder="请输入用户名" required autofocus>
            </div>
            
            <div class="form-group">
                <label for="password">密码</label>
                <input type="password" id="password" name="password" 
                       placeholder="请输入密码" required>
            </div>
            
            <div class="form-group remember">
                <input type="checkbox" id="rememberMe" name="rememberMe">
                <label for="rememberMe">记住用户名</label>
            </div>
            
            <button type="submit" class="btn-login">登录</button>
        </form>
        
        <div class="hint">
            演示账号: <span>admin</span> / <span>admin123</span>
        </div>
    </div>
</body>
</html>