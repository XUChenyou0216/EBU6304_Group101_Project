<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Home - TA Recruitment System</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>

<%@ include file="/jsp/common/header.jsp" %>

    <div class="page-header">
        <div>
            <h1>Welcome, <%= currentUser.getUsername() %>!</h1>
            <p>Browse available positions and manage your applications</p>
        </div>
    </div>

    <div class="dashboard-cards">
        <a href="${pageContext.request.contextPath}/ta/jobs.jsp" class="dash-card">
            <h3>Browse Jobs</h3>
            <p>View available TA positions and apply</p>
        </a>
        <a href="${pageContext.request.contextPath}/ta/profile.jsp" class="dash-card">
            <h3>My Profile</h3>
            <p>Edit your details and upload CV</p>
        </a>
        <a href="${pageContext.request.contextPath}/ta/applications.jsp" class="dash-card">
            <h3>My Applications</h3>
            <p>Check your application status</p>
        </a>
    </div>

<%@ include file="/jsp/common/footer.jsp" %>

</body>
</html>
