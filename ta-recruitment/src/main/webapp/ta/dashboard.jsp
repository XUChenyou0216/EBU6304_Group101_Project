<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html><head><meta charset="UTF-8"><title>TA Dashboard</title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css"></head>
<body><%@ include file="/jsp/common/header.jsp" %>
<div class="container">
    <h1>Welcome, <%= currentUser.getUsername() %>!</h1>
    <p>You are logged in as a Teaching Assistant.</p>
    <div class="dashboard-cards">
        <a href="${pageContext.request.contextPath}/ta/jobs.jsp" class="dash-card"><h3>Browse Jobs</h3><p>View available TA positions</p></a>
        <a href="${pageContext.request.contextPath}/ta/profile.jsp" class="dash-card"><h3>My Profile</h3><p>Edit details and upload CV</p></a>
        <a href="${pageContext.request.contextPath}/ta/applications.jsp" class="dash-card"><h3>My Applications</h3><p>Check application status</p></a>
    </div>
</div></body></html>
