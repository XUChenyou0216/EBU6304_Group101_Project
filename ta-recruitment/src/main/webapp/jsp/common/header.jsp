<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.ta.model.User, com.ta.util.SessionUtil" %>
<%
    User currentUser = SessionUtil.getCurrentUser(request);
    String role = currentUser != null ? currentUser.getRole() : "";
    String ctx = request.getContextPath();
%>
<nav class="navbar">
    <div class="nav-brand"><a href="<%= ctx %>/">TA Recruitment</a></div>
    <div class="nav-links">
        <% if ("TA".equalsIgnoreCase(role)) { %>
            <a href="<%= ctx %>/ta/dashboard.jsp">Dashboard</a>
            <a href="<%= ctx %>/ta/jobs.jsp">Browse Jobs</a>
            <a href="<%= ctx %>/ta/profile.jsp">My Profile</a>
            <a href="<%= ctx %>/ta/applications.jsp">My Applications</a>
        <% } else if ("MO".equalsIgnoreCase(role)) { %>
            <a href="<%= ctx %>/mo/dashboard.jsp">Dashboard</a>
            <a href="<%= ctx %>/mo/jobs.jsp">My Jobs</a>
            <a href="<%= ctx %>/mo/post-job.jsp">Post Job</a>
        <% } else if ("ADMIN".equalsIgnoreCase(role)) { %>
            <a href="<%= ctx %>/admin/dashboard.jsp">Dashboard</a>
            <a href="<%= ctx %>/admin/users.jsp">Manage Users</a>
            <a href="<%= ctx %>/admin/workload.jsp">Workload</a>
        <% } %>
    </div>
    <div class="nav-user">
        <% if (currentUser != null) { %>
            <span class="nav-username"><%= currentUser.getUsername() %> (<%= role %>)</span>
            <a href="<%= ctx %>/logout" class="btn btn-sm">Logout</a>
        <% } %>
    </div>
</nav>
