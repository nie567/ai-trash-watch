<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>仪表盘 - 用户管理系统</title>
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
        .container { max-width: 1200px; margin: 0 auto; padding: 30px; }
        .stats-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
            gap: 24px;
            margin-bottom: 30px;
        }
        .stat-card {
            background: white;
            border-radius: 12px;
            padding: 24px;
            box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
        }
        .stat-card h3 {
            color: #666;
            font-size: 14px;
            font-weight: 500;
            margin-bottom: 12px;
        }
        .stat-card .value {
            color: #333;
            font-size: 36px;
            font-weight: 700;
        }
        .stat-card.total { border-left: 4px solid #667eea; }
        .stat-card.new { border-left: 4px solid #10b981; }
        .stat-card.admin { border-left: 4px solid #f59e0b; }
        .stat-card.user { border-left: 4px solid #3b82f6; }
        .quick-actions {
            background: white;
            border-radius: 12px;
            padding: 24px;
            box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
        }
        .quick-actions h2 {
            color: #333;
            font-size: 18px;
            margin-bottom: 16px;
        }
        .action-links {
            display: flex;
            gap: 16px;
            flex-wrap: wrap;
        }
        .action-links a {
            display: inline-block;
            padding: 12px 24px;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            text-decoration: none;
            border-radius: 8px;
            font-size: 14px;
            font-weight: 500;
            transition: transform 0.2s, box-shadow 0.2s;
        }
        .action-links a:hover {
            transform: translateY(-2px);
            box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
        }
    </style>
</head>
<body>
    <div class="header">
        <h1>用户管理系统 - 管理员</h1>
        <div class="header-right">
            <span>欢迎，${sessionScope.loginUser.username}</span>
            <a href="${pageContext.request.contextPath}/logout">退出登录</a>
        </div>
    </div>
    
    <div class="container">
        <div class="stats-grid">
            <div class="stat-card total">
                <h3>用户总数</h3>
                <div class="value">${stats.totalUsers}</div>
            </div>
            <div class="stat-card new">
                <h3>今日新增</h3>
                <div class="value">${stats.todayNew}</div>
            </div>
            <div class="stat-card admin">
                <h3>管理员数量</h3>
                <div class="value">${stats.adminCount}</div>
            </div>
            <div class="stat-card user">
                <h3>普通用户数量</h3>
                <div class="value">${stats.userCount}</div>
            </div>
        </div>
        
        <div class="quick-actions">
            <h2>快捷操作</h2>
            <div class="action-links">
                <a href="${pageContext.request.contextPath}/admin/users">用户列表</a>
                <a href="${pageContext.request.contextPath}/admin/users/create">添加用户</a>
            </div>
        </div>
    </div>
</body>
</html>