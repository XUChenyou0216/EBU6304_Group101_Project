<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.ta.dao.*, com.ta.model.*, com.ta.util.SessionUtil, java.util.List" %>
<!DOCTYPE html>
<html><head><meta charset="UTF-8"><title>My Applications</title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css"></head>
<body><%@ include file="/jsp/common/header.jsp" %>
<div class="container"><h1>My Applications</h1>
<% String dataDir = SessionUtil.getDataDir(request);
   List<Application> myApps = new ApplicationDAO(dataDir).findByTa(currentUser.getUserId());
   JobDAO jobDAO = new JobDAO(dataDir); %>
<% if (myApps.isEmpty()) { %>
    <p class="empty-state">No applications yet. <a href="${pageContext.request.contextPath}/ta/jobs.jsp">Browse Jobs</a></p>
<% } else { %>
<table class="data-table"><thead><tr><th>Job</th><th>Applied</th><th>Status</th><th>Note</th></tr></thead><tbody>
<% for (Application app : myApps) { Job job = jobDAO.findById(app.getJobId()); %>
<tr><td><%= job!=null?job.getModuleName():app.getJobId() %></td>
    <td><%= app.getAppliedDate() %></td>
    <td><span class="badge badge-<%= app.getStatus().toLowerCase() %>"><%= app.getStatus() %></span></td>
    <td><%= app.getReviewNote()!=null&&!app.getReviewNote().isEmpty()?app.getReviewNote():"-" %></td></tr>
<% } %>
</tbody></table><% } %>
</div></body></html>
