<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.ta.model.Job" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Job Details - TA Recruitment System</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<%@ include file="/jsp/common/header.jsp" %>

<%
    Job job = (Job) request.getAttribute("job");
    boolean alreadyApplied = Boolean.TRUE.equals(request.getAttribute("alreadyApplied"));
    boolean hasProfile     = Boolean.TRUE.equals(request.getAttribute("hasProfile"));
    String title = job.getJobTitle() != null && !job.getJobTitle().isEmpty() ? job.getJobTitle() : job.getModuleName();
    String moduleDisplay = (job.getModuleCode() != null && !job.getModuleCode().isEmpty()) ? job.getModuleCode() + " " + job.getModuleName() : job.getModuleName();
%>

<div class="page-header">
    <div>
        <h1><%= title %></h1>
        <p><%= moduleDisplay %></p>
    </div>
    <a href="${pageContext.request.contextPath}/ta/jobs.jsp" class="btn btn-ghost">&larr; Back to Jobs</a>
</div>

<% if (alreadyApplied) { %>
    <div class="alert alert-success">You have already applied for this position.</div>
<% } %>
<% if (!hasProfile && !alreadyApplied) { %>
    <div class="alert alert-error">
        Please complete your <a href="${pageContext.request.contextPath}/ta/profile.jsp">profile</a> before applying.
    </div>
<% } %>

<div class="card" style="max-width:720px;">
    <div class="card-body" style="padding:32px;">

        <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:24px;">
            <span class="badge badge-open">Open</span>
            <span style="color:var(--text-secondary);font-size:14px;">Deadline: <%= job.getDeadline() %></span>
        </div>

        <% if (job.getWorkingPeriod() != null && !job.getWorkingPeriod().isEmpty()) { %>
        <div style="margin-bottom:20px;">
            <h3 style="font-size:13px;text-transform:uppercase;color:var(--text-secondary);margin-bottom:6px;">Working Period</h3>
            <p><%= job.getWorkingPeriod() %></p>
        </div>
        <% } %>

        <div style="margin-bottom:20px;">
            <h3 style="font-size:13px;text-transform:uppercase;color:var(--text-secondary);margin-bottom:6px;">Description</h3>
            <p><%= job.getDescription() %></p>
        </div>

        <% if (job.getKeyDuties() != null && !job.getKeyDuties().isEmpty()) { %>
        <div style="margin-bottom:20px;">
            <h3 style="font-size:13px;text-transform:uppercase;color:var(--text-secondary);margin-bottom:6px;">Key Duties</h3>
            <p><%= job.getKeyDuties() %></p>
        </div>
        <% } %>

        <div style="margin-bottom:20px;">
            <h3 style="font-size:13px;text-transform:uppercase;color:var(--text-secondary);margin-bottom:6px;">Required Skills</h3>
            <p><%= job.getRequirements() %></p>
        </div>

        <% if (job.getEligibility() != null && !job.getEligibility().isEmpty()) { %>
        <div style="margin-bottom:20px;">
            <h3 style="font-size:13px;text-transform:uppercase;color:var(--text-secondary);margin-bottom:6px;">Eligibility Requirements</h3>
            <p><%= job.getEligibility() %></p>
        </div>
        <% } %>

        <div style="margin-bottom:32px;">
            <h3 style="font-size:13px;text-transform:uppercase;color:var(--text-secondary);margin-bottom:6px;">Vacancies</h3>
            <p><strong><%= job.getVacancies() %></strong> position(s) available</p>
        </div>

        <% if (!alreadyApplied) { %>
            <% if (hasProfile) { %>
                <form method="post" action="${pageContext.request.contextPath}/ta/apply">
                    <input type="hidden" name="jobId" value="<%= job.getJobId() %>">
                    <button type="submit" class="btn btn-primary" style="width:100%;">Apply for this Position</button>
                </form>
            <% } else { %>
                <a href="${pageContext.request.contextPath}/ta/profile.jsp"
                   class="btn btn-primary" style="width:100%;text-align:center;display:block;">
                    Complete Profile to Apply
                </a>
            <% } %>
        <% } else { %>
            <button class="btn" disabled style="width:100%;opacity:0.5;cursor:not-allowed;">Already Applied</button>
        <% } %>

    </div>
</div>

<%@ include file="/jsp/common/footer.jsp" %>
</body>
</html>
