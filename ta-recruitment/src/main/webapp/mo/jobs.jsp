<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.ta.dao.*, com.ta.model.*, com.ta.util.SessionUtil, java.util.List" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Job Posts - TA Recruitment System</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<%@ include file="/jsp/common/header.jsp" %>

    <div class="page-header">
        <div>
            <h1>Job Posts</h1>
            <p>Manage all your TA position postings</p>
        </div>
        <a href="${pageContext.request.contextPath}/mo/post-job.jsp" class="btn btn-primary">+ Create New Job</a>
    </div>

    <%
        String dataDir = SessionUtil.getDataDir(request);
        List<Job> myJobs = new JobDAO(dataDir).findByMo(currentUser.getUserId());
        ApplicationDAO appDAO = new ApplicationDAO(dataDir);
    %>

    <% if ("posted".equals(request.getParameter("success"))) { %>
        <div class="alert alert-success">Job posted successfully!</div>
    <% } %>

    <% if (myJobs.isEmpty()) { %>
        <div class="empty-state">No jobs posted yet. <a href="${pageContext.request.contextPath}/mo/post-job.jsp">Create your first posting</a></div>
    <% } else { %>
        <div class="filter-tabs">
            <span class="filter-tab active">All</span>
            <span class="filter-tab">Active</span>
            <span class="filter-tab">Closed</span>
        </div>
        <p class="text-sm text-muted mb-4">Showing <strong><%= myJobs.size() %></strong> total job posts</p>
        <table class="data-table">
            <thead>
                <tr>
                    <th>Module</th>
                    <th>Job Title</th>
                    <th>Posted</th>
                    <th>Deadline</th>
                    <th>Positions</th>
                    <th>Applicants</th>
                    <th>Status</th>
                    <th>Actions</th>
                </tr>
            </thead>
            <tbody>
                <% for (Job job : myJobs) {
                    List<Application> apps = appDAO.findByJob(job.getJobId());
                %>
                <tr>
                    <td><span class="module-code"><%= job.getJobId() %></span></td>
                    <td><strong><%= job.getModuleName() %></strong></td>
                    <td><%= job.getCreatedDate() %></td>
                    <td><%= job.getDeadline() %></td>
                    <td><%= job.getVacancies() %></td>
                    <td><a href="${pageContext.request.contextPath}/mo/applicants?jobId=<%= job.getJobId() %>" style="color:var(--primary);font-weight:600;"><%= apps.size() %></a></td>
                    <td><span class="badge badge-<%= job.getStatus().toLowerCase() %>"><%= job.getStatus() %></span></td>
                    <td>
                        <a href="${pageContext.request.contextPath}/mo/applicants?jobId=<%= job.getJobId() %>" class="btn btn-primary btn-sm">View</a>
                        <a href="${pageContext.request.contextPath}/mo/edit-job?jobId=<%= job.getJobId() %>" class="btn btn-ghost btn-sm">Edit</a>
                    </td>
                </tr>
                <% } %>
            </tbody>
        </table>
    <% } %>

<%@ include file="/jsp/common/footer.jsp" %>
</body>
</html>
