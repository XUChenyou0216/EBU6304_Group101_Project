<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.ta.model.User, com.ta.util.SessionUtil" %>
<%
    User currentUser = SessionUtil.getCurrentUser(request);
    String role = currentUser != null ? currentUser.getRole() : "";
    String ctx = request.getContextPath();
    String uri = request.getRequestURI();

    // Role display text
    String roleLabel = "TA".equalsIgnoreCase(role) ? "Student Portal" :
                        "MO".equalsIgnoreCase(role) ? "Module Organiser" :
                        "ADMIN".equalsIgnoreCase(role) ? "Administrator" : "";

    String topRoleLabel = "TA".equalsIgnoreCase(role) ? "Student Portal" :
                          "MO".equalsIgnoreCase(role) ? "Module Organiser Portal" :
                          "ADMIN".equalsIgnoreCase(role) ? "Administrator Panel" : "";

    // Initials for avatar
    String initials = currentUser != null ? currentUser.getUsername().substring(0, Math.min(2, currentUser.getUsername().length())).toUpperCase() : "??";
    String avatarClass = "TA".equalsIgnoreCase(role) ? "role-ta" :
                         "MO".equalsIgnoreCase(role) ? "role-mo" : "role-admin";
%>
<!-- Page Wrapper -->
<div class="page-wrapper">

<!-- Sidebar -->
<aside class="sidebar">
    <div class="sidebar-brand">
        <div class="sidebar-logo">
            <svg viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <rect x="2" y="3" width="20" height="14" rx="2" ry="2"></rect>
                <line x1="8" y1="21" x2="16" y2="21"></line>
                <line x1="12" y1="17" x2="12" y2="21"></line>
            </svg>
        </div>
        <div class="sidebar-brand-text">
            <h3>TA Recruitment</h3>
            <span><%= roleLabel %></span>
        </div>
    </div>

    <nav class="sidebar-nav">
        <% if ("TA".equalsIgnoreCase(role)) { %>
            <a href="<%= ctx %>/ta/dashboard.jsp" class="<%= uri.contains("dashboard") ? "active" : "" %>">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg>
                Home
            </a>
            <a href="<%= ctx %>/ta/profile.jsp" class="<%= uri.contains("profile") ? "active" : "" %>">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
                Profile
            </a>
            <a href="<%= ctx %>/ta/jobs.jsp" class="<%= uri.contains("jobs") ? "active" : "" %>">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="2" y="7" width="20" height="14" rx="2" ry="2"/><path d="M16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16"/></svg>
                Jobs
            </a>
            <a href="<%= ctx %>/ta/applications.jsp" class="<%= uri.contains("applications") ? "active" : "" %>">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg>
                Applications
            </a>
        <% } else if ("MO".equalsIgnoreCase(role)) { %>
            <a href="<%= ctx %>/mo/dashboard.jsp" class="<%= uri.contains("dashboard") ? "active" : "" %>">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="3" width="7" height="7"/><rect x="14" y="3" width="7" height="7"/><rect x="14" y="14" width="7" height="7"/><rect x="3" y="14" width="7" height="7"/></svg>
                Dashboard
            </a>
            <a href="<%= ctx %>/mo/jobs.jsp" class="<%= uri.contains("jobs") || uri.contains("post-job") ? "active" : "" %>">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="2" y="7" width="20" height="14" rx="2" ry="2"/><path d="M16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16"/></svg>
                Job Postings
            </a>
            <a href="<%= ctx %>/mo/applicants.jsp" class="<%= uri.contains("applicants") ? "active" : "" %>">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>
                Applicants & Review
            </a>
            <a href="<%= ctx %>/mo/progress.jsp" class="<%= uri.contains("progress") ? "active" : "" %>">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="18" y1="20" x2="18" y2="10"/><line x1="12" y1="20" x2="12" y2="4"/><line x1="6" y1="20" x2="6" y2="14"/></svg>
                Recruitment Progress
            </a>
        <% } else if ("ADMIN".equalsIgnoreCase(role)) { %>
            <a href="<%= ctx %>/admin/dashboard.jsp" class="<%= uri.contains("dashboard") ? "active" : "" %>">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="3" width="7" height="7"/><rect x="14" y="3" width="7" height="7"/><rect x="14" y="14" width="7" height="7"/><rect x="3" y="14" width="7" height="7"/></svg>
                Dashboard
            </a>
            <a href="<%= ctx %>/admin/users.jsp" class="<%= uri.contains("users") ? "active" : "" %>">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>
                Account Management
            </a>
            <a href="<%= ctx %>/admin/ai-workload.jsp" class="<%= uri.contains("workload") ? "active" : "" %>">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="18" y1="20" x2="18" y2="10"/><line x1="12" y1="20" x2="12" y2="4"/><line x1="6" y1="20" x2="6" y2="14"/></svg>
                AI Workload
            </a>
            <a href="<%= ctx %>/admin/settings.jsp" class="<%= uri.contains("settings") ? "active" : "" %>">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="3"/><path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1-2.83 2.83l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-4 0v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83-2.83l.06-.06A1.65 1.65 0 0 0 4.68 15a1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1 0-4h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 2.83-2.83l.06.06A1.65 1.65 0 0 0 9 4.68a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 4 0v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 2.83l-.06.06A1.65 1.65 0 0 0 19.4 9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 0 4h-.09a1.65 1.65 0 0 0-1.51 1z"/></svg>
                Settings
            </a>
        <% } %>
    </nav>

    <div class="sidebar-user">
        <div class="sidebar-avatar <%= avatarClass %>"><%= initials %></div>
        <div class="sidebar-user-info">
            <h4><%= currentUser != null ? currentUser.getUsername() : "" %></h4>
            <span><%= roleLabel %></span>
        </div>
    </div>
</aside>

<!-- Main Area -->
<div class="main-area">

    <!-- Top Bar -->
    <header class="topbar">
        <div class="topbar-title">
            <h2>TA Recruitment System</h2>
            <span><%= topRoleLabel %></span>
        </div>
        <div class="topbar-right">
            <a href="<%= ctx %>/logout" class="btn btn-ghost btn-sm">Logout</a>
            <div class="topbar-avatar <%= avatarClass %>"><%= initials %></div>
        </div>
    </header>

    <!-- Content starts here (closed by each page) -->
    <main class="content">
