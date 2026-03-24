<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html><head><meta charset="UTF-8"><title>Register - TA Recruitment</title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css"></head>
<body><div class="container"><div class="card register-card">
    <h1>Create Account</h1>
    <% if (request.getAttribute("error") != null) { %>
        <div class="alert alert-error"><%= request.getAttribute("error") %></div>
    <% } %>
    <form action="${pageContext.request.contextPath}/register" method="post">
        <div class="form-group"><label>Username *</label><input type="text" name="username" required></div>
        <div class="form-group"><label>Email *</label><input type="email" name="email" required></div>
        <div class="form-group"><label>Role *</label>
            <select name="role" required><option value="">-- Select --</option>
                <option value="TA">Teaching Assistant</option><option value="MO">Module Organiser</option></select></div>
        <div class="form-group"><label>Password * (min 6 chars)</label><input type="password" name="password" required minlength="6"></div>
        <div class="form-group"><label>Confirm Password *</label><input type="password" name="confirmPassword" required></div>
        <div class="form-group"><label>Security Question *</label>
            <select name="securityQuestion" required><option value="">-- Select --</option>
                <option value="pet">What is your pet's name?</option>
                <option value="city">What city were you born in?</option>
                <option value="school">What was your first school?</option></select></div>
        <div class="form-group"><label>Security Answer *</label><input type="text" name="securityAnswer" required></div>
        <button type="submit" class="btn btn-primary btn-full">Register</button>
    </form>
    <div class="form-links"><a href="${pageContext.request.contextPath}/login.jsp">Already have an account? Log in</a></div>
</div></div></body></html>
