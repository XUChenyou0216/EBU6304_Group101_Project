<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.ta.dao.*, com.ta.model.*, com.ta.util.SessionUtil, java.util.List" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Admin Dashboard - TA Recruitment System</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<%@ include file="/jsp/common/header.jsp" %>

    <div class="page-header">
        <div>
            <h1>Admin Dashboard</h1>
            <p>Global overview and system management</p>
        </div>
        <div style="display:flex;gap:12px;">
            <a href="${pageContext.request.contextPath}/admin/exportAllocation"
               class="btn btn-secondary">
                <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24"
                     fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/>
                    <polyline points="7 10 12 15 17 10"/>
                    <line x1="12" y1="15" x2="12" y2="3"/>
                </svg>
                Export Final Allocation<br><span style="font-size:12px;font-weight:400;color:var(--text-secondary);">(.csv)</span>
            </a>
            <a href="${pageContext.request.contextPath}/admin/exportHistory"
               class="btn btn-secondary">
                <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24"
                     fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/>
                    <polyline points="7 10 12 15 17 10"/>
                    <line x1="12" y1="15" x2="12" y2="3"/>
                </svg>
                Export History Archive<br><span style="font-size:12px;font-weight:400;color:var(--text-secondary);">(.txt)</span>
            </a>
        </div>
    </div>

    <%
        String dataDir = SessionUtil.getDataDir(request);
        UserDAO userDAO = new com.ta.dao.UserDAO(dataDir);
        List<User> allUsers = userDAO.findAll();
        int taCount = 0, moCount = 0, adminCount = 0;
        for (User u : allUsers) {
            if ("TA".equalsIgnoreCase(u.getRole())) taCount++;
            else if ("MO".equalsIgnoreCase(u.getRole())) moCount++;
            else if ("ADMIN".equalsIgnoreCase(u.getRole())) adminCount++;
        }
    %>

    <div class="stats-row">
        <div class="stat-card">
            <div class="stat-label">Total Users</div>
            <div class="stat-value"><%= allUsers.size() %></div>
        </div>
        <div class="stat-card">
            <div class="stat-label">Students</div>
            <div class="stat-value"><%= taCount %></div>
            <div class="stat-sub"><%= allUsers.size()>0 ? (taCount*100/allUsers.size()) : 0 %>% of users</div>
        </div>
        <div class="stat-card">
            <div class="stat-label">Module Organisers</div>
            <div class="stat-value"><%= moCount %></div>
            <div class="stat-sub"><%= allUsers.size()>0 ? (moCount*100/allUsers.size()) : 0 %>% of users</div>
        </div>
        <div class="stat-card">
            <div class="stat-label">Administrators</div>
            <div class="stat-value"><%= adminCount %></div>
            <div class="stat-sub"><%= allUsers.size()>0 ? (adminCount*100/allUsers.size()) : 0 %>% of users</div>
        </div>
    </div>

    <table class="data-table">
        <thead>
            <tr>
                <th>User</th>
                <th>Role</th>
                <th>Status</th>
                <th>Actions</th>
            </tr>
        </thead>
        <tbody>
            <% for (User u : allUsers) {
                String uInitials = u.getUsername().substring(0, Math.min(2, u.getUsername().length())).toUpperCase();
                String uRoleClass = "TA".equalsIgnoreCase(u.getRole()) ? "role-ta" :
                                    "MO".equalsIgnoreCase(u.getRole()) ? "role-mo" : "role-admin";
                String badgeRole = "TA".equalsIgnoreCase(u.getRole()) ? "ta" :
                                   "MO".equalsIgnoreCase(u.getRole()) ? "mo" : "admin";
            %>
            <tr>
                <td>
                    <div style="display:flex;align-items:center;gap:10px;">
                        <div class="sidebar-avatar <%= uRoleClass %>" style="width:32px;height:32px;font-size:12px;"><%= uInitials %></div>
                        <div>
                            <div style="font-weight:600;"><%= u.getUsername() %></div>
                            <div class="text-sm text-muted"><%= u.getEmail() %></div>
                        </div>
                    </div>
                </td>
                <td><span class="badge badge-role-<%= badgeRole %>"><%= u.getRole() %></span></td>
                <td><span class="badge badge-<%= u.getStatus().toLowerCase() %>"><%= u.getStatus() %></span></td>
                <td>
                    <span class="text-muted text-sm"><!-- P3 Sprint 3: add suspend/delete actions --></span>
                </td>
            </tr>
            <% } %>
        </tbody>
    </table>

<%@ include file="/jsp/common/footer.jsp" %>
</body>
</html>
