<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.ta.dao.TAProfileDAO, com.ta.model.TAProfile, com.ta.util.SessionUtil" %>
<!DOCTYPE html>
<html><head><meta charset="UTF-8"><title>My Profile</title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css"></head>
<body><%@ include file="/jsp/common/header.jsp" %>
<div class="container"><h1>My Profile</h1>
<% TAProfile profile = new TAProfileDAO(SessionUtil.getDataDir(request)).findByUserId(currentUser.getUserId()); %>
<% if (request.getAttribute("success") != null) { %><div class="alert alert-success"><%= request.getAttribute("success") %></div><% } %>
<% if (request.getAttribute("error") != null) { %><div class="alert alert-error"><%= request.getAttribute("error") %></div><% } %>
<form action="${pageContext.request.contextPath}/ta/profile" method="post">
    <div class="form-group"><label>Student ID *</label><input type="text" name="studentId" required value="<%= profile!=null?profile.getStudentId():"" %>"></div>
    <div class="form-group"><label>Full Name *</label><input type="text" name="fullName" required value="<%= profile!=null?profile.getFullName():"" %>"></div>
    <div class="form-group"><label>Programme *</label><input type="text" name="programme" required value="<%= profile!=null?profile.getProgramme():"" %>"></div>
    <div class="form-group"><label>Year of Study *</label>
        <select name="yearOfStudy" required><option value="">--</option>
            <option value="1" <%= profile!=null&&"1".equals(profile.getYearOfStudy())?"selected":"" %>>Year 1</option>
            <option value="2" <%= profile!=null&&"2".equals(profile.getYearOfStudy())?"selected":"" %>>Year 2</option>
            <option value="3" <%= profile!=null&&"3".equals(profile.getYearOfStudy())?"selected":"" %>>Year 3</option>
            <option value="4" <%= profile!=null&&"4".equals(profile.getYearOfStudy())?"selected":"" %>>Year 4</option>
            <option value="PG" <%= profile!=null&&"PG".equals(profile.getYearOfStudy())?"selected":"" %>>Postgraduate</option></select></div>
    <div class="form-group"><label>Phone</label><input type="text" name="phone" value="<%= profile!=null?profile.getPhone():"" %>"></div>
    <button type="submit" class="btn btn-primary">Save Profile</button>
</form>
<hr><h2>Upload CV</h2>
<% if (profile!=null && profile.getCvFilePath()!=null && !profile.getCvFilePath().isEmpty()) { %>
    <p>Current CV: <a href="${pageContext.request.contextPath}/<%= profile.getCvFilePath() %>" target="_blank">View</a></p>
<% } %>
<form action="${pageContext.request.contextPath}/ta/upload-cv" method="post" enctype="multipart/form-data">
    <div class="form-group"><label>CV file (PDF/DOCX, max 10MB)</label><input type="file" name="cvFile" accept=".pdf,.doc,.docx"></div>
    <button type="submit" class="btn btn-secondary">Upload CV</button>
</form>
</div></body></html>
