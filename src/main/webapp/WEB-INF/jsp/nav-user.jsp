<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="com.example.model.User" %>
<%
    User loginUser = (User) session.getAttribute("loginUser");
    String currentUri = request.getRequestURI();
    String contextPath = request.getContextPath();
%>
<nav class="navbar">
    <a class="navbar-brand" href="<%=contextPath%>/inference">垃圾分类监管系统</a>
    <button class="navbar-toggle" id="navbarToggle" aria-label="菜单">
        <span class="navbar-toggle-bar"></span>
        <span class="navbar-toggle-bar"></span>
        <span class="navbar-toggle-bar"></span>
    </button>
    <ul class="navbar-menu" id="navbarMenu">
        <li><a href="<%=contextPath%>/user/profile" class="<%=currentUri.contains("/user/profile") ? "active" : ""%>">我的资料</a></li>
        <li><a href="<%=contextPath%>/inference" class="<%=currentUri.contains("/inference") ? "active" : ""%>">垃圾投放</a></li>
        <li><a href="<%=contextPath%>/user/garbage-record/list" class="<%=currentUri.contains("/garbage-record") ? "active" : ""%>">投放记录</a></li>
        <li><a href="<%=contextPath%>/user/violation/list" class="<%=currentUri.contains("/violation") ? "active" : ""%>">我的违规</a></li>
        <li><a href="<%=contextPath%>/user/rectification/list" class="<%=currentUri.contains("/rectification") ? "active" : ""%>">我的整改</a></li>
        <li><a href="<%=contextPath%>/user/knowledge/list" class="<%=currentUri.contains("/knowledge") ? "active" : ""%>">分类知识</a></li>
        <li><a href="<%=contextPath%>/user/password" class="<%=currentUri.contains("/password") ? "active" : ""%>">修改密码</a></li>
    </ul>
    <div class="navbar-right">
        <span class="navbar-user"><%=loginUser != null ? loginUser.getUsername() : ""%></span>
        <a href="<%=contextPath%>/logout" class="navbar-logout">退出</a>
    </div>
</nav>
