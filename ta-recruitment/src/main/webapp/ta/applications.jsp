<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.ta.dao.*, com.ta.model.*, com.ta.util.SessionUtil, java.util.List" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>My Applications - TA Recruitment System</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<%@ include file="/jsp/common/header.jsp" %>

    <div class="page-header">
        <div>
            <h1>My Applications</h1>
            <p>Track the status of your submitted applications</p>
        </div>
    </div>

    <%
        String dataDir = SessionUtil.getDataDir(request);
        List<Application> myApps = new ApplicationDAO(dataDir).findByTa(currentUser.getUserId());
        JobDAO jobDAO = new JobDAO(dataDir);
    %>

    <% if ("applied".equals(request.getParameter("success"))) { %>
        <div class="alert alert-success">Application submitted successfully!</div>
    <% } %>

    <% if (myApps.isEmpty()) { %>
        <div class="empty-state">
            No applications yet. <a href="${pageContext.request.contextPath}/ta/jobs.jsp">Browse available positions</a>
        </div>
    <% } else { %>
        <table class="data-table">
            <thead>
                <tr>
                    <th>Job / Module</th>
                    <th>Applied Date</th>
                    <th>Status</th>
                    <th>Review Note</th>
                </tr>
            </thead>
            <tbody>
                <% for (Application app : myApps) {
                    Job job = jobDAO.findById(app.getJobId());
                %>
                <tr>
                    <td><strong><%= job!=null?job.getModuleName():app.getJobId() %></strong></td>
                    <td><%= app.getAppliedDate() %></td>
                    <td><span class="badge badge-<%= app.getStatus().toLowerCase() %>"><%= app.getStatus().replace("_"," ") %></span></td>
                    <td class="text-muted"><%= app.getReviewNote()!=null&&!app.getReviewNote().isEmpty()?app.getReviewNote():"-" %></td>
                </tr>
                <% } %>
            </tbody>
        </table>
    <% } %>

<%@ include file="/jsp/common/footer.jsp" %>
</body>
</html>
