<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.ta.dao.JobDAO, com.ta.dao.ApplicationDAO, com.ta.model.Job, com.ta.model.User, com.ta.util.SessionUtil, java.util.List, java.util.HashSet, java.util.Set" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Available Jobs - TA Recruitment System</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<%@ include file="/jsp/common/header.jsp" %>

    <div class="page-header">
        <div>
            <h1>Available TA Positions</h1>
            <p>Browse and apply for teaching assistant opportunities</p>
        </div>
    </div>

    <%
        String dataDir = SessionUtil.getDataDir(request);
        List<Job> openJobs = new JobDAO(dataDir).findOpen();

        // Collect job IDs this TA has already applied for
        Set<String> appliedJobIds = new HashSet<String>();
        for (com.ta.model.Application a : new ApplicationDAO(dataDir).findByTa(currentUser.getUserId())) {
            appliedJobIds.add(a.getJobId());
        }
    %>

    <% if (request.getParameter("error") != null) { %>
        <div class="alert alert-error">
            <% if ("duplicate".equals(request.getParameter("error"))) { %>
                You have already applied for this position.
            <% } else if ("noprofile".equals(request.getParameter("error"))) { %>
                Please complete your profile before applying. <a href="${pageContext.request.contextPath}/ta/profile.jsp">Go to Profile</a>
            <% } else { %>
                This position is no longer available.
            <% } %>
        </div>
    <% } %>

    <% if (openJobs.isEmpty()) { %>
        <div class="empty-state">No open positions at the moment. Please check back later.</div>
    <% } else { %>
    <div class="job-grid">
        <% for (Job job : openJobs) {
               boolean applied = appliedJobIds.contains(job.getJobId());
        %>
        <div class="job-card">
            <div class="job-card-top">
                <span class="badge badge-open">Open</span>
                <span class="deadline">
                    <svg style="vertical-align:text-bottom;margin-right:4px;" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect><line x1="16" y1="2" x2="16" y2="6"></line><line x1="8" y1="2" x2="8" y2="6"></line><line x1="3" y1="10" x2="21" y2="10"></line></svg>
                    Deadline: <%= job.getDeadline() %>
                </span>
            </div>
            <h3 style="margin-bottom:8px;font-size:18px;"><%= job.getModuleName() %></h3>
            <p class="job-desc" style="color:var(--text-secondary);font-size:14px;flex-grow:1;margin-bottom:20px;">
                <%= job.getRequirements() %>
            </p>
            <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:16px;">
                <div style="display:flex; align-items:center; gap:6px; color:var(--text-secondary); font-size:13px;">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path><circle cx="9" cy="7" r="4"></circle><path d="M23 21v-2a4 4 0 0 0-3-3.87"></path><path d="M16 3.13a4 4 0 0 1 0 7.75"></path></svg>
                    <span>Vacancies: <strong><%= job.getVacancies() %></strong></span>
                </div>
                <% if (applied) { %>
                    <span class="badge badge-submitted">Applied</span>
                <% } %>
            </div>
            <div style="text-align:right;">
                <a href="${pageContext.request.contextPath}/ta/apply?jobId=<%= job.getJobId() %>"
                   class="btn btn-primary" style="background:#2b4acb;border-radius:6px;font-weight:600;padding:8px 16px;">
                    View Details
                </a>
            </div>
        </div>
        <% } %>
    </div>
    <% } %>

<%@ include file="/jsp/common/footer.jsp" %>
</body>
</html>
