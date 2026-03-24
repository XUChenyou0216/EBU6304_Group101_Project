<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html><head><meta charset="UTF-8"><title>Password Recovery</title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css"></head>
<body><div class="container"><div class="card login-card">
    <h1>Password Recovery</h1>
    <% if (request.getAttribute("error") != null) { %><div class="alert alert-error"><%= request.getAttribute("error") %></div><% } %>
    <% if (request.getAttribute("success") != null) { %><div class="alert alert-success"><%= request.getAttribute("success") %></div><% } %>
    <form action="${pageContext.request.contextPath}/recover" method="post">
        <div class="form-group"><label>Username</label><input type="text" name="username" required></div>
        <div class="form-group"><label>Security Answer</label><input type="text" name="securityAnswer" required></div>
        <div class="form-group"><label>New Password</label><input type="password" name="newPassword" required minlength="6"></div>
        <button type="submit" class="btn btn-primary btn-full">Reset Password</button>
    </form>
    <div class="form-links"><a href="${pageContext.request.contextPath}/login.jsp">Back to Login</a></div>
</div></div></body></html>
