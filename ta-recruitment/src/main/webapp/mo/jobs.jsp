<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.ta.dao.*, com.ta.model.*, com.ta.util.SessionUtil, com.ta.util.JobDeadlineUtil, java.util.List" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Job Posts - TA Recruitment System</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <style>
        .deadline-past { color: #dc2626; font-weight: 600; }
        .job-row-past { background: #fff7f7; }
    </style>
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
    <% if ("closedFull".equals(request.getParameter("success"))) {
        String n = request.getParameter("n");
        int k = 0;
        try { if (n != null) k = Integer.parseInt(n); } catch (NumberFormatException ignored) {}
    %>
        <div class="alert alert-success"><%= k == 0
            ? "No open jobs were at full capacity; nothing was closed."
            : ("Closed " + k + " open job(s) that were already at full capacity.") %></div>
    <% } %>
    <% if ("reopened".equals(request.getParameter("success"))) { %>
        <div class="alert alert-success">Posting re-opened for applications (status set to Active).</div>
    <% } %>
    <% if (request.getParameter("error") != null && request.getParameter("error").startsWith("reopen")) {
        String er = request.getParameter("error");
        String msg = "Could not re-open this posting.";
        if ("reopenNotClosed".equals(er)) msg = "Only closed postings can be re-opened this way.";
        else if ("reopenStillFull".equals(er)) msg = "All positions are still filled — reject an accepted applicant first or increase vacancies.";
        else if ("reopenDeadline".equals(er)) msg = "The application deadline has passed; edit the deadline before re-opening.";
        else if ("reopenNoVacancy".equals(er)) msg = "This posting has zero positions configured.";
        else if ("reopenNotFound".equals(er)) msg = "Job not found or you do not have access.";
    %>
        <div class="alert alert-error"><%= msg %></div>
    <% } %>

    <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom: 24px; flex-wrap: wrap; gap: 12px;">
        <div class="filter-tabs" style="margin-bottom: 0;">
            <span class="filter-tab active" onclick="filterJobs('all')">All</span>
            <span class="filter-tab" onclick="filterJobs('OPEN')">Active</span>
            <span class="filter-tab" onclick="filterJobs('CLOSED')">Closed</span>
        </div>
        <div style="display:flex; gap:12px; flex-wrap:wrap; align-items:center;">
            <form action="${pageContext.request.contextPath}/mo/close-full-jobs" method="post" style="margin:0;" onsubmit="return confirm('Close every open job where accepted TAs already fill all vacancies? Remaining applicants can no longer be hired for that posting.');">
                <button type="submit" class="btn btn-secondary" style="padding: 8px 20px; border-radius: 20px;">Close all filled jobs</button>
            </form>
            <a href="${pageContext.request.contextPath}/mo/post-job.jsp" class="btn btn-primary" style="padding: 8px 20px; border-radius: 20px;">+ Create New Job</a>
        </div>
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
                        boolean past = JobDeadlineUtil.isPastDeadline(job.getDeadline());
                        boolean open = "OPEN".equals(job.getStatus());
                        long acceptedCnt = appDAO.countAcceptedForJob(job.getJobId());
                        boolean canReopen = "CLOSED".equals(job.getStatus()) && !past && job.getVacancies() > 0
                            && acceptedCnt < job.getVacancies();
                        String statusBadgeClass;
                        String statusLabel;
                        if (open) {
                            statusBadgeClass = "active";
                            statusLabel = "Active";
                        } else if (past) {
                            statusBadgeClass = "expired";
                            statusLabel = "Expired";
                        } else {
                            statusBadgeClass = "closed";
                            statusLabel = "Closed";
                        }
                    %>
                    <tr class="job-row<%= past ? " job-row-past" : "" %>" data-status="<%= job.getStatus() %>">
                        <td><span class="module-code"><%= job.getJobId() %></span></td>
                        <td><strong><%= job.getJobTitle() != null && !job.getJobTitle().isEmpty() ? job.getJobTitle() : job.getModuleName() %></strong></td>
                        <td><%= job.getCreatedDate() %></td>
                        <td class="<%= past ? "deadline-past" : "" %>"><%= job.getDeadline() %></td>
                        <td><%= job.getVacancies() %></td>
                        <td><a href="${pageContext.request.contextPath}/mo/applicants.jsp?jobId=<%= job.getJobId() %>" style="color:var(--primary);font-weight:700;"><%= apps.size() %></a></td>
                        <td><span class="badge badge-<%= statusBadgeClass %>"><%= statusLabel %></span></td>
                        <td style="display:flex;flex-wrap:wrap;gap:8px;align-items:center;">
                            <a href="${pageContext.request.contextPath}/mo/applicants.jsp?jobId=<%= job.getJobId() %>" class="btn btn-primary btn-sm" style="border-radius:20px;padding:6px 16px;">View</a>
                            <a href="${pageContext.request.contextPath}/mo/edit-job?jobId=<%= job.getJobId() %>" class="btn btn-secondary btn-sm" style="border-radius:20px;padding:6px 16px;">Edit</a>
                            <% if (canReopen) { %>
                            <form action="${pageContext.request.contextPath}/mo/reopen-job" method="post" style="margin:0;display:inline;">
                                <input type="hidden" name="jobId" value="<%= job.getJobId() %>">
                                <input type="hidden" name="returnTo" value="jobs">
                                <button type="submit" class="btn btn-secondary btn-sm" style="border-radius:20px;padding:6px 16px;" title="After you free a slot (e.g. reject an accepted applicant), re-open this posting so TAs can apply again.">Re-open</button>
                            </form>
                            <% } %>
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
