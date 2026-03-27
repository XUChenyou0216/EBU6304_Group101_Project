<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.ta.dao.*, com.ta.model.*, com.ta.util.SessionUtil, java.util.List" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Review Applicants - TA Recruitment System</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<%@ include file="/jsp/common/header.jsp" %>

    <%
        String jobId = request.getParameter("jobId");
        String dataDir = SessionUtil.getDataDir(request);
        Job job = new JobDAO(dataDir).findById(jobId);
        List<Application> applicants = new ApplicationDAO(dataDir).findByJob(jobId);
        TAProfileDAO profileDAO = new TAProfileDAO(dataDir);
    %>

    <div class="page-header">
        <div>
            <h1>Applicants for: <%= job!=null?job.getModuleName():"Unknown" %></h1>
            <p>Review and manage applicant submissions</p>
        </div>
        <a href="${pageContext.request.contextPath}/mo/jobs.jsp" class="btn btn-secondary">Back to Jobs</a>
    </div>

    <% if ("updated".equals(request.getParameter("success"))) { %>
        <div class="alert alert-success">Application status updated!</div>
    <% } %>

    <% if (applicants.isEmpty()) { %>
        <div class="empty-state">No applications received yet.</div>
    <% } else { %>
        <table class="data-table">
            <thead>
                <tr>
                    <th>Applicant</th>
                    <th>Student ID</th>
                    <th>Programme</th>
                    <th>CV</th>
                    <th>Status</th>
                    <th>Action</th>
                </tr>
            </thead>
            <tbody>
                <% for (Application app : applicants) {
                    TAProfile p = profileDAO.findByUserId(app.getTaUserId());
                %>
                <tr>
                    <td><strong><%= p!=null?p.getFullName():app.getTaUserId() %></strong></td>
                    <td><%= p!=null?p.getStudentId():"-" %></td>
                    <td><%= p!=null?p.getProgramme():"-" %></td>
                    <td>
                        <% if (p!=null && p.getCvFilePath()!=null && !p.getCvFilePath().isEmpty()) { %>
                            <a href="${pageContext.request.contextPath}/<%= p.getCvFilePath() %>" target="_blank" class="btn btn-ghost btn-sm">View CV</a>
                        <% } else { %>
                            <span class="text-muted">No CV</span>
                        <% } %>
                    </td>
                    <td><span class="badge badge-<%= app.getStatus().toLowerCase() %>"><%= app.getStatus().replace("_"," ") %></span></td>
                    <td>
                        <form action="${pageContext.request.contextPath}/mo/update-status" method="post" style="display:inline-flex;gap:6px;align-items:center;">
                            <input type="hidden" name="applicationId" value="<%= app.getApplicationId() %>">
                            <input type="hidden" name="jobId" value="<%= jobId %>">
                            <select name="newStatus" style="padding:6px 10px;border:1px solid var(--border-solid);border-radius:6px;font-size:13px;">
                                <option value="SUBMITTED" <%= "SUBMITTED".equals(app.getStatus())?"selected":"" %>>Submitted</option>
                                <option value="UNDER_REVIEW" <%= "UNDER_REVIEW".equals(app.getStatus())?"selected":"" %>>Under Review</option>
                                <option value="ACCEPTED" <%= "ACCEPTED".equals(app.getStatus())?"selected":"" %>>Accepted</option>
                                <option value="REJECTED" <%= "REJECTED".equals(app.getStatus())?"selected":"" %>>Rejected</option>
                            </select>
                            <button type="submit" class="btn btn-primary btn-sm">Update</button>
                        </form>
                    </td>
                </tr>
                <% } %>
            </tbody>
        </table>
    <% } %>

<%@ include file="/jsp/common/footer.jsp" %>
</body>
</html>
