<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.ta.dao.TAProfileDAO, com.ta.model.TAProfile, com.ta.util.SessionUtil" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>My Profile - TA Recruitment System</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<%@ include file="/jsp/common/header.jsp" %>

    <div class="page-header">
        <div>
            <h1>My Profile</h1>
            <p>Manage your personal information and CV</p>
        </div>
    </div>

    <%
        String dataDir = SessionUtil.getDataDir(request);
        TAProfile profile = new TAProfileDAO(dataDir).findByUserId(currentUser.getUserId());
    %>

    <% if ("true".equals(request.getParameter("success"))) { %>
        <div class="alert alert-success">Profile updated successfully!</div>
    <% } %>
    <% if (request.getAttribute("error") != null) { %>
        <div class="alert alert-error"><%= request.getAttribute("error") %></div>
    <% } %>

    <div class="card" style="max-width:640px;">
        <h3 style="margin-bottom:20px;">Personal Information</h3>
        <form action="${pageContext.request.contextPath}/ta/profile" method="post">
            <div class="form-row">
                <div class="form-group">
                    <label>Student ID *</label>
                    <input type="text" name="studentId" required value="<%= profile!=null?profile.getStudentId():"" %>" placeholder="e.g. 231220001">
                </div>
                <div class="form-group">
                    <label>Full Name *</label>
                    <input type="text" name="fullName" required value="<%= profile!=null?profile.getFullName():"" %>" placeholder="Your full name">
                </div>
            </div>
            <div class="form-row">
                <div class="form-group">
                    <label>Programme *</label>
                    <input type="text" name="programme" required value="<%= profile!=null?profile.getProgramme():"" %>" placeholder="e.g. Computer Science">
                </div>
                <div class="form-group">
                    <label>Year of Study *</label>
                    <select name="yearOfStudy" required>
                        <option value="">-- Select --</option>
                        <option value="1" <%= profile!=null&&"1".equals(profile.getYearOfStudy())?"selected":"" %>>Year 1</option>
                        <option value="2" <%= profile!=null&&"2".equals(profile.getYearOfStudy())?"selected":"" %>>Year 2</option>
                        <option value="3" <%= profile!=null&&"3".equals(profile.getYearOfStudy())?"selected":"" %>>Year 3</option>
                        <option value="4" <%= profile!=null&&"4".equals(profile.getYearOfStudy())?"selected":"" %>>Year 4</option>
                        <option value="PG" <%= profile!=null&&"PG".equals(profile.getYearOfStudy())?"selected":"" %>>Postgraduate</option>
                    </select>
                </div>
            </div>
            <div class="form-group">
                <label>Phone Number</label>
                <input type="text" name="phone" value="<%= profile!=null?profile.getPhone():"" %>" placeholder="Optional">
            </div>
            <button type="submit" class="btn btn-primary">Save Profile</button>
        </form>
    </div>

    <div class="card" style="max-width:640px; margin-top:20px;">
        <h3 style="margin-bottom:20px;">CV Upload</h3>
        <% if (profile!=null && profile.getCvFilePath()!=null && !profile.getCvFilePath().isEmpty()) { %>
            <div class="alert alert-success">Current CV: <a href="${pageContext.request.contextPath}/<%= profile.getCvFilePath() %>" target="_blank">View uploaded CV</a></div>
        <% } %>
        <form action="${pageContext.request.contextPath}/ta/upload-cv" method="post" enctype="multipart/form-data">
            <div class="form-group">
                <label>Select CV file (PDF or DOCX, max 10MB)</label>
                <input type="file" name="cvFile" accept=".pdf,.doc,.docx">
            </div>
            <button type="submit" class="btn btn-secondary">Upload CV</button>
        </form>
    </div>

<%@ include file="/jsp/common/footer.jsp" %>
</body>
</html>
