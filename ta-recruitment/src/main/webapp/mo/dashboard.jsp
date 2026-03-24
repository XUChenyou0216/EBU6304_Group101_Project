<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html><head><meta charset="UTF-8"><title>MO Dashboard</title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css"></head>
<body><%@ include file="/jsp/common/header.jsp" %>
<div class="container">
    <h1>Welcome, <%= currentUser.getUsername() %>!</h1>
    <p>You are logged in as a Module Organiser.</p>
    <div class="dashboard-cards">
        <a href="${pageContext.request.contextPath}/mo/jobs.jsp" class="dash-card"><h3>My Job Posts</h3><p>View and manage postings</p></a>
        <a href="${pageContext.request.contextPath}/mo/post-job.jsp" class="dash-card"><h3>Post New Job</h3><p>Create a TA vacancy</p></a>
    </div>
</div></body></html>
