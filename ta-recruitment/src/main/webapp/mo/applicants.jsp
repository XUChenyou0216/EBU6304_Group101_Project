<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.ta.dao.*, com.ta.model.*, com.ta.util.SessionUtil, java.util.List" %>
<%!
    /** Escape for textarea body (avoid breaking HTML / XSS). */
    private static String escNote(String s) {
        if (s == null || s.isEmpty()) return "";
        StringBuilder sb = new StringBuilder(s.length() + 8);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '&') sb.append("&amp;");
            else if (c == '<') sb.append("&lt;");
            else if (c == '>') sb.append("&gt;");
            else sb.append(c);
        }
        return sb.toString();
    }
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Review Applicants - TA Recruitment System</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/applicants-review.css">
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
        <div class="alert alert-success">Application status and/or review note saved.</div>
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
                    <th style="min-width:280px;">Review note &amp; status</th>
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
                    <td style="vertical-align:top;">
                        <form action="${pageContext.request.contextPath}/mo/update-status" method="post" class="mo-applicant-form">
                            <input type="hidden" name="applicationId" value="<%= app.getApplicationId() %>">
                            <input type="hidden" name="jobId" value="<%= jobId %>">
                            <textarea name="reviewNote" class="mo-review-note" rows="3" placeholder="Notes for this applicant (saved to applications.csv)"><%= escNote(app.getReviewNote()) %></textarea>
                            <div class="mo-applicant-form-actions">
                                <select name="newStatus" class="mo-status-select">
                                    <option value="SUBMITTED" <%= "SUBMITTED".equals(app.getStatus())?"selected":"" %>>Submitted</option>
                                    <option value="UNDER_REVIEW" <%= "UNDER_REVIEW".equals(app.getStatus())?"selected":"" %>>Under Review</option>
                                    <option value="ACCEPTED" <%= "ACCEPTED".equals(app.getStatus())?"selected":"" %>>Accepted</option>
                                    <option value="REJECTED" <%= "REJECTED".equals(app.getStatus())?"selected":"" %>>Rejected</option>
                                </select>
                                <button type="submit" class="btn btn-primary btn-sm">Save</button>
                            </div>
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
