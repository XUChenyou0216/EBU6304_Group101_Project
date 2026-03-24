<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html><head><meta charset="UTF-8"><title>Post Job</title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css"></head>
<body><%@ include file="/jsp/common/header.jsp" %>
<div class="container"><h1>Post New TA Position</h1>
<% if (request.getAttribute("error") != null) { %><div class="alert alert-error"><%= request.getAttribute("error") %></div><% } %>
<form action="${pageContext.request.contextPath}/mo/post-job" method="post">
    <div class="form-group"><label>Module Name *</label><input type="text" name="moduleName" required placeholder="e.g. EBU6304 Software Engineering"></div>
    <div class="form-group"><label>Job Description *</label><textarea name="description" rows="4" required></textarea></div>
    <div class="form-group"><label>Requirements *</label><textarea name="requirements" rows="3" required></textarea></div>
    <div class="form-group"><label>Vacancies *</label><input type="number" name="vacancies" min="1" required value="1"></div>
    <div class="form-group"><label>Deadline *</label><input type="date" name="deadline" required></div>
    <button type="submit" class="btn btn-primary">Post Job</button>
    <a href="${pageContext.request.contextPath}/mo/jobs.jsp" class="btn btn-secondary">Cancel</a>
</form></div></body></html>
