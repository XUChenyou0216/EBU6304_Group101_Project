<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.ta.dao.*, com.ta.model.*, com.ta.util.SessionUtil, java.util.List" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>MO Dashboard - TA Recruitment System</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<%@ include file="/jsp/common/header.jsp" %>

    <div class="page-header">
        <div>
            <h1>Dashboard</h1>
            <p>Overview of your recruitment activity</p>
        </div>
    </div>

    <%
        String dataDir = SessionUtil.getDataDir(request);
        List<Job> myJobs = new JobDAO(dataDir).findByMo(currentUser.getUserId());
        ApplicationDAO appDAO = new ApplicationDAO(dataDir);
        int totalApplicants = 0;
        int openJobs = 0;
        for (Job j : myJobs) {
            totalApplicants += appDAO.findByJob(j.getJobId()).size();
            if ("OPEN".equals(j.getStatus())) openJobs++;
        }
    %>

    <div class="stats-row">
        <div class="stat-card">
            <div class="stat-label">Total Job Posts</div>
            <div class="stat-value"><%= myJobs.size() %></div>
        </div>
        <div class="stat-card">
            <div class="stat-label">Active Positions</div>
            <div class="stat-value"><%= openJobs %></div>
        </div>
        <div class="stat-card">
            <div class="stat-label">Total Applicants</div>
            <div class="stat-value"><%= totalApplicants %></div>
        </div>
    </div>

    <div class="dashboard-cards">
        <a href="${pageContext.request.contextPath}/mo/jobs.jsp" class="dash-card">
            <h3>Job Postings</h3>
            <p>View and manage your TA position postings</p>
        </a>
        <a href="${pageContext.request.contextPath}/mo/progress" class="dash-card">
            <h3>Recruitment Progress</h3>
            <p>Track applications, screening activity, and hiring completion by role</p>
        </a>
        <a href="${pageContext.request.contextPath}/mo/post-job.jsp" class="dash-card">
            <h3>Post New Job</h3>
            <p>Create a new TA vacancy for your module</p>
        </a>
    </div>

<%@ include file="/jsp/common/footer.jsp" %>
</body>
</html>
