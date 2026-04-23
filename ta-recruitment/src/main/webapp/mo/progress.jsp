<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.ta.model.JobProgressView, java.util.List" %>
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
        List<JobProgressView> progressRows = (List<JobProgressView>) request.getAttribute("progressRows");
        if (progressRows == null) {
            progressRows = java.util.Collections.emptyList();
        }

        Integer totalApplications = (Integer) request.getAttribute("totalApplications");
        Integer totalUnderReview = (Integer) request.getAttribute("totalUnderReview");
        Integer totalAccepted = (Integer) request.getAttribute("totalAccepted");
        Integer overallFillRate = (Integer) request.getAttribute("overallFillRate");
    %>

    <div class="page-header">
        <div>
            <h1>Recruitment Progress</h1>
            <p>Monitor hiring activity across your roles and spot positions that still need attention</p>
        </div>
    </div>

    <div class="stats-row">
        <div class="stat-card">
            <div class="stat-label">Total Applications</div>
            <div class="stat-value"><%= totalApplications != null ? totalApplications : 0 %></div>
        </div>
        <div class="stat-card">
            <div class="stat-label">Under Review</div>
            <div class="stat-value" style="color:#d97706;"><%= totalUnderReview != null ? totalUnderReview : 0 %></div>
        </div>
        <div class="stat-card">
            <div class="stat-label">Accepted</div>
            <div class="stat-value" style="color:#16a34a;"><%= totalAccepted != null ? totalAccepted : 0 %></div>
        </div>
        <div class="stat-card">
            <div class="stat-label">Overall Fill Rate</div>
            <div class="stat-value" style="color:#2563eb;"><%= overallFillRate != null ? overallFillRate : 0 %>%</div>
        </div>
    </div>

    <div style="margin-top:32px;">
        <h3 style="margin-bottom:8px;">Progress by Job</h3>
        <p style="margin:0 0 16px;color:var(--text-muted);font-size:14px;">
            Fill rate is calculated as accepted applicants divided by required positions for each role.
        </p>

        <% if (progressRows.isEmpty()) { %>
            <div class="empty-state">No jobs posted yet. <a href="${pageContext.request.contextPath}/mo/post-job">Post one now</a></div>
        <% } else { %>
            <div style="background:var(--bg-white); border: 1px solid var(--border-solid); border-radius: var(--radius); overflow: hidden;">
                <table class="data-table" style="border: none; border-radius: 0;">
                    <thead>
                        <tr>
                            <th>JOB</th>
                            <th>MODULE</th>
                            <th>POSITIONS</th>
                            <th>APPLICATIONS</th>
                            <th>SCREENING</th>
                            <th>HIRED</th>
                            <th>COMPLETION RATE</th>
                            <th>STATUS</th>
                        </tr>
                    </thead>
                    <tbody>
                        <% for (JobProgressView row : progressRows) { %>
                        <tr>
                            <td><strong><%= row.getJobTitle() %></strong></td>
                            <td><%= row.getModuleName() %></td>
                            <td><%= row.getVacancies() %></td>
                            <td><%= row.getApplicantsCount() %></td>
                            <td style="color:#d97706;font-weight:600;"><%= row.getUnderReviewCount() %></td>
                            <td style="color:#16a34a;font-weight:600;"><%= row.getAcceptedCount() %></td>
                            <td>
                                <div style="display:flex;align-items:center;gap:8px;">
                                    <div style="flex:1;background:#e5e7eb;border-radius:4px;height:8px;overflow:hidden;">
                                        <div style="width:<%= row.getFillRate() %>%;background:<%= row.getFillRate() >= 100 ? "#16a34a" : "#2563eb" %>;height:100%;border-radius:4px;"></div>
                                    </div>
                                    <span style="font-size:13px;font-weight:600;min-width:36px;"><%= row.getFillRate() %>%</span>
                                </div>
                            </td>
                            <td><span class="badge badge-<%= row.getStatusClass() %>"><%= row.getStatusLabel() %></span></td>
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
