<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.ta.dao.*, com.ta.model.*, com.ta.util.SessionUtil, java.util.List" %>
<!DOCTYPE html>
<html><head><meta charset="UTF-8"><title>Review Applicants</title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css"></head>
<body><%@ include file="/jsp/common/header.jsp" %>
<div class="container">
<% String jobId = request.getParameter("jobId");
   String dataDir = SessionUtil.getDataDir(request);
   Job job = new JobDAO(dataDir).findById(jobId);
   List<Application> applicants = new ApplicationDAO(dataDir).findByJob(jobId);
   TAProfileDAO profileDAO = new TAProfileDAO(dataDir); %>
<h1>Applicants for: <%= job!=null?job.getModuleName():"Unknown" %></h1>
<% if (request.getAttribute("success") != null) { %><div class="alert alert-success"><%= request.getAttribute("success") %></div><% } %>
<% if (applicants.isEmpty()) { %>
    <p class="empty-state">No applications received yet.</p>
<% } else { %>
<table class="data-table"><thead><tr><th>Applicant</th><th>Student ID</th><th>Programme</th><th>CV</th><th>Status</th><th>Action</th></tr></thead><tbody>
<% for (Application app : applicants) { TAProfile p = profileDAO.findByUserId(app.getTaUserId()); %>
<tr><td><%= p!=null?p.getFullName():app.getTaUserId() %></td>
    <td><%= p!=null?p.getStudentId():"-" %></td>
    <td><%= p!=null?p.getProgramme():"-" %></td>
    <td><% if (p!=null&&p.getCvFilePath()!=null&&!p.getCvFilePath().isEmpty()) { %>
        <a href="${pageContext.request.contextPath}/<%= p.getCvFilePath() %>" target="_blank">View CV</a>
    <% } else { %>No CV<% } %></td>
    <td><span class="badge badge-<%= app.getStatus().toLowerCase() %>"><%= app.getStatus() %></span></td>
    <td><form action="${pageContext.request.contextPath}/mo/update-status" method="post" style="display:inline">
        <input type="hidden" name="applicationId" value="<%= app.getApplicationId() %>">
        <input type="hidden" name="jobId" value="<%= jobId %>">
        <select name="newStatus">
            <option value="SUBMITTED" <%= "SUBMITTED".equals(app.getStatus())?"selected":"" %>>Submitted</option>
            <option value="UNDER_REVIEW" <%= "UNDER_REVIEW".equals(app.getStatus())?"selected":"" %>>Under Review</option>
            <option value="ACCEPTED" <%= "ACCEPTED".equals(app.getStatus())?"selected":"" %>>Accepted</option>
            <option value="REJECTED" <%= "REJECTED".equals(app.getStatus())?"selected":"" %>>Rejected</option>
        </select>
        <button type="submit" class="btn btn-sm btn-primary">Update</button>
    </form></td></tr>
<% } %>
</tbody></table><% } %>
<a href="${pageContext.request.contextPath}/mo/jobs.jsp" class="btn btn-secondary">Back to Jobs</a>
</div></body></html>
