<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${empty user.id ? '添加用户' : '编辑用户'} - 用户管理系统</title>
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
        .container { max-width: 600px; margin: 0 auto; padding: 30px; }
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
        .form-group input, .form-group select {
            width: 100%;
            padding: 12px 16px;
            border: 2px solid #e1e1e1;
            border-radius: 8px;
            font-size: 14px;
            transition: border-color 0.3s;
        }
        .form-group input:focus, .form-group select:focus {
            outline: none;
            border-color: #667eea;
        }
        .form-group .hint {
            color: #999;
            font-size: 12px;
            margin-top: 6px;
        }
        .form-row {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 20px;
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
    </style>
</head>
<body>
    <div class="header">
        <h1>用户管理系统 - ${empty user.id ? '添加用户' : '编辑用户'}</h1>
        <div class="header-right">
            <a href="${pageContext.request.contextPath}/admin/dashboard">仪表盘</a>
            <span>欢迎，${sessionScope.loginUser.username}</span>
            <a href="${pageContext.request.contextPath}/logout">退出登录</a>
        </div>
    </div>
    
    <div class="container">
        <div class="card">
            <h2>${empty user.id ? '添加用户' : '编辑用户'}</h2>
            
            <c:if test="${not empty error}">
                <div class="error-message">${error}</div>
            </c:if>
            
            <form method="post" action="${pageContext.request.contextPath}/admin/user/${empty user.id ? 'add' : 'edit'}">
                <c:if test="${not empty user.id}">
                    <input type="hidden" name="id" value="${user.id}">
                </c:if>
                
                <div class="form-group">
                    <label for="username">用户名 <span class="required">*</span></label>
                    <input type="text" id="username" name="username" value="${user.username}" 
                           placeholder="请输入用户名" required ${not empty user.id ? 'readonly' : ''}>
                    <div class="hint">3-50个字符，只能包含字母、数字和下划线</div>
                </div>
                
                <div class="form-group">
                    <label for="password">
                        密码 <c:if test="${empty user.id}"><span class="required">*</span></c:if>
                    </label>
                    <input type="password" id="password" name="password" 
                           placeholder="${empty user.id ? '请输入密码' : '留空则不修改密码'}">
                    <c:if test="${empty user.id}">
                        <div class="hint">至少6个字符</div>
                    </c:if>
                </div>
                
                <div class="form-row">
                    <div class="form-group">
                        <label for="email">邮箱</label>
                        <input type="email" id="email" name="email" value="${user.email}" placeholder="请输入邮箱">
                    </div>
                    
                    <div class="form-group">
                        <label for="phone">手机号</label>
                        <input type="tel" id="phone" name="phone" value="${user.phone}" placeholder="请输入手机号">
                    </div>
                </div>
                
                <div class="form-row">
                    <div class="form-group">
                        <label for="role">角色 <span class="required">*</span></label>
                        <select id="role" name="role" required>
                            <option value="user" ${user.role == 'user' ? 'selected' : ''}>普通用户</option>
                            <option value="admin" ${user.role == 'admin' ? 'selected' : ''}>管理员</option>
                        </select>
                    </div>
                    
                    <div class="form-group">
                        <label for="status">状态 <span class="required">*</span></label>
                        <select id="status" name="status" required>
                            <option value="1" ${user.status == 1 ? 'selected' : ''}>正常</option>
                            <option value="0" ${user.status == 0 ? 'selected' : ''}>禁用</option>
                        </select>
                    </div>
                </div>
                
                <div class="btn-group">
                    <a href="${pageContext.request.contextPath}/admin/user/list" class="btn btn-secondary">取消</a>
                    <button type="submit" class="btn btn-primary">保存</button>
                </div>
            </form>
        </div>
    </div>
</body>
</html>