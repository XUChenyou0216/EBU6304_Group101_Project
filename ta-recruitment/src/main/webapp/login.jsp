<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html><head><meta charset="UTF-8"><title>Login - TA Recruitment</title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css"></head>
<body><div class="container"><div class="card login-card">
    <h1>TA Recruitment System</h1><h2>BUPT International School</h2>
    <% if (request.getAttribute("error") != null) { %>
        <div class="alert alert-error"><%= request.getAttribute("error") %></div>
    <% } %>
    <% if ("true".equals(request.getParameter("registered"))) { %>
        <div class="alert alert-success">Registration successful! Please log in.</div>
    <% } %>
    <form action="${pageContext.request.contextPath}/login" method="post">
        <div class="form-group"><label for="username">Username</label>
            <input type="text" id="username" name="username" required></div>
        <div class="form-group"><label for="password">Password</label>
            <input type="password" id="password" name="password" required></div>
        <button type="submit" class="btn btn-primary btn-full">Log In</button>
    </form>
    <div class="form-links">
        <a href="${pageContext.request.contextPath}/register.jsp">Create an account</a>
        <a href="${pageContext.request.contextPath}/recover.jsp">Forgot password?</a>
    </div>
</div></div></body></html>
