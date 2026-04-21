<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Access Denied - TA Recruitment System</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<div style="display:flex;flex-direction:column;align-items:center;justify-content:center;min-height:100vh;text-align:center;padding:40px;">
    <div style="font-size:72px;font-weight:700;color:#e53e3e;margin-bottom:16px;">403</div>
    <h1 style="font-size:28px;margin-bottom:12px;">Access Denied</h1>
    <p style="color:#718096;margin-bottom:32px;">You do not have permission to access this page.</p>
    <a href="${pageContext.request.contextPath}/login" class="btn btn-primary">Go to Login</a>
</div>
</body>
</html>
