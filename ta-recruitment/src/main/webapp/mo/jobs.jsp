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

    <%
        String dataDir = SessionUtil.getDataDir(request);
        JobDAO jobDAO = new JobDAO(dataDir);
        ApplicationDAO appDAO = new ApplicationDAO(dataDir);
        List<Job> myJobs = jobDAO.findByMo(currentUser.getUserId());
    %>

    <div class="page-header" style="align-items: center;">
        <div>
            <h1>Job Posts</h1>
            <p>Manage all your TA position postings</p>
        </div>
    </div>

    <% if ("posted".equals(request.getParameter("success"))) { %>
        <div class="alert alert-success">Job posted successfully!</div>
    <% } %>
    <% if ("updated".equals(request.getParameter("success"))) { %>
        <div class="alert alert-success">Job updated successfully!</div>
    <% } %>

    <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom: 24px;">
        <div class="filter-tabs" style="margin-bottom: 0;">
            <span class="filter-tab active" onclick="filterJobs('all')">All</span>
            <span class="filter-tab" onclick="filterJobs('OPEN')">Active</span>
            <span class="filter-tab" onclick="filterJobs('CLOSED')">Closed</span>
        </div>
        <a href="${pageContext.request.contextPath}/mo/post-job.jsp" class="btn btn-primary" style="padding: 8px 20px; border-radius: 20px;">+ Create New Job</a>
    </div>

    <% if (myJobs.isEmpty()) { %>
        <div class="empty-state">No jobs posted yet. <a href="${pageContext.request.contextPath}/mo/post-job.jsp">Post one now</a></div>
    <% } else { %>
        <p class="text-sm text-muted mb-4">Showing <strong><%= myJobs.size() %></strong> total job posts</p>

        <div style="background:var(--bg-white); border: 1px solid var(--border-solid); border-radius: var(--radius); overflow: hidden;">
            <table class="data-table" style="border: none; border-radius: 0;">
                <thead>
                    <tr>
                        <th>MODULE</th>
                        <th>JOB TITLE</th>
                        <th>POSTED</th>
                        <th>DEADLINE</th>
                        <th>POSITIONS</th>
                        <th>APPLICANTS</th>
                        <th>STATUS</th>
                        <th>ACTIONS</th>
                    </tr>
                </thead>
                <tbody>
                    <% for (Job job : myJobs) {
                        List<Application> apps = appDAO.findByJob(job.getJobId());
                        String statusClass = "OPEN".equals(job.getStatus()) ? "active" : "closed";
                        String statusLabel = "OPEN".equals(job.getStatus()) ? "Active" : "Closed";
                    %>
                    <tr class="job-row" data-status="<%= job.getStatus() %>">
                        <td><span class="module-code"><%= job.getJobId() %></span></td>
                        <td><strong><%= job.getJobTitle() != null && !job.getJobTitle().isEmpty() ? job.getJobTitle() : job.getModuleName() %></strong></td>
                        <td><%= job.getCreatedDate() %></td>
                        <td><%= job.getDeadline() %></td>
                        <td><%= job.getVacancies() %></td>
                        <td><a href="${pageContext.request.contextPath}/mo/applicants.jsp?jobId=<%= job.getJobId() %>" style="color:var(--primary);font-weight:700;"><%= apps.size() %></a></td>
                        <td><span class="badge badge-<%= statusClass %>"><%= statusLabel %></span></td>
                        <td style="display:flex;gap:8px;">
                            <a href="${pageContext.request.contextPath}/mo/applicants.jsp?jobId=<%= job.getJobId() %>" class="btn btn-primary btn-sm" style="border-radius:20px;padding:6px 16px;">View</a>
                            <a href="${pageContext.request.contextPath}/mo/edit-job?jobId=<%= job.getJobId() %>" class="btn btn-secondary btn-sm" style="border-radius:20px;padding:6px 16px;">Edit</a>
                        </td>
                    </tr>
                    <% } %>
                </tbody>
            </table>
        </div>
    <% } %>

<%@ include file="/jsp/common/footer.jsp" %>

<script>
function filterJobs(status) {
    var tabs = document.querySelectorAll('.filter-tab');
    tabs.forEach(function(t) { t.classList.remove('active'); });
    event.target.classList.add('active');

    var rows = document.querySelectorAll('.job-row');
    rows.forEach(function(row) {
        if (status === 'all' || row.getAttribute('data-status') === status) {
            row.style.display = '';
        } else {
            row.style.display = 'none';
        }
    });

    // Update count
    var visible = document.querySelectorAll('.job-row:not([style*="display: none"])').length;
    var countEl = document.querySelector('.text-sm.text-muted strong');
    if (countEl) countEl.textContent = visible;
}
</script>
</body>
</html>
