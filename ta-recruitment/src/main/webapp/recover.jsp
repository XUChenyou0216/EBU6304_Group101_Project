<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Password Recovery - TA System</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body style="display: flex; justify-content: center; padding-top: 60px;">
    <div class="container" style="max-width: 450px;">
        <div class="card">
            <h1 style="font-size: 24px; margin-bottom: 8px;">Password Recovery</h1>
            <p class="text-muted" style="margin-bottom: 24px;">Reset your account password using security question.</p>

            <%-- 状态反馈：错误或成功提示 [cite: 51, 56] --%>
            <% if (request.getAttribute("error") != null) { %>
                <div class="alert alert-error"><%= request.getAttribute("error") %></div>
            <% } %>
            <% if (request.getAttribute("success") != null) { %>
                <div class="alert alert-success"><%= request.getAttribute("success") %></div>
            <% } %>

            <form action="${pageContext.request.contextPath}/recover" method="post">
                <div class="form-group">
                    <label>Username</label>
                    <input type="text" name="username" placeholder="Enter your username" required>
                </div>
                <div class="form-group">
                    <label>Security Answer</label>
                    <input type="text" name="securityAnswer" placeholder="Your answer" required>
                </div>
                <div class="form-group">
                    <label>New Password</label>
                    <input type="password" name="newPassword" placeholder="Minimum 6 characters" required minlength="6">
                </div>
                <button type="submit" class="btn btn-primary btn-full">Reset Password</button>
            </form>

            <div class="form-links" style="margin-top: 20px; text-align: center;">
                <a href="${pageContext.request.contextPath}/login.jsp" class="text-sm">Back to Login</a>
            </div>
        </div>
    </div>
</body>
</html>