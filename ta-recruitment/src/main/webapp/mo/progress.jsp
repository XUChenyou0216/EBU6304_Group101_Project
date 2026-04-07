<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.ta.dao.*, com.ta.model.*, com.ta.util.SessionUtil, java.util.List" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Recruitment Progress - TA Recruitment System</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<%@ include file="/jsp/common/header.jsp" %>

    <%
        String dataDir = SessionUtil.getDataDir(request);
        JobDAO jobDAO = new JobDAO(dataDir);
        ApplicationDAO appDAO = new ApplicationDAO(dataDir);
        List<Job> myJobs = jobDAO.findByMo(currentUser.getUserId());

        int totalApps = 0, accepted = 0, rejected = 0, underReview = 0, submitted = 0;
        for (Job j : myJobs) {
            List<Application> apps = appDAO.findByJob(j.getJobId());
            for (Application a : apps) {
                totalApps++;
                String s = a.getStatus();
                if ("ACCEPTED".equals(s)) accepted++;
                else if ("REJECTED".equals(s)) rejected++;
                else if ("UNDER_REVIEW".equals(s)) underReview++;
                else submitted++;
            }
        }
    %>

    <div class="page-header">
        <div>
            <h1>Recruitment Progress</h1>
            <p>Track the overall progress of your TA recruitment</p>
        </div>
    </div>

    <div class="stats-row">
        <div class="stat-card">
            <div class="stat-label">Total Applications</div>
            <div class="stat-value"><%= totalApps %></div>
        </div>
        <div class="stat-card">
            <div class="stat-label">Accepted</div>
            <div class="stat-value" style="color:#16a34a;"><%= accepted %></div>
        </div>
        <div class="stat-card">
            <div class="stat-label">Under Review</div>
            <div class="stat-value" style="color:#d97706;"><%= underReview %></div>
        </div>
        <div class="stat-card">
            <div class="stat-label">Submitted</div>
            <div class="stat-value" style="color:#2563eb;"><%= submitted %></div>
        </div>
        <div class="stat-card">
            <div class="stat-label">Rejected</div>
            <div class="stat-value" style="color:#dc2626;"><%= rejected %></div>
        </div>
    </div>

    <div style="margin-top:32px;">
        <h3 style="margin-bottom:16px;">Progress by Job</h3>

        <% if (myJobs.isEmpty()) { %>
            <div class="empty-state">No jobs posted yet. <a href="${pageContext.request.contextPath}/mo/post-job.jsp">Post one now</a></div>
        <% } else { %>
            <div style="background:var(--bg-white); border: 1px solid var(--border-solid); border-radius: var(--radius); overflow: hidden;">
                <table class="data-table" style="border: none; border-radius: 0;">
                    <thead>
                        <tr>
                            <th>JOB</th>
                            <th>MODULE</th>
                            <th>POSITIONS</th>
                            <th>APPLICANTS</th>
                            <th>ACCEPTED</th>
                            <th>UNDER REVIEW</th>
                            <th>REJECTED</th>
                            <th>FILL RATE</th>
                            <th>STATUS</th>
                        </tr>
                    </thead>
                    <tbody>
                        <% for (Job job : myJobs) {
                            List<Application> apps = appDAO.findByJob(job.getJobId());
                            int jAccepted = 0, jReview = 0, jRejected = 0;
                            for (Application a : apps) {
                                String s = a.getStatus();
                                if ("ACCEPTED".equals(s)) jAccepted++;
                                else if ("UNDER_REVIEW".equals(s)) jReview++;
                                else if ("REJECTED".equals(s)) jRejected++;
                            }
                            int pct = job.getVacancies() > 0 ? (int) Math.round(jAccepted * 100.0 / job.getVacancies()) : 0;
                            if (pct > 100) pct = 100;
                            String statusClass = "OPEN".equals(job.getStatus()) ? "active" : "closed";
                            String statusLabel = "OPEN".equals(job.getStatus()) ? "Active" : "Closed";
                        %>
                        <tr>
                            <td><strong><%= job.getJobTitle() != null && !job.getJobTitle().isEmpty() ? job.getJobTitle() : job.getModuleName() %></strong></td>
                            <td><%= job.getModuleName() %></td>
                            <td><%= job.getVacancies() %></td>
                            <td><%= apps.size() %></td>
                            <td style="color:#16a34a;font-weight:600;"><%= jAccepted %></td>
                            <td style="color:#d97706;font-weight:600;"><%= jReview %></td>
                            <td style="color:#dc2626;font-weight:600;"><%= jRejected %></td>
                            <td>
                                <div style="display:flex;align-items:center;gap:8px;">
                                    <div style="flex:1;background:#e5e7eb;border-radius:4px;height:8px;overflow:hidden;">
                                        <div style="width:<%= pct %>%;background:<%= pct >= 100 ? "#16a34a" : "#2563eb" %>;height:100%;border-radius:4px;"></div>
                                    </div>
                                    <span style="font-size:13px;font-weight:600;min-width:36px;"><%= pct %>%</span>
                                </div>
                            </td>
                            <td><span class="badge badge-<%= statusClass %>"><%= statusLabel %></span></td>
                        </tr>
                        <% } %>
                    </tbody>
                </table>
            </div>
        <% } %>
    </div>

<%@ include file="/jsp/common/footer.jsp" %>
</body>
</html>
