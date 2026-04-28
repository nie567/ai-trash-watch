<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="com.example.model.User" %>
<%
    User loginUser = (User) session.getAttribute("loginUser");
    String currentUri = request.getRequestURI();
    String contextPath = request.getContextPath();
%>
<nav class="navbar">
    <a class="navbar-brand" href="<%=contextPath%>/admin/dashboard">垃圾分类监管系统</a>
    <ul class="navbar-menu">
        <li><a href="<%=contextPath%>/admin/dashboard" class="<%=currentUri.contains("/admin/dashboard") ? "active" : ""%>">仪表盘</a></li>
        <li><a href="<%=contextPath%>/admin/users" class="<%=currentUri.contains("/admin/users") ? "active" : ""%>">用户管理</a></li>
        <li><a href="<%=contextPath%>/admin/garbage-record/list" class="<%=currentUri.contains("/admin/garbage-record") ? "active" : ""%>">投放记录</a></li>
        <li><a href="<%=contextPath%>/admin/violation/list" class="<%=currentUri.contains("/admin/violation") ? "active" : ""%>">违规管理</a></li>
        <li><a href="<%=contextPath%>/admin/rectification/list" class="<%=currentUri.contains("/admin/rectification") ? "active" : ""%>">整改任务</a></li>
        <li><a href="<%=contextPath%>/admin/statistics" class="<%=currentUri.contains("/admin/statistics") ? "active" : ""%>">统计分析</a></li>
        <li><a href="<%=contextPath%>/admin/rule/list" class="<%=currentUri.contains("/admin/rule") ? "active" : ""%>">分类规则</a></li>
        <li><a href="<%=contextPath%>/admin/knowledge/list" class="<%=currentUri.contains("/admin/knowledge") ? "active" : ""%>">知识库管理</a></li>
    </ul>
    <div class="navbar-right">
        <span class="navbar-user"><%=loginUser != null ? loginUser.getUsername() : ""%></span>
        <a href="<%=contextPath%>/logout" class="navbar-logout">退出</a>
    </div>
</nav>
