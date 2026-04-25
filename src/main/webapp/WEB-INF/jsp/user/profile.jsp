<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>个人中心 - 用户管理系统</title>
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
        .container { max-width: 800px; margin: 0 auto; padding: 30px; }
        .card {
            background: white;
            border-radius: 12px;
            box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
            padding: 30px;
            margin-bottom: 24px;
        }
        .card h2 {
            color: #333;
            font-size: 18px;
            margin-bottom: 20px;
            padding-bottom: 12px;
            border-bottom: 1px solid #eee;
        }
        .info-grid {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 20px;
        }
        .info-item { }
        .info-item label {
            display: block;
            color: #666;
            font-size: 14px;
            margin-bottom: 6px;
        }
        .info-item .value {
            color: #333;
            font-size: 14px;
            padding: 10px 14px;
            background: #f9fafb;
            border-radius: 6px;
        }
        .btn-group { display: flex; gap: 12px; margin-top: 20px; }
        .btn {
            display: inline-block;
            padding: 10px 20px;
            border-radius: 8px;
            font-size: 14px;
            font-weight: 500;
            text-decoration: none;
            text-align: center;
            cursor: pointer;
            border: none;
            transition: transform 0.2s;
        }
        .btn:hover { transform: translateY(-1px); }
        .btn-primary {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
        }
        .btn-outline {
            background: white;
            color: #333;
            border: 2px solid #e1e1e1;
        }
        .alert {
            padding: 12px 16px;
            border-radius: 8px;
            margin-bottom: 16px;
            font-size: 14px;
        }
        .alert-success {
            background: #dcfce7;
            color: #166534;
            border: 1px solid #bbf7d0;
        }
    </style>
</head>
<body>
    <div class="header">
        <h1>用户管理系统 - 个人中心</h1>
        <div class="header-right">
            <span>欢迎，${sessionScope.loginUser.username}</span>
            <a href="${pageContext.request.contextPath}/logout">退出登录</a>
        </div>
    </div>
    
    <div class="container">
        <c:if test="${param.success == 'updated'}">
            <div class="alert alert-success">个人信息更新成功</div>
        </c:if>
        
        <div class="card">
            <h2>基本信息</h2>
            <div class="info-grid">
                <div class="info-item">
                    <label>用户名</label>
                    <div class="value">${user.username}</div>
                </div>
                <div class="info-item">
                    <label>角色</label>
                    <div class="value">${user.role == 'admin' ? '管理员' : '普通用户'}</div>
                </div>
                <div class="info-item">
                    <label>邮箱</label>
                    <div class="value">${empty user.email ? '-' : user.email}</div>
                </div>
                <div class="info-item">
                    <label>手机号</label>
                    <div class="value">${empty user.phone ? '-' : user.phone}</div>
                </div>
                <div class="info-item">
                    <label>账号状态</label>
                    <div class="value">${user.status == 1 ? '正常' : '禁用'}</div>
                </div>
                <div class="info-item">
                    <label>创建时间</label>
                    <div class="value">
                        <fmt:formatDate value="${user.createTime}" pattern="yyyy-MM-dd HH:mm" />
                    </div>
                </div>
            </div>
            
            <div class="btn-group">
                <a href="${pageContext.request.contextPath}/user/profile/edit" class="btn btn-primary">编辑资料</a>
                <a href="${pageContext.request.contextPath}/user/password" class="btn btn-outline">修改密码</a>
            </div>
        </div>
    </div>
</body>
</html>