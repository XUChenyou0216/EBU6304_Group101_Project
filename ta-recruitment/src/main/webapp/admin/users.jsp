<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.ta.dao.*, com.ta.model.*, com.ta.util.SessionUtil, java.util.List" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Account Management - TA Recruitment System</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <style>
        .user-stats { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; margin-bottom: 28px; }
        .user-stat-card { background: var(--bg-white); border: 1px solid var(--border-solid); border-radius: var(--radius); padding: 20px; }
        .user-stat-card .label { font-size: 13px; color: var(--text-secondary); margin-bottom: 4px; }
        .user-stat-card .value { font-size: 28px; font-weight: 700; color: var(--text-dark); }
        .user-stat-card .sub { font-size: 12px; color: var(--text-muted); margin-top: 2px; }
        .search-row { display: flex; gap: 12px; margin-bottom: 20px; }
        .search-box { flex: 1; display: flex; align-items: center; gap: 8px; background: var(--bg-white); border: 1px solid var(--border-solid); border-radius: 8px; padding: 10px 14px; }
        .search-box input { border: none; outline: none; font-size: 14px; width: 100%; background: transparent; color: var(--text-dark); }
        .search-box svg { color: var(--text-muted); flex-shrink: 0; }
        .user-row { display: flex; align-items: center; padding: 16px 0; border-bottom: 1px solid var(--border); }
        .user-row:last-child { border-bottom: none; }
        .user-avatar { width: 38px; height: 38px; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 13px; font-weight: 600; color: #fff; margin-right: 12px; flex-shrink: 0; }
        .user-avatar.ta { background: #2b4acb; } .user-avatar.mo { background: #16a34a; } .user-avatar.admin { background: #7c3aed; }
        .user-info { flex: 1; }
        .user-info .name { font-size: 14px; font-weight: 600; color: var(--text-dark); }
        .user-info .email { font-size: 13px; color: var(--text-muted); }
        .role-pill { padding: 4px 12px; border-radius: 6px; font-size: 12px; font-weight: 500; margin-right: 16px; }
        .role-pill.ta { background: #eef1fb; color: #2b4acb; } .role-pill.mo { background: #fef3c7; color: #b45309; } .role-pill.admin { background: #f3e8ff; color: #7c3aed; }
        .status-pill { padding: 4px 12px; border-radius: 20px; font-size: 12px; font-weight: 500; margin-right: 16px; }
        .status-pill.active { background: #ecfdf5; color: #16a34a; } .status-pill.suspended { background: #fef2f2; color: #dc2626; } .status-pill.inactive { background: #f3f4f6; color: #6b7280; }
        .btn-outline-green { background: transparent; color: #16a34a; border: 1px solid #16a34a; padding: 5px 12px; border-radius: 6px; font-size: 12px; font-weight: 500; cursor: pointer; }
        .btn-outline-amber { background: transparent; color: #d97706; border: 1px solid #d97706; padding: 5px 12px; border-radius: 6px; font-size: 12px; font-weight: 500; cursor: pointer; }
        .btn-outline-red { background: transparent; color: #dc2626; border: 1px solid #dc2626; padding: 5px 12px; border-radius: 6px; font-size: 12px; font-weight: 500; cursor: pointer; }
        .badge-status-active { background: #ecfdf5; color: #16a34a; padding: 4px 12px; border-radius: 20px; font-size: 12px; font-weight: 500; }
        .badge-status-suspended { background: #fef2f2; color: #dc2626; padding: 4px 12px; border-radius: 20px; font-size: 12px; font-weight: 500; }
        .badge-status-pending { background: #fff7ed; color: #d97706; padding: 4px 12px; border-radius: 20px; font-size: 12px; font-weight: 500; }
        .col-actions {
                flex: 0 0 180px !important;
                display: flex !important;
                justify-content: flex-start !important;
            }
        .action-form {
                display: flex !important;
                gap: 5px !important; /* 按钮之间的间距 */
                align-items: center;
            }
        .col-role { width: 140px; } .col-status { width: 100px; } .col-actions { width: 40px; text-align: right; }
    </style>
</head>
<body>
<%@ include file="/jsp/common/header.jsp" %>

    <%
        String dataDir = SessionUtil.getDataDir(request);
        UserDAO userDAO = new UserDAO(dataDir);
        List<User> allUsers = userDAO.findAll();
        int total = allUsers.size();
        int taCount = 0, moCount = 0, adCount = 0;
        for (User u : allUsers) {
            if ("TA".equalsIgnoreCase(u.getRole())) taCount++;
            else if ("MO".equalsIgnoreCase(u.getRole())) moCount++;
            else if ("ADMIN".equalsIgnoreCase(u.getRole())) adCount++;
        }
    %>

    <div class="page-header" style="align-items:center;">
        <div>
            <h1>Account Management</h1>
            <p>Manage user accounts and permissions</p>
        </div>
    </div>

    <% if (request.getParameter("success") != null) { %>
        <div class="alert alert-success">User <%= request.getParameter("success") %> successfully.</div>
    <% } %>

    <div class="user-stats">
        <div class="user-stat-card">
            <div class="label">Total Users</div>
            <div class="value"><%= total %></div>
        </div>
        <div class="user-stat-card">
            <div class="label">Students</div>
            <div class="value"><%= taCount %></div>
            <div class="sub"><%= total > 0 ? (taCount*100/total) : 0 %>% of users</div>
        </div>
        <div class="user-stat-card">
            <div class="label">Module Organisers</div>
            <div class="value"><%= moCount %></div>
            <div class="sub"><%= total > 0 ? (moCount*100/total) : 0 %>% of users</div>
        </div>
        <div class="user-stat-card">
            <div class="label">Administrators</div>
            <div class="value"><%= adCount %></div>
            <div class="sub"><%= total > 0 ? (adCount*100/total) : 0 %>% of users</div>
        </div>
    </div>

    <div class="section-card" style="background:var(--bg-white);border:1px solid var(--border-solid);border-radius:var(--radius);padding:24px;">
        <div class="search-row">
            <div class="search-box">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
                <input type="text" id="searchInput" placeholder="Search users by name or email..." oninput="filterUsers()">
            </div>
        </div>

        <div style="display:flex;padding:8px 0;border-bottom:1px solid var(--border-solid);margin-bottom:8px;">
            <div style="flex:1;font-size:12px;font-weight:600;color:var(--text-secondary);text-transform:uppercase;letter-spacing:0.05em;">User</div>
            <div class="col-role" style="font-size:12px;font-weight:600;color:var(--text-secondary);text-transform:uppercase;letter-spacing:0.05em;">Role</div>
            <div class="col-status" style="font-size:12px;font-weight:600;color:var(--text-secondary);text-transform:uppercase;letter-spacing:0.05em;">Status</div>
            <div class="col-actions" style="font-size:12px;font-weight:600;color:var(--text-secondary);text-transform:uppercase;letter-spacing:0.05em;">Actions</div>
        </div>

        <% for (User u : allUsers) {
            String ini = u.getUsername().substring(0, Math.min(2, u.getUsername().length())).toUpperCase();
            String rc = "TA".equalsIgnoreCase(u.getRole()) ? "ta" : "MO".equalsIgnoreCase(u.getRole()) ? "mo" : "admin";
            String rl = "TA".equalsIgnoreCase(u.getRole()) ? "Student" : "MO".equalsIgnoreCase(u.getRole()) ? "Module Organiser" : "Admin";
            String sc = "ACTIVE".equalsIgnoreCase(u.getStatus()) ? "active" : "SUSPENDED".equalsIgnoreCase(u.getStatus()) ? "suspended" : "inactive";
        %>
        <div class="user-row" data-name="<%= u.getUsername().toLowerCase() %>" data-email="<%= u.getEmail().toLowerCase() %>">
            <div class="user-avatar <%= rc %>"><%= ini %></div>
            <div class="user-info">
                <div class="name"><%= u.getUsername() %></div>
                <div class="email"><%= u.getEmail() %></div>
            </div>
            <div class="col-role"><span class="role-pill <%= rc %>"><%= rl %></span></div>
            <div class="col-status"><span class="status-pill <%= sc %>"><%= u.getStatus() %></span></div>
            <div class="col-actions">
                <form action="${pageContext.request.contextPath}/admin/users" method="post" class="action-form">
                    <input type="hidden" name="userId" value="<%= u.getUserId() %>">

                    <% if ("ACTIVE".equalsIgnoreCase(u.getStatus())) { %>
                        <button type="submit" name="action" value="suspend" class="btn-outline-amber">Suspend</button>
                    <% } else { %>
                        <button type="submit" name="action" value="activate" class="btn-outline-green">Reactivate</button>
                    <% } %>

                    <button type="submit" name="action" value="delete" class="btn-outline-red"
                            onclick="return confirm('Delete this user permanently?')">Delete</button>
                </form>
            </div>
        </div>
        <% } %>

        <div style="margin-top:16px;font-size:13px;color:var(--text-muted);">Showing <%= allUsers.size() %> users</div>
    </div>

<%@ include file="/jsp/common/footer.jsp" %>

<script>
function filterUsers() {
    var q = document.getElementById('searchInput').value.toLowerCase();
    document.querySelectorAll('.user-row').forEach(function(row) {
        var name = row.getAttribute('data-name');
        var email = row.getAttribute('data-email');
        row.style.display = (name.includes(q) || email.includes(q)) ? '' : 'none';
    });
}
</script>
</body>
</html>
