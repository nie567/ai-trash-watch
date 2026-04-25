<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>修改密码 - 用户管理系统</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body {
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
            background: #f5f6fa;
            min-height: 100vh;
        }
        .header {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            padding: 0 30px;
            height: 60px;
            display: flex;
            align-items: center;
            justify-content: space-between;
        }
        .header h1 { font-size: 20px; font-weight: 500; }
        .header-right { display: flex; align-items: center; gap: 20px; }
        .header-right a { color: white; text-decoration: none; font-size: 14px; }
        .header-right a:hover { text-decoration: underline; }
        .container { max-width: 500px; margin: 0 auto; padding: 30px; }
        .card {
            background: white;
            border-radius: 12px;
            box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
            padding: 30px;
        }
        .card h2 {
            color: #333;
            font-size: 20px;
            margin-bottom: 24px;
        }
        .form-group { margin-bottom: 20px; }
        .form-group label {
            display: block;
            color: #333;
            font-size: 14px;
            font-weight: 500;
            margin-bottom: 8px;
        }
        .form-group label .required { color: #ef4444; }
        .form-group input {
            width: 100%;
            padding: 12px 16px;
            border: 2px solid #e1e1e1;
            border-radius: 8px;
            font-size: 14px;
            transition: border-color 0.3s;
        }
        .form-group input:focus {
            outline: none;
            border-color: #667eea;
        }
        .form-group .hint {
            color: #999;
            font-size: 12px;
            margin-top: 6px;
        }
        .btn-group {
            display: flex;
            gap: 12px;
            margin-top: 30px;
        }
        .btn {
            flex: 1;
            padding: 14px;
            border: none;
            border-radius: 8px;
            font-size: 14px;
            font-weight: 500;
            cursor: pointer;
            text-align: center;
            text-decoration: none;
            transition: transform 0.2s;
        }
        .btn:hover { transform: translateY(-1px); }
        .btn-primary {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
        }
        .btn-secondary {
            background: #e5e7eb;
            color: #333;
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
        .alert {
            background: #dcfce7;
            border: 1px solid #bbf7d0;
            color: #166534;
            padding: 12px 16px;
            border-radius: 8px;
            margin-bottom: 20px;
            font-size: 14px;
        }
    </style>
</head>
<body>
    <div class="header">
        <h1>用户管理系统 - 修改密码</h1>
        <div class="header-right">
            <a href="${pageContext.request.contextPath}/user/profile">返回</a>
            <span>欢迎，${sessionScope.loginUser.username}</span>
            <a href="${pageContext.request.contextPath}/logout">退出登录</a>
        </div>
    </div>
    
    <div class="container">
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