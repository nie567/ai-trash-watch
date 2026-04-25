<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    // 默认重定向到登录页
    response.sendRedirect(request.getContextPath() + "/login");
%>