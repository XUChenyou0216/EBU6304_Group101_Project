<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.ta.dao.JobDAO, com.ta.model.Job, com.ta.util.SessionUtil, java.util.List" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Available Jobs - TA Recruitment System</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<%@ include file="/jsp/common/header.jsp" %>

    <div class="page-header">
        <div>
            <h1>Available TA Positions</h1>
            <p>Browse and apply for teaching assistant opportunities</p>
        </div>
    </div>

    <%
        String dataDir = SessionUtil.getDataDir(request);
        List<Job> openJobs = new JobDAO(dataDir).findOpen();
    %>

    <% if (request.getParameter("error") != null) { %>
        <div class="alert alert-error">
            <% if ("duplicate".equals(request.getParameter("error"))) { %>
                You have already applied for this position.
            <% } else if ("noprofile".equals(request.getParameter("error"))) { %>
                Please complete your profile before applying. <a href="${pageContext.request.contextPath}/ta/profile.jsp">Go to Profile</a>
            <% } else { %>
                This position is no longer available.
            <% } %>
        </div>
    <% } %>

    <% if (openJobs.isEmpty()) { %>
        <div class="empty-state">No open positions at the moment. Check back later.</div>
    <% } else { %>
        <div class="job-grid">
            <% for (Job job : openJobs) { %>
                <div class="job-card">
                    <div class="job-card-top">
                        <span class="badge badge-open">Open</span>
                        <span class="deadline">Deadline: <%= job.getDeadline() %></span>
                    </div>
                    <h3><%= job.getModuleName() %></h3>
                    <p class="job-desc"><%= job.getDescription() %></p>
                    <p class="text-sm text-muted mb-4"><strong>Requirements:</strong> <%= job.getRequirements() %></p>
                    <div class="job-card-footer">
                        <span class="vacancies">Vacancies: <strong><%= job.getVacancies() %></strong></span>
                        <a href="${pageContext.request.contextPath}/ta/apply?jobId=<%= job.getJobId() %>" class="btn btn-primary btn-sm">View Details</a>
                    </div>
                </div>
            <% } %>
        </div>
    <% } %>

<%@ include file="/jsp/common/footer.jsp" %>
</body>
</html>
