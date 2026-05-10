<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.ta.servlet.AdminWorkloadServlet" %>
<%@ page import="java.util.List" %>
<%
    if (request.getAttribute("workloadRows") == null) {
        response.sendRedirect(request.getContextPath() + "/admin/workload");
        return;
    }
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>TA Workload - TA Recruitment System</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <style>
        .workload-header { display: flex; justify-content: space-between; align-items: flex-start; flex-wrap: wrap; gap: 12px; margin-bottom: 20px; }
        .workload-header h1 { margin-bottom: 4px; }
        .workload-meta { font-size: 13px; color: var(--text-muted); max-width: 520px; }
        .workload-warning {
            display: flex; align-items: center; gap: 8px; font-size: 13px; color: var(--text-muted);
            background: #fffbeb; border: 1px solid #fde68a; border-radius: 8px; padding: 10px 14px;
        }
        .workload-warning svg { flex-shrink: 0; color: #d97706; }
        .wl-exceeded { color: #dc2626; font-weight: 600; }
        .wl-row-exceeded { background: #fef2f2 !important; }
        .wl-row-exceeded td { border-color: #fecaca; }
        .wl-badge-normal { background: #ecfdf5; color: #16a34a; padding: 3px 10px; border-radius: 12px; font-size: 12px; font-weight: 500; }
        .wl-badge-exceeded { background: #fef2f2; color: #dc2626; padding: 3px 10px; border-radius: 12px; font-size: 12px; font-weight: 600; }
        .section-card {
            background: var(--bg-white); border: 1px solid var(--border-solid); border-radius: var(--radius);
            padding: 28px;
        }
    </style>
</head>
<body>
<%@ include file="/jsp/common/header.jsp" %>

<%
    @SuppressWarnings("unchecked")
    List<AdminWorkloadServlet.TaWorkloadRow> workloadRows =
            (List<AdminWorkloadServlet.TaWorkloadRow>) request.getAttribute("workloadRows");
    Integer workloadLimitHours = (Integer) request.getAttribute("workloadLimitHours");
    Integer hoursPerAssignment = (Integer) request.getAttribute("hoursPerAssignment");
    if (workloadRows == null) workloadRows = java.util.Collections.emptyList();
    if (workloadLimitHours == null) workloadLimitHours = AdminWorkloadServlet.WORKLOAD_LIMIT_HOURS;
    if (hoursPerAssignment == null) hoursPerAssignment = AdminWorkloadServlet.HOURS_PER_ACCEPTED_ASSIGNMENT;
%>

    <div class="page-header workload-header">
        <div>
            <h1>TA Workload</h1>
            <p class="workload-meta">
                Summaries are based on <strong>ACCEPTED</strong> applications only.
                Estimated hours = accepted positions × <%= hoursPerAssignment %> hrs.
                Rows over <strong><%= workloadLimitHours %> hrs</strong> are highlighted in red.
            </p>
        </div>
        <div class="workload-warning">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/>
                <line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/>
            </svg>
            <span>Threshold: <%= workloadLimitHours %> hrs (policy)</span>
        </div>
    </div>

    <div class="section-card">
        <table class="data-table" style="border:none;">
            <thead>
                <tr>
                    <th>TA</th>
                    <th>Username</th>
                    <th>ACCEPTED positions</th>
                    <th>Assigned modules (jobs)</th>
                    <th>Est. hours</th>
                    <th>Limit</th>
                    <th>Status</th>
                </tr>
            </thead>
            <tbody>
                <% for (AdminWorkloadServlet.TaWorkloadRow r : workloadRows) { %>
                <tr class="<%= r.isExceeded() ? "wl-row-exceeded" : "" %>">
                    <td>
                        <strong class="<%= r.isExceeded() ? "wl-exceeded" : "" %>"><%= r.getDisplayName() %></strong>
                        <div style="font-size:12px;color:var(--text-muted);"><%= r.getUserId() %></div>
                    </td>
                    <td><%= r.getUsername() %></td>
                    <td><strong class="<%= r.isExceeded() ? "wl-exceeded" : "" %>"><%= r.getAcceptedCount() %></strong></td>
                    <td style="max-width:320px;font-size:13px;"><%= r.getModulesSummary() %></td>
                    <td><strong class="<%= r.isExceeded() ? "wl-exceeded" : "" %>"><%= r.getEstimatedHours() %> hrs</strong></td>
                    <td><%= workloadLimitHours %> hrs</td>
                    <td>
                        <span class="<%= r.isExceeded() ? "wl-badge-exceeded" : "wl-badge-normal" %>">
                            <%= r.isExceeded() ? "Exceeded" : "Within limit" %>
                        </span>
                    </td>
                </tr>
                <% } %>
                <% if (workloadRows.isEmpty()) { %>
                <tr>
                    <td colspan="7" style="text-align:center;color:var(--text-muted);padding:28px;">
                        No Teaching Assistant accounts found.
                    </td>
                </tr>
                <% } %>
            </tbody>
        </table>
    </div>

<%@ include file="/jsp/common/footer.jsp" %>
</body>
</html>
