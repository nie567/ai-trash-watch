<%@ page contentType="text/html;charset=UTF-8" language="java" isErrorPage="true" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>错误 - 垃圾分类监管系统</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
    <div class="container" style="padding-top:60px;">
        <div class="form-wrapper">
            <h2 class="text-danger" style="margin-bottom: 20px;">出错了</h2>
            <p class="text-secondary" style="margin-bottom: 20px;">
                抱歉，系统发生了错误。请稍后再试或联系管理员。
            </p>
            <%
                if (exception != null) {
                    exception.printStackTrace(new java.io.PrintWriter(out));
                }
            %>
            <a href="${pageContext.request.contextPath}/" class="btn btn-primary">返回首页</a>
        </div>
    </div>
</body>
</html>
