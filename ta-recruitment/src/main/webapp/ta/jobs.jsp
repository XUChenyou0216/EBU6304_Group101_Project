<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.ta.dao.JobDAO, com.ta.model.Job, com.ta.util.SessionUtil, java.util.List" %>
<!DOCTYPE html>
<html><head><meta charset="UTF-8"><title>Available Jobs</title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css"></head>
<body><%@ include file="/jsp/common/header.jsp" %>
<div class="container"><h1>Available TA Positions</h1>
<%  String dataDir = SessionUtil.getDataDir(request);
    List<Job> openJobs = new JobDAO(dataDir).findOpen(); %>
<% if (openJobs.isEmpty()) { %>
    <p class="empty-state">No open positions at the moment.</p>
<% } else { %><div class="job-list">
    <% for (Job job : openJobs) { %>
    <div class="job-card">
        <h3><%= job.getModuleName() %></h3>
        <p class="job-desc"><%= job.getDescription() %></p>
        <div class="job-meta"><span>Vacancies: <%= job.getVacancies() %></span>
            <span>Deadline: <%= job.getDeadline() %></span>
            <span class="badge badge-open"><%= job.getStatus() %></span></div>
        <p><strong>Requirements:</strong> <%= job.getRequirements() %></p>
        <a href="${pageContext.request.contextPath}/ta/apply?jobId=<%= job.getJobId() %>" class="btn btn-primary">Apply</a>
    </div>
    <% } %>
</div><% } %>
</div></body></html>
