<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.ta.dao.*, com.ta.model.*, com.ta.util.SessionUtil, java.util.List, java.util.Map, java.util.HashMap" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Admin Dashboard - TA Recruitment System</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <style>
        /* Admin Dashboard specific styles */
        .admin-stats { display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px; margin-bottom: 32px; }
        .admin-stat-card {
            background: var(--bg-white); border: 1px solid var(--border-solid); border-radius: var(--radius);
            padding: 24px; display: flex; justify-content: space-between; align-items: flex-start;
        }
        .admin-stat-card .stat-icon {
            width: 40px; height: 40px; border-radius: 10px; display: flex; align-items: center; justify-content: center;
        }
        .admin-stat-card .stat-icon.blue { background: #eef1fb; color: #2b4acb; }
        .admin-stat-card .stat-icon.amber { background: #fff7ed; color: #d97706; }
        .admin-stat-card .stat-icon.green { background: #ecfdf5; color: #16a34a; }
        .admin-stat-label { font-size: 12px; font-weight: 600; text-transform: uppercase; letter-spacing: 0.05em; color: var(--text-secondary); margin-bottom: 8px; }
        .admin-stat-value { font-size: 32px; font-weight: 700; color: var(--text-dark); line-height: 1; }
        .admin-stat-sub { font-size: 13px; color: var(--text-muted); margin-top: 4px; }

        /* Section cards */
        .section-card {
            background: var(--bg-white); border: 1px solid var(--border-solid); border-radius: var(--radius);
            padding: 28px; margin-bottom: 24px;
        }
        .section-card h2 { font-size: 18px; font-weight: 700; color: var(--text-dark); margin-bottom: 20px; }

        /* Account list items */
        .account-item {
            display: flex; align-items: center; justify-content: space-between;
            padding: 14px 0; border-bottom: 1px solid var(--border);
        }
        .account-item:last-child { border-bottom: none; }
        .account-info { display: flex; align-items: center; gap: 12px; }
        .account-avatar {
            width: 38px; height: 38px; border-radius: 50%; display: flex; align-items: center;
            justify-content: center; font-size: 13px; font-weight: 600; color: #fff;
        }
        .account-avatar.av-ta { background: #2b4acb; }
        .account-avatar.av-mo { background: #16a34a; }
        .account-avatar.av-admin { background: #7c3aed; }
        .account-name { font-size: 14px; font-weight: 600; color: var(--text-dark); }
        .account-detail { font-size: 13px; color: var(--text-muted); }
        .account-actions { display: flex; align-items: center; gap: 8px; }
        .btn-outline-green { background: transparent; color: #16a34a; border: 1px solid #16a34a; padding: 5px 12px; border-radius: 6px; font-size: 12px; font-weight: 500; cursor: pointer; }
        .btn-outline-amber { background: transparent; color: #d97706; border: 1px solid #d97706; padding: 5px 12px; border-radius: 6px; font-size: 12px; font-weight: 500; cursor: pointer; }
        .btn-outline-red { background: transparent; color: #dc2626; border: 1px solid #dc2626; padding: 5px 12px; border-radius: 6px; font-size: 12px; font-weight: 500; cursor: pointer; }
        .badge-status-active { background: #ecfdf5; color: #16a34a; padding: 4px 12px; border-radius: 20px; font-size: 12px; font-weight: 500; }
        .badge-status-suspended { background: #fef2f2; color: #dc2626; padding: 4px 12px; border-radius: 20px; font-size: 12px; font-weight: 500; }
        .badge-status-pending { background: #fff7ed; color: #d97706; padding: 4px 12px; border-radius: 20px; font-size: 12px; font-weight: 500; }

        /* Workload table */
        .workload-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
        .workload-warning { display: flex; align-items: center; gap: 6px; font-size: 13px; color: var(--text-muted); }
        .workload-warning svg { color: #d97706; }
        .wl-exceeded { color: #dc2626; font-weight: 600; }
        .wl-row-exceeded { background: #fef2f2; }
        .wl-badge-normal { background: #ecfdf5; color: #16a34a; padding: 3px 10px; border-radius: 12px; font-size: 12px; font-weight: 500; }
        .wl-badge-exceeded { background: #fef2f2; color: #dc2626; padding: 3px 10px; border-radius: 12px; font-size: 12px; font-weight: 500; }
    </style>
</head>
<body>
<%@ include file="/jsp/common/header.jsp" %>

    <%
        String dataDir = SessionUtil.getDataDir(request);
        UserDAO userDAO = new UserDAO(dataDir);
        JobDAO jobDAO = new JobDAO(dataDir);
        ApplicationDAO appDAO = new ApplicationDAO(dataDir);
        TAProfileDAO profileDAO = new TAProfileDAO(dataDir);

        List<User> allUsers = userDAO.findAll();
        List<Job> allJobs = jobDAO.findAll();
        List<Application> allApps = appDAO.findAll();

        // User stats
        int taCount = 0, moCount = 0, adminCount = 0;
        for (User u : allUsers) {
            if ("TA".equalsIgnoreCase(u.getRole())) taCount++;
            else if ("MO".equalsIgnoreCase(u.getRole())) moCount++;
            else if ("ADMIN".equalsIgnoreCase(u.getRole())) adminCount++;
        }

        // Job stats
        int totalJobs = allJobs.size();
        int pendingApps = 0, acceptedApps = 0;
        for (Application a : allApps) {
            if ("SUBMITTED".equals(a.getStatus()) || "UNDER_REVIEW".equals(a.getStatus())) pendingApps++;
            if ("ACCEPTED".equals(a.getStatus())) acceptedApps++;
        }

        // Workload: count accepted jobs per TA
        Map<String, Integer> taWorkload = new HashMap<String, Integer>();
        Map<String, String> taModules = new HashMap<String, String>();
        for (Application a : allApps) {
            if ("ACCEPTED".equals(a.getStatus())) {
                String taId = a.getTaUserId();
                taWorkload.put(taId, taWorkload.getOrDefault(taId, 0) + 1);
                Job j = jobDAO.findById(a.getJobId());
                String mod = j != null ? j.getJobId() : "";
                String existing = taModules.getOrDefault(taId, "");
                taModules.put(taId, existing.isEmpty() ? mod : existing + ", " + mod);
            }
        }
        int workloadLimit = 48;
    %>

    <!-- Page Header with Export Buttons -->
    <div class="page-header" style="align-items:center;">
        <div>
            <h1>Admin Dashboard</h1>
            <p>Global overview and system management</p>
        </div>
        <div style="display:flex;gap:12px;">
            <a href="${pageContext.request.contextPath}/admin/exportAllocation" class="btn btn-secondary" style="font-size:13px;">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="7 10 12 15 17 10"/><line x1="12" y1="15" x2="12" y2="3"/></svg>
                Export Final Allocation (.csv)
            </a>
            <a href="${pageContext.request.contextPath}/admin/exportHistory" class="btn btn-secondary" style="font-size:13px;">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="7 10 12 15 17 10"/><line x1="12" y1="15" x2="12" y2="3"/></svg>
                Export History Archive (.txt)
            </a>
        </div>
    </div>

    <!-- Stats Cards (Job-focused, matching Figma Image 1) -->
    <div class="admin-stats">
        <div class="admin-stat-card">
            <div>
                <div class="admin-stat-label">Total Jobs Posted</div>
                <div class="admin-stat-value"><%= totalJobs %></div>
                <div class="admin-stat-sub">Across all modules</div>
            </div>
            <div class="stat-icon blue">
                <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="2" y="7" width="20" height="14" rx="2" ry="2"/><path d="M16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16"/></svg>
            </div>
        </div>
        <div class="admin-stat-card">
            <div>
                <div class="admin-stat-label">Pending Applications</div>
                <div class="admin-stat-value"><%= pendingApps %></div>
                <div class="admin-stat-sub">Awaiting review</div>
            </div>
            <div class="stat-icon amber">
                <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg>
            </div>
        </div>
        <div class="admin-stat-card">
            <div>
                <div class="admin-stat-label">Positions Filled</div>
                <div class="admin-stat-value"><%= acceptedApps %></div>
                <div class="admin-stat-sub">TAs confirmed</div>
            </div>
            <div class="stat-icon green">
                <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/></svg>
            </div>
        </div>
    </div>

    <!-- Account Management Section (matching Figma Image 1 middle) -->
    <div class="section-card">
        <h2>Account Management</h2>

        <% if ("suspend".equals(request.getParameter("success"))) { %>
            <div class="alert alert-success">User suspended successfully.</div>
        <% } else if ("activate".equals(request.getParameter("success"))) { %>
            <div class="alert alert-success">User activated successfully.</div>
        <% } else if ("delete".equals(request.getParameter("success"))) { %>
            <div class="alert alert-success">User deleted successfully.</div>
        <% } %>

        <% for (User u : allUsers) {
            String uInitials = u.getUsername().substring(0, Math.min(2, u.getUsername().length())).toUpperCase();
            String avClass = "TA".equalsIgnoreCase(u.getRole()) ? "av-ta" :
                             "MO".equalsIgnoreCase(u.getRole()) ? "av-mo" : "av-admin";
            String userRoleLabel = "TA".equalsIgnoreCase(u.getRole()) ? "Student" :
                               "MO".equalsIgnoreCase(u.getRole()) ? "Module Organiser" : "Admin";
            String statusBadge = "ACTIVE".equalsIgnoreCase(u.getStatus()) ? "badge-status-active" :
                                 "SUSPENDED".equalsIgnoreCase(u.getStatus()) ? "badge-status-suspended" : "badge-status-pending";
        %>
        <div class="account-item">
            <div class="account-info">
                <div class="account-avatar <%= avClass %>"><%= uInitials %></div>
                <div>
                    <div class="account-name"><%= u.getUsername() %></div>
                    <div class="account-detail"><%= roleLabel %> &mdash; <%= u.getEmail() %></div>
                </div>
            </div>
            <div class="account-actions">
                <span class="<%= statusBadge %>"><%= u.getStatus() %></span>
            </div>
        </div>
        <% } %>
    </div>

    <!-- TA Workload Monitoring Section (matching Figma Image 1 bottom) -->
    <div class="section-card">
        <div class="workload-header">
            <h2 style="margin-bottom:0;">TA Workload Monitoring</h2>
            <div class="workload-warning">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg>
                Red rows indicate TAs exceeding maximum workload threshold (<%= workloadLimit %> hrs)
            </div>
        </div>

        <table class="data-table" style="border:none;">
            <thead>
                <tr>
                    <th>TA Name</th>
                    <th>Assigned Modules</th>
                    <th>Total Working Hours</th>
                    <th>Limit</th>
                    <th>Status</th>
                </tr>
            </thead>
            <tbody>
                <% for (TAProfile p : profileDAO.findAll()) {
                    int jobs = taWorkload.getOrDefault(p.getUserId(), 0);
                    int hours = jobs * 16; // estimate 16 hours per assigned module
                    boolean exceeded = hours > workloadLimit;
                    String modules = taModules.getOrDefault(p.getUserId(), "-");
                %>
                <tr class="<%= exceeded ? "wl-row-exceeded" : "" %>">
                    <td><strong class="<%= exceeded ? "wl-exceeded" : "" %>"><%= p.getFullName() %></strong></td>
                    <td><%= modules %></td>
                    <td><strong class="<%= exceeded ? "wl-exceeded" : "" %>"><%= hours %> hrs</strong></td>
                    <td><%= workloadLimit %> hrs</td>
                    <td><span class="<%= exceeded ? "wl-badge-exceeded" : "wl-badge-normal" %>"><%= exceeded ? "Exceeded" : "Normal" %></span></td>
                </tr>
                <% } %>
                <% if (profileDAO.findAll().isEmpty()) { %>
                <tr><td colspan="5" style="text-align:center;color:var(--text-muted);padding:24px;">No TA profiles found.</td></tr>
                <% } %>
            </tbody>
        </table>
    </div>

<%@ include file="/jsp/common/footer.jsp" %>
</body>
</html>
