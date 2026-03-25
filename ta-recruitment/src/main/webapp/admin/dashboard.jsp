<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html><head><meta charset="UTF-8"><title>Admin Dashboard</title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css"></head>
<body><%@ include file="/jsp/common/header.jsp" %>
<div class="container">
    <h1>Administrator Dashboard</h1>
    <p>Welcome, <%= currentUser.getUsername() %>.</p>
    <div class="dashboard-cards">
        <a href="${pageContext.request.contextPath}/admin/users.jsp" class="dash-card"><h3>Manage Users</h3><p>View, approve, suspend accounts</p></a>
        <a href="${pageContext.request.contextPath}/admin/workload.jsp" class="dash-card"><h3>TA Workload</h3><p>Check workload distribution</p></a>
    </div>
</div></body></html>
