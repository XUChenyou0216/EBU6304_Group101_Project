<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.ta.dao.*, com.ta.model.*, com.ta.util.SessionUtil, java.util.List" %>
<!DOCTYPE html>
<html><head><meta charset="UTF-8"><title>My Job Posts</title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css"></head>
<body><%@ include file="/jsp/common/header.jsp" %>
<div class="container"><h1>My Job Posts</h1>
<% String dataDir = SessionUtil.getDataDir(request);
   List<Job> myJobs = new JobDAO(dataDir).findByMo(currentUser.getUserId());
   ApplicationDAO appDAO = new ApplicationDAO(dataDir); %>
<% if (myJobs.isEmpty()) { %>
    <p class="empty-state">No jobs posted. <a href="${pageContext.request.contextPath}/mo/post-job.jsp">Post one now</a></p>
<% } else { for (Job job : myJobs) { List<Application> apps = appDAO.findByJob(job.getJobId()); %>
<div class="job-card">
    <h3><%= job.getModuleName() %> <span class="badge badge-<%= job.getStatus().toLowerCase() %>"><%= job.getStatus() %></span></h3>
    <p><%= job.getDescription() %></p>
    <div class="job-meta"><span>Vacancies: <%= job.getVacancies() %></span><span>Deadline: <%= job.getDeadline() %></span><span>Applicants: <%= apps.size() %></span></div>
    <a href="${pageContext.request.contextPath}/mo/edit-job?jobId=<%= job.getJobId() %>" class="btn btn-secondary">Edit</a>
    <a href="${pageContext.request.contextPath}/mo/applicants.jsp?jobId=<%= job.getJobId() %>" class="btn btn-primary">Review Applicants (<%= apps.size() %>)</a>
</div>
<% } } %>
</div></body></html>
