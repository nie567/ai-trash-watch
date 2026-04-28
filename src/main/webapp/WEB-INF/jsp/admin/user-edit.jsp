<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>编辑用户 - 垃圾分类监管系统</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
    <jsp:include page="/WEB-INF/jsp/nav-admin.jsp" />
    
    <div class="main-content">
        <div class="card" style="max-width:600px;">
            <h2 class="card-title">编辑用户信息</h2>
            
            <c:if test="${not empty error}">
                <div style="margin-bottom:20px;padding:12px 16px;background:#f8d7da;color:#721c24;border-radius:4px;">${error}</div>
            </c:if>
            
            <!-- 用户基本信息展示 -->
            <div style="background:#f8f9fa;padding:16px;border-radius:6px;margin-bottom:24px;">
                <div style="display:flex;gap:24px;flex-wrap:wrap;">
                    <div>
                        <span style="color:#7f8c8d;font-size:13px;">用户名</span>
                        <p style="margin:4px 0 0 0;font-size:16px;font-weight:600;">${user.username}</p>
                    </div>
                    <div>
                        <span style="color:#7f8c8d;font-size:13px;">用户ID</span>
                        <p style="margin:4px 0 0 0;font-size:16px;">${user.id}</p>
                    </div>
                </div>
            </div>
            
            <form method="post" action="${pageContext.request.contextPath}/admin/users/edit">
                <input type="hidden" name="id" value="${user.id}">
                
                <div class="form-group">
                    <label for="role">角色</label>
                    <select id="role" name="role"
                            style="width:100%;padding:12px 14px;border:1px solid #ddd;border-radius:4px;font-size:14px;">
                        <option value="user" ${user.role == 'user' ? 'selected' : ''}>普通用户</option>
                        <option value="admin" ${user.role == 'admin' ? 'selected' : ''}>管理员</option>
                    </select>
                </div>
                
                <div class="form-group">
                    <label for="email">邮箱</label>
                    <input type="email" id="email" name="email" value="${user.email}" placeholder="请输入邮箱"
                           style="width:100%;padding:12px 14px;border:1px solid #ddd;border-radius:4px;font-size:14px;">
                </div>
                
                <div class="form-group">
                    <label for="phone">手机号</label>
                    <input type="tel" id="phone" name="phone" value="${user.phone}" placeholder="请输入手机号"
                           style="width:100%;padding:12px 14px;border:1px solid #ddd;border-radius:4px;font-size:14px;">
                </div>
                
                <div style="display:flex;gap:12px;margin-top:24px;padding-top:20px;border-top:1px solid #eee;">
                    <a href="${pageContext.request.contextPath}/admin/users" class="btn btn-secondary">取消</a>
                    <button type="submit" class="btn btn-success">保存修改</button>
                </div>
            </form>
        </div>
    </div>
</body>
</html>
